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
 * Gates the auth filter: {@code DEBUG} skips token verification entirely,
 * {@code DEVELOPMENT} additionally allows passwordless login, {@code PRODUCTION}
 * is whitelist + valid token only. A missing value resolves to
 * {@code PRODUCTION} — a missing config must never open the gate.
 * </p>
 */

public enum FrameworkStatus011 {

    /** No token verification, passthrough. */
    DEBUG("debug"),

    /** Token verified; login checks configured user list by username only. */
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
}
