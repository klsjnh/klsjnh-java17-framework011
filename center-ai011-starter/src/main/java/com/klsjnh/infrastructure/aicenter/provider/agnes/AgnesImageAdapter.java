package com.klsjnh.infrastructure.aicenter.provider.agnes;

/*                AgnesImageAdapter class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  agnes image spi adapter (extra_body / required size / image[])
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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Base64;
import java.util.Map;

/**
 * Agnes Image SPI adapter ({@code POST /v1/images/generations}). Agnes is NOT
 * plain OpenAI-compatible: {@code response_format} must sit inside
 * {@code extra_body}, {@code size} is required, and image-to-image / multi-image
 * composition uses {@code extra_body.image[]} (URL or Data URI). A
 * vendor-specific {@link AiImagePort} bean takes priority over the generic
 * OpenAI adapter ({@link AiImagePort#supports} + the registry's generic
 * fallback rule).
 */

@Component
public class AgnesImageAdapter implements AiImagePort {

    /**
     * Provider code served by this adapter.
     */
    private static final String PROVIDER_CODE = "agnes";

    /**
     * Image generation path.
     */
    private static final String IMAGE_PATH = "/images/generations";

    /**
     * Default size tier (Agnes requires a size).
     */
    private static final String DEFAULT_SIZE = "1K";

    /**
     * JSON codec.
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Whether this provider serves the given code.
     *
     * @param providerCode provider code
     * @return true when agnes
     */
    @Override
    public boolean supports(String providerCode) {
        return PROVIDER_CODE.equalsIgnoreCase(providerCode);
    }

    /**
     * Generate an image through Agnes.
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
        body.put("size", StringUtil011.isBlank(request.size()) ? DEFAULT_SIZE : request.size());

        ObjectNode extra = body.putObject("extra_body");
        extra.put("response_format", wantB64 ? "b64_json" : "url");
        addInputImages(extra, request);

        try {
            HttpResponse011 response = HttpUtil011.postJson(normalize(request.baseUrl()) + IMAGE_PATH,
                    OBJECT_MAPPER.writeValueAsString(body),
                    Map.of("Authorization", "Bearer " + request.apiKey()));

            if (!response.isSuccess()) {
                throw new IllegalStateException("agnes image HTTP " + response.status());
            }

            JsonNode data = OBJECT_MAPPER.readTree(response.body()).path("data").path(0);
            String mime = "image/png";

            if (wantB64) {
                return AiMedia.ofBase64(data.path("b64_json").asText(null), mime);
            }

            return AiMedia.ofUrl(data.path("url").asText(null), mime);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("agnes image interrupted");
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("agnes image failed: " + ex.getMessage());
        }
    }

    /**
     * Add image-to-image / multi-image inputs as {@code extra_body.image[]}
     * (public url or Data URI).
     *
     * @param extra   extra_body node
     * @param request image request
     */
    private void addInputImages(ObjectNode extra, AiImageRequest request) {
        if (!StringUtil011.isBlank(request.imageUrl())) {
            extra.putArray("image").add(request.imageUrl());
        } else if (request.imageBytes() != null && request.imageBytes().length > 0) {
            ArrayNode images = extra.putArray("image");
            images.add("data:image/png;base64," + Base64.getEncoder().encodeToString(request.imageBytes()));
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
