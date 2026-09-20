package com.klsjnh.infrastructure.aicenter.provider.openai;

/*                OpenAiCompatImageAdapter class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  image provider
 *      2026.09.20  generic openai-compatible spi adapter (no vendor class)
 *
 */

import com.klsjnh.common.util.HttpResponse011;
import com.klsjnh.common.util.HttpUtil011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.aicenter.capability.AiMedia;
import com.klsjnh.domain.aicenter.capability.AiReturnTypes;
import com.klsjnh.domain.aicenter.image.AiImagePort;
import com.klsjnh.domain.aicenter.image.AiImageRequest;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;

/**
 * Generic OpenAI-compatible image adapter over {@code POST
 * /v1/images/generations} (text-to-image). A vendor is just a provider row;
 * {@code returnType} selects url or base64.
 */

@Component
public class OpenAiCompatImageAdapter implements AiImagePort {

    /**
     * Image generation path.
     */
    private static final String IMAGE_PATH = "/images/generations";

    /**
     * JSON codec.
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Generic adapter: serves any OpenAI-compatible provider code.
     *
     * @param providerCode provider code
     * @return always true
     */
    @Override
    public boolean supports(String providerCode) {
        return true;
    }

    /**
     * Generate an image.
     *
     * @param request vendor-neutral request
     * @return image artifact
     */
    @Override
    public AiMedia generate(AiImageRequest request) {
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
            HttpResponse011 response = HttpUtil011.postJson(normalize(request.baseUrl()) + IMAGE_PATH,
                    OBJECT_MAPPER.writeValueAsString(body),
                    Map.of("Authorization", "Bearer " + request.apiKey()));

            if (!response.isSuccess()) {
                throw new IllegalStateException("ai image HTTP " + response.status());
            }

            JsonNode data = OBJECT_MAPPER.readTree(response.body()).path("data").path(0);
            String mime = "image/png";

            if (wantB64) {
                return AiMedia.ofBase64(data.path("b64_json").asText(null), mime);
            }

            return AiMedia.ofUrl(data.path("url").asText(null), mime);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("ai image interrupted");
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("ai image failed: " + ex.getMessage());
        }
    }

    /**
     * Normalize a base url (trim, drop trailing slash).
     *
     * @param baseUrl raw base url
     * @return normalized base url
     */
    private String normalize(String baseUrl) {
        String value = baseUrl == null ? "" : baseUrl.trim();

        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }

        return value;
    }
}
