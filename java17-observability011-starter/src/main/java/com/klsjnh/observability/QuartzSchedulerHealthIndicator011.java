package com.klsjnh.observability;

/*                QuartzSchedulerHealthIndicator011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.25
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.25  quartz scheduler health indicator
 *
 */

import com.klsjnh.infrastructure.system011.scheduler.SchedulerEngine;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import org.quartz.Scheduler;

/**
 * Health indicator for the Quartz scheduler engine. Only registered when the
 * scheduler starter is on the classpath (see
 * {@link QuartzObservabilityAutoConfiguration011}); reports whether the
 * underlying Quartz scheduler is started and how many jobs are scheduled.
 */

public class QuartzSchedulerHealthIndicator011 implements HealthIndicator {

    /**
     * Scheduler engine, present when the scheduler starter is included.
     */
    private final ObjectProvider<SchedulerEngine> engine;

    /**
     * Quartz scheduler behind the engine.
     */
    private final ObjectProvider<Scheduler> scheduler;

    /**
     * Create the indicator.
     *
     * @param engine    scheduler engine provider
     * @param scheduler quartz scheduler provider
     */
    public QuartzSchedulerHealthIndicator011(ObjectProvider<SchedulerEngine> engine,
            ObjectProvider<Scheduler> scheduler) {
        this.engine = engine;
        this.scheduler = scheduler;
    }

    /** {@inheritDoc} */
    @Override
    public Health health() {
        if (engine.getIfAvailable() == null) {
            return Health.up().withDetail("engine", "not registered").build();
        }

        try {
            Scheduler quartz = scheduler.getIfAvailable();
            boolean started = quartz != null && quartz.isStarted();
            int jobCount = quartz == null ? 0 : quartz.getJobKeys(null).size();

            return Health.up()
                    .withDetail("engine", "registered")
                    .withDetail("started", started)
                    .withDetail("jobCount", jobCount)
                    .build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
