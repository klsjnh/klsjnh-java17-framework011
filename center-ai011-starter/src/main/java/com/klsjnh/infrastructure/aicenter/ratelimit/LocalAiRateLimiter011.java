package com.klsjnh.infrastructure.aicenter.ratelimit;

/*                LocalAiRateLimiter011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.22  in-process per-provider interval + concurrency limiter
 *
 */

import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.application.aicenter.ratelimit.AiRateLimiterPort;

import com.klsjnh.infrastructure.aicenter.config.KrtAiConfig011;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.LockSupport;

/**
 * In-process rate / concurrency guard: one bucket per scope key. It enforces the
 * minimum interval between request starts (token reservation) and a concurrency
 * semaphore. Waiting is bounded by {@code max-wait-ms}; exceeding it fails with
 * 429.
 * <p>
 * This is per JVM. Multi-instance deployments need a distributed
 * {@link AiRateLimiterPort} bean (Redis / gateway).
 * </p>
 */

public class LocalAiRateLimiter011 implements AiRateLimiterPort {

    /**
     * Config.
     */
    private final KrtAiConfig011 config;

    /**
     * Scope key to bucket.
     */
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    /**
     * Create the limiter.
     *
     * @param config ai-center config
     */
    public LocalAiRateLimiter011(KrtAiConfig011 config) {
        this.config = config;
    }

    /**
     * Acquire one permit.
     *
     * @param providerCode provider code
     * @param apiCode      api key code, nullable
     * @param model        model name, nullable
     * @return permit
     */
    @Override
    public Permit acquire(String providerCode, String apiCode, String model) {
        KrtAiConfig011.ProviderLimit limit = limit(providerCode);

        if (!config.getRateLimit().isEnabled() || limit == null
                || (limit.getMinIntervalMs() <= 0 && limit.getMaxConcurrent() <= 0)) {
            return AiRateLimiterPort.noop();
        }

        String key = key(providerCode, apiCode, model, limit.getScope());
        Bucket bucket = buckets.computeIfAbsent(key,
                ignored -> new Bucket(Math.max(0, limit.getMaxConcurrent())));
        long maxWaitNanos = limit.getMaxWaitMs() <= 0 ? 0 : TimeUnit.MILLISECONDS.toNanos(limit.getMaxWaitMs());
        long deadline = System.nanoTime() + maxWaitNanos;

        if (!bucket.acquireConcurrency(maxWaitNanos)) {
            throw BusinessException.tooManyRequests("ai rate limit: concurrency exceeded for " + providerCode);
        }

        try {
            bucket.acquireInterval(limit.getMinIntervalMs(), deadline, providerCode);
        } catch (RuntimeException ex) {
            bucket.releaseConcurrency();
            throw ex;
        }

        return new BucketPermit(bucket);
    }

    /**
     * Resolve the limit for a provider.
     *
     * @param providerCode provider code
     * @return limit, nullable when disabled
     */
    private KrtAiConfig011.ProviderLimit limit(String providerCode) {
        KrtAiConfig011.RateLimitConfig rateLimit = config.getRateLimit();

        if (rateLimit == null || providerCode == null) {
            return null;
        }

        KrtAiConfig011.ProviderLimit limit = rateLimit.getProviders() == null ? null
                : rateLimit.getProviders().get(providerCode);

        if (limit == null && rateLimit.getProviders() != null) {
            limit = rateLimit.getProviders().get(providerCode.toLowerCase());
        }

        return limit == null ? rateLimit.getDefaults() : limit;
    }

    /**
     * Build the bucket key for the configured scope.
     *
     * @param providerCode provider code
     * @param apiCode      api code
     * @param model        model
     * @param scope        provider / api / model
     * @return key
     */
    private String key(String providerCode, String apiCode, String model, String scope) {
        String provider = providerCode == null ? "" : providerCode;
        String api = apiCode == null || apiCode.isBlank() ? "-" : apiCode;
        String mode = scope == null ? "provider" : scope.toLowerCase();

        if ("api".equals(mode)) {
            return provider + "#" + api;
        }

        if ("model".equals(mode)) {
            return provider + "#" + api + "#" + (model == null ? "-" : model);
        }

        return provider;
    }

    /**
     * Per-key interval + concurrency state.
     */
    private static final class Bucket {

        /**
         * Concurrency semaphore, null when unlimited.
         */
        private final Semaphore semaphore;

        /**
         * Earliest next request start (nano time).
         */
        private long nextAllowed;

        /**
         * Create a bucket.
         *
         * @param maxConcurrent max concurrency, 0 for unlimited
         */
        private Bucket(int maxConcurrent) {
            this.semaphore = maxConcurrent > 0 ? new Semaphore(maxConcurrent, true) : null;
        }

        /**
         * Acquire the concurrency slot.
         *
         * @param maxWaitNanos max wait
         * @return true when acquired
         */
        private boolean acquireConcurrency(long maxWaitNanos) {
            if (semaphore == null) {
                return true;
            }

            try {
                return semaphore.tryAcquire(maxWaitNanos, TimeUnit.NANOSECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("ai rate limit wait interrupted");
            }
        }

        /**
         * Reserve the next interval slot and wait for it.
         *
         * @param minIntervalMs minimum interval
         * @param deadline      wait deadline
         * @param providerCode  provider for the error message
         */
        private synchronized void acquireInterval(long minIntervalMs, long deadline, String providerCode) {
            if (minIntervalMs <= 0) {
                return;
            }

            long now = System.nanoTime();
            long interval = TimeUnit.MILLISECONDS.toNanos(minIntervalMs);
            long slot = Math.max(now, nextAllowed);
            long wait = slot - now;

            if (wait > 0 && wait > Math.max(0, deadline - now)) {
                throw BusinessException.tooManyRequests("ai rate limit: interval wait exceeded for " + providerCode);
            }

            nextAllowed = slot + interval;

            if (wait > 0) {
                LockSupport.parkNanos(wait);
                if (Thread.interrupted()) {
                    throw new IllegalStateException("ai rate limit wait interrupted");
                }
            }
        }

        /**
         * Release the concurrency slot.
         */
        private void releaseConcurrency() {
            if (semaphore != null) {
                semaphore.release();
            }
        }
    }

    /**
     * Permit that releases the concurrency slot once.
     */
    private static final class BucketPermit implements Permit {

        /**
         * Bucket.
         */
        private final Bucket bucket;

        /**
         * Closed flag.
         */
        private final AtomicBoolean closed = new AtomicBoolean();

        /**
         * Create the permit.
         *
         * @param bucket bucket
         */
        private BucketPermit(Bucket bucket) {
            this.bucket = bucket;
        }

        /**
         * Release the slot (idempotent).
         */
        @Override
        public void close() {
            if (closed.compareAndSet(false, true)) {
                bucket.releaseConcurrency();
            }
        }
    }
}
