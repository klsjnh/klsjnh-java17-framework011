package com.klsjnh.enabled;

/*                AccessCenterAutoConfiguration011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.28
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.28  access center auto configuration (own scan) — lives off the
 *                  core scan, pulled in by @EnableAccess011Center
 *
 */

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

import org.mybatis.spring.annotation.MapperScan;

/**
 * Access center auto configuration: registers the access center component beans
 * (user / role / menu / organization / permission use cases and their web
 * adapters) from the center's own jar, and its mappers. Lives in
 * {@code com.klsjnh.enabled} and is pulled in by {@link EnableAccess011Center}
 * only — never scanned by core, so the jar alone stays off.
 * <p>
 * The center owns the {@code iam.access} subpackages (domain stays in
 * {@code com.klsjnh.domain.iam.*}): core keeps only the user / auth half of the
 * {@code iam} tree, so the two are separable by scanning.
 * </p>
 */

@AutoConfiguration
@ComponentScan(basePackages = { "com.klsjnh.application.iam.access", "com.klsjnh.web.iam.access",
        "com.klsjnh.infrastructure.iam.access" })
@MapperScan("com.klsjnh.infrastructure.iam.access")
public class AccessCenterAutoConfiguration011 {
}
