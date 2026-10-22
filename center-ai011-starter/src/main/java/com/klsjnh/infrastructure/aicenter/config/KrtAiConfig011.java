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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
     * Provider file asset guards (krt.ai-center.file.*).
     */
    private FileConfig file = new FileConfig();

    /**
     * Provider rate / concurrency guards (krt.ai-center.rate-limit.*).
     */
    private RateLimitConfig rateLimit = new RateLimitConfig();

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
     * Provider file asset guards.
     */
    @Data
    public static class FileConfig {

        /**
         * Max bytes accepted for one provider file upload
         * (krt.ai-center.file.max-upload-bytes).
         */
        private long maxUploadBytes = 64L * 1024 * 1024;
    }

    /**
     * Provider rate / concurrency guards.
     */
    @Data
    public static class RateLimitConfig {

        /**
         * Enable the in-process rate limiter (krt.ai-center.rate-limit.enabled).
         */
        private boolean enabled = true;

        /**
         * Defaults for providers without an explicit entry.
         */
        private ProviderLimit defaults = new ProviderLimit();

        /**
         * Per-provider overrides (provider code -> limit).
         */
        private Map<String, ProviderLimit> providers = new LinkedHashMap<>();
    }

    /**
     * One provider's rate / concurrency limit. Zero disables that dimension.
     */
    @Data
    public static class ProviderLimit {

        /**
         * Minimum interval between two request starts in milliseconds.
         */
        private long minIntervalMs = 0;

        /**
         * Max in-flight requests; 0 = unlimited.
         */
        private int maxConcurrent = 0;

        /**
         * Max queue wait in milliseconds; 0 = fail immediately when limited.
         */
        private long maxWaitMs = 10000;

        /**
         * Limit scope: provider / api / model.
         */
        private String scope = "provider";
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
