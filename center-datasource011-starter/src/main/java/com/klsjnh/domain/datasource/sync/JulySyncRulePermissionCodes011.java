package com.klsjnh.domain.datasource.sync;

/*                JulySyncRulePermissionCodes011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  july sync rule permission codes
 *
 */

/**
 * Permission codes for julySyncRule management — must match catalog seed.
 */

public final class JulySyncRulePermissionCodes011 {

    /**
     * View (getByCode / getWithChildren / selectListByPage).
     */
    public static final String SELECT = "datasource:julySyncRule:select";

    /**
     * Insert.
     */
    public static final String INSERT = "datasource:julySyncRule:insert";

    /**
     * Update (saveWhole on existing id).
     */
    public static final String UPDATE = "datasource:julySyncRule:update";

    /**
     * Logic delete.
     */
    public static final String LOGIC_DELETE = "datasource:julySyncRule:logicDelete";

    /**
     * Run a rule once.
     */
    public static final String RUN = "datasource:julySyncRule:run";

    private JulySyncRulePermissionCodes011() {
    }
}
