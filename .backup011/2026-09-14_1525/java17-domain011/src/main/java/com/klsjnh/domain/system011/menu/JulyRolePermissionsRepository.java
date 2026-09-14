package com.klsjnh.domain.system011.menu;

/*                JulyRolePermissionsRepository interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july role permissions repository interface
 *
 */

import java.util.List;

/**
 * Repository port for the july_role_permissions junction (role × menu ×
 * permission code). Assignment uses toggle semantics: revive an existing row
 * or insert a new one; unassignment sets dr='1'.
 */

public interface JulyRolePermissionsRepository {

    /**
     * Grant one menu to a role: revive the row when it exists, insert
     * otherwise.
     *
     * @param pkMt           role id (master link)
     * @param pkMenu         menu id
     * @param permissionCode permission code snapshot, blank for pure visibility
     */
    void grant(String pkMt, String pkMenu, String permissionCode);

    /**
     * Revoke all permission rows of one menu from a role.
     *
     * @param pkMt   role id (master link)
     * @param pkMenu menu id
     */
    void revokeByMenu(String pkMt, String pkMenu);

    /**
     * Menu ids currently granted to a role.
     *
     * @param pkMt role id (master link)
     * @return granted menu id list
     */
    List<String> findMenuIds(String pkMt);

    /**
     * Permission codes currently granted to a role (button-level authority).
     *
     * @param pkMt role id (master link)
     * @return permission code list
     */
    List<String> findPermissionCodes(String pkMt);
}
