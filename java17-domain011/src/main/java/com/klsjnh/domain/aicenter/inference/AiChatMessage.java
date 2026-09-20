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
 *
 */

/**
 * One chat message (role + content) sent to a model.
 *
 * @param role    one of system / user / assistant
 * @param content message text
 */

public record AiChatMessage(String role, String content) {

    /** System role. */
    public static final String ROLE_SYSTEM = "system";

    /** User role. */
    public static final String ROLE_USER = "user";

    /** Assistant role. */
    public static final String ROLE_ASSISTANT = "assistant";

    /**
     * Normalize and validate.
     */
    public AiChatMessage {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("message role is required");
        }

        if (content == null) {
            throw new IllegalArgumentException("message content is required");
        }
    }
}
