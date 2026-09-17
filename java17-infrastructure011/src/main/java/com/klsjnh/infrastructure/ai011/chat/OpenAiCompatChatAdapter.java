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
 *
 */

import com.klsjnh.domain.ai011.AiChatCommand;
import com.klsjnh.domain.ai011.AiChatMessage;
import com.klsjnh.domain.ai011.AiChatPort;
import com.klsjnh.domain.ai011.AiChatResult;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * OpenAI-compatible chat adapter: {@code POST {baseUrl}/chat/completions} with
 * a bearer key, JDK HTTP client. Works for any OpenAI-compatible vendor
 * (longcat, deepseek, stepfun ...); a vendor with its own protocol gets its own
 * adapter behind the same port.
 */

@Component
public class OpenAiCompatChatAdapter implements AiChatPort {

    /**
     * Shared http client.
     */
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * JSON mapper.
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Request timeout in seconds.
     */
    private final long timeoutSeconds;

    /**
     * Create the adapter.
     *
     * @param timeoutSeconds request timeout in seconds
     */
    public OpenAiCompatChatAdapter(@Value("${krt.ai011.chat-timeout-seconds:60}") long timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
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

        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + (command.apiKey() == null ? "" : command.apiKey()))
                    .POST(HttpRequest.BodyPublishers.ofString(body(command)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new IllegalStateException("ai chat failed: HTTP " + response.statusCode() + " "
                        + abbreviate(response.body()));
            }

            return parse(response.body());
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
        ObjectNode root = objectMapper.createObjectNode();
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

        return objectMapper.writeValueAsString(root);
    }

    /**
     * Parse the reply json.
     *
     * @param body response body
     * @return chat result
     * @throws Exception parse failure
     */
    private AiChatResult parse(String body) throws Exception {
        JsonNode root = objectMapper.readTree(body);
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
