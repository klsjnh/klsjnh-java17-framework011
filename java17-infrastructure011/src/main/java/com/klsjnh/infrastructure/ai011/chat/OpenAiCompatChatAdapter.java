package com.klsjnh.infrastructure.ai011.chat;

/*                OpenAiCompatChatAdapter class
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
 *
 */

import com.klsjnh.common.util.HttpResponse011;
import com.klsjnh.common.util.HttpUtil011;

import com.klsjnh.domain.ai011.chat.AiChatCommand;
import com.klsjnh.domain.ai011.chat.AiChatMessage;
import com.klsjnh.domain.ai011.chat.AiChatPort;
import com.klsjnh.domain.ai011.chat.AiChatResult;

import com.klsjnh.infrastructure.config.KrtConfig011;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.Duration;
import java.util.Map;

/**
 * OpenAI-compatible chat adapter: {@code POST {baseUrl}/chat/completions} with a
 * bearer key over the shared {@link HttpUtil011}. Works for any
 * OpenAI-compatible vendor (longcat, deepseek, stepfun ...); a vendor with its
 * own protocol gets its own adapter behind the same port.
 */

@Component
public class OpenAiCompatChatAdapter implements AiChatPort {

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
     * @param krtConfig framework config (krt.ai011.chat-timeout-seconds)
     */
    public OpenAiCompatChatAdapter(KrtConfig011 krtConfig) {
        this.timeout = Duration.ofSeconds(krtConfig.getAi011().getChatTimeoutSeconds());
    }

    /**
     * Send a chat completion request.
     *
     * @param command resolved chat request
     * @return model reply, never null
     */
    @Override
    public AiChatResult chat(AiChatCommand command) {
        String url = normalize(command.baseUrl()) + "/chat/completions";
        Map<String, String> headers = Map.of("Authorization", "Bearer " + (command.apiKey() == null ? "" : command.apiKey()));

        try {
            HttpResponse011 response = HttpUtil011.postJson(url, body(command), headers, timeout);

            if (!response.isSuccess()) {
                throw new IllegalStateException("ai chat failed: HTTP " + response.status() + " "
                        + abbreviate(response.body()));
            }

            return parse(response.body());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("ai chat interrupted");
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("ai chat failed: " + ex.getMessage(), ex);
        }
    }

    /**
     * Build the request json.
     *
     * @param command chat request
     * @return json text
     * @throws Exception serialize failure
     */
    private String body(AiChatCommand command) throws Exception {
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

        return OBJECT_MAPPER.writeValueAsString(root);
    }

    /**
     * Parse the reply json.
     *
     * @param body response body
     * @return chat result
     * @throws Exception parse failure
     */
    private AiChatResult parse(String body) throws Exception {
        JsonNode root = OBJECT_MAPPER.readTree(body);
        JsonNode choice = root.path("choices").path(0);
        String content = choice.path("message").path("content").asText(null);
        String finishReason = choice.path("finish_reason").asText(null);
        JsonNode usage = root.path("usage");

        return new AiChatResult(content, finishReason, intOrNull(usage, "prompt_tokens"),
                intOrNull(usage, "completion_tokens"), intOrNull(usage, "total_tokens"));
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
