package com.config;

/*                ApplicationConfig class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  application config class
 *
 */

import org.springframework.context.annotation.Configuration;

import org.mybatis.spring.annotation.MapperScan;

/**
 * Application level configuration that registers the MyBatis mapper scan packages.
 */

@Configuration
@MapperScan({ "com.klsjnh.infrastructure.iam", "com.klsjnh.infrastructure.persistence.mapper", "com.klsjnh.infrastructure.system011",
    "com.klsjnh.demo11.infrastructure.persistence.mapper", "com.klsjnh.infrastructure.datasource",
    "com.klsjnh.infrastructure.aicenter", "com.klsjnh.infrastructure.storagecenter",
    "com.klsjnh.infrastructure.messagecenter" })
public class ApplicationConfig {

}
