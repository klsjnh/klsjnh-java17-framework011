/**
 * Doc ↔ code consistency checks — read-only verification, no source changes.
 *
 * Rule `doc-api-path`: every versioned controller path written in the docs
 * (`/klsjnh/<module>/<controller>/v1`) must match a real controller
 * `@RequestMapping`. Catches the class of bug where a module move (e.g.
 * `system011 → iam`) leaves the docs — and the auth whitelist they describe —
 * pointing at the old path, which is invisible in debug mode and 401s in
 * production.
 *
 * Exemptions: the low-code open API prefix (`/klsjnh/open/`) and lines that
 * mark a counter-example (反例 / ✗ / 示例).
 */

import { readdir, readFile } from 'node:fs/promises';
import { join, relative } from 'node:path';

/**
 * Paths owned by external products (not served by this repo's controllers).
 */
const EXEMPT_PREFIXES = ['/klsjnh/open/'];

/**
 * Lines that intentionally mention a wrong path (counter-examples in the
 * standards docs) are skipped.
 */
const SKIP_LINE = /反例|✗|示例/;

/**
 * Versioned controller path shape: /klsjnh/<module>/<controller>/v1
 */
const PATH_PATTERN = /\/klsjnh\/[a-z0-9]+\/[A-Za-z0-9_]+\/v1/g;

/**
 * Recursively collect files under a directory whose name matches a filter.
 *
 * @param dir    directory
 * @param filter file-name predicate
 * @param out    accumulator
 */
async function walk(dir, filter, out) {
  let entries;

  try {
    entries = await readdir(dir, { withFileTypes: true });
  } catch {
    return;
  }

  for (const entry of entries) {
    const full = join(dir, entry.name);

    if (entry.isDirectory()) {
      await walk(full, filter, out);
    } else if (filter(entry.name)) {
      out.push(full);
    }
  }
}

/**
 * Create the doc checker.
 *
 * @return checker with async check(projectRoot) -> violations
 */
export async function createDocChecker() {
  return {
    /**
     * Compare versioned paths in docs against the controller mappings.
     *
     * @param projectRoot project root
     * @return violations array
     */
    async check(projectRoot) {
      const violations = [];
      const bases = [];

      const sources = [];
      await walk(join(projectRoot, 'java17-web011', 'src', 'main', 'java'), (n) => n.endsWith('.java'), sources);

      for (const file of sources) {
        const source = await readFile(file, 'utf8');

        for (const match of source.matchAll(/@RequestMapping\(\s*"([^"]+)"\s*\)/g)) {
          bases.push(match[1]);
        }

        // Base paths declared as constants and reused (e.g. WebPaths011).
        for (const match of source.matchAll(/static final String [A-Z0-9_]+\s*=\s*"(\/klsjnh\/[^"]+)"/g)) {
          bases.push(match[1]);
        }
      }

      const docs = [];
      await walk(join(projectRoot, 'docs'), (n) => n.endsWith('.md'), docs);

      for (const file of docs) {
        if (relative(projectRoot, file).includes('archive011')) {
          continue;
        }

        const lines = (await readFile(file, 'utf8')).split('\n');

        lines.forEach((line, index) => {
          if (SKIP_LINE.test(line)) {
            return;
          }

          for (const path of line.match(PATH_PATTERN) ?? []) {
            if (EXEMPT_PREFIXES.some((prefix) => path.startsWith(prefix))) {
              continue;
            }

            if (!bases.some((base) => path === base || path.startsWith(base + '/'))) {
              violations.push({
                file,
                line: index + 1,
                rule: 'doc-api-path',
                detail: `doc path '${path}' matches no controller @RequestMapping (moved module?)`,
                fix: 'fix the path in the doc, or declare the base path once and reuse it (see WebPaths011)',
              });
            }
          }
        });
      }

      return violations;
    },
  };
}
