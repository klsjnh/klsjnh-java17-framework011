package com.klsjnh.enabled;

/*                EnableMessage011Center annotation
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.28
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.28  enable annotation for the message center
 *
 */

import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enable the message center on the annotated configuration class: imports
 * {@link MessageCenterAutoConfiguration011}, which scans the center's own
 * packages and registers its mappers, plus
 * {@link MessagePermCatalogSeedConfig011}, which wires the permission catalog
 * seed only when the optional access center is present.
 * <p>
 * Centers are off unless enabled: nothing is auto-configured from a center jar
 * alone, so a consumer only gets the centers it asks for.
 * </p>
 */

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({ MessageCenterAutoConfiguration011.class, MessagePermCatalogSeedConfig011.class })
public @interface EnableMessage011Center {
}
