/**
 * AST-level standards checks (tree-sitter-java).
 *
 * Preferred install: `npm --prefix tools install` (see tools/package.json).
 * Falls back to project-root / user / common global node_modules for older setups.
 * No Maven dependency is added.
 *
 * MANDATORY: any environment problem (package missing, wasm broken) throws, so
 * the gate fails instead of silently skipping the AST rules.
 */

import { existsSync } from 'node:fs';
import { homedir } from 'node:os';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { createRequire } from 'node:module';
import { collectJavaSources } from './klsjnh-standards-lib.mjs';

const LOG_LEVELS = new Set(['info', 'warn', 'error', 'debug', 'trace']);

/** Directory of this module (= tools/). */
const TOOLS_DIR = dirname(fileURLToPath(import.meta.url));

/**
 * Candidate directories that may contain node_modules.
 * Prefer tools/node_modules so the gate works when Node is only via nvm/path
 * and deps were installed next to the scripts.
 */
function candidateRoots() {
  const roots = [
    TOOLS_DIR,
    join(TOOLS_DIR, '..'),
    process.cwd(),
    homedir(),
    '/usr/local/nodejs/lib',
    '/usr/local/lib',
    '/usr/lib',
  ];
  return roots.map((root) => (root.endsWith('node_modules') ? root : join(root, 'node_modules')));
}

/**
 * Resolve a package root directory from the candidate roots.
 *
 * @param pkg package name
 * @return package root path
 */
function resolvePackage(pkg) {
  for (const nm of candidateRoots()) {
    const root = join(nm, pkg);
    if (existsSync(root)) {
      return root;
    }
  }
  throw new Error(
      `ast checks require '${pkg}' in one of: ${candidateRoots().join(', ')}`
      + ` — install with: npm --prefix tools install`);
}

/**
 * Create the AST checker.
 *
 * @return checker with async check(projectRoot) -> violations
 */
export async function createAstChecker() {
  const parserRoot = resolvePackage('tree-sitter');
  const javaRoot = resolvePackage('tree-sitter-java');

  let Parser;
  let Java;

  try {
    Parser = createRequire(join(parserRoot, 'noop.js'))('tree-sitter');
    Java = createRequire(join(javaRoot, 'noop.js'))('tree-sitter-java');
  } catch (err) {
    throw new Error(`ast environment broken: ${err.message}`);
  }

  const parser = new Parser();
  parser.setLanguage(Java);

  /**
   * Whether the class participates in the funcName scope: the repository base
   * itself, its subclasses, or anything in the repository package.
   *
   * @param cls class_declaration node
   * @param pkg file package name
   * @return true when in scope
   */
  function inFuncNameScope(cls, pkg) {
    const superclass = cls.childForFieldName('superclass');
    if (superclass && superclass.text.includes('BaseRepository')) {
      return true;
    }
    return /(\.persistence)?\.repository(\..*)?$/.test(pkg ?? '');
  }

  /**
   * Package name of a file, resolved from the package_declaration at root
   * level (package_declaration is a sibling of type declarations, not an
   * ancestor).
   *
   * @param root program root node
   * @return dotted package name or null
   */
  function filePackageName(root) {
    const pkgDecl = root.namedChildren.find((c) => c.type === 'package_declaration');
    if (!pkgDecl) {
      return null;
    }
    const idNode = pkgDecl.namedChildren.find((c) => c.type === 'scoped_identifier');
    return idNode ? idNode.text : null;
  }

  /**
   * funcName rule: in-scope public instance methods that log or throw must
   * start with `String funcName = "<操作名>";`.
   *
   * @param tree   parsed syntax tree
   * @param file   file path
   * @param out    violations array
   */
  function checkFuncName(tree, pkg, file, out) {
    for (const cls of tree.rootNode.descendantsOfType('class_declaration')) {
      if (!inFuncNameScope(cls, pkg)) {
        continue;
      }

      for (const m of cls.descendantsOfType('method_declaration')) {
        const body = m.childForFieldName('body');
        if (!body) {
          continue;
        }
        const modifiers = (m.namedChildren.find((c) => c.type === 'modifiers') ?? {}).text ?? '';
        if (!/\bpublic\b/.test(modifiers) || /\bstatic\b/.test(modifiers)) {
          continue;
        }
        const logsOrThrows = body.descendantsOfType('method_invocation').some(isLoggerCall)
            || body.descendantsOfType('throw_statement').length > 0;
        if (!logsOrThrows) {
          continue;
        }

        const stmts = body.namedChildren.filter((c) => c.type !== 'line_comment' && c.type !== 'block_comment');
        const first = stmts[0];
        if (!first || !first.text.startsWith('String funcName = "')) {
          const name = m.childForFieldName('name').text;
          out.push({
            file,
            line: m.startPosition.row + 1,
            rule: 'func-name',
            detail: `operation method '${name}' must start with String funcName = "<操作名>";`,
            fix: `insert 'String funcName = "<操作名>";' as the first statement and use it in messages/logs`,
          });
        }
      }
    }
  }

  /**
   * Whether a method invocation is a logger call.
   *
   * @param inv method_invocation node
   * @return true when logger/log receiver with a log level name
   */
  function isLoggerCall(inv) {
    const nameNode = inv.childForFieldName('name');
    const objNode = inv.childForFieldName('object');
    if (!nameNode || !objNode) {
      return false;
    }
    return LOG_LEVELS.has(nameNode.text) && (objNode.text === 'logger' || objNode.text === 'log');
  }

  /**
   * log-concat rule: no string concatenation inside logger arguments.
   *
   * @param tree parsed syntax tree
   * @param file file path
   * @param out  violations array
   */
  function checkLogConcat(tree, file, out) {
    for (const inv of tree.rootNode.descendantsOfType('method_invocation')) {
      if (!isLoggerCall(inv)) {
        continue;
      }

      const args = inv.childForFieldName('arguments');
      if (!args) {
        continue;
      }

      for (const bin of args.descendantsOfType('binary_expression')) {
        const hasPlus = bin.children.some((c) => !c.isNamed && c.text === '+');
        if (hasPlus) {
          out.push({
            file,
            line: bin.startPosition.row + 1,
            rule: 'log-concat',
            detail: `log message must use {} placeholders, not string concatenation (line ${bin.startPosition.row + 1})`,
            fix: 'replace the concatenation with {} placeholders and pass values as arguments',
          });
          break;
        }
      }
    }
  }

  /**
   * Naming rules (015 §8): package → required name suffix. Top-level types
   * only — nested classes inherit the outer class name scope.
   */
  const NAMING_RULES = [
    { pkg: (p) => p.endsWith('.common.vo') || /^com\.klsjnh\.web(\.[\w]+)*\.vo$/.test(p), suffix: /Vo011$/ },
    { pkg: (p) => p.endsWith('.persistence.entity'), suffix: /Po(011)?$/ },
    { pkg: (p) => p.endsWith('.common.page'), suffix: /(Query011|Result011)$/ },
    { pkg: (p) => p.endsWith('.common.enums') || p.endsWith('.common.response') || p.endsWith('.infrastructure.config'), suffix: /011$/ },
  ];

  /**
   * Naming rule: top-level type names must match the suffix convention of
   * their package (015 §8).
   *
   * @param tree parsed syntax tree
   * @param pkg  file package name
   * @param file file path
   * @param out  violations array
   */
  function checkNaming(tree, pkg, file, out) {
    // 接口/枚举暂不强制（如 MasterLinked、常量枚举），只查类与 record
    const types = [
      ...tree.rootNode.descendantsOfType('class_declaration'),
      ...tree.rootNode.descendantsOfType('record_declaration'),
    ];

    for (const t of types) {
      if (t.parent && t.parent.type !== 'program') {
        continue;
      }

      if (!pkg) {
        continue;
      }

      const rule = NAMING_RULES.find((r) => r.pkg(pkg));
      if (!rule) {
        continue;
      }

      const name = t.childForFieldName('name').text;
      if (!rule.suffix.test(name)) {
        out.push({
          file,
          line: t.startPosition.row + 1,
          rule: 'naming',
          detail: `type '${name}' in '${pkg}' must follow the naming convention of 015 §8 (${rule.suffix})`,
          fix: `rename the type so it ends with ${rule.suffix}`,
        });
      }
    }
  }

  /**
   * funcName value format rule: lowercase words separated by single spaces
   * (e.g. 're register') — no hyphens, underscores or uppercase, enforced
   * wherever a funcName declaration appears.
   *
   * @param tree parsed syntax tree
   * @param file file path
   * @param out  violations array
   */
  function testFuncNameFormat(tree, file, out) {
    for (const decl of tree.rootNode.descendantsOfType('local_variable_declaration')) {
      if (!decl.text.startsWith('String funcName = "')) {
        continue;
      }

      const m = decl.text.match(/String funcName = "([^"]*)"/);
      if (m && !/^[a-z0-9]+( [a-z0-9]+)*$/.test(m[1])) {
        out.push({
          file,
          line: decl.startPosition.row + 1,
          rule: 'func-name',
          detail: `funcName value must be lowercase space-separated words (e.g. 're register'), got '${m[1]}'`,
          fix: `replace the value with '${m[1].replace(/[-_]+/g, ' ')}'`,
        });
      }
    }
  }

  /**
   * pk_mt rule (015 §7): inside persistence.entity POs the master link field
   * is always pkMt (MasterLinked contract) — no other *Id style fields.
   * Whitelist: id (PK), pkMt (master link), parentId (tree link).
   */
  function checkPkMt(tree, pkg, file, out) {
    if (!/\.persistence\.entity$/.test(pkg ?? '')) {
      return;
    }

    for (const cls of tree.rootNode.descendantsOfType('class_declaration')) {
      const body = cls.childForFieldName('body');
      if (!body) {
        continue;
      }

      for (const fd of body.namedChildren) {
        if (fd.type !== 'field_declaration') {
          continue;
        }
        for (const vd of fd.descendantsOfType('variable_declarator')) {
          const name = vd.childForFieldName('name')?.text;
          if (!name) {
            continue;
          }
          const lower = name.toLowerCase();
          if (lower === 'id' || lower === 'pkmt' || lower === 'parentid' || lower === 'serialversionuid') {
            continue;
          }
          if (/id$/.test(lower)) {
            out.push({
              file,
              line: vd.startPosition.row + 1,
              rule: 'pk-mt',
              detail: `master link field must be 'pkMt' (MasterLinked contract), got '${name}'`,
              fix: `rename '${name}' to 'pkMt' and implement the MasterLinked interface`,
            });
          }
        }
      }
    }
  }

  /**
   * log-funcname rule: inside a method that declares funcName, every logger
   * call with placeholders must use funcName as the first placeholder
   * argument. Calls without a {} placeholder (e.g. bare message + throwable)
   * are exempt.
   *
   * @param tree parsed syntax tree
   * @param file file path
   * @param out  violations array
   */
  function testLogFuncNameFirst(tree, file, out) {
    for (const m of tree.rootNode.descendantsOfType('method_declaration')) {
      const body = m.childForFieldName('body');
      if (!body) {
        continue;
      }

      const declaresFuncName = body.namedChildren.some((c) =>
          c.type === 'local_variable_declaration' && c.text.startsWith('String funcName = '));
      if (!declaresFuncName) {
        continue;
      }

      for (const inv of body.descendantsOfType('method_invocation')) {
        if (!isLoggerCall(inv)) {
          continue;
        }

        const args = inv.childForFieldName('arguments');
        if (!args) {
          continue;
        }

        const named = args.namedChildren;
        const format = named[0];
        const hasPlaceholder = format && format.type === 'string_literal' && format.text.includes('{}');
        const second = named[1];

        if (hasPlaceholder && (!second || second.type !== 'identifier' || second.text !== 'funcName')) {
          out.push({
            file,
            line: inv.startPosition.row + 1,
            rule: 'log-funcname',
            detail: 'log first placeholder must be funcName (015 §4)',
            fix: 'pass funcName as the first placeholder argument',
          });
        }
      }
    }
  }

  return {
    /**
     * Run AST checks over all Java sources of the project.
     *
     * @param projectRoot project root
     * @return violations array
     */
    async check(projectRoot) {
      const violations = [];
      const files = await collectJavaSources(projectRoot);

      for (const file of files) {
        const source = await (await import('node:fs/promises')).readFile(file, 'utf8');
        if (!/^package\s+/m.test(source)) {
          continue;
        }

        const tree = parser.parse(source);
        const pkg = filePackageName(tree.rootNode);
        checkFuncName(tree, pkg, file, violations);
        testFuncNameFormat(tree, file, violations);
        testLogFuncNameFirst(tree, file, violations);
        checkLogConcat(tree, file, violations);
        checkNaming(tree, pkg, file, violations);
        checkPkMt(tree, pkg, file, violations);
      }

      return violations;
    },
  };
}
