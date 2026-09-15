package com.klsjnh.infrastructure.persistence.sql;

/*                QuotedLiteral class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  quoted literal class
 *
 */

import com.klsjnh.common.exception.BusinessException;

import java.util.regex.Pattern;

/**
 * Renders a runtime string as a quoted SQL literal for the statements that
 * cannot bind parameters ({@code CommonMapper} has no parameter binding — the
 * statement arrives as one raw string).
 * <p>
 * This is the narrow escape hatch of the native-SQL path, NOT general escaping
 * support. The value is emitted inside single quotes with every embedded quote
 * doubled, which is the ANSI rule accepted by MySQL / PostgreSQL / Oracle /
 * SQL Server. Callers must keep the value a plain identifier-like token
 * (a dsCode); control characters and comment openers are rejected outright so a
 * crafted value cannot break out of the literal and start a new statement.
 * </p>
 */

public final class QuotedLiteral {

    /**
     * Accepted value charset: identifier-like tokens only.
     */
    private static final Pattern SAFE_VALUE = Pattern.compile("^[\\p{Alnum}._:@/\\\\-]{1,200}$");

    /**
     * No instances — static helper.
     */
    private QuotedLiteral() {
    }

    /**
     * Render a value as a quoted SQL literal.
     *
     * @param value raw value
     * @return the value wrapped in single quotes, safe to splice
     */
    public static String of(String value) {
        if (value == null || value.isBlank()) {
            throw BusinessException.badRequest("quoted literal: value is required");
        }

        if (!SAFE_VALUE.matcher(value).matches()) {
            throw BusinessException.badRequest("quoted literal: unsupported characters in value");
        }

        return "'" + value.replace("'", "''") + "'";
    }
}
