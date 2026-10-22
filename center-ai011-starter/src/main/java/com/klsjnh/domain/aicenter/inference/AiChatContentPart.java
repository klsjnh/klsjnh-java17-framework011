package com.klsjnh.domain.aicenter.inference;

/*                AiChatContentPart record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.22  multimodal content part record
 *
 */

import java.util.Base64;

/**
 * One typed content part of a chat message: either a text fragment or one
 * image reference. The OpenAI-compatible wire form is
 * {@code {"type":"text","text":"..."}} /
 * {@code {"type":"image_url","image_url":{"url":"..."}}}.
 * <p>
 * The framework only carries and validates the reference: it never downloads an
 * image, never compresses or tiles it and never logs the payload.
 * </p>
 *
 * @param type     {@link #TYPE_TEXT} or {@link #TYPE_IMAGE_URL}
 * @param text     text value, null for an image part
 * @param imageUrl data URL or https URL, null for a text part
 */

public record AiChatContentPart(String type, String text, String imageUrl) {

    /** Text content type. */
    public static final String TYPE_TEXT = "text";

    /** Image URL content type. */
    public static final String TYPE_IMAGE_URL = "image_url";

    /**
     * Accepted data URL prefixes (lower case).
     */
    private static final String[] IMAGE_DATA_PREFIXES = { "data:image/png;base64,",
            "data:image/jpeg;base64,", "data:image/jpg;base64,", "data:image/webp;base64," };

    /**
     * Normalize and validate the shape: type is required and exactly one of
     * text / imageUrl is carried. URL scheme and size are checked separately by
     * {@link #validateImageUrl(String, long)} because size is a runtime policy.
     */
    public AiChatContentPart {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("content part type is required");
        }

        if (TYPE_TEXT.equals(type)) {
            if (text == null) {
                throw new IllegalArgumentException("text content is required");
            }
            if (imageUrl != null) {
                throw new IllegalArgumentException("text content must not carry imageUrl");
            }
        } else if (TYPE_IMAGE_URL.equals(type)) {
            if (imageUrl == null || imageUrl.isBlank()) {
                throw new IllegalArgumentException("imageUrl is required");
            }
            if (text != null) {
                throw new IllegalArgumentException("image_url content must not carry text");
            }
        } else {
            throw new IllegalArgumentException("unsupported content part type: " + type);
        }
    }

    /**
     * Build a text part.
     *
     * @param text text
     * @return text part
     */
    public static AiChatContentPart text(String text) {
        return new AiChatContentPart(TYPE_TEXT, text, null);
    }

    /**
     * Build an image part.
     *
     * @param url data URL or https URL
     * @return image part
     */
    public static AiChatContentPart imageUrl(String url) {
        return new AiChatContentPart(TYPE_IMAGE_URL, null, url);
    }

    /**
     * Whether the URL is an accepted image reference: a base64 data URL for
     * png / jpeg / jpg / webp, or an https URL. Plain http, file and relative
     * paths are rejected (the framework never reads local files).
     *
     * @param url image URL
     * @return true when accepted
     */
    public static boolean isSupportedImageUrl(String url) {
        if (url == null) {
            return false;
        }

        if (url.startsWith("https://")) {
            return true;
        }

        for (String prefix : IMAGE_DATA_PREFIXES) {
            if (url.startsWith(prefix)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Validate an image reference against the runtime policy: supported scheme,
     * decodable base64 body and a byte budget. Performs no network access and
     * never echoes the payload in an error message.
     *
     * @param url      image URL
     * @param maxBytes max decoded bytes for a data URL (ignored for https)
     */
    public static void validateImageUrl(String url, long maxBytes) {
        if (!isSupportedImageUrl(url)) {
            throw new IllegalArgumentException("unsupported image url: only data:image/(png|jpeg|jpg|webp);base64 "
                    + "or https:// is accepted");
        }

        if (url.startsWith("https://")) {
            return;
        }

        int comma = url.indexOf(',');
        String payload = comma < 0 ? "" : url.substring(comma + 1);

        if (payload.isEmpty()) {
            throw new IllegalArgumentException("image data url payload is empty");
        }

        long decodedBytes = (long) payload.length() * 3 / 4;

        if (maxBytes > 0 && decodedBytes > maxBytes) {
            throw new IllegalArgumentException("image data url is too large: about " + decodedBytes
                    + " bytes, limit " + maxBytes);
        }

        try {
            Base64.getDecoder().decode(payload);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("image data url payload is not valid base64");
        }
    }
}
