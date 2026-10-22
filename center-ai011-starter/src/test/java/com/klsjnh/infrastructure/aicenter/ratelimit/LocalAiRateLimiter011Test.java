package com.klsjnh.infrastructure.aicenter.ratelimit;

/*                LocalAiRateLimiter011Test class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.22  interval / concurrency limiter tests
 *
 */

import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.application.aicenter.ratelimit.AiRateLimiterPort;

import com.klsjnh.infrastructure.aicenter.config.KrtAiConfig011;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link LocalAiRateLimiter011}.
 */

class LocalAiRateLimiter011Test {

    @Test
    /**
     * Waits for the configured minimum interval.
     */
    void enforcesMinimumInterval() {
        KrtAiConfig011 config = new KrtAiConfig011();
        KrtAiConfig011.ProviderLimit limit = new KrtAiConfig011.ProviderLimit();
        limit.setMinIntervalMs(200);
        limit.setMaxWaitMs(1000);
        config.getRateLimit().getProviders().put("stepfun", limit);
        LocalAiRateLimiter011 limiter = new LocalAiRateLimiter011(config);

        try (AiRateLimiterPort.Permit first = limiter.acquire("stepfun", "default", "m")) {
            long start = System.nanoTime();
            try (AiRateLimiterPort.Permit second = limiter.acquire("stepfun", "default", "m")) {
                long elapsedMs = (System.nanoTime() - start) / 1_000_000;
                assertTrue(elapsedMs >= 120, "expected a wait, got " + elapsedMs + "ms");
            }
        }
    }

    @Test
    /**
     * Returns 429 when the concurrency slot is busy.
     */
    void rejectsWhenConcurrencyExceeded() {
        KrtAiConfig011 config = new KrtAiConfig011();
        KrtAiConfig011.ProviderLimit limit = new KrtAiConfig011.ProviderLimit();
        limit.setMaxConcurrent(1);
        limit.setMaxWaitMs(20);
        config.getRateLimit().getProviders().put("stepfun", limit);
        LocalAiRateLimiter011 limiter = new LocalAiRateLimiter011(config);

        try (AiRateLimiterPort.Permit first = limiter.acquire("stepfun", "default", "m")) {
            BusinessException ex = assertThrows(BusinessException.class,
                    () -> limiter.acquire("stepfun", "default", "m"));
            assertEquals(429, ex.getCode());
        }

        try (AiRateLimiterPort.Permit again = limiter.acquire("stepfun", "default", "m")) {
            assertTrue(again != null);
        }
    }

    @Test
    /**
     * Does nothing when no limit is configured.
     */
    void noLimitsIsNoop() {
        LocalAiRateLimiter011 limiter = new LocalAiRateLimiter011(new KrtAiConfig011());

        try (AiRateLimiterPort.Permit permit = limiter.acquire("stepfun", "default", "m")) {
            assertTrue(permit != null);
        }
    }
}
