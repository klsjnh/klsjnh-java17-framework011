package com.klsjnh.infrastructure.config;

/*                AccessCenterAutoConfiguration011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.28
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.28  access center auto configuration (own scan + enable switch)
 *
 */

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Access center auto configuration: registers the access center component beans
 * (user / role / menu / organization / permission use cases and their web
 * adapters) from the center's own jar, so the base does not have to know the
 * center's package names. Disable with {@code krt.center.access.enabled=false}.
 */

@AutoConfiguration
@ComponentScan(basePackages = { "com.klsjnh.application.iam", "com.klsjnh.web.iam", "com.klsjnh.infrastructure.iam" })
public class AccessCenterAutoConfiguration011 {
}
