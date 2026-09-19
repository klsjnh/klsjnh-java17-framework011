package com.klsjnh.domain.datasource.kernel;

/*                SeedResult011 record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  yaml seed result record
 *
 */

/**
 * Outcome of one yaml bootstrap seed pass (startup only), reported as-is in the
 * seed summary log line.
 * <p>
 * The four counters partition the input, so
 * {@code total == inserted + skipped + failed} always holds: {@code inserted}
 * are the yaml entries that materialized into the table, {@code skipped} the
 * ones already present (kept as is, never updated), {@code failed} the entries
 * that could not be processed (each one logged at ERROR; startup is never
 * aborted by a bad seed entry).
 * </p>
 *
 * @param total    yaml entries examined
 * @param inserted entries inserted because the dsCode was unknown to the table
 * @param skipped  entries skipped because the dsCode already existed
 * @param failed   entries that could not be processed
 */

public record SeedResult011(int total, int inserted, int skipped, int failed) {

    /**
     * Build an empty result (nothing to seed).
     *
     * @return zero filled result
     */
    public static SeedResult011 empty() {
        return new SeedResult011(0, 0, 0, 0);
    }

    /**
     * Derive the result of a finished pass.
     *
     * @param total    yaml entries examined
     * @param inserted entries inserted
     * @param skipped  entries skipped
     * @param failed   entries failed
     * @return the summary
     */
    public static SeedResult011 of(int total, int inserted, int skipped, int failed) {
        return new SeedResult011(total, inserted, skipped, failed);
    }

    /**
     * Whether any entry was inserted.
     *
     * @return true when at least one entry was inserted
     */
    public boolean hasInserted() {
        return inserted > 0;
    }
}
