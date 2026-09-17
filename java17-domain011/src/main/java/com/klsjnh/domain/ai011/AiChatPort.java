package com.klsjnh.domain.ai011;

/*                AiChatPort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  ai chat port
 *
 */

/**
 * Chat completion port: one resolved request in, one reply out. The OpenAI
 * compatible adapter implements it; other vendor protocols add adapters without
 * changing callers.
 */

public interface AiChatPort {

    /**
     * Send a chat completion request.
     *
     * @param command resolved chat request
     * @return model reply, never null
     */
    AiChatResult chat(AiChatCommand command);
}
