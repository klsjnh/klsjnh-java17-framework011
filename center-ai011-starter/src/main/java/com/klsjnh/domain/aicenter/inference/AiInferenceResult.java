package com.klsjnh.domain.aicenter.inference;

/*                AiInferenceResult record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  ai chat result record
 *
 */

/**
 * Model reply: the assistant content plus finish reason and token usage. All
 * metrics are nullable when the provider omits them.
 *
 * @param content            assistant message text
 * @param finishReason       stop reason, nullable
 * @param promptTokens       prompt tokens, nullable
 * @param completionTokens   completion tokens, nullable
 * @param totalTokens        total tokens, nullable
 */

public record AiInferenceResult(String content, String finishReason, Integer promptTokens, Integer completionTokens,
        Integer totalTokens) {
}
