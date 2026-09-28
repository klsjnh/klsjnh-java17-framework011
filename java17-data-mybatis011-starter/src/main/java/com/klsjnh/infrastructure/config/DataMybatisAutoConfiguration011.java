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
 * MyBatis auto configuration of the framework: registers the framework-owned
 * mapper packages that do not belong to a single center — the shared
 * persistence base ({@code CommonMapper}) and the IAM mappers (the {@code iam}
 * package is shared by core, the security starter and the access center).
 * <p>
 * Center mappers are registered by the centers' own auto configurations
 * ({@code @MapperScan} on {@code *CenterAutoConfiguration011}), so leaving a
 * center jar out also drops its mappers.
 * </p>
 */

@AutoConfiguration
@MapperScan({ "com.klsjnh.infrastructure.iam", "com.klsjnh.infrastructure.persistence.mapper" })
public class DataMybatisAutoConfiguration011 {
}
