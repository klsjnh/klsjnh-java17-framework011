package com.klsjnh.infrastructure.aicenter.config;

/*                KrtAiConfig011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.25
 *      @modifydate 2026.09.28
 *
 *===========================================
 *          modify history
 *
 *      2026.09.25  ai-center krt config (split from KrtConfig011, absorbs the
 *                  media key-prefix @Value)
 *      2026.09.28  moved from infrastructure.config to the AI-owned package so
 *                  the core scan of infrastructure.config cannot pick it up
 *
 */

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * AI-center framework config bound to the {@code krt.ai-center.*} keys:
 * inference timeout and generated media key prefix. Lives in the AI starter and
 * is scanned by {@code AiCenterAutoConfiguration011} through
 * {@code com.klsjnh.infrastructure.aicenter} — never through the core scan.
 */

@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "krt.ai-center")
public class KrtAiConfig011 {

    /**
     * AI settings.
     */
    private AiConfig aiCenter = new AiConfig();

    /**
     * Generated media settings (krt.ai-center.media.*).
     */
    private MediaConfig media = new MediaConfig();

    /**
     * AI settings.
     */
    @Data
    public static class AiConfig {

        /**
         * Chat request timeout seconds (krt.ai-center.chat-timeout-seconds).
         */
        private long chatTimeoutSeconds = 60;
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
