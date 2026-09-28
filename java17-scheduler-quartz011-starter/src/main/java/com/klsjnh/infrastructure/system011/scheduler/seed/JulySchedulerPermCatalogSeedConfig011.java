package com.klsjnh.infrastructure.system011.scheduler.seed;

/*                JulySchedulerPermCatalogSeedConfig011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.28
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.28  register the scheduler perm catalog seed only when the
 *                  optional access center (July perm repositories) is present
 *
 */

import com.klsjnh.domain.iam.perm.JulyPermActionRepository;
import com.klsjnh.domain.iam.perm.JulyPermObjectRepository;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the julyScheduler permission catalog seed (and its startup runner)
 * as beans, but only when the <b>optional</b> access center types are on the
 * classpath.
 * <p>
 * The scheduler starter has no {@code @Enable} annotation of its own: its whole
 * stack lives under {@code com.klsjnh.{application,web,infrastructure}.system011}
 * and is scanned by {@code PlatformCenterAutoConfiguration011}. This config sits
 * in that scanned package for the same reason, so it is picked up exactly when
 * the scheduler stack is. The condition is a class <i>name</i> on the
 * configuration class, so the class is skipped (and its {@code @Bean} signatures
 * never resolved) when access is absent. The seed and runner are beans here
 * rather than {@code @Component}s because a {@code @ConditionalOnBean} on a
 * component-scanned class is order-sensitive.
 * </p>
 */

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = "com.klsjnh.domain.iam.perm.JulyPermObjectRepository")
public class JulySchedulerPermCatalogSeedConfig011 {

    /**
     * julyScheduler permission catalog seed.
     *
     * @param objectRepository July permission object catalog (access center)
     * @param actionRepository July permission action catalog (access center)
     * @return seed
     */
    @Bean
    public JulySchedulerPermCatalogSeed011 julySchedulerPermCatalogSeed011(
            JulyPermObjectRepository objectRepository, JulyPermActionRepository actionRepository) {
        return new JulySchedulerPermCatalogSeed011(objectRepository, actionRepository);
    }

    /**
     * Startup runner that seeds the catalog.
     *
     * @param seed julyScheduler perm catalog seed
     * @return seed runner
     */
    @Bean
    public JulySchedulerPermCatalogSeedRunner julySchedulerPermCatalogSeedRunner(
            JulySchedulerPermCatalogSeed011 seed) {
        return new JulySchedulerPermCatalogSeedRunner(seed);
    }
}
