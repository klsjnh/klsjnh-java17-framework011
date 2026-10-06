package com.klsjnh.enabled;

/*                DatasourceCenterAutoConfiguration011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.28
 *      @modifydate 2026.10.05
 *
 *===========================================
 *          modify history
 *
 *      2026.09.28  datasource center auto configuration (own scan) — lives off
 *                  the core scan, pulled in by @EnableDatasource011Center
 *      2026.10.05  optional syncRule scheduler handler (conditional on the
 *                  scheduler starter)
 *
 */

import com.klsjnh.domain.datasource.sync.JulySyncRuleRepository;
import com.klsjnh.domain.datasource.sync.SyncEnginePort;

import com.klsjnh.infrastructure.datasource.sync.SyncRuleJobHandler;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import org.mybatis.spring.annotation.MapperScan;

/**
 * Datasource center auto configuration: registers the datasource center
 * component beans (registry, dialects, sync engine and their web adapters) from
 * the center's own jar. Lives in {@code com.klsjnh.enabled} and is pulled in by
 * {@link EnableDatasource011Center} only — never scanned by core, so the jar
 * alone stays off.
 */

@AutoConfiguration
@ComponentScan(basePackages = { "com.klsjnh.application.datasource", "com.klsjnh.web.datasource",
        "com.klsjnh.infrastructure.datasource" })
@MapperScan("com.klsjnh.infrastructure.datasource")
public class DatasourceCenterAutoConfiguration011 {

    /**
     * Optional scheduler integration: registers the {@code syncRule} job
     * handler only when the scheduler starter is on the classpath — datasource
     * center alone must not force a Quartz dependency.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(name = "com.klsjnh.domain.system011.scheduler.JobHandler")
    static class SyncSchedulerIntegration011 {

        /**
         * Register the syncRule job handler.
         *
         * @param syncEngine     sync engine
         * @param ruleRepository sync rule repository
         * @return job handler
         */
        @Bean
        SyncRuleJobHandler syncRuleJobHandler(SyncEnginePort syncEngine, JulySyncRuleRepository ruleRepository) {
            return new SyncRuleJobHandler(syncEngine, ruleRepository);
        }
    }
}
