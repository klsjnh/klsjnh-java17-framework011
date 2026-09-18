package com.klsjnh.application.lowcode011;

/*                AiSourceAdapter class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.18
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.18  ai prompt source adapter class
 *
 */

import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.application.ai011.AiInvokeOutcome;
import com.klsjnh.application.ai011.AiInvokeUseCase;
import com.klsjnh.domain.ai011.AiChatMessage;
import com.klsjnh.domain.lowcode011.ModelSourcePort;
import com.klsjnh.domain.lowcode011.SourceRequest;
import com.klsjnh.domain.lowcode011.records.BaseColumn011;
import com.klsjnh.domain.lowcode011.records.MetaDtoKey011;
import com.klsjnh.domain.lowcode011.records.MetadataContent;
import com.klsjnh.domain.lowcode011.records.MetadataContentCodec;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AI source ("one-sentence development"): ask a model to fill the metadata
 * skeleton, parse the JSON and validate it; on failure feed the error back and
 * retry (bounded). The skeleton and rules come from the shared template guide,
 * so AI is just another adapter and its output goes through the same review.
 */

@Component
public class AiSourceAdapter implements ModelSourcePort {

    /**
     * Maximum attempts (initial + retries).
     */
    private static final int MAX_ATTEMPTS = 3;

    /**
     * AI invoke use case (model call).
     */
    private final AiInvokeUseCase aiInvokeUseCase;

    /**
     * Template use case (skeleton + guide).
     */
    private final JulyMetadataTemplateUseCase templateUseCase;

    /**
     * JSON mapper.
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Create the adapter.
     *
     * @param aiInvokeUseCase ai invoke use case
     * @param templateUseCase template use case
     */
    public AiSourceAdapter(AiInvokeUseCase aiInvokeUseCase, JulyMetadataTemplateUseCase templateUseCase) {
        this.aiInvokeUseCase = aiInvokeUseCase;
        this.templateUseCase = templateUseCase;
    }

    /** {@inheritDoc} */
    @Override
    public String kind() {
        return "ai";
    }

    /** {@inheritDoc} */
    @Override
    public MetadataContent intake(SourceRequest request) {
        if (request.prompt() == null || request.prompt().isBlank()) {
            throw BusinessException.badRequest("prompt required for ai source");
        }

        String skeleton;

        try {
            skeleton = objectMapper.writeValueAsString(templateUseCase.blankTemplate());
        } catch (Exception ex) {
            throw BusinessException.badRequest("cannot build ai prompt skeleton");
        }

        String system = "You are a low-code metadata designer. Fill the JSON skeleton below and return ONLY the JSON "
                + "(no markdown, no comments). Keep the four sections metaData / fieldData / displayData / serviceData; "
                + "the rules are in _guide. A business key businessField is expected (its field name is free; 'sid' is "
                + "NOT special and NOT required); if you cannot determine one, set businessField to \"id\". Skeleton: "
                + skeleton;

        List<AiChatMessage> messages = new ArrayList<>();
        messages.add(new AiChatMessage("system", system));
        messages.add(new AiChatMessage("user", request.prompt()));

        String lastError = null;

        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            AiInvokeOutcome outcome = aiInvokeUseCase.chat(request.provider(), request.api(), request.model(), messages,
                    null, null);

            try {
                return parse(outcome.content(), request);
            } catch (RuntimeException ex) {
                lastError = ex.getMessage();
                messages.add(new AiChatMessage("assistant", outcome.content()));
                messages.add(new AiChatMessage("user",
                        "Invalid result: " + lastError + ". Return ONLY corrected JSON per the skeleton."));
            }
        }

        throw BusinessException.badRequest("ai source failed to produce valid metadata: " + lastError);
    }

    /**
     * Parse the model output into content (reconstitute validates).
     *
     * @param text    model output
     * @param request source request
     * @return content
     */
    private MetadataContent parse(String text, SourceRequest request) {
        String json = extractJson(text);

        try {
            Map<String, Object> dto = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() { });
            fallbackBusinessField(dto);
            return rename(MetadataContentCodec.fromMetaDto(dto), request.objectName());
        } catch (BusinessException ex) {
            throw ex;
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("ai output is not valid JSON: " + ex.getMessage());
        }
    }

    /**
     * Relaxed rule for AI: when the model gives no business key, fall back to
     * the platform identity column {@code id} (the framework prefers an explicit
     * business key, but AI output may omit it). Other sources stay strict.
     *
     * @param dto MetaDTO value
     */
    @SuppressWarnings("unchecked")
    private void fallbackBusinessField(Map<String, Object> dto) {
        if (!(dto.get(MetaDtoKey011.META_DATA) instanceof Map)) {
            return;
        }

        Map<String, Object> metaData = (Map<String, Object>) dto.get(MetaDtoKey011.META_DATA);
        Object businessField = metaData.get(MetaDtoKey011.BUSINESS_FIELD);

        if (businessField == null || String.valueOf(businessField).isBlank()) {
            metaData.put(MetaDtoKey011.BUSINESS_FIELD, BaseColumn011.ID);
        }
    }

    /**
     * Extract the first JSON object from the model output (tolerates prose and
     * code fences).
     *
     * @param text model output
     * @return JSON object text
     */
    private String extractJson(String text) {
        if (text == null) {
            throw new IllegalArgumentException("ai output is empty");
        }

        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');

        if (start < 0 || end <= start) {
            throw new IllegalArgumentException("ai output has no JSON object");
        }

        return text.substring(start, end + 1);
    }
}
