package com.klsjnh.infrastructure.config;

/*                CoreAutoConfiguration011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.25
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.25  framework component registration moved here from the host
 *                  scan (consumers no longer scan com.klsjnh)
 *
 */

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Core auto configuration: registers the framework's own component beans
 * (security core, platform support, persistence base, global web adapters)
 * through a scoped component scan of the core packages only.
 * <p>
 * Centers are no longer swept from here: every center ships its own
 * {@code *CenterAutoConfiguration011} (own scan, own {@code @MapperScan}) in its
 * own {@code com.klsjnh.enabled} package, pulled in only by its
 * {@code @EnableXxx011Center} — so the base does not have to know center
 * package names and a consumer can exclude a center by leaving its starter jar
 * out. The scan below keeps only the core-owned half of the {@code iam} tree
 * ({@code application.iam.user}, {@code infrastructure.iam.{user,auth}},
 * {@code web.iam.{controller,converter,vo.julyuser,vo.julyuseraudit}}); the
 * access center owns the sibling {@code iam.access} subpackages, so the two are
 * separable by scanning.
 * </p>
 */

@AutoConfiguration
@ComponentScan(basePackages = { "com.klsjnh.common", "com.klsjnh.application.iam.user",
        "com.klsjnh.application.platform011", "com.klsjnh.infrastructure.config", "com.klsjnh.infrastructure.crypto",
        "com.klsjnh.infrastructure.iam.auth", "com.klsjnh.infrastructure.iam.user",
        "com.klsjnh.infrastructure.persistence", "com.klsjnh.infrastructure.platform011", "com.klsjnh.web.config",
        "com.klsjnh.web.global", "com.klsjnh.web.iam.controller", "com.klsjnh.web.iam.converter",
        "com.klsjnh.web.iam.vo.julyuser", "com.klsjnh.web.iam.vo.julyuseraudit", "com.klsjnh.web.platform011.export",
        "com.klsjnh.web.util" })
public class CoreAutoConfiguration011 {
}
