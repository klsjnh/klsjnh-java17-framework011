package com.klsjnh.domain.lowcode011;

/*                TemplateCodec interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.18
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.18  template codec port
 *
 */

import com.klsjnh.domain.lowcode011.enums.TemplateFormat011;

import java.util.Map;

/**
 * A template codec: turns a template value (the MetaDTO map) to and from one
 * file format. Import always produces the shared MetaDTO shape, so the review /
 * deploy downstream stays unique; adding a format means adding an
 * implementation, not changing the template use case.
 */

public interface TemplateCodec {

    /**
     * Format handled by this codec.
     *
     * @return format
     */
    TemplateFormat011 format();

    /**
     * Parse file bytes into a template value.
     *
     * @param data file bytes
     * @return template value (MetaDTO shape)
     */
    Map<String, Object> importTemplate(byte[] data);

    /**
     * Write a template value to file bytes.
     *
     * @param template template value
     * @return file bytes
     */
    byte[] exportTemplate(Map<String, Object> template);
}
