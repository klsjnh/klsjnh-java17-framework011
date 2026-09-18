package com.klsjnh.domain.lowcode011.modeling;

/*                ProbeOutcome class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  probe outcome class
 *
 */

import java.util.List;
import java.util.Map;

/**
 * Result of one take-out SQL probe: the raw column metadata read from
 * {@code ResultSetMetaData} plus a small sample of rows.
 * <p>
 * The outcome carries raw JDBC type codes on purpose — the type inference that
 * turns {@code java.sql.Types} into a field type lives outside this object, so
 * the value object stays a plain transport of what the driver reported.
 * </p>
 * <p>
 * {@code success=false} is a normal outcome, not an error: the probe ACTION
 * succeeded, the SQL or the target datasource was simply unusable. The web
 * layer maps it to HTTP 200 with this flag false, mirroring the connectivity
 * probe of the datasource topic (031).
 * </p>
 *
 * @param success whether the SQL could be executed and read
 * @param message client-safe hint, never carries credentials
 * @param columns probed column metadata, never null
 * @param rows    sample rows, never null
 */

public record ProbeOutcome(boolean success, String message, List<ProbeColumn> columns,
        List<Map<String, Object>> rows) {

    /**
     * Normalize the collections so a caller never has to null-check them.
     *
     * @param success whether the SQL could be executed and read
     * @param message client-safe hint
     * @param columns probed column metadata, nullable
     * @param rows    sample rows, nullable
     */
    public ProbeOutcome {
        columns = columns == null ? List.of() : List.copyOf(columns);
        rows = rows == null ? List.of() : List.copyOf(rows);
    }

    /**
     * Build a success outcome.
     *
     * @param columns probed column metadata, nullable
     * @param rows    sample rows, nullable
     * @return success outcome
     */
    public static ProbeOutcome ok(List<ProbeColumn> columns, List<Map<String, Object>> rows) {
        return new ProbeOutcome(true, "probe success", columns, rows);
    }

    /**
     * Build a failure outcome.
     *
     * @param message client-safe failure hint
     * @return failure outcome
     */
    public static ProbeOutcome fail(String message) {
        return new ProbeOutcome(false, message, null, null);
    }

    /**
     * Whether any column metadata was read.
     *
     * @return true when at least one column is present
     */
    public boolean hasColumns() {
        return !columns.isEmpty();
    }

    /**
     * One probed column: what the driver reported, before any inference.
     *
     * @param code     column label
     * @param jdbcType raw {@code java.sql.Types} constant
     * @param length   reported display size, 0 when unknown
     * @param nullable whether the column accepts null
     */
    public record ProbeColumn(String code, int jdbcType, int length, boolean nullable) {
    }
}
