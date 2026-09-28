package com.klsjnh.enabled;

/*                MessagePermCatalogSeedConfig011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.28
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.28  register the message perm catalog seed only when the
 *                  optional access center (July perm repositories) is present
 *
 */

import com.klsjnh.domain.iam.perm.JulyPermActionRepository;
import com.klsjnh.domain.iam.perm.JulyPermObjectRepository;

import com.klsjnh.infrastructure.messagecenter.seed.MessagePermCatalogSeed011;
import com.klsjnh.infrastructure.messagecenter.seed.MessagePermCatalogSeedRunner;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers the message center permission catalog seed (and its startup runner)
 * as beans, but only when the <b>optional</b> access center types are on the
 * classpath. Pulled in by {@link EnableMessage011Center} — never listed in
 * {@code AutoConfiguration.imports}, so a jar alone stays off.
 * <p>
 * The condition is a class <i>name</i> on the configuration class, so the class
 * is skipped (and its {@code @Bean} signatures never resolved) when access is
 * absent: a message-only consumer no longer needs
 * {@code center-access011-starter} on the classpath. The seed and runner are
 * beans here rather than {@code @Component}s because a
 * {@code @ConditionalOnBean} on a component-scanned class is order-sensitive.
 * </p>
 */

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = "com.klsjnh.domain.iam.perm.JulyPermObjectRepository")
public class MessagePermCatalogSeedConfig011 {

    /**
     * Message center permission catalog seed.
     *
     * @param objectRepository July permission object catalog (access center)
     * @param actionRepository July permission action catalog (access center)
     * @return seed
     */
    @Bean
    public MessagePermCatalogSeed011 messagePermCatalogSeed011(JulyPermObjectRepository objectRepository,
            JulyPermActionRepository actionRepository) {
        return new MessagePermCatalogSeed011(objectRepository, actionRepository);
    }

    /**
     * Startup runner that seeds the catalog.
     *
     * @param seed message perm catalog seed
     * @return seed runner
     */
    @Bean
    public MessagePermCatalogSeedRunner messagePermCatalogSeedRunner(MessagePermCatalogSeed011 seed) {
        return new MessagePermCatalogSeedRunner(seed);
    }
}
