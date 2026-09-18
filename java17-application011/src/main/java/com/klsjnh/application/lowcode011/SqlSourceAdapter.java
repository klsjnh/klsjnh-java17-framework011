package com.klsjnh.application.lowcode011;

/*                SqlSourceAdapter class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.18
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.18  sql source adapter class
 *
 */

import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.application.dataservice011.BusinessModelingProbeResult;
import com.klsjnh.application.dataservice011.JulyBusinessModelingUseCase;
import com.klsjnh.domain.lowcode011.JulyMetadataDisplay;
import com.klsjnh.domain.lowcode011.JulyMetadataField;
import com.klsjnh.domain.lowcode011.JulyMetadataService;
import com.klsjnh.domain.lowcode011.ModelSourcePort;
import com.klsjnh.domain.lowcode011.SourceRequest;
import com.klsjnh.domain.lowcode011.records.MetadataContent;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * SQL source: probe a read-only SQL on a business datasource and infer the
 * fields (reuses the 033 probe). Displays default to one input column per field
 * and a query service is added, so the object is immediately usable.
 */

@Component
public class SqlSourceAdapter implements ModelSourcePort {

    /**
     * Business modeling use case (probe inference).
     */
    private final JulyBusinessModelingUseCase modelingUseCase;

    /**
     * Create the adapter.
     *
     * @param modelingUseCase business modeling use case
     */
    public SqlSourceAdapter(JulyBusinessModelingUseCase modelingUseCase) {
        this.modelingUseCase = modelingUseCase;
    }

    /** {@inheritDoc} */
    @Override
    public String kind() {
        return "sql";
    }

    /** {@inheritDoc} */
    @Override
    public MetadataContent intake(SourceRequest request) {
        if (blank(request.dataSourceCode())) {
            throw BusinessException.badRequest("dataSourceCode required for sql source");
        }

        if (blank(request.sql())) {
            throw BusinessException.badRequest("sql required for sql source");
        }

        if (blank(request.objectName())) {
            throw BusinessException.badRequest("objectName required for sql source");
        }

        if (blank(request.businessField())) {
            throw BusinessException.badRequest("businessField required for sql source");
        }

        BusinessModelingProbeResult probe = modelingUseCase.probeAndInfer(request.dataSourceCode(), request.sql(),
                request.objectName());

        if (!probe.success()) {
            throw BusinessException.badRequest("probe failed: " + probe.message());
        }

        List<JulyMetadataField> fields = probe.fields();

        if (fields.isEmpty()) {
            throw BusinessException.badRequest("probe returned no fields");
        }

        List<JulyMetadataDisplay> displays = new ArrayList<>();
        int fallbackSort = 1;

        for (JulyMetadataField field : fields) {
            int sort = field.sortOrder() == null ? fallbackSort : field.sortOrder();
            displays.add(new JulyMetadataDisplay(field.fieldCode(), field.fieldName(), "left", 0, "input", "all", null,
                    sort));
            fallbackSort++;
        }

        List<JulyMetadataService> services = new ArrayList<>();
        services.add(new JulyMetadataService("query", "query", "", "type011", "query", "", true, 1));

        return MetadataContent.reconstitute(request.objectName(), or(request.objectType(), "type011"),
                request.description(), request.businessField(), request.packageName(),
                or(request.routerPath(), "/runtime/" + request.objectName()), fields, displays, services);
    }

    /**
     * Blank check.
     *
     * @param value value
     * @return true when null or blank
     */
    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Value or fallback.
     *
     * @param value    value
     * @param fallback fallback
     * @return value or fallback
     */
    private String or(String value, String fallback) {
        return blank(value) ? fallback : value;
    }
}
