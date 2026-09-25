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
 * Core auto configuration: registers the framework component beans (use
 * cases, management controllers, persistence implementations) through a
 * scoped component scan, so the host application never scans
 * {@code com.klsjnh}. Starter adapters living under the same package tree
 * are picked up by classpath presence; starter-specific wiring (framework
 * mapper scan, conditional health indicators) ships in the starters' own
 * auto-configurations.
 */

@AutoConfiguration
@ComponentScan(basePackages = { "com.klsjnh.application", "com.klsjnh.web", "com.klsjnh.infrastructure" })
public class CoreAutoConfiguration011 {
}
