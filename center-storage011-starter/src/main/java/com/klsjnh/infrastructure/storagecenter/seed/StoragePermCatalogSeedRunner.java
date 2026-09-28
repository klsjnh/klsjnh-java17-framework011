package com.klsjnh.infrastructure.storagecenter.seed;

/*                StoragePermCatalogSeedRunner class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  startup runner for storage center permission catalog seed
 *
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;

/**
 * Startup runner: seeds the storage center permission catalog when both the
 * access center and the storage center starter are present. Registered by
 * {@code StoragePermCatalogSeedConfig011} (never component-scanned) so it only
 * exists alongside the seed bean.
 */

@Order(StoragePermCatalogSeedRunner.SEED_ORDER)
public class StoragePermCatalogSeedRunner implements ApplicationRunner {

    public static final int SEED_ORDER = 119;

    private static final Logger logger = LoggerFactory.getLogger(StoragePermCatalogSeedRunner.class);

    private final StoragePermCatalogSeed011 seed;

    /**
     * Create the runner.
     *
     * @param seed seed
     */
    public StoragePermCatalogSeedRunner(StoragePermCatalogSeed011 seed) {
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
            logger.error("storage center permission catalog seed aborted, startup continues", ex);
        }
    }
}
