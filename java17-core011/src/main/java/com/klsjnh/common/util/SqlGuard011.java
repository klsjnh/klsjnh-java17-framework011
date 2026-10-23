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
 *      2026.10.11  comment-tolerant file-form match + cross-DB server-access words
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
     * Separator run between SQL tokens: whitespace or a comment token (block /
     * line). MySQL accepts a comment in place of whitespace, so {@code
     * INTO&#47;**&#47;OUTFILE} must match the same way {@code INTO OUTFILE} does.
     */
    private static final String TOKEN_GAP = "(?:\\s|/\\*.*?\\*/|--[^\\r\\n]*|#[^\\r\\n]*)+";

    /**
     * Forbidden server-file access forms: {@code SELECT ... INTO OUTFILE /
     * DUMPFILE} writes and {@code LOAD_FILE()} reads database-server files
     * when the DB account holds the FILE privilege. Comment tokens count as
     * token separators (MySQL lexer).
     */
    private static final Pattern FORBIDDEN_FILE = Pattern.compile(
            "\\binto" + TOKEN_GAP + "(outfile|dumpfile)\\b|\\bload_file(?:" + TOKEN_GAP + ")?\\(",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /**
     * Cross-database server-file / host-command access words, checked on the
     * execute endpoint and the read-only guard alike: PostgreSQL file
     * functions and large objects, SQL Server shell / bulk-external access,
     * Oracle PL/SQL file and JVM packages.
     */
    private static final Pattern FORBIDDEN_SERVER_ACCESS = Pattern.compile(
            "\\b(pg_read_file|pg_read_binary_file|pg_ls_dir|pg_stat_file|lo_import|lo_export|bfilename"
                    + "|xp_cmdshell|openrowset|openquery|opendatasource|utl_file|dbms_java)\\b",
            Pattern.CASE_INSENSITIVE);

    /**
     * PostgreSQL {@code COPY} as a statement-leading word ({@code COPY ... TO
     * 'file' / PROGRAM} reads and writes server files).
     */
    private static final Pattern LEADING_COPY = Pattern.compile("^\\s*copy\\b", Pattern.CASE_INSENSITIVE);

    /**
     * Time / lock functions that let a caller turn a query into a DoS vector.
     */
    private static final Pattern FORBIDDEN_FUNCTIONS = Pattern.compile(
            "\\b(sleep|benchmark|pg_sleep|waitfor|get_lock|release_lock)\\b", Pattern.CASE_INSENSITIVE);

    /**
     * Framework credential / tenant control tables that the dynamic SQL
     * endpoint must never read or write.
     */
    private static final Pattern FORBIDDEN_TABLES = Pattern.compile(
            "\\b(july_user|july_user_role|july_role_permissions|july_ai_model_provider"
                    + "|july_ai_model_provider_api|july_datasource|july_message_outbound_channel"
                    + "|july_message_inbound_channel|july_storage_provider)\\b", Pattern.CASE_INSENSITIVE);

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
     * holds the FILE privilege) and no cross-database server-access word
     * (pg_read_file / xp_cmdshell / OPENROWSET / UTL_FILE / ...). Authorization
     * itself is the execute permission code.
     *
     * @param sql developer-authored statement
     */
    public static void assertExecutable(String sql) {
        assertSingleStatement(sql);

        String trimmed = sql.trim();

        if (FORBIDDEN_FILE.matcher(trimmed).find()) {
            throw BusinessException.badRequest("sql contains a forbidden file-access form");
        }

        if (FORBIDDEN_SERVER_ACCESS.matcher(trimmed).find()) {
            throw BusinessException.badRequest("sql contains a forbidden server-access keyword");
        }

        if (LEADING_COPY.matcher(trimmed).find()) {
            throw BusinessException.badRequest("sql must not be a COPY statement");
        }

        assertNoDosOrSensitiveTable(trimmed);
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

        if (FORBIDDEN_SERVER_ACCESS.matcher(trimmed).find()) {
            throw BusinessException.badRequest("sql contains a forbidden server-access keyword");
        }

        assertNoDosOrSensitiveTable(trimmed);
    }

    /**
     * Reject time/lock functions and framework credential tables.
     *
     * @param sql trimmed statement
     */
    private static void assertNoDosOrSensitiveTable(String sql) {
        if (FORBIDDEN_FUNCTIONS.matcher(sql).find()) {
            throw BusinessException.badRequest("sql contains a forbidden time/lock function");
        }

        if (FORBIDDEN_TABLES.matcher(sql).find()) {
            throw BusinessException.badRequest("sql must not access framework credential tables");
        }
    }
}
