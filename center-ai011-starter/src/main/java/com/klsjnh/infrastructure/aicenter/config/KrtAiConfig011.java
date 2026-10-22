package com.klsjnh.infrastructure.aicenter.config;

/*                KrtAiConfig011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  ai center config (bound to krt.ai-center)
 *      2026.10.22  flatten chat-timeout-seconds (krt.ai-center.chat-timeout-seconds);
 *                  add inference guards (image data url budget, reasoning allow-list)
 *
 */

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * AI-center framework config bound to the {@code krt.ai-center.*} keys:
 * inference timeout, multimodal input guards and generated media key prefix.
 * Lives in the AI starter and is scanned by
 * {@code AiCenterAutoConfiguration011} through
 * {@code com.klsjnh.infrastructure.aicenter} — never through the core scan.
 * <p>
 * The timeout key is flat on purpose: {@code krt.ai-center.chat-timeout-seconds}
 * (a nested {@code ai-center} field silently bound
 * {@code krt.ai-center.ai-center.chat-timeout-seconds} before 2026.10.22).
 * </p>
 */

@Data
@Component
@ConfigurationProperties(prefix = "krt.ai-center")
public class KrtAiConfig011 {

    /**
     * Chat request timeout seconds (krt.ai-center.chat-timeout-seconds).
     */
    private long chatTimeoutSeconds = 60;

    /**
     * Multimodal inference guards (krt.ai-center.inference.*).
     */
    private InferenceConfig inference = new InferenceConfig();

    /**
     * Generated media settings (krt.ai-center.media.*).
     */
    private MediaConfig media = new MediaConfig();

    /**
     * Multimodal input guards.
     */
    @Data
    public static class InferenceConfig {

        /**
         * Max decoded bytes of one {@code data:image/...;base64,} part
         * (krt.ai-center.inference.max-image-data-url-bytes). https URLs are not
         * downloaded and therefore not limited here.
         */
        private long maxImageDataUrlBytes = 10L * 1024 * 1024;

        /**
         * Provider codes allowed to receive {@code reasoning_effort}
         * (krt.ai-center.inference.reasoning-effort-providers). Empty means the
         * vendor extension is never sent — requesting it fails fast.
         */
        private List<String> reasoningEffortProviders = new ArrayList<>();
    }

    /**
     * Generated media settings.
     */
    @Data
    public static class MediaConfig {

        /**
         * Key prefix for generated media (krt.ai-center.media.key-prefix).
         */
        private String keyPrefix = "ai/";
    }
}
