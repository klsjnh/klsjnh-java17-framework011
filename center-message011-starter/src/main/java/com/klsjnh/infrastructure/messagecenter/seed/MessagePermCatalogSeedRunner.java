package com.klsjnh.infrastructure.messagecenter.seed;

/*                MessagePermCatalogSeedRunner class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  startup runner for message center permission catalog seed
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
 * Startup runner: seeds the message center permission catalog when both the
 * access center and the message center starter are present.
 */

@Component
@Order(MessagePermCatalogSeedRunner.SEED_ORDER)
@ConditionalOnBean(MessagePermCatalogSeed011.class)
public class MessagePermCatalogSeedRunner implements ApplicationRunner {

    public static final int SEED_ORDER = 118;

    private static final Logger logger = LoggerFactory.getLogger(MessagePermCatalogSeedRunner.class);

    private final MessagePermCatalogSeed011 seed;

    /**
     * Create the runner.
     *
     * @param seed seed
     */
    public MessagePermCatalogSeedRunner(MessagePermCatalogSeed011 seed) {
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
            logger.error("message center permission catalog seed aborted, startup continues", ex);
        }
    }
}
