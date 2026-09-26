package com.klsjnh.domain.iam.auth;

/*                AuthTokenPort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  auth token port interface
 *      2026.09.26  issue carries tokenVersion (tv claim)
 *
 */

/**
 * Auth token port: the JWT adapter lives in infrastructure (jjwt +
 * krt.jwt.secret from KrtSecurityConfig011). Issued tokens carry a
 * {@code tv} claim; verify rejects version mismatch or a disabled account
 * via {@link TokenCredentialPort}.
 */

public interface AuthTokenPort {

    /**
     * Verified operator identity carried by a signed token.
     *
     * @param id          user id
     * @param userAccount login account (the token subject)
     */
    record OperatorIdentity(String id, String userAccount) {
    }

    /**
     * Issue a signed token for a user.
     *
     * @param id           user id
     * @param userAccount  login account
     * @param tokenVersion current {@code july_user.token_version} at issue time
     * @return signed JWT string
     */
    String issue(String id, String userAccount, int tokenVersion);

    /**
     * Verify a signed token and return the full operator identity (id +
     * account) — what the auth filter needs to fill both audit columns.
     *
     * @param token signed JWT
     * @return operator identity, or null when missing / malformed / expired /
     *         signature-invalid / version-mismatched / account-disabled
     */
    OperatorIdentity verify(String token);

    /**
     * Verify a signed token and return its operator user id.
     *
     * @param token signed JWT
     * @return the user id claim, or null when missing / malformed / expired /
     *         signature-invalid / version-mismatched / account-disabled
     */
    String verifyAndGetId(String token);
}
