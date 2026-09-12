#!/usr/bin/env node
/**
 * [1/2 CHECK] klsjnh Java17 comment standards — read-only verification.
 * Used by git hooks before commit/push. Does NOT modify source files.
 *
 * Usage: node tools/check-klsjnh-standards.mjs [projectRoot]
 */

import { fileURLToPath } from 'node:url';
import { relPath, resolveProjectRoot, runStandardsCheck } from './klsjnh-standards-lib.mjs';
import { createAstChecker } from './klsjnh-standards-ast.mjs';

async function main() {
  const scriptDir = fileURLToPath(new URL('.', import.meta.url));
  const projectRoot = resolveProjectRoot(scriptDir, process.argv[2] || '.');

  const ast = await createAstChecker();
  const { violations, fileCount, scannedCount } = await runStandardsCheck(projectRoot);
  console.log(`scanned ${scannedCount} Java files (${fileCount} under src/**/java) ...`);

  const astViolations = await ast.check(projectRoot);
  console.log(`ast checks (funcName / log-concat, tree-sitter): ${astViolations.length} issue(s) ...`);
  violations.push(...astViolations);

  if (violations.length > 0) {
    console.error(`klsjnh standards check FAILED (${violations.length} issue(s)) — fix manually (AI-assisted); the fixer script has been removed ...`);
    for (const v of violations.slice(0, 50)) {
      const loc = `${relPath(projectRoot, v.file)}${v.line ? ':' + v.line : ''}`;
      console.error(`  ${loc} [${v.rule}] ${v.detail}`);
      if (v.fix) {
        console.error(`      -> fix: ${v.fix}`);
      }
    }
    if (violations.length > 50) {
      console.error(`  ... and ${violations.length - 50} more`);
    }
    process.exit(1);
  }

  console.log(`klsjnh standards check PASSED ...`);
}

main().catch((err) => { console.error(err); process.exit(1); });
