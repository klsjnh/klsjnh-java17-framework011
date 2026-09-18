package com.klsjnh.application.lowcode011.intake;

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

import com.klsjnh.application.lowcode011.modeling.BusinessModelingProbeResult;
import com.klsjnh.application.lowcode011.modeling.JulyBusinessModelingUseCase;
import com.klsjnh.domain.lowcode011.JulyMetadataDisplay;
import com.klsjnh.domain.lowcode011.JulyMetadataField;
import com.klsjnh.domain.lowcode011.JulyMetadataService;
import com.klsjnh.domain.lowcode011.ModelSourceKind011;
import com.klsjnh.domain.lowcode011.ModelSourcePort;
import com.klsjnh.domain.lowcode011.SourceRequest;
import com.klsjnh.domain.lowcode011.enums.DisplayType011;
import com.klsjnh.domain.lowcode011.enums.ObjectType011;
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
     * Default object type.
     */
    private static final String DEFAULT_OBJECT_TYPE = ObjectType011.TYPE011.getCode();

    /**
     * Default display align.
     */
    private static final String DEFAULT_ALIGN = "left";

    /**
     * Default display component.
     */
    private static final String DEFAULT_COMPONENT = "input";

    /**
     * Default display scene.
     */
    private static final String DEFAULT_DISPLAY_TYPE = DisplayType011.ALL.getCode();

    /**
     * Default service code.
     */
    private static final String DEFAULT_SERVICE_CODE = "query";

    /**
     * Default service param type.
     */
    private static final String DEFAULT_SERVICE_PARAM_TYPE = "query";

    /**
     * Runtime route prefix.
     */
    private static final String ROUTE_PREFIX = "/runtime/";

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
        return ModelSourceKind011.SQL;
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
            displays.add(new JulyMetadataDisplay(field.fieldCode(), field.fieldName(), DEFAULT_ALIGN, 0,
                    DEFAULT_COMPONENT, DEFAULT_DISPLAY_TYPE, null, sort));
            fallbackSort++;
        }

        List<JulyMetadataService> services = new ArrayList<>();
        services.add(new JulyMetadataService(DEFAULT_SERVICE_CODE, DEFAULT_SERVICE_CODE, "", DEFAULT_OBJECT_TYPE,
                DEFAULT_SERVICE_PARAM_TYPE, "", true, 1));

        return MetadataContent.reconstitute(request.objectName(), or(request.objectType(), DEFAULT_OBJECT_TYPE),
                request.description(), request.businessField(), request.packageName(),
                or(request.routerPath(), ROUTE_PREFIX + request.objectName()), fields, displays, services);
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
