package com.klsjnh.common.enums;

/*                Status011 enum
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  row status flag 0 disabled 1 enabled
 *
 */

import java.util.Locale;

/**
 * Row status flag bound to the {@code status} column.
 * <p>
 * {@code "0"} = disabled, {@code "1"} = enabled. Replaces magic string literals
 * scattered in business code; callers use {@code Status011.ENABLED.getCode()}.
 * </p>
 */

public enum Status011 {

    /**
     * Disabled, code "0".
     */
    DISABLED("0"),

    /**
     * Enabled, code "1".
     */
    ENABLED("1");

    /**
     * Column value, string.
     */
    private final String code;

    /**
     * Create a status constant.
     *
     * @param code column value
     */
    Status011(String code) {
        this.code = code;
    }

    /**
     * Get the column value.
     *
     * @return column value ("0" / "1")
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
    public static Status011 of(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String v = value.trim().toLowerCase(Locale.ROOT);

        for (Status011 status : values()) {
            if (status.code.equals(v)) {
                return status;
            }
        }

        return null;
    }
}
