package com.klsjnh.enabled;

/*                EnableStorage011Center annotation
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.28
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.28  enable annotation for the storage center
 *
 */

import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enable the storage center on the annotated configuration class: imports
 * {@link StorageCenterAutoConfiguration011}, which scans the center's own
 * packages and registers its mappers, plus
 * {@link StoragePermCatalogSeedConfig011}, which wires the permission catalog
 * seed only when the optional access center is present.
 * <p>
 * Centers are off unless enabled: nothing is auto-configured from a center jar
 * alone, so a consumer only gets the centers it asks for.
 * </p>
 */

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({ StorageCenterAutoConfiguration011.class, StoragePermCatalogSeedConfig011.class })
public @interface EnableStorage011Center {
}
