package com.klsjnh.enabled;

/*                DatasourceCenterAutoConfiguration011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.28
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.28  datasource center auto configuration (own scan) — lives off
 *                  the core scan, pulled in by @EnableDatasource011Center
 *
 */

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

import org.mybatis.spring.annotation.MapperScan;

/**
 * Datasource center auto configuration: registers the datasource center
 * component beans (registry, dialects, sync engine and their web adapters) from
 * the center's own jar. Lives in {@code com.klsjnh.enabled} and is pulled in by
 * {@link EnableDatasource011Center} only — never scanned by core, so the jar
 * alone stays off.
 */

@AutoConfiguration
@ComponentScan(basePackages = { "com.klsjnh.application.datasource", "com.klsjnh.web.datasource",
        "com.klsjnh.infrastructure.datasource" })
@MapperScan("com.klsjnh.infrastructure.datasource")
public class DatasourceCenterAutoConfiguration011 {
}
