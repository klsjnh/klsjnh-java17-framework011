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
 * code. Use cases call {@link #assertHas} to declare and enforce a code.
 * <p>
 * Hang {@link #assertHas} on management write (and preferably read) entries
 * everywhere. Enforcement depends on {@code krt.status}:
 * </p>
 * <ul>
 * <li><b>debug / development</b> — {@code assertHas} / {@code has} are no-op
 * (always allow); convenient for local integration while call sites stay
 * hung.</li>
 * <li><b>production</b> — real code check (missing → 403). The web whitelist
 * gate also requires that a mutating protected request marked a check via
 * {@link PermissionCheckContext011}; otherwise the filter answers 403.</li>
 * </ul>
 * <p>Built-in roles bypass with a full grant when checks are active.</p>
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
     * Require the permission code or throw forbidden / unauthorized. Successful
     * and failing calls both mark {@link PermissionCheckContext011} so the
     * production whitelist gate sees that a check occurred (a missing code
     * still yields 403 from this method).
     *
     * @param operatorId     user id
     * @param permissionCode permission code
     */
    void assertHas(String operatorId, String permissionCode);
}
