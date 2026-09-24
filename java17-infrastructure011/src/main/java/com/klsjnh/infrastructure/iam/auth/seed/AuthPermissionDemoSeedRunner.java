package com.klsjnh.infrastructure.iam.auth.seed;

/*                AuthPermissionDemoSeedRunner class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.24
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.24  startup runner for julyConfig permission demo seed
 *
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Startup runner: seeds the julyConfig permission demo (033 §021). Failures
 * are logged and do not abort startup.
 */

@Component
@Order(AuthPermissionDemoSeedRunner.SEED_ORDER)
public class AuthPermissionDemoSeedRunner implements ApplicationRunner {

    /**
     * Run after storage seed.
     */
    public static final int SEED_ORDER = 120;

    /**
     * Logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(AuthPermissionDemoSeedRunner.class);

    /**
     * Seed.
     */
    private final AuthPermissionDemoSeed011 seed;

    /**
     * Create the runner.
     *
     * @param seed seed
     */
    public AuthPermissionDemoSeedRunner(AuthPermissionDemoSeed011 seed) {
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
            logger.error("auth permission demo seed aborted, startup continues", ex);
        }
    }
}
