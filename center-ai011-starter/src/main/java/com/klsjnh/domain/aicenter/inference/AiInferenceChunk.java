package com.klsjnh.domain.aicenter.inference;

/*                AiInferenceChunk record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  ai inference stream chunk
 *
 */

/**
 * One streamed inference fragment: the delta content plus the finish reason
 * when the stream ends (nullable).
 *
 * @param content      incremental text, nullable on the final frame
 * @param finishReason stop reason, nullable until the last frame
 */

public record AiInferenceChunk(String content, String finishReason) {
}
