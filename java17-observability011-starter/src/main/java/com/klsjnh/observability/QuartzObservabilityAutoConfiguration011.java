package com.klsjnh.observability;

/*                QuartzObservabilityAutoConfiguration011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.25
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.25  quartz health auto configuration (class-level conditional
 *                  so a missing scheduler starter never loads Quartz types)
 *
 */

import com.klsjnh.infrastructure.system011.scheduler.SchedulerEngine;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

import org.quartz.Scheduler;

/**
 * Registers the Quartz health indicator only when the scheduler starter is on
 * the classpath. Declared through the auto-configuration imports file because
 * a plain {@code @Component} would fail class loading when Quartz is absent.
 */

@AutoConfiguration
@ConditionalOnClass(SchedulerEngine.class)
public class QuartzObservabilityAutoConfiguration011 {

    /**
     * Register the Quartz health indicator.
     *
     * @param engine    scheduler engine provider
     * @param scheduler quartz scheduler provider
     * @return health indicator
     */
    @Bean
    public HealthIndicator quartzSchedulerHealthIndicator(ObjectProvider<SchedulerEngine> engine,
            ObjectProvider<Scheduler> scheduler) {
        return new QuartzSchedulerHealthIndicator011(engine, scheduler);
    }
}
