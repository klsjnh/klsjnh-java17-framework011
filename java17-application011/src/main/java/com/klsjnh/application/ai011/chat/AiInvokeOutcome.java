package com.klsjnh.application.ai011.chat;

/*                AiInvokeOutcome record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  ai invoke outcome record
 *
 */

/**
 * AI invocation outcome: which provider / api / model answered, the assistant
 * content and the token usage. The api key is never included.
 *
 * @param providerCode     resolved provider code
 * @param apiCode          resolved api code (not the secret)
 * @param model            resolved model name
 * @param content          assistant content
 * @param finishReason     stop reason, nullable
 * @param promptTokens     prompt tokens, nullable
 * @param completionTokens completion tokens, nullable
 * @param totalTokens      total tokens, nullable
 */

public record AiInvokeOutcome(String providerCode, String apiCode, String model, String content, String finishReason,
        Integer promptTokens, Integer completionTokens, Integer totalTokens) {
}
