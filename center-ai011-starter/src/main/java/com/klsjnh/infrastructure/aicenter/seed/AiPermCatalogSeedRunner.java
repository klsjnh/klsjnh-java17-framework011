package com.klsjnh.infrastructure.aicenter.seed;

/*                AiPermCatalogSeedRunner class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  startup runner for ai center permission catalog seed
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
 * Startup runner: seeds the AI center permission catalog when both the access
 * center and the ai center starter are present.
 */

@Component
@Order(AiPermCatalogSeedRunner.SEED_ORDER)
@ConditionalOnBean(AiPermCatalogSeed011.class)
public class AiPermCatalogSeedRunner implements ApplicationRunner {

    public static final int SEED_ORDER = 117;

    private static final Logger logger = LoggerFactory.getLogger(AiPermCatalogSeedRunner.class);

    private final AiPermCatalogSeed011 seed;

    /**
     * Create the runner.
     *
     * @param seed seed
     */
    public AiPermCatalogSeedRunner(AiPermCatalogSeed011 seed) {
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
            logger.error("ai center permission catalog seed aborted, startup continues", ex);
        }
    }
}
