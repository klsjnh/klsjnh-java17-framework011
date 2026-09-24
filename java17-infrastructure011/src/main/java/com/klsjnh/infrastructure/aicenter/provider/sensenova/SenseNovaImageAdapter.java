package com.klsjnh.infrastructure.aicenter.provider.sensenova;

/*                SenseNovaImageAdapter class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.24
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.24  sensenova image spi (generations + edits)
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

import java.time.Duration;
import java.util.Base64;
import java.util.Map;

/**
 * SenseNova image SPI adapter. Text-to-image uses {@code POST
 * /v1/images/generations}; image-to-image / text+image uses {@code POST
 * /v1/images/edits} with {@code images:[{image_url}]} (public URL or Data URI).
 * Model id example: {@code sensenova-u1.5-lite}. Vendor-specific bean wins over
 * the generic OpenAI adapter.
 */

@Component
public class SenseNovaImageAdapter implements AiImagePort {

    /**
     * Provider code served by this adapter.
     */
    private static final String PROVIDER_CODE = "sensenova";

    /**
     * Text-to-image path.
     */
    private static final String GENERATIONS_PATH = "/images/generations";

    /**
     * Image edit / image-to-image path.
     */
    private static final String EDITS_PATH = "/images/edits";

    /**
     * SenseNova image generation often exceeds the default 10s HTTP timeout.
     */
    private static final Duration IMAGE_TIMEOUT = Duration.ofSeconds(120);

    /**
     * JSON codec.
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Whether this provider serves the given code.
     *
     * @param providerCode provider code
     * @return true when sensenova
     */
    @Override
    public boolean supports(String providerCode) {
        return PROVIDER_CODE.equalsIgnoreCase(providerCode);
    }

    /**
     * Generate or edit an image through SenseNova.
     *
     * @param request vendor-neutral request
     * @return image artifact
     */
    @Override
    public AiMedia generate(AiImageRequest request) {
        boolean wantB64 = AiReturnTypes.B64.equalsIgnoreCase(request.target().returnType());
        boolean imageToImage = request.isImageToImage();

        ObjectNode body = OBJECT_MAPPER.createObjectNode();
        body.put("model", request.target().model());
        body.put("prompt", request.prompt());
        body.put("n", 1);
        body.put("response_format", wantB64 ? "b64_json" : "url");

        if (!StringUtil011.isBlank(request.size())) {
            body.put("size", request.size());
        } else if (imageToImage) {
            body.put("size", "auto");
        }

        if (imageToImage) {
            addInputImages(body, request);
        }

        String path = imageToImage ? EDITS_PATH : GENERATIONS_PATH;

        try {
            HttpResponse011 response = HttpUtil011.postJson(normalize(request.baseUrl()) + path,
                    OBJECT_MAPPER.writeValueAsString(body),
                    Map.of("Authorization", "Bearer " + request.apiKey()), IMAGE_TIMEOUT);

            if (!response.isSuccess()) {
                throw new IllegalStateException("sensenova image HTTP " + response.status());
            }

            JsonNode data = OBJECT_MAPPER.readTree(response.body()).path("data").path(0);
            String mime = "image/png";

            if (wantB64) {
                return AiMedia.ofBase64(data.path("b64_json").asText(null), mime);
            }

            return AiMedia.ofUrl(data.path("url").asText(null), mime);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("sensenova image interrupted");
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("sensenova image failed: " + ex.getMessage());
        }
    }

    /**
     * Add reference image(s) as SenseNova {@code images:[{image_url}]} (public
     * URL or {@code data:image/...;base64,...} Data URI; bare base64 is rejected
     * by the vendor).
     *
     * @param body    request body
     * @param request image request
     */
    private void addInputImages(ObjectNode body, AiImageRequest request) {
        ArrayNode images = body.putArray("images");
        ObjectNode item = images.addObject();

        if (!StringUtil011.isBlank(request.imageUrl())) {
            item.put("image_url", request.imageUrl());
        } else if (request.imageBytes() != null && request.imageBytes().length > 0) {
            item.put("image_url",
                    "data:image/png;base64," + Base64.getEncoder().encodeToString(request.imageBytes()));
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
