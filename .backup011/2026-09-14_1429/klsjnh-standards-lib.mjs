/**
 * klsjnh coding-standards check helpers (regex tier).
 *
 * The script CHECKS ONLY — fixing is done by AI / developers based on the
 * check output (file:line, rule, fix hint). Rules source: docs/015.coding-standards.md.
 */

import { readdir, readFile } from 'node:fs/promises';
import { isAbsolute, join, relative, resolve, sep } from 'node:path';

export const AUTHOR = 'xiangrkrs@163.com';

/**
 * 1-based line number for a character index in the source.
 *
 * @param content full source
 * @param index   character index
 * @return line number
 */
function lineOf(content, index) {
  return content.slice(0, index).split('\n').length;
}

export function resolveProjectRoot(scriptDir, arg) {
  if (arg) {
    return isAbsolute(arg) ? resolve(arg) : resolve(process.cwd(), arg);
  }
  return resolve(scriptDir, '..');
}

export async function walkJavaFiles(dir, out) {
  let entries;
  try {
    entries = await readdir(dir, { withFileTypes: true });
  } catch {
    return;
  }
  for (const entry of entries) {
    const full = join(dir, entry.name);
    if (entry.isDirectory()) {
      if (entry.name === 'target') {
        continue;
      }
      await walkJavaFiles(full, out);
    } else if (entry.isFile() && entry.name.endsWith('.java')) {
      out.push(full);
    }
  }
}

export async function collectJavaSources(projectRoot) {
  const files = [];
  await walkJavaFiles(projectRoot, files);
  return files.filter((file) => {
    const norm = file.replace(/\\/g, '/');
    return /\/src\/(main|test)\/java\//.test(norm) && !/\/target\//.test(norm);
  });
}

export function relPath(projectRoot, file) {
  return relative(projectRoot, file).split(sep).join('/');
}

export function getTypeName(content) {
  const match = content.match(/^(?:public\s+)?(?:abstract\s+)?(?:final\s+)?(?:class|interface|enum|record)\s+(\w+)/m);
  return match ? match[1] : null;
}

export function getTypeDescription(typeName) {
  if (!typeName) {
    return 'klsjnh component class';
  }
  return typeName
    .replace(/([a-z])([A-Z])/g, '$1 $2')
    .replace(/([a-zA-Z])(\d)/g, '$1 $2')
    .toLowerCase() + ' class';
}

export function hasClassJavadoc(content) {
  return /\/\*\*[\s\S]*?\*\/\s*\n(?:@[\w.]+\s*(?:\([^)]*\))?\s*\n)*\s*(?:public\s+)?(?:abstract\s+)?(?:final\s+)?(?:class|interface|enum|record)\s/m.test(
    content,
  );
}

export function isControlFlowLine(line) {
  return /^\s*(if|for|while|catch|switch|else|try|synchronized)\s*\(/.test(line);
}

export function hasJavadocAbove(lines, blockStart) {
  if (blockStart >= 0 && /^\s*\/\*\*/.test(lines[blockStart])) {
    return true;
  }
  for (let k = blockStart - 1; k >= Math.max(0, blockStart - 12); k--) {
    if (/^\s*\/\*\*/.test(lines[k])) {
      return true;
    }
  }
  let j = blockStart - 1;
  while (j >= 0 && /^\s*$/.test(lines[j])) {
    j--;
  }
  if (j < 0 || lines[j].trim() !== '*/') {
    return false;
  }
  while (j >= 0) {
    if (lines[j].trim().startsWith('/**')) {
      return true;
    }
    if (lines[j].trim().startsWith('*')) {
      j--;
      continue;
    }
    break;
  }
  return false;
}

const SKIP_METHOD_NAMES = new Set(['if', 'for', 'while', 'switch', 'catch', 'throw', 'return', 'new', 'super', 'this']);

function isFieldInitializerSemicolonLine(line) {
  return /\)\s*\)\s*;\s*$/.test(line) || /^\s*\)\s*;\s*$/.test(line);
}

function isMethodSemicolonLine(line) {
  if (!/\)\s*;\s*$/.test(line)) {
    return false;
  }
  if (/^\s*(throw|return)\s/.test(line)) {
    return false;
  }
  if (/\.\w+\s*\(/.test(line)) {
    return false;
  }
  if (isFieldInitializerSemicolonLine(line)) {
    return false;
  }
  return /^\s*(?:public|protected|private|[\w<>,\[\]?.]+\s+\w+\s*\([^)]*\)\s*;\s*$)/.test(line);
}

function scanMethodBlockStart(lines, sigEnd) {
  let start = sigEnd;
  while (start > 0) {
    const prev = lines[start - 1].trim();
    if (/^@\w+/.test(prev) || /^(public|protected|private)\b/.test(prev) || /^[\w<>,\s\[\]?]+\s+\w+\s*,?\s*$/.test(prev)) {
      start--;
      continue;
    }
    break;
  }
  return start;
}

function parseMethodBlock(lines, start, sigEnd) {
  const chunk = lines.slice(start, sigEnd + 1).join(' ');
  if (/\b(?:class|interface|enum|record)\s+\w+/.test(chunk)) {
    return null;
  }
  if (/=\s*new\s/.test(chunk) || /=\s*Collections\./.test(chunk) || /static\s+final/.test(chunk)) {
    return null;
  }
  const match = chunk.match(/(?:public|protected|private|\s)([\w<>,\[\]?]+)\s+(\w+)\s*\(/);
  if (!match) {
    return null;
  }
  const methodName = match[2];
  if (SKIP_METHOD_NAMES.has(methodName)) {
    return null;
  }
  let hasOverride = false;
  for (let k = start; k <= sigEnd; k++) {
    if (/@Override/.test(lines[k])) {
      hasOverride = true;
      break;
    }
  }
  const isConstructor = new RegExp(`(?:public|protected|private)\\s+${methodName}\\s*\\(`).test(chunk);
  return {
    blockStart: start,
    sigEnd,
    methodName,
    signature: chunk,
    hasOverride,
    isConstructor,
    line: start + 1,
  };
}

export function findMethodBlocks(lines) {
  const blocks = new Map();

  for (let i = 0; i < lines.length; i++) {
    const line = lines[i];
    if (isControlFlowLine(line)) {
      continue;
    }

    if (isMethodSemicolonLine(line)) {
      const start = scanMethodBlockStart(lines, i);
      const block = parseMethodBlock(lines, start, i);
      if (block && !blocks.has(start)) {
        blocks.set(start, block);
      }
      continue;
    }

    let sigEnd;
    if (/\)\s*(\{|throws\s+[\w.\s,<>?]+\s*\{)\s*$/.test(line)) {
      sigEnd = i;
    } else if (/\)\s*$/.test(line)) {
      if (i + 1 >= lines.length || !/^\s*\{\s*$/.test(lines[i + 1])) {
        continue;
      }
      sigEnd = i + 1;
    } else {
      continue;
    }

    const start = scanMethodBlockStart(lines, sigEnd);
    const block = parseMethodBlock(lines, start, sigEnd);
    if (block && !blocks.has(start)) {
      blocks.set(start, block);
    }
  }

  return [...blocks.values()];
}

export function blockKindFromHead(head) {
  const h = head.replace(/\s+/g, ' ').trim();
  if (/^(else if|if)\b/.test(h) || /\belse if\b/.test(h) || /^else\b/.test(h)) {
    return 'if';
  }
  if (/^for\b/.test(h) || /\bfor\s*\(/.test(h)) {
    return 'loop';
  }
  if (/^while\b/.test(h) || /\bwhile\s*\(/.test(h)) {
    return 'loop';
  }
  if (/^try\b/.test(h)) {
    return 'try';
  }
  if (/^catch\b/.test(h)) {
    return 'catch';
  }
  return 'other';
}

/**
 * Update block stack while scanning a source line (handles `} else {` etc.).
 */
export function updateBlockStack(stack, line) {
  let buf = '';
  for (const ch of line) {
    if (ch === '{') {
      stack.push(blockKindFromHead(buf));
      buf = '';
    } else if (ch === '}') {
      if (stack.length) {
        stack.pop();
      }
      buf = '';
    } else {
      buf += ch;
    }
  }
}

export function inIfElseBlock(stack) {
  return stack.length > 0 && stack[stack.length - 1] === 'if';
}

/**
 * 013.016 — blank adjacent to `{`/`}` only illegal inside if/else bodies.
 */
export function testBraceAdjacentBlankLines(path, content, violations) {
  const lines = content.split(/\r?\n/);
  const stack = [];
  for (let i = 0; i < lines.length; i++) {
    const trimmed = lines[i].trim();
    if (trimmed === '') {
      const prev = i > 0 ? lines[i - 1].trim() : '';
      const next = i + 1 < lines.length ? lines[i + 1].trim() : '';
      if (inIfElseBlock(stack) && (prev.endsWith('{') || next === '}' || next.startsWith('}'))) {
        violations.push({
          file: path,
          line: i + 1,
          rule: 'brace-blank',
          detail: `blank line adjacent to brace inside if/else at line ${i + 1}`,
          fix: 'remove the blank line adjacent to the brace inside the if/else block',
        });
      }
      continue;
    }
    updateBlockStack(stack, lines[i]);
  }
}

/**
 * 013.016 — flag blank lines inside if/else bodies.
 */
export function testNestedControlBlankLines(path, content, violations) {
  const lines = content.split(/\r?\n/);
  const stack = [];
  for (let i = 0; i < lines.length; i++) {
    if (lines[i].trim() === '' && inIfElseBlock(stack)) {
      violations.push({
        file: path,
        line: i + 1,
        rule: 'control-blank',
        detail: `blank line inside if/else block at line ${i + 1}`,
        fix: 'remove the blank line inside the if/else block',
      });
      continue;
    }
    updateBlockStack(stack, lines[i]);
  }
}

/**
 * Import group for a line: 0 org.slf4j, 1 com.klsjnh.common, 2 other klsjnh,
 * 3 third-party, 4 JDK (java / javax).
 */
function importGroup(line) {
  const m = line.match(/^import\s+((?:static\s+)?[\w.]+)\./);
  if (!m) {
    return 3;
  }
  const p = m[1].replace(/^static\s+/, '');
  if (p.startsWith('org.slf4j')) {
    return 0;
  }
  if (p === 'com.klsjnh.common' || p.startsWith('com.klsjnh.common.')) {
    return 1;
  }
  if (p === 'com.klsjnh' || p.startsWith('com.klsjnh.')) {
    return 2;
  }
  if (p.startsWith('java.') || p.startsWith('javax.')) {
    return 4;
  }
  return 3;
}

/**
 * Import order rule (015 §9): groups in canonical order, blank line between
 * groups, no blank lines inside a group.
 */
export function testImportOrder(path, content, violations) {
  const lines = content.split(/\r?\n/);
  const imports = [];

  for (let i = 0; i < lines.length; i++) {
    if (/^import\s+/.test(lines[i])) {
      imports.push({ line: i, group: importGroup(lines[i]) });
    }
  }

  if (imports.length === 0) {
    return;
  }

  for (let k = 1; k < imports.length; k++) {
    const prev = imports[k - 1];
    const cur = imports[k];
    const gap = cur.line - prev.line;

    if (cur.group < prev.group) {
      violations.push({
        file: path,
        line: cur.line + 1,
        rule: 'import-order',
        detail: `import group out of order (group ${prev.group} -> ${cur.group})`,
        fix: 'regroup imports: org.slf4j, com.klsjnh.common, other com.klsjnh, third-party, java/javax — blank line between groups (015 §9)',
      });
    } else if (cur.group === prev.group && gap > 1) {
      violations.push({
        file: path,
        line: cur.line + 1,
        rule: 'import-order',
        detail: 'imports of the same group must stay together',
        fix: 'remove the blank line inside the import group',
      });
    } else if (cur.group > prev.group && gap === 1) {
      violations.push({
        file: path,
        line: cur.line + 1,
        rule: 'import-order',
        detail: 'need blank line between import groups',
        fix: 'insert one blank line before the new import group',
      });
    }
  }
}

export async function runStandardsCheck(projectRoot) {
  const files = await collectJavaSources(projectRoot);
  const violations = [];
  let scanned = 0;

  for (const file of files) {
    const content = await readFile(file, 'utf8');
    if (!/^package\s+/m.test(content)) {
      continue;
    }
    scanned++;
    testFileHeader(file, content, violations);
    testClassJavadoc(file, content, violations);
    testClassJavadocBlank(file, content, violations);
    testMethodJavadocs(file, content, violations);
    testDuplicateJavadocs(file, content, violations);
    testJavadocEnglish(file, content, violations);
    testImportOrder(file, content, violations);
    testApiUrlStandards(file, content, violations);
    testBraceAdjacentBlankLines(file, content, violations);
    testNestedControlBlankLines(file, content, violations);
  }

  return { violations, fileCount: files.length, scannedCount: scanned };
}

export function testFileHeader(path, content, violations) {
  if (!new RegExp(`@author\\s+${AUTHOR.replace('.', '\\.')}`).test(content)) {
    violations.push({
      file: path,
      line: 1,
      rule: 'file-header',
      detail: `missing @author ${AUTHOR}`,
      fix: 'add the klsjnh file header block after the package statement (015 §1)',
    });
    return;
  }
  if (!/\/\*\s+\S+\s+\w+[\s\S]*?@author\s+xiangrkrs@163\.com[\s\S]*?\n \*\//m.test(content)) {
    violations.push({
      file: path,
      line: 1,
      rule: 'file-header',
      detail: 'missing klsjnh file header block (/* TypeName kind ... */)',
      fix: 'add the file header block after the package statement (015 §1)',
    });
    return;
  }
  const blankImport = /@author\s+xiangrkrs@163\.com[\s\S]*?\n \*\/\n(?!\n)(?=import )/m.exec(content);
  if (blankImport) {
    violations.push({
      file: path,
      line: lineOf(content, blankImport.index),
      rule: 'file-header',
      detail: 'need blank line between file header */ and import',
      fix: 'insert one blank line between the header block and the first import',
    });
  }
  const badHistory = /^\s{6}\d{4}\.\d{2}\.\d{2}\s/m.exec(content);
  if (badHistory) {
    violations.push({
      file: path,
      line: lineOf(content, badHistory.index),
      rule: 'file-header',
      detail: 'modify history line must use " *      yyyy.MM.dd" prefix',
      fix: 'prefix the history line with " *      "',
    });
  }
  const pkg = /^package\s+[\w.]+;[ \t]*$/m.exec(content);
  if (pkg && /^\r?\n\/\*/.test(content.slice(pkg.index + pkg[0].length))) {
    violations.push({
      file: path,
      line: lineOf(content, pkg.index),
      rule: 'file-header',
      detail: 'need blank line between package and file header',
      fix: 'insert one blank line after the package statement',
    });
  }
  const typeName = getTypeName(content);
  if (typeName) {
    for (const m of content.matchAll(/^\s*\*\s+\d{4}\.\d{2}\.\d{2}\s+(.+?)\s*$/gm)) {
      if (m[1] === typeName) {
        violations.push({
          file: path,
          line: lineOf(content, m.index),
          rule: 'file-header',
          detail: `modify history line must be a lowercase description (e.g. '${getTypeDescription(typeName)}'), not the type name`,
          fix: `replace the description with '${getTypeDescription(typeName)}'`,
        });
        break;
      }
    }
  }
}

export function testClassJavadoc(path, content, violations) {
  const decl = /^(?:public\s+)?(?:abstract\s+)?(?:final\s+)?(?:class|interface|enum|record)\s+\w+/m.exec(content);
  if (!decl) {
    return;
  }
  if (hasClassJavadoc(content)) {
    return;
  }
  if (/\/\*\*[\s\S]*?\*\/\s*\n\s*(?:public\s+)?(?:abstract\s+)?(?:final\s+)?(?:class|interface|enum|record)\s/m.test(content)) {
    return;
  }
  violations.push({
    file: path,
    line: lineOf(content, decl.index),
    rule: 'class-javadoc',
    detail: 'missing class/interface/record Javadoc before type declaration',
    fix: 'add an English class Javadoc followed by a blank line before the type declaration (015 §2.1)',
  });
}

export function testClassJavadocBlank(path, content, violations) {
  const lines = content.split(/\r?\n/);
  for (let i = 0; i < lines.length; i++) {
    if (!/^\s*(?:public\s+)?(?:abstract\s+)?(?:final\s+)?(?:class|interface|enum|record)\s/.test(lines[i])) {
      continue;
    }
    let k = i - 1;
    while (k >= 0 && /^\s*@\w/.test(lines[k])) {
      k--;
    }
    if (k >= 0 && /^\s*\*\/\s*$/.test(lines[k])) {
      violations.push({
        file: path,
        line: k + 2,
        rule: 'class-javadoc-blank',
        detail: `need blank line between class Javadoc and type declaration (near line ${k + 2})`,
        fix: 'insert one blank line between the class Javadoc and the type declaration',
      });
    }
    break;
  }
}

export function testMethodJavadocs(path, content, violations) {
  const lines = content.split(/\r?\n/);
  for (const block of findMethodBlocks(lines)) {
    if (!hasJavadocAbove(lines, block.blockStart)) {
      violations.push({
        file: path,
        line: block.line,
        rule: 'method-javadoc',
        detail: `method '${block.methodName}' missing Javadoc (near line ${block.line})`,
        fix: 'add English Javadoc with @param/@return above the method ({@inheritDoc} for @Override)',
      });
    }
  }
}

export function testDuplicateJavadocs(path, content, violations) {
  const m = /(^[ \t]*\/\*\*[^\r\n]*\r?\n(?:^[ \t]*\*[^\r\n]*\r?\n)*^[ \t]*\*\/\s*\r?\n)(?=^[ \t]*\/\*\*)/m.exec(content);
  if (m) {
    violations.push({
      file: path,
      line: lineOf(content, m.index),
      rule: 'method-javadoc',
      detail: 'consecutive duplicate Javadoc blocks',
      fix: 'remove the duplicated Javadoc block',
    });
  }
}

export function testJavadocEnglish(path, content, violations) {
  const javadocs = [];
  const re = /\/\*\*[\s\S]*?\*\//g;
  let m;
  while ((m = re.exec(content)) !== null) {
    javadocs.push({ block: m[0], index: m.index });
  }

  for (const j of javadocs) {
    if (/[\u4e00-\u9fff]/.test(j.block)) {
      violations.push({
        file: path,
        line: lineOf(content, j.index),
        rule: 'javadoc-english',
        detail: 'Javadoc must be English (no CJK characters)',
        fix: 'rewrite the Javadoc in English — Chinese descriptions belong in @Schema or DDL comments',
      });
      return;
    }
  }
}

export function testApiUrlStandards(path, content, violations) {
  const norm = path.replace(/\\/g, '/');
  if (!norm.includes('/controller/') && !norm.endsWith('Controller.java')) {
    return;
  }
  const pattern = /@(?:Get|Post|Put|Delete|Patch|Request)Mapping\s*\([^)]*["'][^"']*\{[a-zA-Z_][\w]*\}/g;
  let match;
  while ((match = pattern.exec(content)) !== null) {
    violations.push({
      file: path,
      line: lineOf(content, match.index),
      rule: 'api-url',
      detail: `forbidden path variable in mapping (use ?query or JSON body): ${match[0]}`,
      fix: 'move the path parameter to a Query parameter or JSON Body',
    });
  }
}
