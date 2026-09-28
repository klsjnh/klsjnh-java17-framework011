package com.klsjnh.enabled;

/*                EnableAccess011Center annotation
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.28
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.28  enable annotation for the access center
 *
 */

import com.klsjnh.infrastructure.config.AccessCenterAutoConfiguration011;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enable the access center on the annotated configuration class: imports
 * {@link AccessCenterAutoConfiguration011}, which scans the center's own
 * packages and registers its mappers.
 * <p>
 * With this center enabled the real {@code AuthorizationPort} exists and the
 * permissive debug fallback ({@code krt.security.permissive=true}) is no longer
 * needed. Centers are off unless enabled: nothing is auto-configured from a
 * center jar alone.
 * </p>
 */

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(AccessCenterAutoConfiguration011.class)
public @interface EnableAccess011Center {
}
