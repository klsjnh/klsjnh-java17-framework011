package com.klsjnh.common.util;

/*                SqlGuard011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  read-only sql guard
 *
 */

import com.klsjnh.common.exception.BusinessException;

import java.util.regex.Pattern;

/**
 * SQL guard for the data source center's read endpoint: enforce a strict
 * read-only statement — must start with {@code SELECT}, no {@code ;} and no
 * DDL / DML keyword. Non-SELECT statements will move to a separate, separately
 * authorized endpoint (see {@code 017.datasource-center}).
 */

public final class SqlGuard011 {

    /**
     * Statement must start with SELECT.
     */
    private static final Pattern READ_ONLY = Pattern.compile("^\\s*select\\b", Pattern.CASE_INSENSITIVE);

    /**
     * Forbidden DDL / DML keywords (whole words only).
     */
    private static final Pattern FORBIDDEN = Pattern.compile(
            "\\b(insert|update|delete|drop|alter|truncate|create|grant|revoke|merge|replace|call|exec|execute)\\b",
            Pattern.CASE_INSENSITIVE);

    private SqlGuard011() {
    }

    /**
     * Assert the statement is a single read-only SELECT.
     *
     * @param sql developer-authored statement
     */
    public static void assertReadOnly(String sql) {
        if (sql == null || sql.isBlank()) {
            throw BusinessException.badRequest("sql is required");
        }

        String trimmed = sql.trim();

        if (trimmed.contains(";")) {
            throw BusinessException.badRequest("sql must not contain ';'");
        }

        if (!READ_ONLY.matcher(trimmed).find()) {
            throw BusinessException.badRequest("only SELECT is allowed");
        }

        if (FORBIDDEN.matcher(trimmed).find()) {
            throw BusinessException.badRequest("sql contains a forbidden keyword");
        }
    }
}
