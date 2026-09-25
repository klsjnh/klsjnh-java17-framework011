package com.klsjnh.infrastructure.iam.perm.seed;

/*                JulyPermCatalogSeedRunner class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.24
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.24  startup runner for permission catalog seed
 *
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Startup runner: seeds the P3 permission catalog. Failures are logged and do
 * not abort startup.
 */

@Component
@Order(JulyPermCatalogSeedRunner.SEED_ORDER)
public class JulyPermCatalogSeedRunner implements ApplicationRunner {

    /**
     * Run before the auth permission demo seed.
     */
    public static final int SEED_ORDER = 115;

    /**
     * Logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(JulyPermCatalogSeedRunner.class);

    /**
     * Seed.
     */
    private final JulyPermCatalogSeed011 seed;

    /**
     * Create the runner.
     *
     * @param seed seed
     */
    public JulyPermCatalogSeedRunner(JulyPermCatalogSeed011 seed) {
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
            logger.error("permission catalog seed aborted, startup continues", ex);
        }
    }
}
