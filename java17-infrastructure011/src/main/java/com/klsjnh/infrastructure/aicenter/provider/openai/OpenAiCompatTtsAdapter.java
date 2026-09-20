package com.klsjnh.infrastructure.aicenter.provider.openai;

/*                OpenAiCompatTtsAdapter class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  tts provider
 *      2026.09.20  generic openai-compatible spi adapter (no vendor class)
 *
 */

import com.klsjnh.common.util.HttpResponseBytes011;
import com.klsjnh.common.util.HttpUtil011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.aicenter.audio.AiTtsPort;
import com.klsjnh.domain.aicenter.audio.AiTtsRequest;
import com.klsjnh.domain.aicenter.capability.AiMedia;
import com.klsjnh.domain.aicenter.capability.AiReturnTypes;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Map;

/**
 * Generic OpenAI-compatible speech synthesis adapter over {@code POST
 * /v1/audio/speech}. Returns the audio as a binary stream, or a json url when
 * {@code returnType=url}.
 */

@Component
public class OpenAiCompatTtsAdapter implements AiTtsPort {

    /**
     * Speech synthesis path.
     */
    private static final String SPEECH_PATH = "/audio/speech";

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
     * Generic fallback: the registry prefers a vendor-specific port over this.
     *
     * @return always true
     */
    @Override
    public boolean generic() {
        return true;
    }

    /**
     * Synthesize speech.
     *
     * @param request vendor-neutral request
     * @return audio artifact
     */
    @Override
    public AiMedia synthesize(AiTtsRequest request) {
        String format = StringUtil011.isBlank(request.format()) ? "mp3" : request.format();
        boolean wantUrl = AiReturnTypes.URL.equalsIgnoreCase(request.target().returnType());

        ObjectNode body = OBJECT_MAPPER.createObjectNode();
        body.put("model", request.target().model());
        body.put("input", request.input());

        if (!StringUtil011.isBlank(request.voice())) {
            body.put("voice", request.voice());
        }

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
                String text = HttpUtil011.postJson(normalize(request.baseUrl()) + SPEECH_PATH, json, headers).body();
                return AiMedia.ofUrl(OBJECT_MAPPER.readTree(text).path("data").path("url").asText(null), mime(format));
            }

            HttpResponseBytes011 response = HttpUtil011.postJsonBytes(normalize(request.baseUrl()) + SPEECH_PATH,
                    json, headers);

            if (!response.isSuccess()) {
                throw new IllegalStateException("ai tts HTTP " + response.status());
            }

            return AiMedia.ofBytes(response.body(), mime(format));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("ai tts interrupted");
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("ai tts failed: " + ex.getMessage());
        }
    }

    /**
     * Audio mime type for a format.
     *
     * @param format audio format
     * @return mime type
     */
    private String mime(String format) {
        return "wav".equalsIgnoreCase(format) ? "audio/wav" : "audio/mpeg";
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
