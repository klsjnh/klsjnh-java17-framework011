package com.klsjnh.domain.iam.auth;

/*                UserRoleCodesPort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  optional role-code lookup for login (access center)
 *
 */

import java.util.List;

/**
 * Optional port: resolve role codes granted to a user. Implemented by the
 * access center; core login returns an empty list when no implementation is
 * registered.
 */

public interface UserRoleCodesPort {

    /**
     * Role codes currently granted to the user.
     *
     * @param userId user id
     * @return role code list, never null
     */
    List<String> findRoleCodes(String userId);
}
