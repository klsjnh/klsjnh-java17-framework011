package com.klsjnh.domain.system011.config;

/*                JulyConfigPermissionCodes011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.24
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.24  july config permission codes for auth demo
 *
 */

/**
 * Permission codes for the julyConfig demo blueprint (temporarily object-
 * scoped; export / backup will later lift to platform-level codes).
 */

public final class JulyConfigPermissionCodes011 {

    /**
     * View (getById / selectListByPage).
     */
    public static final String SELECT = "system011:julyConfig:select";

    /**
     * Insert.
     */
    public static final String INSERT = "system011:julyConfig:insert";

    /**
     * Update.
     */
    public static final String UPDATE = "system011:julyConfig:update";

    /**
     * Logic delete.
     */
    public static final String LOGIC_DELETE = "system011:julyConfig:logicDelete";

    /**
     * Export (demo hang under julyConfig; later platform).
     */
    public static final String EXPORT = "system011:julyConfig:export";

    /**
     * Backup (demo hang under julyConfig; later platform).
     */
    public static final String BACKUP = "system011:julyConfig:backup";

    private JulyConfigPermissionCodes011() {
    }
}
