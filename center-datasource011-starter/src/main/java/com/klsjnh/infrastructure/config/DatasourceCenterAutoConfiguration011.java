package com.klsjnh.infrastructure.config;

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
 *      2026.09.28  datasource center auto configuration (own scan + enable switch)
 *
 */

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

import org.mybatis.spring.annotation.MapperScan;

/**
 * Datasource center auto configuration: registers the datasource center
 * component beans (registry, dialects, sync engine and their web adapters) from
 * the center's own jar. Disable with {@code krt.center.datasource.enabled=false}.
 */

@AutoConfiguration
@ComponentScan(basePackages = { "com.klsjnh.application.datasource", "com.klsjnh.web.datasource",
        "com.klsjnh.infrastructure.datasource" })
@MapperScan("com.klsjnh.infrastructure.datasource")
public class DatasourceCenterAutoConfiguration011 {
}
