package com.klsjnh.application.lowcode011;

/*                ModelingSourceAdapter class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.18
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.18  modeling hand-off source adapter class
 *
 */

import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.application.dataservice011.JulyBusinessModelingUseCase;
import com.klsjnh.domain.dataservice011.JulyBusinessModeling;
import com.klsjnh.domain.lowcode011.ModelSourcePort;
import com.klsjnh.domain.lowcode011.SourceRequest;
import com.klsjnh.domain.lowcode011.records.MetadataContent;

import org.springframework.stereotype.Component;

/**
 * Modeling hand-off source: read a 033 business-modeling record's content
 * (optionally renamed). This is the two-domain hand-off made concrete.
 */

@Component
public class ModelingSourceAdapter implements ModelSourcePort {

    /**
     * Business modeling use case (hand-off read).
     */
    private final JulyBusinessModelingUseCase modelingUseCase;

    /**
     * Create the adapter.
     *
     * @param modelingUseCase business modeling use case
     */
    public ModelingSourceAdapter(JulyBusinessModelingUseCase modelingUseCase) {
        this.modelingUseCase = modelingUseCase;
    }

    /** {@inheritDoc} */
    @Override
    public String kind() {
        return "modeling";
    }

    /** {@inheritDoc} */
    @Override
    public MetadataContent intake(SourceRequest request) {
        if (request.ref() == null || request.ref().isBlank()) {
            throw BusinessException.badRequest("ref (modelCode) required for modeling source");
        }

        JulyBusinessModeling model = modelingUseCase.getByCode(request.ref());

        if (model == null) {
            throw BusinessException.badRequest("modeling record not found: " + request.ref());
        }

        return rename(model.content(), request.objectName());
    }
}
