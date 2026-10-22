package com.klsjnh.domain.aicenter.inference;

/*                AiChatMessage record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  ai chat message record
 *      2026.10.22  multimodal content parts (text / image_url), text-only kept
 *
 */

import java.util.List;

/**
 * One chat message sent to a model: either plain text ({@code content}) or a
 * multimodal block list ({@code contentParts}). Exactly one of the two is
 * carried, so old text-only callers stay source and wire compatible.
 *
 * @param role         one of system / user / assistant
 * @param content      message text, null for a multimodal message
 * @param contentParts typed parts (text / image_url), null for a text message
 */

public record AiChatMessage(String role, String content, List<AiChatContentPart> contentParts) {

    /** System role. */
    public static final String ROLE_SYSTEM = "system";

    /** User role. */
    public static final String ROLE_USER = "user";

    /** Assistant role. */
    public static final String ROLE_ASSISTANT = "assistant";

    /**
     * Max content parts in one multimodal message.
     */
    private static final int MAX_CONTENT_PARTS = 20;

    /**
     * Text-only message (backward compatible constructor).
     *
     * @param role    role
     * @param content message text
     */
    public AiChatMessage(String role, String content) {
        this(role, content, null);
    }

    /**
     * Normalize and validate: role is required, text and parts are mutually
     * exclusive, a multimodal message carries 1..20 parts and at least one text
     * part (an image alone has no instruction).
     */
    public AiChatMessage {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("message role is required");
        }

        contentParts = contentParts == null ? null : List.copyOf(contentParts);

        boolean hasParts = contentParts != null && !contentParts.isEmpty();

        if (content != null && hasParts) {
            throw new IllegalArgumentException("message must not carry both content and contentParts");
        }

        if (content == null && !hasParts) {
            throw new IllegalArgumentException("message content is required");
        }

        if (hasParts) {
            if (contentParts.size() > MAX_CONTENT_PARTS) {
                throw new IllegalArgumentException("too many content parts: " + contentParts.size() + ", limit "
                        + MAX_CONTENT_PARTS);
            }
            boolean hasText = false;
            for (AiChatContentPart part : contentParts) {
                if (part == null) {
                    throw new IllegalArgumentException("content part must not be null");
                }
                hasText = hasText || AiChatContentPart.TYPE_TEXT.equals(part.type());
            }
            if (!hasText) {
                throw new IllegalArgumentException("multimodal message requires at least one text part");
            }
        }
    }
}

