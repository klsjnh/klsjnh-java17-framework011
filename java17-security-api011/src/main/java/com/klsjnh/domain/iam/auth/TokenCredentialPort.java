package com.klsjnh.domain.iam.auth;

/*                TokenCredentialPort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  token credential lookup for jwt revoke
 *
 */

/**
 * Lookup of the durable credential state used to revoke JWTs without a
 * denylist: the signed token carries {@code tv} (token version); verify loads
 * the current version and account status and rejects mismatches / disabled
 * accounts.
 */

public interface TokenCredentialPort {

    /**
     * Durable credential state for one user.
     *
     * @param tokenVersion monotonic version embedded in issued JWTs
     * @param status       account status ({@code 1} enabled / {@code 0} disabled)
     */
    record CredentialState(int tokenVersion, String status) {
    }

    /**
     * Load credential state by user id.
     *
     * @param userId july_user primary key
     * @return state, or null when the user is missing / deleted
     */
    CredentialState findByUserId(String userId);
}
