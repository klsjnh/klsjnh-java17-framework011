package com.klsjnh.domain.datasource.management;

/*                JulyDatasource class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july datasource class
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

/**
 * JulyDatasource aggregate root (data service context): one runtime business
 * database connection registered by dsCode — the table-driven source of truth
 * for dynamic datasource routing.
 * <p>
 * Boundary: {@code krt.ci011} (application yaml) is the bootstrap source only;
 * a given dsCode lives in exactly ONE home. {@code ds_code} is the pool name
 * and the routing key, unique and immutable after create. The {@code master}
 * code is reserved for the primary datasource and must never be declared here.
 * </p>
 */

public class JulyDatasource {

    /**
     * Reserved dsCode of the primary datasource; never declarable.
     */
    private static final String RESERVED_CODE = "master";

    /**
     * Default sort order when none is given (matches BasePo011 / DDL default).
     */
    private static final int DEFAULT_SORT_ORDER = 9999;

    /**
     * Primary key.
     */
    private final EntityId id;

    /**
     * Datasource code, unique, immutable, doubles as the pool name.
     */
    private final String dsCode;

    /**
     * Manual sort order, smaller comes first.
     */
    private Integer sortOrder;

    /**
     * Datasource name, display only.
     */
    private String dsName;

    /**
     * Database type code (mysql / oracle / sqlserver / postgresql).
     */
    private String dbType;

    /**
     * JDBC URL.
     */
    private String jdbcUrl;

    /**
     * Schema / database name, optional.
     */
    private String schemaName;

    /**
     * Login user, optional.
     */
    private String username;

    /**
     * Login password, optional; never echoed back by the web layer.
     * Domain validates plaintext length ≤300; persistence may store ciphertext
     * ({@code enc:v1:}, DB column VARCHAR(512)).
     */
    private String password;

    /**
     * JDBC driver class, optional; blank falls back to the database type
     * default.
     */
    private String driverClass;

    /**
     * Pool config JSON, optional; reserved for phase two, not parsed yet.
     */
    private String poolConfig;

    /**
     * Remark, optional.
     */
    private String remark;

    /**
     * Row status: '1' enabled / '0' disabled (disabled is removed from the
     * runtime registry).
     */
    private String status;

    /**
     * Audit info.
     */
    private AuditInfo audit;

    /**
     * Full constructor (also the rehydration path from persistence).
     *
     * @param id          primary key
     * @param dsCode      datasource code, unique
     * @param sortOrder   manual sort order, null falls back to the default
     * @param dsName      datasource name
     * @param dbType      database type code
     * @param jdbcUrl     jdbc url
     * @param schemaName  schema name, optional
     * @param username    login user, optional
     * @param password    login password, optional
     * @param driverClass jdbc driver class, optional
     * @param poolConfig  pool config json, optional
     * @param remark      remark, optional
     * @param status      row status
     * @param audit       audit info
     */
    public JulyDatasource(EntityId id, String dsCode, Integer sortOrder, String dsName, String dbType, String jdbcUrl,
            String schemaName, String username, String password, String driverClass, String poolConfig, String remark,
            String status, AuditInfo audit) {
        this.id = id;
        this.dsCode = dsCode;
        this.sortOrder = sortOrder == null ? DEFAULT_SORT_ORDER : sortOrder;
        this.dsName = dsName;
        this.dbType = dbType;
        this.jdbcUrl = jdbcUrl;
        this.schemaName = schemaName;
        this.username = username;
        this.password = password;
        this.driverClass = driverClass;
        this.poolConfig = poolConfig;
        this.remark = remark;
        this.status = status == null ? Status011.ENABLED.getCode() : status;
        this.audit = audit == null ? AuditInfo.empty() : audit;
    }

    /**
     * Factory for a new datasource entry.
     *
     * @param id          primary key
     * @param dsCode      datasource code, unique, max 60
     * @param sortOrder   manual sort order, null falls back to the default
     * @param dsName      datasource name, max 100
     * @param dbType      database type code, max 20
     * @param jdbcUrl     jdbc url, max 500, must start with jdbc:
     * @param schemaName  schema name, optional, max 60
     * @param username    login user, optional, max 100
     * @param password    login password, optional, max 300
     * @param driverClass jdbc driver class, optional, max 200
     * @param remark      remark, optional, max 300
     * @param audit       audit info
     * @return new aggregate
     */
    public static JulyDatasource create(EntityId id, String dsCode, Integer sortOrder, String dsName, String dbType,
            String jdbcUrl, String schemaName, String username, String password, String driverClass, String remark,
            AuditInfo audit) {
        validate(dsCode, dsName, dbType, jdbcUrl, schemaName, username, password, driverClass, remark);

        return new JulyDatasource(id, dsCode, sortOrder, dsName, dbType, jdbcUrl, schemaName, username, password,
                driverClass, null, remark, Status011.ENABLED.getCode(), audit);
    }

    /**
     * Update the mutable fields (dsCode is immutable after create). A blank
     * password keeps the stored one (the web layer never echoes it back, so a
     * blank means "unchanged").
     *
     * @param dsName      datasource name
     * @param sortOrder   manual sort order, null keeps the stored one
     * @param dbType      database type code
     * @param jdbcUrl     jdbc url
     * @param schemaName  schema name, optional
     * @param username    login user, optional
     * @param password    login password, optional; blank keeps the stored one
     * @param driverClass jdbc driver class, optional
     * @param remark      remark, optional
     */
    public void update(String dsName, Integer sortOrder, String dbType, String jdbcUrl, String schemaName,
            String username, String password, String driverClass, String remark) {
        validate(this.dsCode, dsName, dbType, jdbcUrl, schemaName, username, password, driverClass, remark);
        this.dsName = dsName;

        if (sortOrder != null) {
            this.sortOrder = sortOrder;
        }

        this.dbType = dbType;
        this.jdbcUrl = jdbcUrl;
        this.schemaName = schemaName;
        this.username = username;
        this.driverClass = driverClass;
        this.remark = remark;

        if (!StringUtil011.isBlank(password)) {
            this.password = password;
        }
    }

    /**
     * Validate the shared create / update basics.
     *
     * @param dsCode      datasource code
     * @param dsName      datasource name
     * @param dbType      database type code
     * @param jdbcUrl     jdbc url
     * @param schemaName  schema name
     * @param username    login user
     * @param password    login password
     * @param driverClass jdbc driver class
     * @param remark      remark
     */
    private static void validate(String dsCode, String dsName, String dbType, String jdbcUrl, String schemaName,
            String username, String password, String driverClass, String remark) {
        StringUtil011.requirePresent(dsCode, "datasource code", 60);

        if (RESERVED_CODE.equals(dsCode)) {
            throw new IllegalArgumentException("datasource code is reserved: " + RESERVED_CODE);
        }

        StringUtil011.requirePresent(dsName, "datasource name", 100);

        if (StringUtil011.isMissing(dbType, 20)) {
            throw new IllegalArgumentException("database type is required: " + dbType);
        }

        StringUtil011.requirePresent(jdbcUrl, "jdbc url", 500);

        if (!jdbcUrl.startsWith("jdbc:")) {
            throw new IllegalArgumentException("jdbc url must start with jdbc:");
        }

        StringUtil011.requireMax(schemaName, "schema name", 60);

        StringUtil011.requireMax(username, "username", 100);

        StringUtil011.requireMax(password, "password", 300);

        StringUtil011.requireMax(driverClass, "driver class", 200);

        StringUtil011.requireMax(remark, "remark", 300);
    }

    /**
     * Get the primary key.
     *
     * @return id value object
     */
    public EntityId id() {
        return id;
    }

    /**
     * Get the datasource code.
     *
     * @return datasource code
     */
    public String dsCode() {
        return dsCode;
    }

    /**
     * Get the manual sort order.
     *
     * @return sort order, smaller comes first
     */
    public Integer sortOrder() {
        return sortOrder;
    }

    /**
     * Get the datasource name.
     *
     * @return datasource name
     */
    public String dsName() {
        return dsName;
    }

    /**
     * Get the database type code.
     *
     * @return database type code
     */
    public String dbType() {
        return dbType;
    }

    /**
     * Get the jdbc url.
     *
     * @return jdbc url
     */
    public String jdbcUrl() {
        return jdbcUrl;
    }

    /**
     * Get the schema name.
     *
     * @return schema name or null
     */
    public String schemaName() {
        return schemaName;
    }

    /**
     * Get the login user.
     *
     * @return username or null
     */
    public String username() {
        return username;
    }

    /**
     * Get the login password.
     *
     * @return password or null
     */
    public String password() {
        return password;
    }

    /**
     * Get the jdbc driver class.
     *
     * @return driver class or null
     */
    public String driverClass() {
        return driverClass;
    }

    /**
     * Get the pool config json.
     *
     * @return pool config or null
     */
    public String poolConfig() {
        return poolConfig;
    }

    /**
     * Get the remark.
     *
     * @return remark or null
     */
    public String remark() {
        return remark;
    }

    /**
     * Get the row status.
     *
     * @return '1' enabled / '0' disabled
     */
    public String status() {
        return status;
    }

    /**
     * Get the audit info.
     *
     * @return audit info
     */
    public AuditInfo audit() {
        return audit;
    }
}
