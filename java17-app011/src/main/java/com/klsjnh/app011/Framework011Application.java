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
 */

@SpringBootApplication(scanBasePackages = "com.klsjnh")
@MapperScan({"com.klsjnh.infrastructure.persistence.mapper", "com.klsjnh.infrastructure.system011.mapper",
        "com.klsjnh.infrastructure.dataservice011.mapper", "com.klsjnh.demo11.infrastructure.persistence.mapper"})
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
