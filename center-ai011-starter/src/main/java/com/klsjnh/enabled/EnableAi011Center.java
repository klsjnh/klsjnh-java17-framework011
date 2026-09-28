package com.klsjnh.enabled;

/*                EnableAi011Center annotation
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.28
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.28  enable annotation for the ai center
 *
 */

import com.klsjnh.infrastructure.config.AiCenterAutoConfiguration011;

import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enable the AI center on the annotated configuration class: imports
 * {@link AiCenterAutoConfiguration011}, which scans the center's own packages
 * and registers its mappers.
 * <p>
 * The AI center <b>depends on</b> the storage center (prompt bodies are stored
 * through it), so this annotation also enables
 * {@link EnableStorage011Center} — enabling AI alone would leave its storage
 * dependency unsatisfied.
 * </p>
 * <p>
 * Centers are off unless enabled: nothing is auto-configured from a center jar
 * alone, so a consumer only gets the centers it asks for.
 * </p>
 */

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@EnableStorage011Center
@Import(AiCenterAutoConfiguration011.class)
public @interface EnableAi011Center {
}
