/**
 * Shared klsjnh Java17 comment standards helpers (check + fix scripts).
 */

import { readdir, readFile, writeFile } from 'node:fs/promises';
import { isAbsolute, join, relative, resolve, sep } from 'node:path';

export const AUTHOR = 'xiangrkrs@163.com';

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

export function getClassSummary(typeName) {
  if (!typeName) {
    return 'klsjnh component.';
  }
  if (typeName.endsWith('Test')) {
    return `unit tests for ${typeName.slice(0, -4)}.`;
  }
  if (typeName.endsWith('Controller')) {
    return `${typeName.replace(/Controller$/, '')} HTTP adapter.`;
  }
  if (typeName.endsWith('Service')) {
    return `Business service for ${typeName.replace(/Service$/, '')}.`;
  }
  if (typeName.endsWith('ApplicationService')) {
    return `${typeName.replace(/ApplicationService$/, '')} application service.`;
  }
  if (typeName.endsWith('Application')) {
    return `klsjnh Boot entry (${typeName}).`;
  }
  if (typeName.endsWith('Runner')) {
    return `${typeName} bootstrap runner.`;
  }
  if (typeName.endsWith('Plugin')) {
    return `MCP gateway plugin (${typeName}).`;
  }
  if (typeName.endsWith('Port')) {
    return `${typeName} domain port.`;
  }
  if (typeName.endsWith('Repository')) {
    return `${typeName} repository port or adapter.`;
  }
  if (typeName.endsWith('AutoConfiguration')) {
    return `Spring auto configuration for ${typeName.replace(/AutoConfiguration$/, '')}.`;
  }
  if (typeName.endsWith('Properties')) {
    return `configuration properties (${typeName}).`;
  }
  if (typeName.endsWith('Vo')) {
    return `HTTP request VO (${typeName}).`;
  }
  if (typeName.endsWith('Dto')) {
    return `application DTO (${typeName}).`;
  }
  if (typeName.endsWith('Po')) {
    return `persistence PO (${typeName}).`;
  }
  if (typeName.endsWith('Mapper')) {
    return `MyBatis mapper (${typeName}).`;
  }
  if (typeName.endsWith('Filter')) {
    return `Servlet filter (${typeName}).`;
  }
  if (typeName.endsWith('Exception')) {
    return `Business exception (${typeName}).`;
  }
  if (typeName.endsWith('Config')) {
    return `Spring configuration (${typeName}).`;
  }
  if (typeName.endsWith('Handler')) {
    return `Handler (${typeName}).`;
  }
  if (typeName.startsWith('July') && !typeName.endsWith('Vo')) {
    return `Entity or platform model (${typeName}).`;
  }
  if (typeName.endsWith('Factory')) {
    return `${typeName} factory.`;
  }
  if (typeName.endsWith('Adapter')) {
    return `${typeName} adapter.`;
  }
  return `${typeName}.`;
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

export function buildFileHeader(typeName, createDate) {
  const desc = getTypeDescription(typeName);
  return `/*                ${typeName} class
 *
 *      @author     ${AUTHOR}
 *      @version    ver 0.0.1
 *      @createdate ${createDate}
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      ${createDate}  ${desc}
 *
 */
`;
}

export function fixHeaderBodyBlankLine(content) {
  return content.replace(
    /(\/\*[\s\S]*?@author\s+xiangrkrs@163\.com(?:(?!\/\*\*)[\s\S])*?\n \*\/\n)(?!\n)(?=(?:import |\/\*\*|@|(?:public\s+)?(?:abstract\s+)?(?:final\s+)?(?:class|interface|enum|record)\s))/m,
    '$1\n',
  );
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

const METHOD_SUMMARY_CUSTOM = {};

export function getMethodSummary(methodName, signature, hasOverride, isConstructor, className) {
  if (hasOverride) {
    return '{@inheritDoc}';
  }
  if (isConstructor) {
    return `Create ${className}.`;
  }
  if (methodName === 'main') {
    return 'Boot entry point.';
  }
  if (METHOD_SUMMARY_CUSTOM[methodName]) {
    return METHOD_SUMMARY_CUSTOM[methodName];
  }
  if (methodName.startsWith('find')) {
    return `Find ${methodName.slice(4)}.`;
  }
  if (methodName.startsWith('list')) {
    return `List ${methodName.slice(4)}.`;
  }
  if (methodName.startsWith('insert')) {
    return 'Insert record.';
  }
  if (methodName.startsWith('update')) {
    return 'Update record.';
  }
  if (/^delete|^remove|^logicDelete/.test(methodName)) {
    return 'Remove record.';
  }
  if (methodName.startsWith('save')) {
    return 'Save entity.';
  }
  if (methodName.startsWith('seed')) {
    return 'Seed initial data if empty.';
  }
  if (methodName.startsWith('login')) {
    return 'Authenticate user.';
  }
  if (methodName.endsWith('Test') || /@Test/.test(signature)) {
    return `Test ${methodName}.`;
  }
  const words = methodName.replace(/([A-Z])/g, ' $1').trim().toLowerCase();
  if (!words) {
    return `${methodName}.`;
  }
  return words.charAt(0).toUpperCase() + words.slice(1) + '.';
}

export function testFileHeader(path, content, violations) {
  if (!new RegExp(`@author\\s+${AUTHOR.replace('.', '\\.')}`).test(content)) {
    violations.push({ file: path, rule: 'file-header', detail: `missing @author ${AUTHOR}` });
    return;
  }
  if (!/\/\*\s+\S+\s+\w+[\s\S]*?@author\s+xiangrkrs@163\.com[\s\S]*?\n \*\//m.test(content)) {
    violations.push({ file: path, rule: 'file-header', detail: 'missing klsjnh file header block (/* TypeName kind ... */)' });
    return;
  }
  if (/@author\s+xiangrkrs@163\.com[\s\S]*?\n \*\/\n(?!\n)(?=import )/m.test(content)) {
    violations.push({ file: path, rule: 'file-header', detail: 'need blank line between file header */ and import' });
  }
  if (/^\s{6}\d{4}\.\d{2}\.\d{2}\s/m.test(content)) {
    violations.push({ file: path, rule: 'file-header', detail: 'modify history line must use " *      yyyy.MM.dd" prefix' });
  }
  const pkg = content.match(/^package\s+[\w.]+;[ \t]*$/m);
  if (pkg && /^\r?\n\/\*/.test(content.slice(pkg.index + pkg[0].length))) {
    violations.push({ file: path, rule: 'file-header', detail: 'need blank line between package and file header' });
  }
  const typeName = getTypeName(content);
  if (typeName) {
    for (const m of content.matchAll(/^\s*\*\s+\d{4}\.\d{2}\.\d{2}\s+(.+?)\s*$/gm)) {
      if (m[1] === typeName) {
        violations.push({
          file: path,
          rule: 'file-header',
          detail: `modify history line must be a lowercase description (e.g. '${getTypeDescription(typeName)}'), not the type name`,
        });
        break;
      }
    }
  }
}

export function testClassJavadoc(path, content, violations) {
  if (!/^(?:public\s+)?(?:abstract\s+)?(?:final\s+)?(?:class|interface|enum|record)\s+\w+/m.test(content)) {
    return;
  }
  if (hasClassJavadoc(content)) {
    return;
  }
  if (/\/\*\*[\s\S]*?\*\/\s*\n\s*(?:public\s+)?(?:abstract\s+)?(?:final\s+)?(?:class|interface|enum|record)\s/m.test(content)) {
    return;
  }
  violations.push({ file: path, rule: 'class-javadoc', detail: 'missing class/interface/record Javadoc before type declaration' });
}

export function testMethodJavadocs(path, content, violations) {
  const lines = content.split(/\r?\n/);
  for (const block of findMethodBlocks(lines)) {
    if (!hasJavadocAbove(lines, block.blockStart)) {
      violations.push({
        file: path,
        rule: 'method-javadoc',
        detail: `method '${block.methodName}' missing Javadoc (near line ${block.line})`,
      });
    }
  }
}

export function testDuplicateJavadocs(path, content, violations) {
  if (/(^[ \t]*\/\*\*[^\r\n]*\r?\n(?:^[ \t]*\*[^\r\n]*\r?\n)*^[ \t]*\*\/\s*\r?\n)(?=^[ \t]*\/\*\*)/m.test(content)) {
    violations.push({ file: path, rule: 'method-javadoc', detail: 'consecutive duplicate Javadoc blocks' });
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
      rule: 'api-url',
      detail: `forbidden path variable in mapping (use ?query or JSON body): ${match[0]}`,
    });
  }
}

/**
 * Infer block kind when encountering `{` from text before it on the same line.
 */
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
          rule: 'brace-blank',
          detail: `blank line adjacent to brace inside if/else at line ${i + 1}`,
        });
      }
      continue;
    }
    updateBlockStack(stack, lines[i]);
  }
}

/**
 * Remove blank lines immediately inside if/else `{` / before if/else `}`.
 */
export function fixBraceAdjacentBlankLines(content) {
  const lines = content.split(/\r?\n/);
  const out = [];
  const stack = [];
  for (let i = 0; i < lines.length; i++) {
    const trimmed = lines[i].trim();
    if (trimmed === '') {
      const prev = out.length ? out[out.length - 1].trim() : '';
      const next = i + 1 < lines.length ? lines[i + 1].trim() : '';
      if (inIfElseBlock(stack) && (prev.endsWith('{') || next === '}' || next.startsWith('}'))) {
        continue;
      }
      out.push(lines[i]);
      continue;
    }
    out.push(lines[i]);
    updateBlockStack(stack, lines[i]);
  }
  return out.join('\n');
}

/**
 * 013.016 — drop any blank lines inside if/else bodies (compact continue/return).
 */
export function fixNestedControlBlankLines(content) {
  const lines = content.split(/\r?\n/);
  const out = [];
  const stack = [];
  for (const line of lines) {
    if (line.trim() === '' && inIfElseBlock(stack)) {
      continue;
    }
    out.push(line);
    updateBlockStack(stack, line);
  }
  return out.join('\n');
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
        rule: 'control-blank',
        detail: `blank line inside if/else block at line ${i + 1}`,
      });
      continue;
    }
    updateBlockStack(stack, lines[i]);
  }
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
        rule: 'class-javadoc-blank',
        detail: `need blank line between class Javadoc and type declaration (near line ${k + 2})`,
      });
    }
    break;
  }
}

export function testJavadocEnglish(path, content, violations) {
  const javadocs = content.match(/\/\*\*[\s\S]*?\*\//g) || [];
  for (const block of javadocs) {
    if (/[\u4e00-\u9fff]/.test(block)) {
      violations.push({ file: path, rule: 'javadoc-english', detail: 'Javadoc must be English (no CJK characters)' });
      return;
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
    testApiUrlStandards(file, content, violations);
    testBraceAdjacentBlankLines(file, content, violations);
    testNestedControlBlankLines(file, content, violations);
  }

  return { violations, fileCount: files.length, scannedCount: scanned };
}

export function removeConsecutiveJavadocs(content) {
  const lines = content.split(/\r?\n/);
  let i = 0;
  while (i < lines.length) {
    if (/^\s*\/\*\*/.test(lines[i])) {
      let end = i;
      while (end < lines.length && !/^\s*\*\/\s*$/.test(lines[end])) {
        end++;
      }
      if (end >= lines.length) {
        break;
      }
      let next = end + 1;
      while (next < lines.length && /^\s*$/.test(lines[next])) {
        next++;
      }
      if (next < lines.length && /^\s*\/\*\*/.test(lines[next])) {
        lines.splice(i, end - i + 1);
        continue;
      }
      i = end + 1;
      continue;
    }
    i++;
  }
  return lines.join('\n');
}

export function removeJavadocBeforeControlFlow(content) {
  return content.replace(
    /^[ \t]*\/\*\*[^\r\n]*\r?\n(?:^[ \t]*\*[^\r\n]*\r?\n)*^[ \t]*\*\/\s*\r?\n(?=[ \t]*(?:if|for|while|catch|switch|else|try|synchronized|throw|return)\s)/gm,
    '',
  );
}

export function cleanupMethodJavadocs(content) {
  let prev;
  let next = content;
  do {
    prev = next;
    next = removeJavadocBeforeControlFlow(removeConsecutiveJavadocs(prev));
  } while (next !== prev);
  return next;
}

export async function writeUtf8NoBom(path, content) {
  await writeFile(path, content, { encoding: 'utf8' });
}
