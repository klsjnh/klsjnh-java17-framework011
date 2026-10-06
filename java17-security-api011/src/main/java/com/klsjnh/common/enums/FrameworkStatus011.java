package com.klsjnh.common.enums;

/*                FrameworkStatus011 enum
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  framework status 011 class
 *
 */

import java.util.Locale;

/**
 * Runtime mode of the framework, bound to {@code krt.status}.
 * <p>
 * Gates the auth filter: {@code DEBUG} accepts anonymous read-class requests
 * (write-class requests still need a valid token), {@code DEVELOPMENT}
 * requires a valid token and allows passwordless login, {@code PRODUCTION}
 * requires a valid token. A missing value resolves to {@code PRODUCTION} — a
 * missing config must never open the gate.
 * </p>
 * <p>
 * Also selects the permission PEP posture: {@code DEVELOPMENT} and
 * {@code PRODUCTION} run real {@code assertHas}/{@code has} checks
 * (development mirrors production so permission bugs surface before
 * shipping); {@code PRODUCTION} additionally enables the write whitelist
 * gate. {@code DEBUG} keeps the checks no-op (local integration is not
 * blocked). See IAM dynamic-permission docs.
 * </p>
 */

public enum FrameworkStatus011 {

    /** Anonymous read-class requests pass; write-class requests need a token; best-effort parses a token for the audit operator. */
    DEBUG("debug"),

    /** Token verified; real permission checks; login checks configured user list by username only. */
    DEVELOPMENT("development"),

    /** Token verified; login checks database with bcrypt. */
    PRODUCTION("production");

    /**
     * Canonical config code.
     */
    private final String code;

    /**
     * Create a status constant.
     *
     * @param code canonical config code
     */
    FrameworkStatus011(String code) {
        this.code = code;
    }

    /**
     * Get the canonical config code.
     *
     * @return canonical code (debug / development / production)
     */
    public String getCode() {
        return code;
    }

    /**
     * Resolve from a raw config value; unknown values are rejected instead of
     * silently falling back (fail fast at startup).
     *
     * @param value raw {@code krt.status} string
     * @return resolved status, {@code PRODUCTION} when blank
     */
    public static FrameworkStatus011 fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return PRODUCTION;
        }

        String v = value.trim().toLowerCase(Locale.ROOT);

        for (FrameworkStatus011 status : values()) {
            if (status.code.equals(v)) {
                return status;
            }
        }

        throw new IllegalArgumentException("unknown krt.status: " + value);
    }

    /**
     * Whether the gate is fully open.
     *
     * @return true for {@code DEBUG}
     */
    public boolean isDebug() {
        return this == DEBUG;
    }

    /**
     * Whether passwordless login is permitted.
     *
     * @return true for {@code DEBUG} / {@code DEVELOPMENT}
     */
    public boolean allowsPasswordlessLogin() {
        return this != PRODUCTION;
    }

    /**
     * Whether this status is production.
     *
     * @return true for {@code PRODUCTION}
     */
    public boolean isProduction() {
        return this == PRODUCTION;
    }

    /**
     * Whether permission enforcement is whitelist (deny-by-default for
     * unchecked mutating requests on protected paths). Bound to production
     * status only; debug keeps {@code assertHas} a no-op, development runs
     * real checks without the post-hoc whitelist gate.
     *
     * @return true for {@code PRODUCTION}
     */
    public boolean isPermissionWhitelistMode() {
        return this == PRODUCTION;
    }
}
