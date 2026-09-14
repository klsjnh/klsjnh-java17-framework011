/**
 * Rewrite Java import blocks to the canonical 015 §9 group order.
 *
 * The grouping used here is imported from klsjh-standards-lib.mjs so that the
 * rewriter and the gate can never drift apart: whatever this script emits,
 * testImportOrder accepts.
 *
 * Behaviour:
 *   - collects every top-level `import` line
 *   - regroups them by the canonical group number, keeping the relative order
 *     that was already inside each group (no alphabetical reshuffle: that would
 *     churn unrelated lines)
 *   - joins groups with exactly one blank line
 *   - leaves package / file header / everything after the imports untouched
 *
 * Usage:
 *   node tools/fix-import-order.mjs <root>            # dry run, prints diffs
 *   node tools/fix-import-order.mjs <root> --write    # applies the rewrite
 */
import fs from 'fs';
import path from 'path';
import { importGroupForRewrite, testImportOrder } from './klsjnh-standards-lib.mjs';

const skipDirs = new Set(['node_modules', 'target', 'dist']);

function collectJava(dir, out) {
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const p = path.join(dir, entry.name);
    if (entry.isDirectory()) {
      // Mirror the gate's walkJavaFiles: build output, dependencies, and every
      // dot-directory (.git / .backup011 / .vscode / .workbuddy / ...) are not
      // source. Dot-dirs are skipped by prefix so new ones are covered for free.
      if (!skipDirs.has(entry.name) && !entry.name.startsWith('.')) {
        collectJava(p, out);
      }
    } else if (entry.name.endsWith('.java')) {
      out.push(p);
    }
  }
  return out;
}

/**
 * Rebuild the import block. Returns the new file content, or null when the
 * file has no imports.
 */
export function rewriteImports(content) {
  const lines = content.split(/\r?\n/);

  const importIndexes = [];
  for (let i = 0; i < lines.length; i++) {
    if (/^import\s+/.test(lines[i])) {
      importIndexes.push(i);
    }
  }
  if (importIndexes.length === 0) {
    return null;
  }

  const first = importIndexes[0];
  const last = importIndexes[importIndexes.length - 1];

  // Group the imports, preserving the order they already had inside a group.
  const groups = new Map();
  for (const i of importIndexes) {
    const g = importGroupForRewrite(lines[i]);
    if (!groups.has(g)) {
      groups.set(g, []);
    }
    groups.get(g).push(lines[i]);
  }

  const rebuilt = [];
  for (const g of [...groups.keys()].sort((a, b) => a - b)) {
    if (rebuilt.length > 0) {
      rebuilt.push('');
    }
    rebuilt.push(...groups.get(g));
  }

  const next = [
    ...lines.slice(0, first),
    ...rebuilt,
    ...lines.slice(last + 1),
  ];
  return next.join('\n');
}

function main() {
  const [, , rootArg, ...flags] = process.argv;
  const root = rootArg || '.';
  const write = flags.includes('--write');

  const files = collectJava(root, []);
  let changed = 0;
  let stillBad = 0;

  for (const file of files) {
    const before = fs.readFileSync(file, 'utf8');
    const after = rewriteImports(before);
    if (after === null || after === before) {
      continue;
    }

    if (write) {
      fs.writeFileSync(file, after, 'utf8');
    }
    changed++;
    console.log((write ? 'fixed  ' : 'would  ') + file);

    if (write) {
      const v = [];
      testImportOrder(file, after, v);
      if (v.length > 0) {
        stillBad++;
        for (const x of v) {
          console.log('   STILL BAD L' + x.line + ' | ' + x.detail);
        }
      }
    }
  }

  console.log('---');
  console.log('files scanned  =', files.length);
  console.log('files changed  =', changed, write ? '' : '(dry run, pass --write to apply)');
  if (write) {
    console.log('still violating =', stillBad);
  }
}

if (import.meta.url === `file://${process.argv[1]}` || process.argv[1]?.endsWith('fix-import-order.mjs')) {
  main();
}
