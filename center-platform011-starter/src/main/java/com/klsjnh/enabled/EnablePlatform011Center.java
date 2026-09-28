package com.klsjnh.enabled;

/*                EnablePlatform011Center annotation
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.28
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.28  enable annotation for the platform center
 *
 */

import com.klsjnh.infrastructure.config.PlatformCenterAutoConfiguration011;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enable the platform center on the annotated configuration class: imports
 * {@link PlatformCenterAutoConfiguration011}, which scans the center's own
 * packages and registers its mappers.
 * <p>
 * Centers are off unless enabled: nothing is auto-configured from a center jar
 * alone, so a consumer only gets the centers it asks for.
 * </p>
 */

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(PlatformCenterAutoConfiguration011.class)
public @interface EnablePlatform011Center {
}
