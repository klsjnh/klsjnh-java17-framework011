package com.klsjnh.infrastructure.aicenter.provider.openai;

/*                OpenAiCompatInferenceAdapter class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  openai compatible chat adapter class
 *      2026.09.19  restore under package-by-feature layout (use HttpUtil011)
 *      2026.09.20  generic spi adapter + sse streaming
 *
 */

import com.klsjnh.common.util.HttpResponse011;
import com.klsjnh.common.util.HttpResponseStream011;
import com.klsjnh.common.util.HttpUtil011;

import com.klsjnh.domain.aicenter.inference.AiChatMessage;
import com.klsjnh.domain.aicenter.inference.AiInferenceChunk;
import com.klsjnh.domain.aicenter.inference.AiInferenceCommand;
import com.klsjnh.domain.aicenter.inference.AiInferencePort;
import com.klsjnh.domain.aicenter.inference.AiInferenceResult;

import com.klsjnh.infrastructure.config.KrtConfig011;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Generic OpenAI-compatible inference adapter: {@code POST
 * {baseUrl}/chat/completions} with a bearer key over the shared
 * {@link HttpUtil011}. Works for any OpenAI-compatible vendor (stepfun,
 * deepseek, longcat ...) — a vendor is just a provider row, no vendor class. A
 * vendor with its own protocol implements {@link AiInferencePort} as its own
 * SPI bean.
 */

@Component
public class OpenAiCompatInferenceAdapter implements AiInferencePort {

    /**
     * JSON mapper.
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Request timeout.
     */
    private final Duration timeout;

    /**
     * Create the adapter.
     *
     * @param krtConfig framework config (krt.ai-center.chat-timeout-seconds)
     */
    public OpenAiCompatInferenceAdapter(KrtConfig011 krtConfig) {
        this.timeout = Duration.ofSeconds(krtConfig.getAiCenter().getChatTimeoutSeconds());
    }

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
     * Send an inference request and return the whole reply.
     *
     * @param command resolved inference request
     * @return model reply, never null
     */
    @Override
    public AiInferenceResult chat(AiInferenceCommand command) {
        String url = normalize(command.baseUrl()) + "/chat/completions";
        Map<String, String> headers = Map.of("Authorization",
                "Bearer " + (command.apiKey() == null ? "" : command.apiKey()));

        try {
            HttpResponse011 response = HttpUtil011.postJson(url, body(command, false), headers, timeout);

            if (!response.isSuccess()) {
                throw new IllegalStateException("ai inference failed: HTTP " + response.status() + " "
                        + abbreviate(response.body()));
            }

            return parse(response.body());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("ai inference interrupted");
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("ai inference failed: " + ex.getMessage(), ex);
        }
    }

    /**
     * Stream the inference fragments over SSE.
     *
     * @param command resolved inference request
     * @return fragment stream, never null
     */
    @Override
    public Stream<AiInferenceChunk> stream(AiInferenceCommand command) {
        String url = normalize(command.baseUrl()) + "/chat/completions";
        Map<String, String> headers = Map.of("Authorization",
                "Bearer " + (command.apiKey() == null ? "" : command.apiKey()));

        try {
            HttpResponseStream011 response = HttpUtil011.postJsonStream(url, body(command, true), headers, timeout);

            if (!response.isSuccess()) {
                throw new IllegalStateException("ai inference failed: HTTP " + response.status() + " "
                        + snippet(response.body()));
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(response.body(), StandardCharsets.UTF_8));

            return reader.lines()
                    .filter(line -> line.startsWith("data:"))
                    .map(line -> line.substring("data:".length()).trim())
                    .takeWhile(data -> !"[DONE]".equals(data))
                    .map(this::parseChunk)
                    .filter(Objects::nonNull)
                    .onClose(() -> closeQuietly(reader));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("ai inference interrupted");
        } catch (Exception ex) {
            throw new IllegalStateException("ai inference failed: " + ex.getMessage(), ex);
        }
    }

    /**
     * Build the request json.
     *
     * @param command chat request
     * @param stream  whether to ask for a streamed reply
     * @return json text
     * @throws Exception serialize failure
     */
    private String body(AiInferenceCommand command, boolean stream) throws Exception {
        ObjectNode root = OBJECT_MAPPER.createObjectNode();
        root.put("model", command.model());

        ArrayNode messages = root.putArray("messages");

        for (AiChatMessage message : command.messages()) {
            ObjectNode node = messages.addObject();
            node.put("role", message.role());
            node.put("content", message.content());
        }

        if (command.temperature() != null) {
            root.put("temperature", command.temperature());
        }

        if (command.maxTokens() != null) {
            root.put("max_tokens", command.maxTokens());
        }

        if (stream) {
            root.put("stream", true);
        }

        return OBJECT_MAPPER.writeValueAsString(root);
    }

    /**
     * Parse the reply json.
     *
     * @param body response body
     * @return chat result
     * @throws Exception parse failure
     */
    private AiInferenceResult parse(String body) throws Exception {
        JsonNode root = OBJECT_MAPPER.readTree(body);
        JsonNode choice = root.path("choices").path(0);
        String content = choice.path("message").path("content").asText(null);
        String finishReason = choice.path("finish_reason").asText(null);
        JsonNode usage = root.path("usage");

        return new AiInferenceResult(content, finishReason, intOrNull(usage, "prompt_tokens"),
                intOrNull(usage, "completion_tokens"), intOrNull(usage, "total_tokens"));
    }

    /**
     * Parse one SSE data frame.
     *
     * @param data json payload
     * @return chunk, null when the frame carries no delta
     */
    private AiInferenceChunk parseChunk(String data) {
        try {
            JsonNode choice = OBJECT_MAPPER.readTree(data).path("choices").path(0);
            String delta = choice.path("delta").path("content").asText(null);
            String finishReason = choice.path("finish_reason").asText(null);

            if (delta == null && finishReason == null) {
                return null;
            }

            return new AiInferenceChunk(delta, finishReason);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Read an optional int field.
     *
     * @param node  json node
     * @param field field name
     * @return int or null
     */
    private Integer intOrNull(JsonNode node, String field) {
        JsonNode value = node.path(field);

        return value.isNumber() ? value.asInt() : null;
    }

    /**
     * Close a reader, ignoring failure.
     *
     * @param reader reader
     */
    private void closeQuietly(BufferedReader reader) {
        try {
            reader.close();
        } catch (Exception ignored) {
            // best effort
        }
    }

    /**
     * Read a short snippet of a stream for an error message.
     *
     * @param stream body stream
     * @return up to 300 characters
     */
    private String snippet(InputStream stream) {
        try (InputStream in = stream; ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[300];
            int read = in.read(buffer);
            if (read > 0) {
                out.write(buffer, 0, read);
            }
            return out.toString(StandardCharsets.UTF_8);
        } catch (Exception ex) {
            return "";
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

    /**
     * Abbreviate a response body for the error message.
     *
     * @param body response body
     * @return short text
     */
    private String abbreviate(String body) {
        if (body == null) {
            return "";
        }

        return body.length() <= 300 ? body : body.substring(0, 300);
    }
}
