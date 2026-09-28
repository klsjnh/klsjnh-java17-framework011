package com.klsjnh.infrastructure.config;

/*                MessageCenterAutoConfiguration011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.28
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.28  message center auto configuration (own scan + enable switch)
 *
 */

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

import org.mybatis.spring.annotation.MapperScan;

/**
 * Message center auto configuration: registers the message center component
 * beans (outbound / inbound channels, templates, registries and their web
 * adapters) from the center's own jar. Disable with
 * {@code krt.center.message.enabled=false}.
 */

@AutoConfiguration
@ComponentScan(basePackages = { "com.klsjnh.application.messagecenter", "com.klsjnh.web.messagecenter",
        "com.klsjnh.infrastructure.messagecenter" })
@MapperScan("com.klsjnh.infrastructure.messagecenter")
public class MessageCenterAutoConfiguration011 {
}
