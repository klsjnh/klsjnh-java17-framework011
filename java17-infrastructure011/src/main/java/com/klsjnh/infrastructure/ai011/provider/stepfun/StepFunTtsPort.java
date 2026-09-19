package com.klsjnh.infrastructure.ai011.provider.stepfun;

/*                StepFunTtsPort class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  stepfun tts provider
 *
 */

import com.klsjnh.common.util.HttpResponseBytes011;
import com.klsjnh.common.util.HttpUtil011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.ai011.capability.AiMedia;
import com.klsjnh.domain.ai011.capability.AiReturnTypes;
import com.klsjnh.domain.ai011.capability.tts.AiTtsPort;
import com.klsjnh.domain.ai011.capability.tts.AiTtsRequest;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;

/**
 * StepFun speech synthesis provider over the OpenAI-compatible
 * {@code POST /v1/audio/speech} endpoint. StepFun returns the audio as a
 * binary stream; {@code return_url=true} yields a json url instead. The
 * adapter maps the vendor-neutral {@link AiTtsRequest} to StepFun wire fields.
 */

@Component
public class StepFunTtsPort implements AiTtsPort {

    /**
     * Provider code served by this port.
     */
    private static final String PROVIDER_CODE = "stepfun";

    /**
     * Speech synthesis path.
     */
    private static final String SPEECH_PATH = "/audio/speech";

    /**
     * JSON codec for request / url responses.
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
     * Synthesize speech through StepFun.
     *
     * @param request vendor-neutral request
     * @return audio artifact
     */
    @Override
    public AiMedia synthesize(AiTtsRequest request) {
        String baseUrl = baseUrl(request.baseUrl());
        String format = StringUtil011.isBlank(request.format()) ? "mp3" : request.format();
        boolean wantUrl = AiReturnTypes.URL.equalsIgnoreCase(request.target().returnType());

        ObjectNode body = OBJECT_MAPPER.createObjectNode();
        body.put("model", request.target().model());
        body.put("input", request.input());
        body.put("voice", StringUtil011.isBlank(request.voice()) ? "cixingnansheng" : request.voice());
        body.put("response_format", format);

        if (!StringUtil011.isBlank(request.instruction())) {
            body.put("instruction", request.instruction());
        }

        if (request.speed() != null) {
            body.put("speed", request.speed());
        }

        if (request.volume() != null) {
            body.put("volume", request.volume());
        }

        if (request.sampleRate() != null) {
            body.put("sample_rate", request.sampleRate());
        }

        if (wantUrl) {
            body.put("return_url", true);
        }

        try {
            String json = OBJECT_MAPPER.writeValueAsString(body);
            Map<String, String> headers = Map.of("Authorization", "Bearer " + request.apiKey());

            if (wantUrl) {
                String text = HttpUtil011.postJson(baseUrl + SPEECH_PATH, json, headers).body();
                return AiMedia.ofUrl(OBJECT_MAPPER.readTree(text).path("data").path("url").asText(null),
                        mime(format));
            }

            HttpResponseBytes011 response = HttpUtil011.postJsonBytes(baseUrl + SPEECH_PATH, json, headers);

            if (!response.isSuccess()) {
                throw new IllegalStateException("stepfun tts HTTP " + response.status());
            }

            return AiMedia.ofBytes(response.body(), mime(format));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("stepfun tts interrupted");
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("stepfun tts failed: " + ex.getMessage());
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

    /**
     * Audio mime type of a format.
     *
     * @param format audio format
     * @return mime type
     */
    private String mime(String format) {
        return switch (format.toLowerCase()) {
            case "wav" -> "audio/wav";
            case "flac" -> "audio/flac";
            case "opus" -> "audio/opus";
            case "pcm" -> "audio/pcm";
            default -> "audio/mpeg";
        };
    }
}
