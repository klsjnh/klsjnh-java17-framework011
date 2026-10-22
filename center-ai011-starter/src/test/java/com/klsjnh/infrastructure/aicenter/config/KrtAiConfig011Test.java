package com.klsjnh.infrastructure.aicenter.config;

/*                KrtAiConfig011Test class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.22  config binding tests (flat timeout + inference guards)
 *
 */

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

/**
 * Unit tests for {@link KrtAiConfig011}: the timeout key must bind flat
 * ({@code krt.ai-center.chat-timeout-seconds}), and the multimodal guards must
 * bind under {@code krt.ai-center.inference.*}.
 */

class KrtAiConfig011Test {

    /**
     * Context runner with only the config bound.
     */
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class);

    @Test
    void bindsFlatTimeout() {
        runner.withPropertyValues("krt.ai-center.chat-timeout-seconds=17")
                .run(context -> assertEquals(17, context.getBean(KrtAiConfig011.class).getChatTimeoutSeconds()));
    }

    @Test
    void defaultsTimeoutToSixty() {
        runner.run(context -> assertEquals(60, context.getBean(KrtAiConfig011.class).getChatTimeoutSeconds()));
    }

    @Test
    /**
     * Ignores the legacy nested timeout path.
     */
    void ignoresLegacyNestedPath() {
        runner.withPropertyValues("krt.ai-center.ai-center.chat-timeout-seconds=99")
                .run(context -> assertEquals(60, context.getBean(KrtAiConfig011.class).getChatTimeoutSeconds()));
    }

    @Test
    /**
     * Binds the multimodal guard keys.
     */
    void bindsMultimodalGuards() {
        runner.withPropertyValues("krt.ai-center.inference.max-image-data-url-bytes=2048",
                "krt.ai-center.inference.reasoning-effort-providers[0]=sensenova")
                .run(context -> {
                    KrtAiConfig011 config = context.getBean(KrtAiConfig011.class);

                    assertEquals(2048, config.getInference().getMaxImageDataUrlBytes());
                    assertEquals(List.of("sensenova"), config.getInference().getReasoningEffortProviders());
                });
    }

    @Test
    /**
     * Defaults close the optional extension.
     */
    void defaultsGuardsCloseTheExtension() {
        runner.run(context -> {
            KrtAiConfig011 config = context.getBean(KrtAiConfig011.class);

            assertEquals(10L * 1024 * 1024, config.getInference().getMaxImageDataUrlBytes());
            assertEquals(List.of(), config.getInference().getReasoningEffortProviders());
        });
    }

    /**
     * Minimal configuration binding the properties class.
     */
    @Configuration
    @EnableConfigurationProperties(KrtAiConfig011.class)
    static class TestConfig {
    }
}
