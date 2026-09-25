package com.klsjnh.infrastructure.datasource.kernel.dialect;

/*                SqlserverDialect011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  sqlserver pagination dialect
 *      2026.09.20  ROW_NUMBER() form (works 2005+; OFFSET/FETCH needs 2012+)
 *
 */

import com.klsjnh.common.constant.DatabaseTypes011;

import com.klsjnh.domain.datasource.kernel.SqlDialectPort011;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * SQL Server pagination dialect built on {@code ROW_NUMBER()} (SQL Server 2005+),
 * so pre-2012 servers that lack {@code OFFSET / FETCH NEXT} are covered too. A
 * derived table may not carry {@code ORDER BY}, so a top-level {@code ORDER BY}
 * is moved into the {@code OVER(...)} clause; without one,
 * {@code ORDER BY (SELECT NULL)} is used. The helper column is named
 * {@code klsjnh_rn} and stripped from the result by the executor.
 */

@Component
public class SqlserverDialect011 implements SqlDialectPort011 {

    /**
     * ORDER keyword.
     */
    private static final String ORDER = "order";

    /**
     * BY keyword.
     */
    private static final String BY = "by";

    /**
     * A bare ordinal order expression ({@code 1} / {@code 2 desc}), which
     * {@code OVER()} does not accept — falls back to a constant order.
     */
    private static final Pattern ORDINAL = Pattern.compile("^\\d+(\\s+(asc|desc))?$", Pattern.CASE_INSENSITIVE);

    /** {@inheritDoc} */
    @Override
    public String dbType() {
        return DatabaseTypes011.SQLSERVER;
    }

    /** {@inheritDoc} */
    @Override
    public String pageSql(String sql, long offset, int pageSize) {
        String base = sql == null ? "" : sql.trim();
        int orderStart = topLevelOrderStart(base);
        String inner = orderStart < 0 ? base : base.substring(0, orderStart).trim();
        String order = orderStart < 0 ? "(SELECT NULL)" : orderExpression(base, orderStart);

        if (ORDINAL.matcher(order).matches()) {
            order = "(SELECT NULL)";
        }

        long upper = offset + pageSize;

        return "SELECT * FROM ( SELECT klsjnh_inner.*, ROW_NUMBER() OVER (ORDER BY " + order
                + ") klsjnh_rn FROM ( " + inner + " ) klsjnh_inner ) klsjnh_page WHERE klsjnh_rn > " + offset
                + " AND klsjnh_rn <= " + upper;
    }

    /** {@inheritDoc} */
    @Override
    public String countSql(String sql) {
        String base = sql == null ? "" : sql;
        int orderStart = topLevelOrderStart(base);
        String inner = orderStart < 0 ? base : base.substring(0, orderStart).trim();

        return "SELECT COUNT(*) FROM ( " + inner + " ) klsjnh_count";
    }

    /**
     * Index of a top-level ORDER BY keyword, or -1.
     *
     * @param sql statement
     * @return index of the ORDER keyword, or -1
     */
    private int topLevelOrderStart(String sql) {
        int depth = 0;

        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);

            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
            } else if (depth == 0 && isOrderByAt(sql, i)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * The order expression after a top-level ORDER BY.
     *
     * @param sql        statement
     * @param orderStart index of the ORDER keyword
     * @return expression text (e.g. {@code ds_code asc})
     */
    private String orderExpression(String sql, int orderStart) {
        int i = orderStart + ORDER.length();

        while (i < sql.length() && Character.isWhitespace(sql.charAt(i))) {
            i++;
        }

        i += BY.length();

        return sql.substring(i).trim();
    }

    /**
     * Whether an ORDER BY keyword starts at the given index.
     *
     * @param sql statement
     * @param i   index
     * @return true when ORDER BY starts here
     */
    private boolean isOrderByAt(String sql, int i) {
        int afterOrder = i + ORDER.length();

        if (afterOrder >= sql.length() || !sql.regionMatches(true, i, ORDER, 0, ORDER.length())
                || !Character.isWhitespace(sql.charAt(afterOrder))) {
            return false;
        }

        int j = afterOrder;

        while (j < sql.length() && Character.isWhitespace(sql.charAt(j))) {
            j++;
        }

        return sql.regionMatches(true, j, BY, 0, BY.length())
                && (j + BY.length() >= sql.length()
                        || !Character.isLetterOrDigit(sql.charAt(j + BY.length())));
    }
}
