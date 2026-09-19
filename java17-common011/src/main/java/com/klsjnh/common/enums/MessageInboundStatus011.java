package com.klsjnh.common.enums;

/*                MessageInboundStatus011 enum
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  message inbound status enum (numeric codes)
 *
 */

import java.util.Locale;

/**
 * Inbound message handling status bound to {@code july_message_inbound.status}
 * — the business status of a received record.
 * <p>
 * {@code "1"} = received, {@code "2"} = handled, {@code "3"} = failed (4/5…
 * reserved). Stores a numeric code, never the enum name.
 * </p>
 */

public enum MessageInboundStatus011 {

    /**
     * Received: persisted, not yet handled (code "1").
     */
    RECEIVED("1"),

    /**
     * Handled: the listener finished successfully (code "2").
     */
    HANDLED("2"),

    /**
     * Failed: the listener threw (code "3").
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
    MessageInboundStatus011(String code) {
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
    public static MessageInboundStatus011 of(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String v = value.trim().toLowerCase(Locale.ROOT);

        for (MessageInboundStatus011 status : values()) {
            if (status.code.equals(v)) {
                return status;
            }
        }

        return null;
    }
}
