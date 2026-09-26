package com.klsjnh.infrastructure.system011.scheduler.seed;

/*                JulySchedulerPermCatalogSeedRunner class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  startup runner for julyScheduler permission catalog seed
 *
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Startup runner: seeds the julyScheduler permission catalog when both the
 * access center and the quartz scheduler starter are present. Failures are
 * logged and do not abort startup.
 */

@Component
@Order(JulySchedulerPermCatalogSeedRunner.SEED_ORDER)
@ConditionalOnBean(JulySchedulerPermCatalogSeed011.class)
public class JulySchedulerPermCatalogSeedRunner implements ApplicationRunner {

    /**
     * Run alongside the core julyConfig catalog seed (order 115), before the
     * auth permission demo seed.
     */
    public static final int SEED_ORDER = 116;

    /**
     * Logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(JulySchedulerPermCatalogSeedRunner.class);

    /**
     * Seed.
     */
    private final JulySchedulerPermCatalogSeed011 seed;

    /**
     * Create the runner.
     *
     * @param seed seed
     */
    public JulySchedulerPermCatalogSeedRunner(JulySchedulerPermCatalogSeed011 seed) {
        this.seed = seed;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run(ApplicationArguments args) {
        try {
            seed.seed();
        } catch (Exception ex) {
            logger.error("julyScheduler permission catalog seed aborted, startup continues", ex);
        }
    }
}
