package com.klsjnh.domain.iam.organization;

/*                JulyOrganizationPermissionCodes011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  july organization permission codes for auth
 *
 */

/**
 * Permission codes for julyOrganization management (assertHas in UseCase).
 */

public final class JulyOrganizationPermissionCodes011 {

    /**
     * View (getById / selectListByPage / getTree).
     */
    public static final String SELECT = "iam:julyOrganization:select";

    /**
     * Insert.
     */
    public static final String INSERT = "iam:julyOrganization:insert";

    /**
     * Update.
     */
    public static final String UPDATE = "iam:julyOrganization:update";

    /**
     * Logic delete.
     */
    public static final String LOGIC_DELETE = "iam:julyOrganization:logicDelete";

    /**
     * Export.
     */
    public static final String EXPORT = "iam:julyOrganization:export";

    /**
     * Backup.
     */
    public static final String BACKUP = "iam:julyOrganization:backup";

    private JulyOrganizationPermissionCodes011() {
    }
}
