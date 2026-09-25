package com.klsjnh.domain.iam.auth;

/*                AuthorizationPort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.24
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.24  authorization port for permission codes
 *
 */

import java.util.Set;

/**
 * Authorization port (IAM): resolve whether an operator holds a permission
 * code. Callers (use cases) decide when to check; this port never intercepts
 * HTTP globally. Built-in roles bypass with a full grant.
 */

public interface AuthorizationPort {

    /**
     * List distinct non-blank permission codes granted to the operator through
     * their roles. Built-in role holders receive a sentinel that makes
     * {@link #has} always true (callers should prefer {@link #has} /
     * {@link #assertHas}).
     *
     * @param operatorId user id
     * @return permission code set, never null
     */
    Set<String> listCodes(String operatorId);

    /**
     * Whether the operator holds the permission code (built-in role = true).
     *
     * @param operatorId     user id
     * @param permissionCode permission code ({@code module:object:action})
     * @return true when allowed
     */
    boolean has(String operatorId, String permissionCode);

    /**
     * Require the permission code or throw forbidden / unauthorized.
     *
     * @param operatorId     user id
     * @param permissionCode permission code
     */
    void assertHas(String operatorId, String permissionCode);
}
