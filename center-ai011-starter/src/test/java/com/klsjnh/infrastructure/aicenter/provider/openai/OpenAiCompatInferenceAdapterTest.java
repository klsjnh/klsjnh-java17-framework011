package com.klsjnh.infrastructure.aicenter.provider.openai;

/*                OpenAiCompatInferenceAdapterTest class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.22  request body / error / sse regression tests
 *
 */

import com.klsjnh.common.util.OutboundUrlGuard011;

import com.klsjnh.domain.aicenter.inference.AiChatContentPart;
import com.klsjnh.domain.aicenter.inference.AiChatMessage;
import com.klsjnh.domain.aicenter.inference.AiInferenceChunk;
import com.klsjnh.domain.aicenter.inference.AiInferenceCommand;
import com.klsjnh.domain.aicenter.inference.AiInferenceOptions;
import com.klsjnh.domain.aicenter.inference.AiInferenceResult;
import com.klsjnh.domain.aicenter.inference.AiReasoningEffort;

import com.klsjnh.infrastructure.aicenter.config.KrtAiConfig011;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Unit tests for {@link OpenAiCompatInferenceAdapter}: wire format, options,
 * error mapping and SSE parsing against a local JDK http server (no network).
 */

class OpenAiCompatInferenceAdapterTest {

    /**
     * JSON mapper.
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Small valid png data url.
     */
    private static final String PNG = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8"
            + "z8DwHwAFAAH/q842iQAAAABJRU5ErkJggg==";

    /**
     * Local server.
     */
    private HttpServer server;

    /**
     * Captured request body.
     */
    private AtomicReference<String> requestBody;

    /**
     * Response status to send.
     */
    private int responseStatus;

    /**
     * Response body to send.
     */
    private String responseBody;

    /**
     * Adapter under test.
     */
    private OpenAiCompatInferenceAdapter adapter;

    /**
     * Base url of the local server.
     */
    private String baseUrl;

    @BeforeEach
    void setUp() throws IOException {
        OutboundUrlGuard011.configure(new OutboundUrlGuard011.Policy(true, true, 10 * 1024 * 1024));
        requestBody = new AtomicReference<>();
        responseStatus = 200;
        responseBody = "{\"choices\":[{\"message\":{\"content\":\"ok\"},\"finish_reason\":\"stop\"}],"
                + "\"usage\":{\"prompt_tokens\":1,\"completion_tokens\":2,\"total_tokens\":3}}";

        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", this::handle);
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort() + "/v1";

        KrtAiConfig011 config = new KrtAiConfig011();
        config.setChatTimeoutSeconds(5);
        adapter = new OpenAiCompatInferenceAdapter(config);
    }

    @AfterEach
    /**
     * Restore the default outbound policy.
     */
    void tearDown() {
        server.stop(0);
        OutboundUrlGuard011.configure(OutboundUrlGuard011.Policy.defaults());
    }

    @Test
    /**
     * Keeps the text-only body compatible.
     */
    void keepsTextOnlyRequestCompatible() throws Exception {
        AiInferenceResult result = adapter.chat(command(new AiChatMessage("user", "hello"), null));

        JsonNode body = MAPPER.readTree(requestBody.get());
        assertEquals("m1", body.path("model").asText());
        assertTrue(body.path("messages").path(0).path("content").isTextual());
        assertEquals("hello", body.path("messages").path(0).path("content").asText());
        assertFalse(body.has("response_format"));
        assertFalse(body.has("reasoning_effort"));
        assertEquals("ok", result.content());
        assertEquals(3, result.totalTokens());
    }

    @Test
    /**
     * Serializes content parts as an array.
     */
    void serializesMultimodalContentParts() throws Exception {
        AiChatMessage message = new AiChatMessage("user", null,
                List.of(AiChatContentPart.text("look"), AiChatContentPart.imageUrl(PNG)));

        adapter.chat(command(message, null));

        JsonNode content = MAPPER.readTree(requestBody.get()).path("messages").path(0).path("content");
        assertTrue(content.isArray());
        assertEquals(2, content.size());
        assertEquals("text", content.path(0).path("type").asText());
        assertEquals("look", content.path(0).path("text").asText());
        assertEquals("image_url", content.path(1).path("type").asText());
        assertEquals(PNG, content.path(1).path("image_url").path("url").asText());
    }

    @Test
    /**
     * Sends response_format and reasoning_effort when requested.
     */
    void sendsStructuredOutputAndReasoningWhenRequested() throws Exception {
        AiInferenceOptions options = new AiInferenceOptions(true, AiReasoningEffort.NONE);

        adapter.chat(command(new AiChatMessage("user", "hello"), options));

        JsonNode body = MAPPER.readTree(requestBody.get());
        assertEquals("json_object", body.path("response_format").path("type").asText());
        assertEquals("none", body.path("reasoning_effort").asText());
    }

    @Test
    /**
     * Maps 401 without leaking the key.
     */
    void mapsAuthFailureWithoutLeakingKey() {
        responseStatus = 401;
        responseBody = "{\"error\":\"invalid api key\"}";

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> adapter.chat(command(new AiChatMessage("user", "hello"), null)));

        assertTrue(ex.getMessage().contains("HTTP 401 (authentication failed)"));
        assertFalse(ex.getMessage().contains("sk-secret"));
    }

    @Test
    /**
     * Maps 413 to a payload-size failure.
     */
    void mapsOversizedPayload() {
        responseStatus = 413;
        responseBody = "{\"error\":\"too large\"}";

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> adapter.chat(command(new AiChatMessage("user", "hello"), null)));

        assertTrue(ex.getMessage().contains("HTTP 413 (request payload too large)"));
    }

    @Test
    /**
     * Parses SSE chunks and stops at [DONE].
     */
    void parsesSseChunksAndStopsAtDone() {
        responseBody = "data: {\"choices\":[{\"delta\":{\"content\":\"a\"}}]}\n\n"
                + "data: {\"choices\":[{\"delta\":{\"content\":\"b\"},\"finish_reason\":\"stop\"}]}\n\n"
                + "data: [DONE]\n\n";

        List<AiInferenceChunk> chunks = adapter.stream(command(new AiChatMessage("user", "hello"), null))
                .collect(Collectors.toList());

        assertEquals(2, chunks.size());
        assertEquals("a", chunks.get(0).content());
        assertEquals("b", chunks.get(1).content());
        assertEquals("stop", chunks.get(1).finishReason());
    }

    /**
     * Build a command against the local server.
     *
     * @param message chat message
     * @param options optional controls, nullable
     * @return command
     */
    private AiInferenceCommand command(AiChatMessage message, AiInferenceOptions options) {
        return new AiInferenceCommand(baseUrl, "sk-secret", "m1", List.of(message), 0.1, 128, options);
    }

    /**
     * Handle one local request.
     *
     * @param exchange http exchange
     * @throws IOException write failure
     */
    private void handle(HttpExchange exchange) throws IOException {
        requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));

        byte[] bytes = responseBody.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(responseStatus, bytes.length);

        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}
