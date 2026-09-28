package com.klsjnh.infrastructure.config;

/*                MessageCenterAutoConfigurationTest011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.28
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.28  assembly test: enable annotation wires the center auto configuration
 *
 */

import com.klsjnh.enabled.EnableMessage011Center;
import com.klsjnh.enabled.MessageCenterAutoConfiguration011;
import com.klsjnh.enabled.MessagePermCatalogSeedConfig011;

import org.springframework.context.annotation.Import;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Assembly contract for the per-center enable annotation: applying
 * {@code @EnableXxx011Center} is what pulls a center in — the annotation must
 * import the center's auto configuration (plus the access-gated permission seed
 * config), and the center must not be listed in
 * {@code AutoConfiguration.imports} (nothing is auto-configured from the jar
 * alone). The enabled path itself is covered by the app's startup smoke.
 */

class MessageCenterAutoConfigurationTest011 {

    /**
     * The enable annotation imports the center auto configuration and the
     * access-gated permission seed config.
     */
    @Test
    void annotationImportsTheCenterAutoConfiguration() {
        Import imported = EnableMessage011Center.class.getAnnotation(Import.class);

        Assertions.assertNotNull(imported, "@EnableMessage011Center must carry @Import");
        Assertions.assertArrayEquals(
                new Class<?>[] { MessageCenterAutoConfiguration011.class, MessagePermCatalogSeedConfig011.class },
                imported.value());
    }
}
