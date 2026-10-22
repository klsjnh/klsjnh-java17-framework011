package com.klsjnh.infrastructure.aicenter.modelprovider.probe;

/*                AiModelProbe011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  ai model probe 011 class
 *      2026.10.22  split connectivity probe vs chat probe (model + auth + shape)
 *
 */

import com.klsjnh.common.util.HttpResponse011;
import com.klsjnh.common.util.HttpUtil011;

import com.klsjnh.domain.aicenter.modelprovider.AiModelProbePort;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.Duration;
import java.util.Map;

/**
 * Probe over the shared JDK HTTP helper. Two separate probes, because a healthy
 * {@code /models} says nothing about chat or vision:
 * <ul>
 *   <li>{@link #probe} — GET {@code {baseUrl}/models}: connectivity / key.</li>
 *   <li>{@link #probeChat} — POST {@code {baseUrl}/chat/completions} with a
 *   one-word prompt: endpoint, key, model and reply shape.</li>
 * </ul>
 * The key never appears in any message and probe payloads carry no business
 * data.
 */

@Component
public class AiModelProbe011 implements AiModelProbePort {

    /**
     * JSON mapper.
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Chat probe read timeout (short: it is a health check, not a workload).
     */
    private static final Duration CHAT_PROBE_TIMEOUT = Duration.ofSeconds(15);

    /**
     * One-word prompt used by the chat probe.
     */
    private static final String CHAT_PROBE_PROMPT = "ping";

    /**
     * Probe an endpoint with the given api key.
     *
     * @param baseUrl OpenAI-compatible base url
     * @param apiKey  api key secret
     * @return probe result, never null
     */
    @Override
    public ProbeResult probe(String baseUrl, String apiKey) {
        String url = normalize(baseUrl) + "/models";

        try {
            HttpResponse011 response = HttpUtil011.get(url,
                    Map.of("Authorization", "Bearer " + (apiKey == null ? "" : apiKey)));
            int status = response.status();

            return new ProbeResult(response.isSuccess(), "HTTP " + status, status);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();

            return new ProbeResult(false, "probe interrupted", null);
        } catch (Exception ex) {
            return new ProbeResult(false, ex.getClass().getSimpleName() + ": " + ex.getMessage(), null);
        }
    }

    /**
     * Probe one model over POST /chat/completions with a minimal prompt.
     *
     * @param baseUrl OpenAI-compatible base url
     * @param apiKey  api key secret
     * @param model   model name
     * @return probe result, never null
     */
    @Override
    public ProbeResult probeChat(String baseUrl, String apiKey, String model) {
        String url = normalize(baseUrl) + "/chat/completions";

        try {
            HttpResponse011 response = HttpUtil011.postJson(url, chatProbeBody(model),
                    Map.of("Authorization", "Bearer " + (apiKey == null ? "" : apiKey)), CHAT_PROBE_TIMEOUT);
            int status = response.status();

            if (!response.isSuccess()) {
                return new ProbeResult(false, HttpUtil011.describeStatus(status), status);
            }

            JsonNode choices = OBJECT_MAPPER.readTree(response.body()).path("choices");

            if (!choices.isArray() || choices.isEmpty()) {
                return new ProbeResult(false, "chat response has no choices", status);
            }

            return new ProbeResult(true, "chat ok", status);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();

            return new ProbeResult(false, "probe interrupted", null);
        } catch (Exception ex) {
            return new ProbeResult(false, ex.getClass().getSimpleName(), null);
        }
    }

    /**
     * Build the one-word chat probe body.
     *
     * @param model model name
     * @return json text
     */
    private String chatProbeBody(String model) {
        ObjectNode root = OBJECT_MAPPER.createObjectNode();
        root.put("model", model == null ? "" : model);
        root.put("max_tokens", 1);
        root.put("stream", false);

        ArrayNode messages = root.putArray("messages");
        messages.addObject().put("role", "user").put("content", CHAT_PROBE_PROMPT);

        return root.toString();
    }

    /**
     * Strip trailing slashes from the base url.
     *
     * @param baseUrl base url
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
