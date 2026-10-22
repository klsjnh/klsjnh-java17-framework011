package com.klsjnh.domain.aicenter.inference;

/*                AiReasoningEffort enum
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.22  controlled reasoning_effort values
 *
 */

import java.util.Locale;

/**
 * The controlled {@code reasoning_effort} values a caller may request. The
 * vendor extension is opt-in per provider: the use case only forwards it when
 * the provider code is allow-listed in
 * {@code krt.ai-center.inference.reasoning-effort-providers}.
 */

public enum AiReasoningEffort {

    /**
     * No extra reasoning.
     */
    NONE("none"),

    /**
     * Low effort.
     */
    LOW("low"),

    /**
     * Medium effort.
     */
    MEDIUM("medium"),

    /**
     * High effort.
     */
    HIGH("high");

    /**
     * Wire value.
     */
    private final String code;

    /**
     * Create the constant.
     *
     * @param code wire value
     */
    AiReasoningEffort(String code) {
        this.code = code;
    }

    /**
     * Get the wire value.
     *
     * @return wire value
     */
    public String getCode() {
        return code;
    }

    /**
     * Parse a wire value, case insensitive.
     *
     * @param value wire value
     * @return matching constant, null for a blank value
     * @throws IllegalArgumentException for an unsupported value
     */
    public static AiReasoningEffort from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);

        for (AiReasoningEffort effort : values()) {
            if (effort.code.equals(normalized)) {
                return effort;
            }
        }

        throw new IllegalArgumentException("unsupported reasoning effort: " + value);
    }
}
