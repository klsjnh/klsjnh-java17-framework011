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
 * Gates the auth filter: {@code DEBUG} is fully open (no token, virtual
 * operator {@code dev011}), {@code DEVELOPMENT} requires a valid token but
 * allows passwordless login, {@code PRODUCTION} requires a valid token and
 * disables passwordless login. A missing value resolves to
 * {@code PRODUCTION} — a missing config must never open the gate.
 * </p>
 * <p>
 * Also selects the permission PEP posture: {@code DEBUG} and
 * {@code DEVELOPMENT} keep {@code assertHas}/{@code has} no-op so local
 * integration and role-less development are not blocked; {@code PRODUCTION}
 * runs real checks and enables the read/write whitelist gate. See IAM
 * dynamic-permission docs.
 * </p>
 */

public enum FrameworkStatus011 {

    /** Fully open: all requests pass; anonymous callers get the virtual dev011 operator. */
    DEBUG("debug"),

    /** Token verified; passwordless GET/POST login enabled; permission checks are no-op. */
    DEVELOPMENT("development"),

    /** Token verified; passwordless login disabled; real RBAC + read/write whitelist gate. */
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

        if ("production".equals(v) || "prod".equals(v)) {
            return PRODUCTION;
        }

        if (v.equals("debug") || v.startsWith("debug")) {
            return DEBUG;
        }

        if (v.equals("development") || v.equals("dev") || v.startsWith("dev")) {
            return DEVELOPMENT;
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
     * unchecked requests on protected paths, reads included). Bound to
     * production status only; debug and development keep {@code assertHas} a
     * no-op.
     *
     * @return true for {@code PRODUCTION}
     */
    public boolean isPermissionWhitelistMode() {
        return this == PRODUCTION;
    }
}
