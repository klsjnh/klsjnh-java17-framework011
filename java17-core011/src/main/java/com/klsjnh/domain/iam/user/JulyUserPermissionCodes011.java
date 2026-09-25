package com.klsjnh.domain.iam.user;

/*                JulyUserPermissionCodes011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  july user permission codes for auth
 *
 */

/**
 * Permission codes for julyUser management (assertHas in UseCase).
 */

public final class JulyUserPermissionCodes011 {

    /**
     * View (getById / selectListByPage).
     */
    public static final String SELECT = "iam:julyUser:select";

    /**
     * Insert.
     */
    public static final String INSERT = "iam:julyUser:insert";

    /**
     * Update.
     */
    public static final String UPDATE = "iam:julyUser:update";

    /**
     * Logic delete.
     */
    public static final String LOGIC_DELETE = "iam:julyUser:logicDelete";

    /**
     * Admin reset password.
     */
    public static final String RESET_PASSWORD = "iam:julyUser:resetPassword";

    /**
     * Self change password.
     */
    public static final String CHANGE_PASSWORD = "iam:julyUser:changePassword";

    /**
     * Assign roles to a user.
     */
    public static final String ASSIGN_ROLES = "iam:julyUser:assignRoles";

    /**
     * Export.
     */
    public static final String EXPORT = "iam:julyUser:export";

    /**
     * Backup.
     */
    public static final String BACKUP = "iam:julyUser:backup";

    private JulyUserPermissionCodes011() {
    }
}
