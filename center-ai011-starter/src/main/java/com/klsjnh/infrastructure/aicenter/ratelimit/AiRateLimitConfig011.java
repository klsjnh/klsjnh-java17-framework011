package com.klsjnh.infrastructure.aicenter.ratelimit;

/*                AiRateLimitConfig011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.22  local rate limiter bean (replaceable by a distributed one)
 *
 */

import com.klsjnh.application.aicenter.ratelimit.AiRateLimiterPort;

import com.klsjnh.infrastructure.aicenter.config.KrtAiConfig011;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the in-process rate limiter unless the consumer provides its own
 * {@link AiRateLimiterPort} bean (e.g. Redis / gateway backed).
 */

@Configuration(proxyBeanMethods = false)
public class AiRateLimitConfig011 {

    /**
     * Local rate limiter.
     *
     * @param config ai-center config
     * @return limiter
     */
    @Bean
    @ConditionalOnMissingBean(AiRateLimiterPort.class)
    public AiRateLimiterPort aiRateLimiterPort(KrtAiConfig011 config) {
        return new LocalAiRateLimiter011(config);
    }
}
