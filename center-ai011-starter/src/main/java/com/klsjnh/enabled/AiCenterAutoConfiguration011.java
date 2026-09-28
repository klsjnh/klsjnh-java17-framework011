package com.klsjnh.enabled;

/*                AiCenterAutoConfiguration011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.28
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.28  ai center auto configuration (own scan) — lives off the core
 *                  scan, pulled in by @EnableAi011Center
 *
 */

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

import org.mybatis.spring.annotation.MapperScan;

/**
 * AI center auto configuration: registers the AI center component beans
 * (model provider, capability adapters, prompt use cases and their web
 * adapters) from the center's own jar. Lives in {@code com.klsjnh.enabled} and
 * is pulled in by {@link EnableAi011Center} only — never scanned by core, so
 * the jar alone stays off.
 */

@AutoConfiguration
@ComponentScan(basePackages = { "com.klsjnh.application.aicenter", "com.klsjnh.web.aicenter",
        "com.klsjnh.infrastructure.aicenter" })
@MapperScan("com.klsjnh.infrastructure.aicenter")
public class AiCenterAutoConfiguration011 {
}
