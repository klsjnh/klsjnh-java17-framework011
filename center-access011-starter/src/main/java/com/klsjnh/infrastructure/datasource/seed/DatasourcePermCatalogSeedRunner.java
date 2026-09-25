package com.klsjnh.infrastructure.datasource.seed;

/*                DatasourcePermCatalogSeedRunner class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  startup runner for datasource center permission catalog seed
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
 * Startup runner: seeds the datasource center permission catalog when both the
 * access center and the datasource center starter are present.
 */

@Component
@Order(DatasourcePermCatalogSeedRunner.SEED_ORDER)
@ConditionalOnBean(DatasourcePermCatalogSeed011.class)
public class DatasourcePermCatalogSeedRunner implements ApplicationRunner {

    public static final int SEED_ORDER = 120;

    private static final Logger logger = LoggerFactory.getLogger(DatasourcePermCatalogSeedRunner.class);

    private final DatasourcePermCatalogSeed011 seed;

    /**
     * Create the runner.
     *
     * @param seed seed
     */
    public DatasourcePermCatalogSeedRunner(DatasourcePermCatalogSeed011 seed) {
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
            logger.error("datasource center permission catalog seed aborted, startup continues", ex);
        }
    }
}