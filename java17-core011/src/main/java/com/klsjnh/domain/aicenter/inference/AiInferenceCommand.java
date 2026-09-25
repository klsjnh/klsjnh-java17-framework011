package com.klsjnh.domain.aicenter.inference;

/*                AiInferenceCommand record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  ai chat command record
 *
 */

import java.util.List;

/**
 * A resolved chat request: the provider endpoint, the api key, the model and
 * the messages. Built by the application layer after id/code resolution.
 *
 * @param baseUrl     OpenAI-compatible base url (e.g. https://.../v1)
 * @param apiKey      api key secret, never echoed back
 * @param model       model name
 * @param messages    chat messages, at least one
 * @param temperature sampling temperature, nullable
 * @param maxTokens   max output tokens, nullable
 */

public record AiInferenceCommand(String baseUrl, String apiKey, String model, List<AiChatMessage> messages,
        Double temperature, Integer maxTokens) {

    /**
     * Normalize and validate.
     */
    public AiInferenceCommand {
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalArgumentException("baseUrl is required");
        }

        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("model is required");
        }

        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("messages are required");
        }
    }
}
