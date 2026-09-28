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

import com.klsjnh.enabled.EnableAccess011Center;
import com.klsjnh.enabled.EnableAi011Center;
import com.klsjnh.enabled.EnableDatasource011Center;
import com.klsjnh.enabled.EnableMessage011Center;
import com.klsjnh.enabled.EnablePlatform011Center;
import com.klsjnh.enabled.EnableStorage011Center;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.mybatis.spring.annotation.MapperScan;

/**
 * klsjnh Java17 framework boot entry, the only main of the whole project.
 * <p>
 * Framework beans register through core (and starter) auto-configuration; the
 * host never scans {@code com.klsjnh} itself. Centers are <b>explicitly
 * enabled</b> by the {@code @EnableXxx011Center} annotations below — a center
 * jar on the classpath does nothing until it is asked for, so a consumer only
 * gets the centers it lists. {@code @EnableAi011Center} already implies the
 * storage center (AI stores prompt bodies through it); both are listed for
 * readability.
 * </p>
 */

@EnableAccess011Center
@EnableAi011Center
@EnableDatasource011Center
@EnableMessage011Center
@EnablePlatform011Center
@EnableStorage011Center
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
