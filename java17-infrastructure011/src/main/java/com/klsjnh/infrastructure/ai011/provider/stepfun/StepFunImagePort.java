package com.klsjnh.infrastructure.ai011.provider.stepfun;

/*                StepFunImagePort class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  stepfun image provider
 *
 */

import com.klsjnh.common.util.HttpResponse011;
import com.klsjnh.common.util.HttpUtil011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.ai011.capability.AiMedia;
import com.klsjnh.domain.ai011.capability.AiReturnTypes;
import com.klsjnh.domain.ai011.capability.image.AiImagePort;
import com.klsjnh.domain.ai011.capability.image.AiImageRequest;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;

/**
 * StepFun image generation provider over the OpenAI-compatible
 * {@code POST /v1/images/generations} endpoint (text-to-image and, when the
 * vendor supports it, image-to-image). The adapter maps the vendor-neutral
 * {@link AiImageRequest} to StepFun wire fields; {@code return_type} selects
 * url or base64.
 */

@Component
public class StepFunImagePort implements AiImagePort {

    /**
     * Provider code served by this port.
     */
    private static final String PROVIDER_CODE = "stepfun";

    /**
     * Image generation path.
     */
    private static final String IMAGE_PATH = "/images/generations";

    /**
     * JSON codec for request / response bodies.
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Whether this provider serves the given code.
     *
     * @param providerCode provider code
     * @return true when stepfun
     */
    @Override
    public boolean supports(String providerCode) {
        return PROVIDER_CODE.equalsIgnoreCase(providerCode);
    }

    /**
     * Generate an image through StepFun.
     *
     * @param request vendor-neutral request
     * @return image artifact
     */
    @Override
    public AiMedia generate(AiImageRequest request) {
        String baseUrl = baseUrl(request.baseUrl());
        boolean wantB64 = AiReturnTypes.B64.equalsIgnoreCase(request.target().returnType());

        ObjectNode body = OBJECT_MAPPER.createObjectNode();
        body.put("model", request.target().model());
        body.put("prompt", request.prompt());
        body.put("response_format", wantB64 ? "b64_json" : "url");

        if (!StringUtil011.isBlank(request.size())) {
            body.put("size", request.size());
        }

        if (request.steps() != null) {
            body.put("steps", request.steps());
        }

        if (request.seed() != null) {
            body.put("seed", request.seed());
        }

        if (request.guidanceScale() != null) {
            body.put("cfg_scale", request.guidanceScale());
        }

        if (!StringUtil011.isBlank(request.negativePrompt())) {
            body.put("negative_prompt", request.negativePrompt());
        }

        try {
            String json = OBJECT_MAPPER.writeValueAsString(body);
            HttpResponse011 response = HttpUtil011.postJson(baseUrl + IMAGE_PATH, json,
                    Map.of("Authorization", "Bearer " + request.apiKey()));

            if (!response.isSuccess()) {
                throw new IllegalStateException("stepfun image HTTP " + response.status());
            }

            JsonNode data = OBJECT_MAPPER.readTree(response.body()).path("data").path(0);
            String mime = "image/png";

            if (wantB64) {
                return AiMedia.ofBase64(data.path("b64_json").asText(null), mime);
            }

            return AiMedia.ofUrl(data.path("url").asText(null), mime);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("stepfun image interrupted");
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("stepfun image failed: " + ex.getMessage());
        }
    }

    /**
     * Normalize the base url (strip a trailing slash).
     *
     * @param baseUrl base url
     * @return normalized base url
     */
    private String baseUrl(String baseUrl) {
        String value = baseUrl == null ? "" : baseUrl.trim();

        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }

        return value;
    }
}
