package com.klsjnh.enabled;

/*                StorageCenterAutoConfiguration011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.28
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.28  storage center auto configuration (own scan) — lives off the
 *                  core scan, pulled in by @EnableStorage011Center
 *
 */

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

import org.mybatis.spring.annotation.MapperScan;

/**
 * Storage center auto configuration: registers the storage center component
 * beans (object storage ports, provider factories, management use cases and
 * their web adapters) from the center's own jar. Lives in
 * {@code com.klsjnh.enabled} and is pulled in by {@link EnableStorage011Center}
 * only — never scanned by core, so the jar alone stays off.
 */

@AutoConfiguration
@ComponentScan(basePackages = { "com.klsjnh.application.storagecenter", "com.klsjnh.web.storagecenter",
        "com.klsjnh.infrastructure.storagecenter" })
@MapperScan("com.klsjnh.infrastructure.storagecenter")
public class StorageCenterAutoConfiguration011 {
}
