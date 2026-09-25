package com.klsjnh.common.enums;

/*                MessageStatus011 enum
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  message send status enum (numeric codes)
 *
 */

import java.util.Locale;

/**
 * Message send status bound to the {@code july_message_outbound.status} column — the
 * business status of a send record (the generic {@code status} column carries a
 * table-specific numeric vocabulary when the business declares one).
 * <p>
 * {@code "1"} = pending, {@code "2"} = success, {@code "3"} = failed (4/5…
 * reserved). Stores a numeric code, never the enum name; callers use
 * {@code MessageStatus011.SUCCESS.getCode()}.
 * </p>
 */

public enum MessageStatus011 {

    /**
     * Pending: not yet dispatched (code "1").
     */
    PENDING("1"),

    /**
     * Success: the channel accepted the message (code "2").
     */
    SUCCESS("2"),

    /**
     * Failed: the channel rejected the message (code "3").
     */
    FAILED("3");

    /**
     * Column value, string.
     */
    private final String code;

    /**
     * Create a status constant.
     *
     * @param code column value
     */
    MessageStatus011(String code) {
        this.code = code;
    }

    /**
     * Get the column value.
     *
     * @return column value ("1" / "2" / "3")
     */
    public String getCode() {
        return code;
    }

    /**
     * Resolve from a raw column value; null when blank or unknown.
     *
     * @param value raw status column value
     * @return resolved constant, null when blank or unknown
     */
    public static MessageStatus011 of(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String v = value.trim().toLowerCase(Locale.ROOT);

        for (MessageStatus011 status : values()) {
            if (status.code.equals(v)) {
                return status;
            }
        }

        return null;
    }
}
