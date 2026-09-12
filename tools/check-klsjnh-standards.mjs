#!/usr/bin/env node
/**
 * [1/2 CHECK] klsjnh Java17 comment standards — read-only verification.
 * Used by git hooks before commit/push. Does NOT modify source files.
 *
 * Usage: node tools/check-klsjnh-standards.mjs [projectRoot]
 */

import { fileURLToPath } from 'node:url';
import { relPath, resolveProjectRoot, runStandardsCheck } from './klsjnh-standards-lib.mjs';

async function main() {
  const scriptDir = fileURLToPath(new URL('.', import.meta.url));
  const projectRoot = resolveProjectRoot(scriptDir, process.argv[2] || '.');

  const { violations, fileCount, scannedCount } = await runStandardsCheck(projectRoot);
  console.log(`scanned ${scannedCount} Java files (${fileCount} under src/**/java) ...`);

  if (violations.length > 0) {
    console.error(`klsjnh standards check FAILED (${violations.length} issue(s)) ...`);
    for (const v of violations.slice(0, 50)) {
      console.error(`  ${relPath(projectRoot, v.file)} [${v.rule}] ${v.detail}`);
    }
    if (violations.length > 50) {
      console.error(`  ... and ${violations.length - 50} more`);
    }
    process.exit(1);
  }

  console.log(`klsjnh standards check PASSED ...`);
}

main().catch((err) => { console.error(err); process.exit(1); });
