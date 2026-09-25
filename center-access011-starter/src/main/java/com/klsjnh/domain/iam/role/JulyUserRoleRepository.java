package com.klsjnh.domain.iam.role;

/*                JulyUserRoleRepository interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july user role repository interface
 *
 */

import java.util.List;

/**
 * Repository port for the july_user_role junction (user ↔ role, N:N).
 * Assignment uses the toggle semantics: revive (dr='0') an existing row or
 * insert a new one; unassign sets dr='1'.
 */

public interface JulyUserRoleRepository {

    /**
     * Assign a role to a user: revive the row when it exists, insert otherwise.
     *
     * @param pkMt   user id (master link)
     * @param pkRole role id
     */
    void assign(String pkMt, String pkRole);

    /**
     * Unassign a role from a user: set dr='1' on the row.
     *
     * @param pkMt   user id (master link)
     * @param pkRole role id
     */
    void unassign(String pkMt, String pkRole);

    /**
     * Role ids currently granted to a user.
     *
     * @param pkMt user id (master link)
     * @return enabled role id list
     */
    List<String> findRoleIds(String pkMt);

    /**
     * User ids currently holding a role.
     *
     * @param pkRole role id
     * @return enabled user id list
     */
    List<String> findUserIds(String pkRole);
}
