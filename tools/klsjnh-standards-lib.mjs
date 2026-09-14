/**
 * klsjnh coding-standards check helpers (regex tier).
 *
 * The script CHECKS ONLY — fixing is done by AI / developers based on the
 * check output (file:line, rule, fix hint). Rules source: docs/016.coding-standards.md.
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
      // Only src/main/java and src/test/java are source. Everything else has to
      // be excluded here rather than in collectJavaSources' path filter, because
      // tooling directories mirror the original src/main/java layout:
      //   .backup011/  snapshot copies      .workbuddy/  agent memory
      //   .vscode/     editor state         .git/        history
      // So: skip build output, dependencies, and every dot-directory.
      if (entry.name === 'target'
        || entry.name === 'node_modules'
        || entry.name.startsWith('.')) {
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
 * Import group for a line (015 §9, canonical order):
 *   1 org.slf4j        (logging, highest priority)
 *   2 lombok
 *   3 com.klsjnh.common
 *   4 com.klsjnh.domain + com.klsjnh.application
 *   5 com.klsjnh.web.*.converter
 *   6 com.klsjnh.web.*.vo
 *   7 io.swagger
 *   8 org.springframework
 *   9 jakarta / javax / java
 *  10 other third-party (com.baomidou / cn.hutool / ...)
 * A file only needs the groups it actually uses; group numbers must be
 * non-decreasing across the import block.
 */
function importGroup(line) {
  // Take the full import path, minus the trailing semicolon. Matching the whole
  // path matters: a lazy regex would drop the last segment and lose the
  // "converter" marker.
  const m = line.match(/^import\s+(?:static\s+)?([\w.]+?)\s*;/);
  if (!m) {
    return 10;
  }
  const p = m[1];
  if (p.startsWith('org.slf4j')) {
    return 1;
  }
  if (p === 'lombok' || p.startsWith('lombok.')) {
    return 2;
  }
  if (p === 'com.klsjnh.common' || p.startsWith('com.klsjnh.common.')) {
    return 3;
  }
  if (p === 'com.klsjnh.domain' || p.startsWith('com.klsjnh.domain.')
    || p === 'com.klsjnh.application' || p.startsWith('com.klsjnh.application.')) {
    return 4;
  }
  if (p.startsWith('com.klsjnh.web.')) {
    return /\.converter\./.test(p) ? 5 : 6;
  }
  if (p === 'com.klsjnh' || p.startsWith('com.klsjnh.')) {
    return 6;
  }
  if (p === 'io.swagger' || p.startsWith('io.swagger.')) {
    return 7;
  }
  if (p === 'org.springframework' || p.startsWith('org.springframework.')) {
    return 8;
  }
  if (p.startsWith('java.') || p.startsWith('javax.')) {
    return 10;
  }
  return 9;
}

/**
 * Same grouping as importGroup, exported for the rewriter so that the gate and
 * the auto-fix can never disagree about what the canonical order is.
 */
export function importGroupForRewrite(line) {
  return importGroup(line);
}

/**
 * Import order rule (015 §9): groups in canonical order, exactly one blank
 * line between groups, no blank lines inside a group.
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
        fix: 'regroup imports: org.slf4j, lombok, com.klsjnh.common, com.klsjnh.domain/application, com.klsjnh.web.converter, com.klsjnh.web.vo, third-party, java/javax (015 §9)',
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
    } else if (cur.group > prev.group && gap > 2) {
      violations.push({
        file: path,
        line: cur.line + 1,
        rule: 'import-order',
        detail: 'need exactly one blank line between import groups',
        fix: 'collapse to a single blank line before the new import group',
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

/**
 * Point-read actions (015 §6.2): a single row is addressed by a key, so the
 * request is trivially expressible as a query string. These MUST be
 * @GetMapping — safe + idempotent per RFC 9110.
 *
 * Naming rule: "getByXxx" is a point read (getById / getByCode / getByName...).
 */
const POINT_READ_ACTIONS = new Set(['getById']);

/**
 * Conditional-read actions (015 §6.2): a list / page / tree query carries a
 * filter object whose shape is open-ended (nested lists, date ranges, dynamic
 * criteria) and does not fit a query string. These are safe + idempotent in
 * semantics but are deliberately mapped with @PostMapping and the filter VO in
 * the JSON body — the same trade-off Elasticsearch makes with POST _search.
 * The gate therefore accepts either GET or POST here.
 *
 * "getBy..." is point read; "select..." is conditional read.
 */
const SELECT_READ_ACTIONS = new Set([
  'selectListByPage', 'selectTree', 'selectUserMenuTree', 'selectList', 'select',
]);

/** Match getBy<Something> point reads (getById is listed explicitly). */
const POINT_READ_PATTERN = /^getBy[A-Z]\w*$/;

/**
 * Write-purpose action names: unsafe, so the handler MUST be mapped with
 * @PostMapping.
 */
const WRITE_ACTIONS = new Set([
  'insert', 'update', 'upsert', 'logicDelete', 'logicDeleteBatch', 'delete', 'deleteBatch',
  'assignRoles', 'assignMenus', 'resetPassword', 'changePassword', 'login', 'loginByUserName',
  'logout', 'start', 'stop', 'runOnce', 'export',
]);

/**
 * Single-target delete actions: they MUST take a single id (IdVo011), not a
 * list. A list parameter means the endpoint IS a batch one and must be named
 * "logicDeleteBatch" instead (rule: api-delete-arity).
 */
const SINGLE_DELETE_ACTIONS = new Set(['logicDelete', 'delete']);

/**
 * Batch delete actions: they MUST take a collection parameter.
 */
const BATCH_DELETE_ACTIONS = new Set(['logicDeleteBatch', 'deleteBatch']);

/**
 * Resolve the action segment of a mapping path, i.e. the trailing literal
 * segment of the URL (e.g. "/klsjnh/system011/julyUser/v1/getById" -> getById).
 *
 * @param mappingText raw mapping annotation text
 * @returns action name, or null when not resolvable
 */
function extractAction(mappingText) {
  const urlMatch = /["']([^"']*)["']/.exec(mappingText);
  if (!urlMatch) {
    return null;
  }
  const segments = urlMatch[1].split('/').filter(Boolean);
  return segments.length > 0 ? segments[segments.length - 1] : null;
}

/**
 * Extract the HTTP method annotation from a mapping annotation text.
 *
 * @param mappingText raw mapping annotation text
 * @returns 'GET' | 'POST' | 'PUT' | 'DELETE' | 'PATCH' | 'Request'
 */
function extractHttpMethod(mappingText) {
  const m = /@(Get|Post|Put|Delete|Patch|Request)Mapping/.exec(mappingText);
  return m ? m[1].toUpperCase() : 'UNKNOWN';
}

export function testApiUrlStandards(path, content, violations) {
  const norm = path.replace(/\\/g, '/');
  if (!norm.includes('/controller/') && !norm.endsWith('Controller.java')) {
    return;
  }

  // Rule A (unchanged): path variables are forbidden — parameters go to the
  // query string or the JSON body.
  const pathVarPattern = /@(?:Get|Post|Put|Delete|Patch|Request)Mapping\s*\([^)]*["'][^"']*\{[a-zA-Z_][\w]*\}/g;
  let match;
  while ((match = pathVarPattern.exec(content)) !== null) {
    violations.push({
      file: path,
      line: lineOf(content, match.index),
      rule: 'api-url',
      detail: `forbidden path variable in mapping (use ?query or JSON body): ${match[0]}`,
      fix: 'move the path parameter to a Query parameter or JSON Body',
    });
  }

  // Rule B: HTTP method must match the action semantics (RFC 9110).
  //   point read (getByXxx)  -> @GetMapping, id via query string
  //   conditional read (selectXxx) -> GET or POST; POST carries a filter VO
  //   write (insert/update/...)    -> @PostMapping
  const mappingPattern = /@(?:Get|Post|Put|Delete|Patch)Mapping\s*\(([^)]*)\)/g;
  while ((match = mappingPattern.exec(content)) !== null) {
    const action = extractAction(match[1]);
    if (!action) {
      continue;
    }
    const httpMethod = extractHttpMethod(match[0]);
    const isPointRead = POINT_READ_ACTIONS.has(action) || POINT_READ_PATTERN.test(action);
    const isSelectRead = SELECT_READ_ACTIONS.has(action);
    const isWrite = WRITE_ACTIONS.has(action);

    if (isPointRead && httpMethod !== 'GET') {
      violations.push({
        file: path,
        line: lineOf(content, match.index),
        rule: 'api-method',
        detail: `point read "${action}" must be @GetMapping, found @${httpMethod}Mapping`,
        fix: 'a key-addressed read is safe + idempotent — map it with @GetMapping and pass the key via the query string',
      });
    } else if (isWrite && httpMethod !== 'POST') {
      violations.push({
        file: path,
        line: lineOf(content, match.index),
        rule: 'api-method',
        detail: `write action "${action}" must be @PostMapping, found @${httpMethod}Mapping`,
        fix: 'write actions are unsafe — map them with @PostMapping and pass parameters via the JSON body',
      });
    }
    // isSelectRead: deliberately unconstrained. A filter object does not fit a
    // query string; POST with a *Vo011 body is the accepted form (015 §6.2).

    // Rule C: delete arity must match the action name. "logicDelete" takes a
    // single id (IdVo011); "logicDeleteBatch" takes an id-list VO (IdsVo011).
    // A bare collection in the request body is forbidden — request payloads
    // are always named *Vo011 types (rule: api-vo-payload).
    if (SINGLE_DELETE_ACTIONS.has(action) || BATCH_DELETE_ACTIONS.has(action)) {
      const signature = methodSignatureAfter(content, match.index + match[0].length);
      if (signature) {
        const takesBareCollection = /@RequestBody\s+List\s*</.test(signature)
          || /@RequestBody\s+\w+\s*\[\s*\]/.test(signature);
        // A plural id VO (IdsVo011) carries a list just as much as a bare
        // List<String> does — both satisfy "batch shape".
        const takesCollectionOfAnyKind = /\bList\s*</.test(signature)
          || /\[\s*\]/.test(signature)
          || /\bIds\w*Vo\d+\b/.test(signature);
        const expectsCollection = BATCH_DELETE_ACTIONS.has(action);

        if (takesBareCollection) {
          violations.push({
            file: path,
            line: lineOf(content, match.index),
            rule: 'api-vo-payload',
            detail: `request body must not be a bare collection: ${signature.trim().slice(0, 60)}`,
            fix: 'wrap it in a named VO — batch id lists go into IdsVo011 and are read via idsVo.getIds()',
          });
        } else if (expectsCollection && !takesCollectionOfAnyKind) {
          violations.push({
            file: path,
            line: lineOf(content, match.index),
            rule: 'api-delete-arity',
            detail: `batch action "${action}" should take an id-list VO (IdsVo011), found a single value`,
            fix: 'batch delete takes IdsVo011 and returns BatchDeleteResultVo011',
          });
        } else if (!expectsCollection && takesCollectionOfAnyKind) {
          violations.push({
            file: path,
            line: lineOf(content, match.index),
            rule: 'api-delete-arity',
            detail: `single action "${action}" should take one id (IdVo011), found a collection`,
            fix: 'rename this endpoint to "logicDeleteBatch" for list deletes, or take IdVo011 for a single id',
          });
        }
      }
    }
  }
}

/**
 * Grab the Java method signature that follows a mapping annotation, up to the
 * opening brace of the body. Used to inspect what a handler actually accepts.
 *
 * @param content full file content
 * @param fromIndex index just after the mapping annotation
 * @returns signature text, or null when not resolvable
 */
function methodSignatureAfter(content, fromIndex) {
  const rest = content.slice(fromIndex, fromIndex + 600);
  const braceIndex = rest.indexOf('{');
  if (braceIndex === -1) {
    return null;
  }
  const head = rest.slice(0, braceIndex);
  const parenIndex = head.lastIndexOf('(');
  return parenIndex === -1 ? null : head.slice(parenIndex);
}
