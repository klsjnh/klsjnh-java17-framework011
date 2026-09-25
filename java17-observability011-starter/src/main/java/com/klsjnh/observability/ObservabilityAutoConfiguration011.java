package com.klsjnh.observability;

/*                ObservabilityAutoConfiguration011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.25
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.25  observability component registration (traceId MDC filter,
 *                  framework health indicators) without a host package scan
 *
 */

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Registers the observability components (traceId MDC filter, dynamic
 * datasource and storage health indicators) through a scoped component scan;
 * the Quartz indicator stays in the class-conditional auto-configuration.
 */

@AutoConfiguration
@ComponentScan("com.klsjnh.observability")
public class ObservabilityAutoConfiguration011 {
}
