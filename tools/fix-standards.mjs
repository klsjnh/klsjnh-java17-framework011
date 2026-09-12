#!/usr/bin/env node
/**
 * [2/2 FIX] All-in-one klsjnh Java17 standards fixer.
 *
 * Usage: node tools/fix-standards.mjs <projectRoot> [createDate]
 */

import { readFile } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';
import {
  buildFileHeader,
  cleanupMethodJavadocs,
  collectJavaSources,
  fixBraceAdjacentBlankLines,
  fixHeaderBodyBlankLine,
  fixNestedControlBlankLines,
  findMethodBlocks,
  getClassSummary,
  getMethodSummary,
  getTypeName,
  hasClassJavadoc,
  hasJavadocAbove,
  inIfElseBlock,
  resolveProjectRoot,
  updateBlockStack,
  writeUtf8NoBom,
} from './klsjnh-standards-lib.mjs';

async function main() {
const scriptDir = fileURLToPath(new URL('.', import.meta.url));
const projectRoot = resolveProjectRoot(scriptDir, process.argv[2]);
const createDate = process.argv[3] || '2026.09.12';

let stats = { fileHeader: 0, classJavadoc: 0, methodJavadoc: 0, methodBlankLine: 0, braceBlank: 0, controlBlank: 0, formatting: 0 };

// --- fix-file-class-doc helpers ---

function fixModifyHistoryLine(content) {
  return content.replace(/^(\s{6})(\d{4}\.\d{2}\.\d{2}\s+.+)$/gm, ' *      $2');
}

function fixPackageBlankLine(content) {
  return content.replace(/^(package\s+[\w.]+;)\s*(?:\n\s*)*/m, '$1\n\n');
}

function removeAutoMethodJavadocs(content) {
  return content.replace(/^[ \t]*\/\*\*\s*\n[ \t]*\*[ \t]+\w+\.\s*\n[ \t]*\*\/\s*\n(?!\n*(?:(?:@\w+(?:\([^)]*\))?[ \t]*\n)*)[ \t]*(?:public\s+)?(?:abstract\s+)?(?:final\s+)?(?:class|interface|enum|record)\s)/gm, '');
}

function fixClassJavadocPlacement(content) {
  return content.replace(
    /^([ \t]*(?:@\w+(?:\([^)]*\))?[ \t]*\n)+)(\/\*\*[\s\S]*?\*\/\s*\n)([ \t]*(?:public\s+)?(?:abstract\s+)?(?:final\s+)?(?:class|interface|enum|record)\s)/m,
    '$2$1$3',
  );
}

// --- fix-apply-method-doc helper ---

function addMethodJavadocs(content, className) {
  content = cleanupMethodJavadocs(content);
  const lines = content.split(/\r?\n/);
  const blocks = findMethodBlocks(lines);
  if (blocks.length === 0) return content;

  const inserts = [];
  for (const block of blocks) {
    if (hasJavadocAbove(lines, block.blockStart)) continue;
    const indent = (lines[block.blockStart].match(/^(\s*)/) || ['', ''])[1];
    const summary = getMethodSummary(block.methodName, block.signature, block.hasOverride, block.isConstructor, className);
    inserts.push({ index: block.blockStart, text: `${indent}/**\n${indent} * ${summary}\n${indent} */` });
  }

  if (inserts.length === 0) return content;

  inserts.sort((a, b) => b.index - a.index);
  const list = [...lines];
  for (const ins of inserts) {
    list.splice(ins.index, 0, ins.text);
  }
  return cleanupMethodJavadocs(list.join('\n'));
}

// --- fix-method-blank-line helper ---

function findMethodEnd(lines, sigEnd) {
  // sigEnd 行为方法签名行（含 opening `{`），必须从该行起算括号，否则会在首个内层 if 的 `}` 处误结束
  let braceCount = 0;
  let foundOpen = false;
  for (let i = sigEnd; i < lines.length; i++) {
    for (const char of lines[i]) {
      if (char === '{') {
        braceCount++;
        foundOpen = true;
      } else if (char === '}') {
        braceCount--;
        if (foundOpen && braceCount === 0) {
          return i;
        }
      }
    }
  }
  return lines.length - 1;
}

function classifyLine(line) {
  const t = line.trim();
  if (t === '}' || t.startsWith('}')) return 'brace';
  if (/^return\b/.test(t)) return 'return';
  if (/^(else\s+if|else|if|for|while|try|catch)\b/.test(t)) return 'control';
  // List<Foo> x = ... / boolean started = ...
  if (!/^return\b/.test(t) && /^[\w<>\[\],.?\s]+\s+\w+\s*=/.test(t)) return 'declaration';
  return 'logic';
}

function shouldInsertMethodBlank(prevType, currentType, prevTrim, stack) {
  if (inIfElseBlock(stack)) {
    return false;
  }
  if (!prevTrim) {
    return false;
  }
  const top = stack.length ? stack[stack.length - 1] : null;

  // for/try/catch 开括号后空一行再写块内首段
  if (prevTrim.endsWith('{') && (top === 'loop' || top === 'try' || top === 'catch')) {
    return true;
  }
  if (prevTrim.endsWith('{')) {
    return false;
  }
  if (currentType === 'brace') {
    return false;
  }
  // if 块结束后空一行
  if (prevType === 'brace') {
    return true;
  }
  // 控制语句（for/if/while/try）前空一行
  if (currentType === 'control') {
    return true;
  }
  // logger 等逻辑 → 声明
  if (prevType === 'logic' && currentType === 'declaration') {
    return true;
  }
  // 方法顶层 return 前空一行
  if (currentType === 'return' && top === null) {
    return true;
  }
  return false;
}

function fixMethodBlankLines(content) {
  const lines = content.split(/\r?\n/);
  const blocks = findMethodBlocks(lines);
  if (blocks.length === 0) return content;

  for (let b = blocks.length - 1; b >= 0; b--) {
    const block = blocks[b];
    const methodEnd = findMethodEnd(lines, block.sigEnd);
    if (methodEnd <= block.sigEnd + 1) continue;

    const methodLines = [];
    let prevType = null;
    const stack = []; // method-relative block stack

    for (let i = block.sigEnd + 1; i <= methodEnd; i++) {
      const line = lines[i];
      const trimmed = line.trim();

      if (!trimmed || trimmed.startsWith('//') || trimmed.startsWith('/*') || trimmed.startsWith('*')) {
        if (!trimmed && inIfElseBlock(stack)) {
          continue;
        }
        methodLines.push(line);
        continue;
      }

      const currentType = classifyLine(trimmed);
      const prevTrim = methodLines.length ? methodLines[methodLines.length - 1].trim() : '';

      if (prevType && methodLines.length > 0 && prevTrim !== '') {
        if (shouldInsertMethodBlank(prevType, currentType, prevTrim, stack)) {
          const indent = (line.match(/^(\s*)/) || ['', ''])[1];
          methodLines.push(indent);
        }
      }

      methodLines.push(line);
      prevType = currentType;
      updateBlockStack(stack, line);
    }

    lines.splice(block.sigEnd + 1, methodEnd - block.sigEnd, ...methodLines);
  }

  return lines.join('\n');
}

// --- main ---

for (const path of await collectJavaSources(projectRoot)) {
  let content = await readFile(path, 'utf8');
  if (!/^package\s+/m.test(content)) continue;

  const original = content;
  const typeName = getTypeName(content);
  if (!typeName) continue;

  // 1. Apply file header + class Javadoc
  const summary = getClassSummary(typeName);
  if (!/@author\s+xiangrkrs@163\.com/.test(content)) {
    const header = buildFileHeader(typeName, createDate);
    content = content.replace(/^(package\s+[\w.]+\;\s*)/m, `$1\n${header}`);
    stats.fileHeader++;
  }

  if (!hasClassJavadoc(content)) {
    const javadoc = `/**\n * ${summary}\n */\n`;
    if (/^([ \t]*(?:@\w+(?:\([^)]*\))?[ \t]*\n)+)([ \t]*(?:public\s+)?(?:abstract\s+)?(?:final\s+)?(?:class|interface|enum|record)\s)/m.test(content)) {
      content = content.replace(/^([ \t]*(?:@\w+(?:\([^)]*\))?[ \t]*\n)+)([ \t]*(?:public\s+)?(?:abstract\s+)?(?:final\s+)?(?:class|interface|enum|record)\s)/m, `${javadoc}$1$2`);
    } else {
      content = content.replace(/^(?:public\s+)?(?:abstract\s+)?(?:final\s+)?(?:class|interface|enum|record)\s/m, `${javadoc}$&`);
    }
    stats.classJavadoc++;
  }

  // 2. Format file header / class Javadoc placement
  content = fixPackageBlankLine(content);
  content = fixHeaderBodyBlankLine(content);
  content = fixModifyHistoryLine(content);
  content = removeAutoMethodJavadocs(content);
  content = fixClassJavadocPlacement(content);

  // 3. Apply method Javadoc
  const beforeMethod = content;
  content = addMethodJavadocs(content, typeName);
  if (content !== beforeMethod) stats.methodJavadoc++;

  // 4. Fix method blank lines；再清花括号内侧误插空行（013.016）
  const beforeBlankLine = content;
  content = fixMethodBlankLines(content);
  if (content !== beforeBlankLine) stats.methodBlankLine++;

  const beforeBraceBlank = content;
  content = fixBraceAdjacentBlankLines(content);
  if (content !== beforeBraceBlank) stats.braceBlank++;

  const beforeControlBlank = content;
  content = fixNestedControlBlankLines(content);
  if (content !== beforeControlBlank) stats.controlBlank++;

  if (content !== original) {
    await writeUtf8NoBom(path, content);
    stats.formatting++;
  }
}

console.log(`Fixed ${stats.formatting} files under ${projectRoot}`);
console.log(`  file-header: ${stats.fileHeader}, class-javadoc: ${stats.classJavadoc}, method-javadoc: ${stats.methodJavadoc}, method-blank-line: ${stats.methodBlankLine}, brace-blank: ${stats.braceBlank}, control-blank: ${stats.controlBlank}`);
}

main().catch((err) => { console.error(err); process.exit(1); });
