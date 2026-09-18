package com.klsjnh.domain.lowcode011.modeling;

/*                BusinessModelingProbePort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  business modeling probe port interface
 *
 */

/**
 * Take-out SQL probe port: runs a read-only statement against the datasource
 * identified by dsCode and reports both the column metadata and a row sample,
 * so the management view can preview the fields a modeling would produce
 * before anything is persisted.
 * <p>
 * The port exists separately from {@code SqlRoutingPort} because a plain select
 * returns only column-value maps and drops the type metadata, which is exactly
 * what the inference needs. The implementation still runs the same statement
 * over the same pool — it merely reads {@code ResultSetMetaData} alongside the
 * rows.
 * </p>
 * <p>
 * A probe NEVER throws on an unreachable target or a broken statement: it
 * reports {@link ProbeOutcome#fail(String)}. Only programming errors propagate.
 * </p>
 */

public interface BusinessModelingProbePort {

    /**
     * Rows sampled for the preview; the inference reads types, not data, so a
     * small window is enough and a large table is never drained.
     */
    int SAMPLE_ROWS = 10;

    /**
     * Probe one read-only statement.
     *
     * @param dsCode datasource code to run against, required
     * @param sql    read-only statement, required
     * @return probe outcome, never null
     */
    ProbeOutcome probe(String dsCode, String sql);
}
