package com.klsjnh.common.enums;

/*                DatabaseType011 enum
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  database type 011 class
 *
 */

import java.util.Locale;

/**
 * Database type flag bound to the {@code krt.ci011[].type} config value.
 * <p>
 * Carries the JDBC driver class name so callers never hardcode driver strings;
 * {@link #fromString(String)} resolves case-insensitively and accepts the
 * {@code postgres} alias for {@code postgresql}.
 * </p>
 */

public enum DatabaseType011 {

    /** MySQL, mysql-connector-j driver. */
    MYSQL("mysql", "com.mysql.cj.jdbc.Driver"),

    /** Oracle, ojdbc driver. */
    ORACLE("oracle", "oracle.jdbc.OracleDriver"),

    /** SQL Server, mssql-jdbc driver. */
    SQLSERVER("sqlserver", "com.microsoft.sqlserver.jdbc.SQLServerDriver"),

    /** PostgreSQL, pgjdbc driver; accepts the {@code postgres} alias. */
    POSTGRESQL("postgresql", "org.postgresql.Driver");

    /**
     * Config code, string.
     */
    private final String code;

    /**
     * JDBC driver class name, string.
     */
    private final String driverClassName;

    /**
     * Create a database type constant.
     *
     * @param code            config code
     * @param driverClassName jdbc driver class name
     */
    DatabaseType011(String code, String driverClassName) {
        this.code = code;
        this.driverClassName = driverClassName;
    }

    /**
     * Get the config code.
     *
     * @return config code
     */
    public String getCode() {
        return code;
    }

    /**
     * Get the JDBC driver class name.
     *
     * @return jdbc driver class name
     */
    public String getDriverClassName() {
        return driverClassName;
    }

    /**
     * Resolve from a raw config value; null when blank or unknown.
     *
     * @param value raw {@code type} string
     * @return resolved type, null when blank or unknown
     */
    public static DatabaseType011 fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String v = value.trim().toLowerCase(Locale.ROOT);

        if ("postgres".equals(v)) {
            v = "postgresql";
        }

        for (DatabaseType011 type : values()) {
            if (type.code.equals(v)) {
                return type;
            }
        }

        return null;
    }
}
