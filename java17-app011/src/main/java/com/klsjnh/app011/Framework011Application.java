package com.klsjnh.app011;

/*                Framework011Application class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  framework 011 application class
 *
 */

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.mybatis.spring.annotation.MapperScan;

/**
 * klsjnh Java17 framework boot entry, the only main of the whole project.
 * <p>
 * Framework beans register through auto-configuration (the host never scans
 * {@code com.klsjnh}); only the demo slice and the demo JobHandler declare
 * their own packages here. Business projects scan ONLY their own packages.
 * </p>
 */

@SpringBootApplication(scanBasePackages = { "com.klsjnh.demo11", "com.klsjnh.scheduler" })
@MapperScan("com.klsjnh.demo11.infrastructure.persistence.mapper")
public class Framework011Application {

    /**
     * Boot entry point.
     *
     * @param args command line args
     */
    public static void main(String[] args) {
        SpringApplication.run(Framework011Application.class, args);
    }
}
