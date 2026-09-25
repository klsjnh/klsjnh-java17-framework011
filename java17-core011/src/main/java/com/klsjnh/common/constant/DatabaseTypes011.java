package com.klsjnh.common.constant;

/*                DatabaseTypes011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  database type codes (open string vocabulary)
 *
 */

import java.util.Locale;

/**
 * Built-in database type codes. These are plain string constants, not an enum:
 * a consumer may support any other database by adding a dialect plug-in
 * ({@code SqlDialectPort011}) and configuring its own {@code dbType} code, and
 * by supplying an explicit {@code driverClass} when the platform has no default.
 */

public final class DatabaseTypes011 {

    /** MySQL. */
    public static final String MYSQL = "mysql";

    /** Oracle. */
    public static final String ORACLE = "oracle";

    /** SQL Server. */
    public static final String SQLSERVER = "sqlserver";

    /** PostgreSQL (accepts the {@code postgres} alias). */
    public static final String POSTGRESQL = "postgresql";

    private DatabaseTypes011() {
    }

    /**
     * Normalize a raw db type: trim, lower-case, map the {@code postgres} alias.
     *
     * @param value raw db type
     * @return normalized code, null when blank
     */
    public static String normalize(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);

        return "postgres".equals(normalized) ? POSTGRESQL : normalized;
    }

    /**
     * The default JDBC driver class of a built-in database type.
     *
     * @param value raw db type
     * @return driver class name, null when the type has no built-in default
     */
    public static String driverClass(String value) {
        String type = normalize(value);

        if (type == null) {
            return null;
        }

        return switch (type) {
            case MYSQL -> "com.mysql.cj.jdbc.Driver";
            case ORACLE -> "oracle.jdbc.OracleDriver";
            case SQLSERVER -> "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            case POSTGRESQL -> "org.postgresql.Driver";
            default -> null;
        };
    }
}
