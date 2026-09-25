package com.klsjnh.infrastructure.config;

/*                DataMybatisAutoConfiguration011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.25
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.25  framework mapper auto-scan moved here from the host main
 *                  class (consumers no longer list framework mapper packages)
 *
 */

import org.springframework.boot.autoconfigure.AutoConfiguration;

import org.mybatis.spring.annotation.MapperScan;

/**
 * MyBatis auto configuration of the framework: registers the framework mapper
 * packages so consumers only declare {@code @MapperScan} for their own
 * business mapper packages. Component-style beans still arrive through the
 * conventional host scan of {@code com.klsjnh}.
 */

@AutoConfiguration
@MapperScan({ "com.klsjnh.infrastructure.iam", "com.klsjnh.infrastructure.persistence.mapper", "com.klsjnh.infrastructure.system011",
        "com.klsjnh.infrastructure.datasource", "com.klsjnh.infrastructure.aicenter",
        "com.klsjnh.infrastructure.storagecenter", "com.klsjnh.infrastructure.messagecenter" })
public class DataMybatisAutoConfiguration011 {
}
