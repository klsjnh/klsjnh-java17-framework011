package com.klsjnh.infrastructure.system011.platform.seed;

/*                JulyPlatformPermCatalogSeedRunner class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  startup runner for platform permission catalog seed
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
 * Startup runner: seeds the julyConfig / julyDictionary permission catalog
 * when both the access center and the platform starter are present. Failures
 * are logged and do not abort startup.
 */

@Component
@Order(JulyPlatformPermCatalogSeedRunner.SEED_ORDER)
@ConditionalOnBean(JulyPlatformPermCatalogSeed011.class)
public class JulyPlatformPermCatalogSeedRunner implements ApplicationRunner {

    /**
     * Run alongside the IAM catalog seed (order 115), before the auth
     * permission demo seed.
     */
    public static final int SEED_ORDER = 115;

    /**
     * Logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(JulyPlatformPermCatalogSeedRunner.class);

    /**
     * Seed.
     */
    private final JulyPlatformPermCatalogSeed011 seed;

    /**
     * Create the runner.
     *
     * @param seed seed
     */
    public JulyPlatformPermCatalogSeedRunner(JulyPlatformPermCatalogSeed011 seed) {
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
            logger.error("platform permission catalog seed aborted, startup continues", ex);
        }
    }
}
