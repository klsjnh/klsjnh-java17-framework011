package com.klsjnh.domain.iam.role;

/*                JulyRolePermissionCodes011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  july role permission codes for auth
 *
 */

/**
 * Permission codes for julyRole management (assertHas in UseCase).
 */

public final class JulyRolePermissionCodes011 {

    /**
     * View (getById / selectListByPage / getMenusByRole / getUsersByRole /
     * listPermissionCodes).
     */
    public static final String SELECT = "iam:julyRole:select";

    /**
     * Insert.
     */
    public static final String INSERT = "iam:julyRole:insert";

    /**
     * Update.
     */
    public static final String UPDATE = "iam:julyRole:update";

    /**
     * Logic delete.
     */
    public static final String LOGIC_DELETE = "iam:julyRole:logicDelete";

    /**
     * Assign menus to a role.
     */
    public static final String ASSIGN_MENUS = "iam:julyRole:assignMenus";

    /**
     * Assign catalog object actions to a role.
     */
    public static final String ASSIGN_OBJECT_ACTIONS = "iam:julyRole:assignObjectActions";

    /**
     * Export.
     */
    public static final String EXPORT = "iam:julyRole:export";

    /**
     * Backup.
     */
    public static final String BACKUP = "iam:julyRole:backup";

    private JulyRolePermissionCodes011() {
    }
}
