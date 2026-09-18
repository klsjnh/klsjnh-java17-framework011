package com.klsjnh.domain.lowcode011.modeling;

/*                ModelingSqlGuard class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  modeling sql guard class
 *
 */

import com.klsjnh.common.util.StringUtil011;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Read-only SQL guard for the business modeling topic (033): the three hard
 * checks shared by the probe endpoint and the two execute endpoints.
 * <p>
 * The rules are deliberately few and cheap — this is a guard rail for a
 * management surface operated by trusted staff, not a SQL firewall:
 * </p>
 * <ol>
 *   <li>The statement must start with {@code SELECT}, ignoring leading
 *       whitespace and opening parentheses.</li>
 *   <li>No statement separator ({@code ;}) — one statement only.</li>
 *   <li>No write / DDL keyword, matched on word boundaries.</li>
 * </ol>
 * <p>
 * Matching is intentionally fail-closed: a dangerous keyword appearing inside
 * a string literal is still rejected, because a correct SQL lexer is out of
 * scope here and a false rejection is recoverable while a false acceptance is
 * not.
 * </p>
 * <p>
 * A statement starting with {@code WITH} (common table expression) is
 * therefore also rejected — callers wrap the CTE in a sub-select or rewrite it
 * as a plain select. Invalid input raises {@link IllegalArgumentException},
 * which the application layer translates to HTTP 400.
 * </p>
 */

public final class ModelingSqlGuard {

    /**
     * Statement prefix every accepted statement must carry.
     */
    private static final String SELECT_PREFIX = "select";

    /**
     * Statement separator; rejected anywhere in the text.
     */
    private static final String SEPARATOR = ";";

    /**
     * Write / DDL keywords rejected on word boundaries. Read-only by intent:
     * the modeling module never mutates a business database.
     */
    private static final List<String> FORBIDDEN_KEYWORDS = List.of(
            "INSERT", "UPDATE", "DELETE", "DROP", "TRUNCATE", "ALTER", "CREATE",
            "GRANT", "REVOKE", "INTO OUTFILE", "LOAD_FILE");

    /**
     * Compiled keyword matcher: case-insensitive, word-bounded, so a column
     * named {@code create_time} or {@code last_update} does not trip the rule.
     */
    private static final Pattern FORBIDDEN_PATTERN = Pattern.compile("\\b("
            + String.join("|", FORBIDDEN_KEYWORDS).toLowerCase(Locale.ROOT).replace(" ", "\\s+")
            + ")\\b");

    /**
     * Private constructor: static utility.
     */
    private ModelingSqlGuard() {
    }

    /**
     * Validate a read-only statement, throwing on the first broken rule. Blank
     * input is rejected: a modeling record may omit the SQL, but a caller that
     * asks the guard to check one must supply it.
     *
     * @param sql candidate statement
     */
    public static void validate(String sql) {
        if (StringUtil011.isBlank(sql)) {
            throw new IllegalArgumentException("sql is required");
        }

        if (sql.contains(SEPARATOR)) {
            throw new IllegalArgumentException("sql must contain a single statement, without ';'");
        }

        if (!startsWithSelect(sql)) {
            throw new IllegalArgumentException("sql must start with SELECT");
        }

        if (FORBIDDEN_PATTERN.matcher(sql.toLowerCase(Locale.ROOT)).find()) {
            throw new IllegalArgumentException("sql must be read-only: a write or DDL keyword was found");
        }
    }

    /**
     * Whether the statement passes all three checks.
     *
     * @param sql candidate statement
     * @return true when the statement is accepted
     */
    public static boolean isSafe(String sql) {
        if (StringUtil011.isBlank(sql) || sql.contains(SEPARATOR) || !startsWithSelect(sql)) {
            return false;
        }

        return !FORBIDDEN_PATTERN.matcher(sql.toLowerCase(Locale.ROOT)).find();
    }

    /**
     * Whether the statement opens with SELECT, ignoring leading whitespace and
     * opening parentheses (a single wrapped select is a valid read).
     *
     * @param sql candidate statement
     * @return true when the first meaningful token is SELECT
     */
    public static boolean startsWithSelect(String sql) {
        if (StringUtil011.isBlank(sql)) {
            return false;
        }

        int index = 0;

        while (index < sql.length()) {
            char current = sql.charAt(index);

            if (Character.isWhitespace(current) || current == '(') {
                index++;
                continue;
            }

            break;
        }

        if (sql.length() - index < SELECT_PREFIX.length()) {
            return false;
        }

        return SELECT_PREFIX.equalsIgnoreCase(sql.substring(index, index + SELECT_PREFIX.length()));
    }
}
