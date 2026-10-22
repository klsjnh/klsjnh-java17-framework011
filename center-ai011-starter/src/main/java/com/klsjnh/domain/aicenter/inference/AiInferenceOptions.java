package com.klsjnh.domain.aicenter.inference;

/*                AiInferenceOptions record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.22  optional structured output / reasoning controls
 *
 */

/**
 * Optional, deliberately small inference controls: structured JSON output and a
 * controlled reasoning effort. There is no free-form parameter map — callers
 * cannot smuggle arbitrary vendor fields into the request.
 * <p>
 * {@code jsonObject} asks for {@code response_format={"type":"json_object"}};
 * it guarantees valid JSON, not the business schema. {@code reasoningEffort} is
 * a vendor extension and is only sent when the provider is allow-listed.
 * </p>
 *
 * @param jsonObject      true to request a JSON object reply
 * @param reasoningEffort optional reasoning effort, null for "do not send"
 */

public record AiInferenceOptions(boolean jsonObject, AiReasoningEffort reasoningEffort) {

    /**
     * Default options: no structured output, no reasoning field.
     *
     * @return default options
     */
    public static AiInferenceOptions none() {
        return new AiInferenceOptions(false, null);
    }
}
