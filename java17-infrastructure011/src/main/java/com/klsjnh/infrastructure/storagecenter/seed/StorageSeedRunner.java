package com.klsjnh.infrastructure.storagecenter.seed;

/*                StorageSeedRunner class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  storage seed runner class
 *
 */

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Startup runner: seeds the default storage instance from the yaml bootstrap
 * config. Never aborts startup — a failure is logged and the service still
 * boots.
 */

@Slf4j
@Component
@Order(StorageSeedRunner.SEED_ORDER)
public class StorageSeedRunner implements ApplicationRunner {

    /**
     * Run order: after the datasource seed.
     */
    public static final int SEED_ORDER = 110;

    /**
     * Storage seed.
     */
    private final StorageSeed011 seed;

    /**
     * Create the runner.
     *
     * @param seed storage seed
     */
    public StorageSeedRunner(StorageSeed011 seed) {
        this.seed = seed;
    }

    /**
     * Seed the default storage instance.
     *
     * @param args application arguments
     */
    @Override
    public void run(ApplicationArguments args) {
        try {
            seed.seed();
        } catch (Exception ex) {
            log.error("storage seed aborted, startup continues", ex);
        }
    }
}
