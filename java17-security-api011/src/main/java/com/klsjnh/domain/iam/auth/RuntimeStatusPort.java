package com.klsjnh.domain.iam.auth;

/*                RuntimeStatusPort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  runtime status port interface
 *
 */

/**
 * Runtime mode port: exposes the krt.status gate decisions (bound by
 * KrtSecurityConfig011 in infrastructure) to the application layer without a framework
 * dependency.
 */

public interface RuntimeStatusPort {

    /**
     * Whether passwordless login is permitted by the current runtime mode
     * (debug / development allow it, production does not).
     *
     * @return true when passwordless login is permitted
     */
    boolean allowsPasswordlessLogin();

    /**
     * Whether the current runtime mode is debug (verbose error details allowed
     * in the response envelope).
     *
     * @return true for {@code debug}
     */
    boolean isDebug();

    /**
     * Whether permission PEP is active (production whitelist): unchecked
     * mutating requests on protected paths are denied, and
     * {@link AuthorizationPort#assertHas} / {@code has} perform real checks.
     * Debug / development return false — assertHas is a no-op for local
     * integration.
     *
     * @return true when {@code krt.status} is production (and access center present)
     */
    boolean isPermissionWhitelistMode();
}
