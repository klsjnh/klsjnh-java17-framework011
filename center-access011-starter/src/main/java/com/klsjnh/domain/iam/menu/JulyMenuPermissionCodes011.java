package com.klsjnh.domain.iam.menu;

/*                JulyMenuPermissionCodes011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  july menu permission codes for auth
 *
 */

/**
 * Permission codes for julyMenu management (assertHas in UseCase).
 */

public final class JulyMenuPermissionCodes011 {

    /**
     * View (getById / selectListByPage / getTree).
     */
    public static final String SELECT = "iam:julyMenu:select";

    /**
     * Insert.
     */
    public static final String INSERT = "iam:julyMenu:insert";

    /**
     * Update.
     */
    public static final String UPDATE = "iam:julyMenu:update";

    /**
     * Logic delete.
     */
    public static final String LOGIC_DELETE = "iam:julyMenu:logicDelete";

    /**
     * Export.
     */
    public static final String EXPORT = "iam:julyMenu:export";

    /**
     * Backup.
     */
    public static final String BACKUP = "iam:julyMenu:backup";

    private JulyMenuPermissionCodes011() {
    }
}
