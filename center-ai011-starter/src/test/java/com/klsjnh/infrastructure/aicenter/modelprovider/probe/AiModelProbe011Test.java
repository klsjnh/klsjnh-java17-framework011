package com.klsjnh.infrastructure.aicenter.modelprovider.probe;

/*                AiModelProbe011Test class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.22  separated connectivity / chat probe tests
 *
 */

import com.klsjnh.common.util.OutboundUrlGuard011;

import com.klsjnh.domain.aicenter.modelprovider.AiModelProbePort;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Unit tests for {@link AiModelProbe011}: the chat probe must use
 * {@code /chat/completions}, a one-token prompt and a safe message.
 */

class AiModelProbe011Test {

    /**
     * JSON mapper.
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Local server.
     */
    private HttpServer server;

    /**
     * Captured request body.
     */
    private AtomicReference<String> requestBody;

    /**
     * Response status.
     */
    private int responseStatus;

    /**
     * Response body.
     */
    private String responseBody;

    /**
     * Probe under test.
     */
    private AiModelProbe011 probe;

    /**
     * Base url.
     */
    private String baseUrl;

    @BeforeEach
    void setUp() throws IOException {
        OutboundUrlGuard011.configure(new OutboundUrlGuard011.Policy(true, true, 10 * 1024 * 1024));
        requestBody = new AtomicReference<>();
        responseStatus = 200;
        responseBody = "{\"choices\":[{\"message\":{\"content\":\"p\"}}]}";

        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", this::handle);
        server.createContext("/v1/models", this::handle);
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort() + "/v1";

        probe = new AiModelProbe011();
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
     * Succeeds when the reply carries choices.
     */
    void chatProbeSucceedsWithChoices() throws Exception {
        AiModelProbePort.ProbeResult result = probe.probeChat(baseUrl, "sk-secret", "m1");

        assertTrue(result.success());
        assertEquals(200, result.httpStatus());

        JsonNode body = MAPPER.readTree(requestBody.get());
        assertEquals("m1", body.path("model").asText());
        assertEquals(1, body.path("max_tokens").asInt());
        assertFalse(body.path("stream").asBoolean());
        assertEquals("user", body.path("messages").path(0).path("role").asText());
        assertFalse(requestBody.get().contains("sk-secret"));
    }

    @Test
    /**
     * Reports an auth failure safely.
     */
    void chatProbeReportsAuthFailure() {
        responseStatus = 401;
        responseBody = "{}";

        AiModelProbePort.ProbeResult result = probe.probeChat(baseUrl, "sk-secret", "m1");

        assertFalse(result.success());
        assertEquals("HTTP 401 (authentication failed)", result.message());
    }

    @Test
    /**
     * Rejects a reply without choices.
     */
    void chatProbeRejectsResponseWithoutChoices() {
        responseBody = "{\"choices\":[]}";

        AiModelProbePort.ProbeResult result = probe.probeChat(baseUrl, "sk-secret", "m1");

        assertFalse(result.success());
        assertEquals("chat response has no choices", result.message());
    }

    /**
     * Handle one request.
     *
     * @param exchange exchange
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
