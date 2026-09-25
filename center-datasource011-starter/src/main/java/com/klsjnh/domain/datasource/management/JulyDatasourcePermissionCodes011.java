package com.klsjnh.domain.datasource.management;

/*                JulyDatasourcePermissionCodes011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  july datasource permission codes
 *
 */

/**
 * Permission codes for julyDatasource management — must match catalog seed.
 */

public final class JulyDatasourcePermissionCodes011 {

    /**
     * View (getById / selectListByPage).
     */
    public static final String SELECT = "datasource:julyDatasource:select";

    /**
     * Insert.
     */
    public static final String INSERT = "datasource:julyDatasource:insert";

    /**
     * Update.
     */
    public static final String UPDATE = "datasource:julyDatasource:update";

    /**
     * Logic delete (single + batch).
     */
    public static final String LOGIC_DELETE = "datasource:julyDatasource:logicDelete";

    /**
     * Test connection (draft + saved retest).
     */
    public static final String TEST_CONNECTION = "datasource:julyDatasource:testConnection";

    /**
     * Reload runtime registry from the table.
     */
    public static final String RELOAD_REGISTRY = "datasource:julyDatasource:reloadRegistry";

    private JulyDatasourcePermissionCodes011() {
    }
}
