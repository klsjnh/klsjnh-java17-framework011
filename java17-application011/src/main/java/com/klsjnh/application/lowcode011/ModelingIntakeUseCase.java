package com.klsjnh.application.lowcode011;

/*                ModelingIntakeUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.18
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.18  modeling intake use case class
 *
 */

import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.domain.lowcode011.ModelSourcePort;
import com.klsjnh.domain.lowcode011.SourceRequest;
import com.klsjnh.domain.lowcode011.records.MetaDtoKey011;
import com.klsjnh.domain.lowcode011.records.MetadataContent;
import com.klsjnh.domain.lowcode011.records.MetadataContentCodec;
import com.klsjnh.domain.lowcode011.records.IntakeKey011;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Modeling intake engine: "one engine, N gates". Routes a request to the source
 * adapter that matches its kind, gets the shared content contract, then reuses
 * the single downstream — template review and deploy (save + publish). Adding
 * an entry point means adding an adapter, never touching this engine.
 */

@Service
public class ModelingIntakeUseCase {

    /**
     * Registered adapters by kind.
     */
    private final Map<String, ModelSourcePort> ports = new LinkedHashMap<>();

    /**
     * Template use case (shared review / deploy).
     */
    private final JulyMetadataTemplateUseCase templateUseCase;

    /**
     * Create the use case.
     *
     * @param adapters       all source adapters (Spring-collected)
     * @param templateUseCase template use case
     */
    public ModelingIntakeUseCase(List<ModelSourcePort> adapters, JulyMetadataTemplateUseCase templateUseCase) {
        for (ModelSourcePort adapter : adapters) {
            ports.put(adapter.kind(), adapter);
        }

        this.templateUseCase = templateUseCase;
    }

    /**
     * Review an intake request: build the content, then validate + preview with
     * zero side effects.
     *
     * @param body intake request
     * @return review report
     */
    public Map<String, Object> review(Map<String, Object> body) {
        SourceRequest request = toRequest(body);

        return templateUseCase.reviewTemplate(toTemplate(intake(request), request));
    }

    /**
     * Create an object from an intake request: build the content, then save and
     * publish it.
     *
     * @param body intake request
     * @return create result
     */
    public Map<String, Object> create(Map<String, Object> body) {
        SourceRequest request = toRequest(body);
        MetadataContent content = intake(request);
        boolean overwrite = Boolean.TRUE.equals(body.get(IntakeKey011.OVERWRITE));

        return templateUseCase.deployObject(toTemplate(content, request), content.objectName(), overwrite);
    }

    /**
     * Supported source kinds, in registration order.
     *
     * @return kinds
     */
    public List<String> kinds() {
        return List.copyOf(ports.keySet());
    }

    /**
     * Route the request to its adapter.
     *
     * @param request source request
     * @return content
     */
    private MetadataContent intake(SourceRequest request) {
        String kind = request.kind();
        ModelSourcePort port = kind == null ? null : ports.get(kind);

        if (port == null) {
            throw BusinessException.badRequest(
                    "unknown source kind: " + kind + " (supported: " + String.join(", ", ports.keySet()) + ")");
        }

        MetadataContent content;

        try {
            content = port.intake(request);
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }

        if (!"ai".equals(kind) && (content.businessField() == null || content.businessField().isBlank())) {
            throw BusinessException.badRequest("businessField required (business key) for source kind: " + kind);
        }

        return content;
    }

    /**
     * Wrap content as a MetaDTO template carrying the source descriptor, so the
     * shared review / deploy and the source persistence work unchanged.
     *
     * @param content content
     * @param request source request
     * @return template value
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> toTemplate(MetadataContent content, SourceRequest request) {
        Map<String, Object> template = new LinkedHashMap<>(MetadataContentCodec.toMetaDto(content));
        template.put(MetaDtoKey011.TEMPLATE_VERSION, "1.0");

        Map<String, Object> metaData = (Map<String, Object>) template.get(MetaDtoKey011.META_DATA);
        Map<String, Object> source = new LinkedHashMap<>();
        source.put(MetaDtoKey011.KIND, request.kind());

        if (notBlank(request.dataSourceCode())) {
            source.put(MetaDtoKey011.DATA_SOURCE_CODE, request.dataSourceCode());
        }

        String detail = notBlank(request.sql()) ? request.sql()
                : (notBlank(request.prompt()) ? request.prompt() : request.ref());

        if (notBlank(detail)) {
            source.put(MetaDtoKey011.PROBE_SQL, detail);
        }

        metaData.put(MetaDtoKey011.SOURCE, source);

        return template;
    }

    /**
     * Read the intake request from the body.
     *
     * @param body request body
     * @return source request
     */
    @SuppressWarnings("unchecked")
    private SourceRequest toRequest(Map<String, Object> body) {
        if (body == null) {
            throw BusinessException.badRequest("body required");
        }

        Map<String, Object> template = body.get(IntakeKey011.TEMPLATE) instanceof Map
                ? (Map<String, Object>) body.get(IntakeKey011.TEMPLATE)
                : null;

        return new SourceRequest(text(body.get(IntakeKey011.KIND)), text(body.get(IntakeKey011.OBJECT_NAME)), text(body.get(IntakeKey011.OBJECT_TYPE)),
                text(body.get(IntakeKey011.DESCRIPTION)), text(body.get(IntakeKey011.BUSINESS_FIELD)), text(body.get(IntakeKey011.PACKAGE_NAME)),
                text(body.get(IntakeKey011.ROUTER_PATH)), text(body.get(IntakeKey011.DATA_SOURCE_CODE)), text(body.get(IntakeKey011.TABLE)),
                text(body.get(IntakeKey011.SQL)), text(body.get(IntakeKey011.PROMPT)), text(body.get(IntakeKey011.REF)), text(body.get(IntakeKey011.PROVIDER)),
                text(body.get(IntakeKey011.API)), text(body.get(IntakeKey011.MODEL)), template);
    }

    /**
     * Read a string value.
     *
     * @param value raw value
     * @return string or null
     */
    private String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    /**
     * Non-blank check.
     *
     * @param value value
     * @return true when not null and not blank
     */
    private boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}
