package com.klsjnh.infrastructure.aicenter.provider.openai;

/*                OpenAiCompatAsrAdapter class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  generic openai-compatible asr adapter
 *
 */

import com.klsjnh.common.util.HttpResponse011;
import com.klsjnh.common.util.HttpUtil011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.aicenter.audio.AiAsrPort;
import com.klsjnh.domain.aicenter.audio.AiAsrRequest;
import com.klsjnh.domain.aicenter.audio.AiAsrResult;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic OpenAI-compatible speech recognition adapter over {@code POST
 * /v1/audio/transcriptions} (multipart file upload). A vendor is just a
 * provider row.
 */

@Component
public class OpenAiCompatAsrAdapter implements AiAsrPort {

    /**
     * Transcription path.
     */
    private static final String TRANSCRIPTION_PATH = "/audio/transcriptions";

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
     * Transcribe audio to text.
     *
     * @param request vendor-neutral request
     * @return recognition result
     */
    @Override
    public AiAsrResult recognize(AiAsrRequest request) {
        if (request.audio() == null || request.audio().length == 0) {
            throw new IllegalStateException("asr requires audio bytes (url download is the caller's job)");
        }

        String format = StringUtil011.isBlank(request.format()) ? "mp3" : request.format();
        Map<String, String> fields = new HashMap<>();
        fields.put("model", request.target().model());
        fields.put("response_format", "json");

        if (!StringUtil011.isBlank(request.language())) {
            fields.put("language", request.language());
        }

        List<HttpUtil011.Part> files = List.of(
                new HttpUtil011.Part("file", "audio." + format, mime(format), request.audio()));

        try {
            HttpResponse011 response = HttpUtil011.postMultipart(normalize(request.baseUrl()) + TRANSCRIPTION_PATH,
                    Map.of("Authorization", "Bearer " + request.apiKey()), fields, files);

            if (!response.isSuccess()) {
                throw new IllegalStateException("ai asr HTTP " + response.status());
            }

            String text = OBJECT_MAPPER.readTree(response.body()).path("text").asText(null);
            return new AiAsrResult(text, null);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("ai asr interrupted");
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("ai asr failed: " + ex.getMessage());
        }
    }

    /**
     * Audio mime type for a format.
     *
     * @param format audio format
     * @return mime type
     */
    private String mime(String format) {
        if ("wav".equalsIgnoreCase(format)) {
            return "audio/wav";
        }

        return "mp3".equalsIgnoreCase(format) ? "audio/mpeg" : "application/octet-stream";
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
