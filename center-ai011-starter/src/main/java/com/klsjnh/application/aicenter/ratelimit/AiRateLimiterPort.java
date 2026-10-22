package com.klsjnh.application.aicenter.ratelimit;

/*                AiRateLimiterPort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.22  provider rate / concurrency guard SPI
 *
 */

/**
 * Provider rate / concurrency guard. The default implementation is in-process;
 * a multi-instance deployment can provide a distributed implementation (Redis,
 * gateway ...) by declaring another bean.
 */

public interface AiRateLimiterPort {

    /**
     * Acquire one permit for a provider call. The caller must close the permit
     * when the call finishes; a streaming call closes it when the stream closes.
     *
     * @param providerCode provider code
     * @param apiCode      api key code, nullable
     * @param model        model name, nullable
     * @return permit, never null
     */
    Permit acquire(String providerCode, String apiCode, String model);

    /**
     * One held permit.
     */
    interface Permit extends AutoCloseable {

        /**
         * Release the permit (idempotent).
         */
        @Override
        void close();
    }

    /**
     * A no-op permit for calls that are not limited.
     *
     * @return permit
     */
    static Permit noop() {
        return () -> {
        };
    }
}
