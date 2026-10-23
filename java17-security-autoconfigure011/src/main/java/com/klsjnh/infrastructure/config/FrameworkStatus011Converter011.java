package com.klsjnh.infrastructure.config;

/*                FrameworkStatus011Converter011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.23
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.23  bind krt.status through FrameworkStatus011.fromString
 *
 */

import com.klsjnh.common.enums.FrameworkStatus011;

import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

/**
 * Configuration-properties converter for {@code krt.status}.
 * <p>
 * Spring Boot's default enum converter calls {@code Enum.valueOf}, which only
 * accepts the exact constant names. This converter delegates to
 * {@link FrameworkStatus011#fromString(String)} so tolerant values such as
 * {@code debug011} and {@code dev011} bind correctly while unknown values still
 * fail fast.
 * </p>
 */

@Component
@ConfigurationPropertiesBinding
public class FrameworkStatus011Converter011 implements Converter<String, FrameworkStatus011> {

    /**
     * Convert a raw {@code krt.status} value.
     *
     * @param source raw config value
     * @return resolved status
     */
    @Override
    public FrameworkStatus011 convert(String source) {
        return FrameworkStatus011.fromString(source);
    }
}
