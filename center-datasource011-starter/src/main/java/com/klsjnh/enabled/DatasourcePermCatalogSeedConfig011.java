package com.klsjnh.enabled;

/*                DatasourcePermCatalogSeedConfig011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.28
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.28  register the datasource perm catalog seed only when the
 *                  optional access center (July perm repositories) is present
 *
 */

import com.klsjnh.domain.iam.perm.JulyPermActionRepository;
import com.klsjnh.domain.iam.perm.JulyPermObjectRepository;

import com.klsjnh.infrastructure.datasource.seed.DatasourcePermCatalogSeed011;
import com.klsjnh.infrastructure.datasource.seed.DatasourcePermCatalogSeedRunner;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the datasource center permission catalog seed (and its startup
 * runner) as beans, but only when the <b>optional</b> access center types are
 * on the classpath. Pulled in by {@link EnableDatasource011Center} — never
 * listed in {@code AutoConfiguration.imports}, so a jar alone stays off.
 * <p>
 * The condition is a class <i>name</i> on the configuration class, so the
 * class is skipped (and its {@code @Bean} signatures never resolved) when access
 * is absent: a datasource-only consumer no longer needs
 * {@code center-access011-starter} on the classpath. The seed and runner are
 * beans here rather than {@code @Component}s because a
 * {@code @ConditionalOnBean} on a component-scanned class is order-sensitive.
 * </p>
 */

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = "com.klsjnh.domain.iam.perm.JulyPermObjectRepository")
public class DatasourcePermCatalogSeedConfig011 {

    /**
     * Datasource center permission catalog seed.
     *
     * @param objectRepository July permission object catalog (access center)
     * @param actionRepository July permission action catalog (access center)
     * @return seed
     */
    @Bean
    public DatasourcePermCatalogSeed011 datasourcePermCatalogSeed011(JulyPermObjectRepository objectRepository,
            JulyPermActionRepository actionRepository) {
        return new DatasourcePermCatalogSeed011(objectRepository, actionRepository);
    }

    /**
     * Startup runner that seeds the catalog.
     *
     * @param seed datasource perm catalog seed
     * @return seed runner
     */
    @Bean
    public DatasourcePermCatalogSeedRunner datasourcePermCatalogSeedRunner(DatasourcePermCatalogSeed011 seed) {
        return new DatasourcePermCatalogSeedRunner(seed);
    }
}
