package com.klsjnh.common.util;

/*                JdbcUrlGuard011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.05
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.05  jdbc url guard (scheme whitelist + forbidden parameters)
 *
 */

import com.klsjnh.common.constant.DatabaseTypes011;
import com.klsjnh.common.exception.BusinessException;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * JDBC URL guard for the datasource center: a built-in database type must use
 * its own scheme prefix (no cross-type URLs, no protocol tricks), and known
 * driver-side attack parameters (local-file read, client deserialization,
 * interceptor injection) are rejected for every type.
 */

public final class JdbcUrlGuard011 {

    /**
     * Forbidden connection-parameter fragments (substring, case-insensitive):
     * MySQL local-file read ({@code allowLoadLocalInfile} /
     * {@code allowUrlInLocalInfile}), client deserialization
     * ({@code autoDeserialize}) and interceptor injection
     * ({@code queryInterceptors} / {@code statementInterceptors}).
     */
    private static final Pattern FORBIDDEN_PARAM = Pattern.compile(
            "allowloadlocalinfile|allowurlinlocalinfile|autodeserialize|queryinterceptors|statementinterceptors",
            Pattern.CASE_INSENSITIVE);

    private JdbcUrlGuard011() {
    }

    /**
     * Assert a JDBC URL is allowed for the database type: a built-in type must
     * use its own scheme prefix, and forbidden connection parameters are
     * rejected for every type.
     *
     * @param dbType database type code (custom types keep the bare {@code jdbc:} rule)
     * @param url    jdbc url
     */
    public static void assertAllowed(String dbType, String url) {
        if (url == null || url.isBlank()) {
            throw BusinessException.badRequest("jdbc url is required");
        }

        String prefix = DatabaseTypes011.jdbcUrlPrefix(dbType);

        if (prefix != null && !url.toLowerCase(Locale.ROOT).startsWith(prefix)) {
            throw BusinessException.badRequest(
                    "jdbc url must start with " + prefix + " for database type " + dbType);
        }

        if (FORBIDDEN_PARAM.matcher(url).find()) {
            throw BusinessException.badRequest("jdbc url contains a forbidden connection parameter");
        }
    }
}
