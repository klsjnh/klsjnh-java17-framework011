package com.klsjnh.enabled;

/*                PlatformCenterAutoConfiguration011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.28
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.28  platform center auto configuration (own scan) — lives off the
 *                  core scan, pulled in by @EnablePlatform011Center
 *
 */

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

import org.mybatis.spring.annotation.MapperScan;

/**
 * Platform center auto configuration: registers the platform center component
 * beans (config / menu / organization / dictionary management and export
 * providers plus their web adapters) from the center's own jar. Lives in
 * {@code com.klsjnh.enabled} and is pulled in by {@link EnablePlatform011Center}
 * only — never scanned by core, so the jar alone stays off.
 */

@AutoConfiguration
@ComponentScan(basePackages = { "com.klsjnh.application.system011", "com.klsjnh.web.system011",
        "com.klsjnh.infrastructure.system011" })
@MapperScan("com.klsjnh.infrastructure.system011")
public class PlatformCenterAutoConfiguration011 {
}
