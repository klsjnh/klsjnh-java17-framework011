package com.klsjnh.domain.datasource;

/*                ConnectionInfo record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate 2026.09.15
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  connection info record
 *      2026.09.15  add schemaName / driverClass / poolConfig
 *
 */

import com.klsjnh.common.util.StringUtil011;

/**
 * Dynamic datasource connection config: one business database target
 * addressable by its unique dsCode.
 * <p>
 * Two producers feed this record — the bootstrap yaml ({@code krt.ci011}) and
 * the table-driven source (july_datasource). Both must express the same set of
 * fields, hence the extended tail: {@code schemaName} / {@code driverClass} /
 * {@code poolConfig}. The three extra fields are optional strings; a blank
 * {@code driverClass} falls back to the database type default.
 * </p>
 *
 * @param dsCode      datasource code, unique
 * @param dsName      datasource name, display only
 * @param dsType      database type (mysql / postgresql / oracle / sqlserver)
 * @param dsUrl       jdbc url
 * @param username    login user
 * @param password    login password
 * @param schemaName  schema / database name, optional
 * @param driverClass jdbc driver class, optional; blank uses the type default
 * @param poolConfig  pool config json, optional; reserved for phase two
 */

public record ConnectionInfo(String dsCode, String dsName, String dsType, String dsUrl, String username,
        String password, String schemaName, String driverClass, String poolConfig) {

    /**
     * Normalize the optional tail so yaml binding and table mapping produce
     * the same shape (blank → null); the five required identity fields plus
     * the two credential fields are left untouched.
     *
     * @param dsCode      datasource code, unique
     * @param dsName      datasource name, display only
     * @param dsType      database type
     * @param dsUrl       jdbc url
     * @param username    login user
     * @param password    login password
     * @param schemaName  schema / database name, optional
     * @param driverClass jdbc driver class, optional
     * @param poolConfig  pool config json, optional
     */
    public ConnectionInfo {
        schemaName = blankToNull(schemaName);
        driverClass = blankToNull(driverClass);
        poolConfig = blankToNull(poolConfig);
    }

    /**
     * Build the bootstrap shape from the six core fields (yaml ci011 path).
     *
     * @param dsCode   datasource code
     * @param dsName   datasource name
     * @param dsType   database type
     * @param dsUrl    jdbc url
     * @param username login user
     * @param password login password
     * @return connection info with a null optional tail
     */
    public static ConnectionInfo ofCore(String dsCode, String dsName, String dsType, String dsUrl, String username,
            String password) {
        return new ConnectionInfo(dsCode, dsName, dsType, dsUrl, username, password, null, null, null);
    }

    /**
     * Whether this config targets the same physical target as another one —
     * the pool retention criterion of the registry reload: dsCode equal AND
     * dsUrl / username / password / dsType all equal means the built pool can
     * be reused as is.
     *
     * @param other the other config, nullable
     * @return true when the physical target is unchanged
     */
    public boolean sameTarget(ConnectionInfo other) {
        if (other == null) {
            return false;
        }

        return eq(dsUrl, other.dsUrl)
                && eq(username, other.username)
                && eq(password, other.password)
                && eq(dsType, other.dsType);
    }

    /**
     * Null-safe string equality.
     *
     * @param left  left value
     * @param right right value
     * @return true when both null or both equal
     */
    private static boolean eq(String left, String right) {
        return left == null ? right == null : left.equals(right);
    }

    /**
     * Normalize a blank optional string to null.
     *
     * @param value raw value
     * @return trimmed value or null when blank
     */
    private static String blankToNull(String value) {
        return StringUtil011.isBlank(value) ? null : value.trim();
    }
}
