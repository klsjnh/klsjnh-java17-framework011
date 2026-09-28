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
import org.springframework.context.annotation.ComponentScan;

import org.mybatis.spring.annotation.MapperScan;

/**
 * MyBatis auto configuration of the framework: registers the framework-owned
 * mapper packages that do not belong to a single center — the shared
 * persistence base ({@code CommonMapper}) and the IAM mappers (the {@code iam}
 * package is shared by core, the security starter and the access center).
 * <p>
 * It also scans the dynamic datasource kernel
 * ({@code com.klsjnh.infrastructure.datasource.kernel}) that physically lives in
 * this starter, so {@code SqlRoutingPort} / {@code DynamicDataSourceRegistryPort}
 * / the dialect and probe beans are injectable from the data starter alone —
 * without having to enable the datasource management center. The kernel is the
 * library half; the center only adds the management / SQL / sync surface.
 * </p>
 * <p>
 * Center mappers are registered by the centers' own auto configurations
 * ({@code @MapperScan} on {@code *CenterAutoConfiguration011}), so leaving a
 * center jar out also drops its mappers. Only the core-owned IAM mappers
 * ({@code infrastructure.iam.user}) stay here; the access center's are under
 * {@code infrastructure.iam.access} and are registered by its own
 * {@code @EnableAccess011Center}.
 * </p>
 */

@AutoConfiguration
@ComponentScan("com.klsjnh.infrastructure.datasource.kernel")
@MapperScan({ "com.klsjnh.infrastructure.iam.user", "com.klsjnh.infrastructure.persistence.mapper" })
public class DataMybatisAutoConfiguration011 {
}
