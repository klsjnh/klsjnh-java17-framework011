package com.klsjnh.infrastructure.lowcode011.template;

/*                JsonTemplateCodec class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.18
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.18  json template codec class
 *
 */

import com.klsjnh.domain.lowcode011.TemplateCodec;
import com.klsjnh.domain.lowcode011.enums.TemplateFormat011;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * JSON template codec: the canonical, self-contained MetaDTO value (keys
 * unchanged, includes {@code _guide}).
 */

@Component
public class JsonTemplateCodec implements TemplateCodec {

    /**
     * JSON mapper.
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /** {@inheritDoc} */
    @Override
    public TemplateFormat011 format() {
        return TemplateFormat011.JSON;
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, Object> importTemplate(byte[] data) {
        try {
            return objectMapper.readValue(data, new TypeReference<Map<String, Object>>() { });
        } catch (Exception ex) {
            throw new IllegalArgumentException("template file is not valid json: " + ex.getMessage());
        }
    }

    /** {@inheritDoc} */
    @Override
    public byte[] exportTemplate(Map<String, Object> template) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(template);
        } catch (Exception ex) {
            throw new IllegalArgumentException("cannot write json template: " + ex.getMessage());
        }
    }
}
