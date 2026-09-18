package com.klsjnh.application.lowcode011;

/*                TemplateSourceAdapter class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.18
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.18  template source adapter class
 *
 */

import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.domain.lowcode011.ModelSourcePort;
import com.klsjnh.domain.lowcode011.SourceRequest;
import com.klsjnh.domain.lowcode011.records.MetadataContent;
import com.klsjnh.domain.lowcode011.records.MetadataContentCodec;

import org.springframework.stereotype.Component;

/**
 * Template source: turn an uploaded MetaDTO / template value into content
 * (optionally renamed). The template rules are re-checked downstream by the
 * shared review / deploy.
 */

@Component
public class TemplateSourceAdapter implements ModelSourcePort {

    /** {@inheritDoc} */
    @Override
    public String kind() {
        return "template";
    }

    /** {@inheritDoc} */
    @Override
    public MetadataContent intake(SourceRequest request) {
        if (request.template() == null || request.template().isEmpty()) {
            throw BusinessException.badRequest("template required for template source");
        }

        return rename(MetadataContentCodec.fromMetaDto(request.template()), request.objectName());
    }
}
