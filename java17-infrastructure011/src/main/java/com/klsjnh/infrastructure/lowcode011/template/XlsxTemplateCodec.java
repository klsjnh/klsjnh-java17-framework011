package com.klsjnh.infrastructure.lowcode011.template;

/*                XlsxTemplateCodec class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.18
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.18  xlsx template codec placeholder class
 *
 */

import com.klsjnh.domain.lowcode011.TemplateCodec;
import com.klsjnh.domain.lowcode011.enums.TemplateFormat011;

import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * XLSX template codec placeholder: the multi-sheet Excel format is reserved for
 * a later phase (needs apache poi-ooxml); until then both directions report
 * "not supported" so the endpoint contract is already in place.
 */

@Component
public class XlsxTemplateCodec implements TemplateCodec {

    /** {@inheritDoc} */
    @Override
    public TemplateFormat011 format() {
        return TemplateFormat011.XLSX;
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, Object> importTemplate(byte[] data) {
        throw new UnsupportedOperationException("xlsx template not supported yet");
    }

    /** {@inheritDoc} */
    @Override
    public byte[] exportTemplate(Map<String, Object> template) {
        throw new UnsupportedOperationException("xlsx template not supported yet");
    }
}
