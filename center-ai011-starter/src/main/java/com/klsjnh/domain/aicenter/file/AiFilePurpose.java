package com.klsjnh.domain.aicenter.file;

/*                AiFilePurpose enum
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.22  neutral provider file purposes
 *
 */

import java.util.Locale;

/**
 * Neutral purpose for an AI provider file asset. The wire value matches the
 * common OpenAI / StepFun vocabulary; a vendor adapter maps it when its spelling
 * differs.
 */

public enum AiFilePurpose {

    /** General storage for image / video understanding, voice cloning ... */
    STORAGE("storage"),

    /** Document content extraction (pdf / word / excel / ppt / text). */
    TEXT_EXTRACT("file-extract"),

    /** Text knowledge base. */
    RETRIEVAL_TEXT("retrieval-text"),

    /** Image knowledge base. */
    RETRIEVAL_IMAGE("retrieval-image"),

    /** OpenAI assistants file. */
    ASSISTANTS("assistants"),

    /** OpenAI batch file. */
    BATCH("batch"),

    /** OpenAI fine-tune file. */
    FINE_TUNE("fine-tune"),

    /** OpenAI vision file. */
    VISION("vision");

    /**
     * Wire value.
     */
    private final String code;

    /**
     * Create the constant.
     *
     * @param code wire value
     */
    AiFilePurpose(String code) {
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
     * @return constant, null for a blank value
     * @throws IllegalArgumentException for an unsupported value
     */
    public static AiFilePurpose from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);

        for (AiFilePurpose purpose : values()) {
            if (purpose.code.equals(normalized)) {
                return purpose;
            }
        }

        throw new IllegalArgumentException("unsupported file purpose: " + value);
    }
}
