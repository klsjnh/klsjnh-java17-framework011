package com.klsjnh.common.enums;

/*                AuditType011 enum
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.16
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.16  audit type 011 enum
 *
 */

/**
 * Audit event type bound to {@code july_user_audit.audit_type}: the single
 * vocabulary for user-audit events, so no caller hand-writes the string.
 * <p>
 * IUD events (INSERT / UPDATE / DELETE) are recorded at the WEB layer (controller
 * IUD actions), never in the application service; auth and platform events
 * (LOGIN / LOGOUT / ... / EXPORT / IMPORT / BACKUP) are recorded where the action itself
 * lives.
 * </p>
 */

public enum AuditType011 {

    /** Login success. */
    LOGIN("LOGIN"),

    /** Login failure (wrong password / disabled / rejected passwordless). */
    LOGIN_FAILED("LOGIN_FAILED"),

    /** Logout. */
    LOGOUT("LOGOUT"),

    /** Password changed. */
    CHANGE_PASSWORD("CHANGE_PASSWORD"),

    /** Insert (controller IUD). */
    INSERT("INSERT"),

    /** Update (controller IUD). */
    UPDATE("UPDATE"),

    /** Logic delete (controller IUD). */
    DELETE("DELETE"),

    /** Platform export. */
    EXPORT("EXPORT"),

    /** Platform import. */
    IMPORT("IMPORT"),

    /** Platform backup. */
    BACKUP("BACKUP");

    /**
     * Persistence code, string.
     */
    private final String code;

    /**
     * Create an audit type constant.
     *
     * @param code persistence code
     */
    AuditType011(String code) {
        this.code = code;
    }

    /**
     * Get the persistence code.
     *
     * @return persistence code
     */
    public String getCode() {
        return code;
    }

    /**
     * Resolve from a raw code; null when blank or unknown.
     *
     * @param value raw code
     * @return resolved constant, null when blank or unknown
     */
    public static AuditType011 of(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        for (AuditType011 type : values()) {
            if (type.code.equals(value)) {
                return type;
            }
        }

        return null;
    }
}
