package com.klsjnh.common.util;

/*                SqlGuard011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate 2026.10.05
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  read-only sql guard
 *      2026.10.05  forbid server-file access (INTO OUTFILE / DUMPFILE / LOAD_FILE)
 *
 */

import com.klsjnh.common.exception.BusinessException;

import java.util.regex.Pattern;

/**
 * SQL guard for the data source center's read endpoint: enforce a strict
 * read-only statement — must start with {@code SELECT}, no {@code ;} and no
 * DDL / DML keyword or server-file access form. Non-SELECT statements will
 * move to a separate, separately authorized endpoint (see {@code 017.datasource-center}).
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

    /**
     * Forbidden server-file access forms: {@code SELECT ... INTO OUTFILE /
     * DUMPFILE} writes and {@code LOAD_FILE()} reads database-server files
     * when the DB account holds the FILE privilege.
     */
    private static final Pattern FORBIDDEN_FILE = Pattern.compile(
            "\\binto\\s+(outfile|dumpfile)\\b|\\bload_file\\s*\\(",
            Pattern.CASE_INSENSITIVE);

    private SqlGuard011() {
    }

    /**
     * Assert the statement is a single statement (no statement separator) —
     * the guard for the D3 execute endpoint: authorization is the permission
     * code, the guard only rules out multi-statement smuggling. Null or blank
     * is rejected.
     *
     * @param sql developer-authored statement
     */
    public static void assertSingleStatement(String sql) {
        if (sql == null || sql.isBlank()) {
            throw BusinessException.badRequest("sql is required");
        }

        if (sql.contains(";")) {
            throw BusinessException.badRequest("sql must not contain ';'");
        }
    }

    /**
     * Assert the statement is executable on the D3 write endpoint: a single
     * statement that carries no server-file access form (INTO OUTFILE /
     * DUMPFILE / LOAD_FILE work through executeUpdate too when the DB account
     * holds the FILE privilege). Authorization itself is the execute
     * permission code.
     *
     * @param sql developer-authored statement
     */
    public static void assertExecutable(String sql) {
        assertSingleStatement(sql);

        if (FORBIDDEN_FILE.matcher(sql.trim()).find()) {
            throw BusinessException.badRequest("sql contains a forbidden file-access form");
        }
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

        if (FORBIDDEN_FILE.matcher(trimmed).find()) {
            throw BusinessException.badRequest("sql contains a forbidden file-access form");
        }
    }
}
