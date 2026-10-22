package com.klsjnh.infrastructure.aicenter.provider.openai;

/*                OpenAiCompatFileAdapterTest class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.22  file adapter request tests
 *
 */

import com.klsjnh.common.util.OutboundUrlGuard011;

import com.klsjnh.domain.aicenter.file.AiFileCommand;
import com.klsjnh.domain.aicenter.file.AiFileListCommand;
import com.klsjnh.domain.aicenter.file.AiFilePurpose;
import com.klsjnh.domain.aicenter.file.AiFileRef;
import com.klsjnh.domain.aicenter.file.AiFileUploadCommand;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Unit tests for {@link OpenAiCompatFileAdapter}.
 */

class OpenAiCompatFileAdapterTest {

    private HttpServer server;
    private AtomicReference<String> lastBody;
    private AtomicReference<String> lastMethod;
    private AtomicReference<String> lastPath;
    private String baseUrl;
    private OpenAiCompatFileAdapter adapter;

    @BeforeEach
    /**
     * Starts a local fake provider server.
     */
    void setUp() throws IOException {
        OutboundUrlGuard011.configure(new OutboundUrlGuard011.Policy(true, true, 10 * 1024 * 1024));
        lastBody = new AtomicReference<>();
        lastMethod = new AtomicReference<>();
        lastPath = new AtomicReference<>();

        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/files", this::handle);
        server.createContext("/v1/files/file_1", this::handle);
        server.createContext("/v1/files/file_1/content", this::handle);
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort() + "/v1";
        adapter = new OpenAiCompatFileAdapter();
    }

    @AfterEach
    /**
     * Stops the server and restores the outbound policy.
     */
    void tearDown() {
        server.stop(0);
        OutboundUrlGuard011.configure(OutboundUrlGuard011.Policy.defaults());
    }

    @Test
    /**
     * Uploads bytes as multipart.
     */
    void uploadsBytesAsMultipart() {
        AiFileRef ref = adapter.upload(new AiFileUploadCommand(baseUrl, "sk", "stepfun", "a.pdf",
                "pdf".getBytes(StandardCharsets.UTF_8), null, AiFilePurpose.TEXT_EXTRACT));

        assertEquals("file_1", ref.fileId());
        assertEquals("a.pdf", ref.fileName());
        assertEquals("file-extract", ref.purpose());
        assertTrue(lastBody.get().contains("name=\"file\""));
        assertTrue(lastBody.get().contains("file-extract"));
    }

    @Test
    /**
     * Uploads a remote URL as JSON.
     */
    void uploadsRemoteUrlAsJson() {
        AiFileRef ref = adapter.upload(new AiFileUploadCommand(baseUrl, "sk", "stepfun", null, null,
                "https://example.com/a.pdf", AiFilePurpose.STORAGE));

        assertEquals("file_1", ref.fileId());
        assertTrue(lastBody.get().contains("https://example.com/a.pdf"));
        assertTrue(lastBody.get().contains("storage"));
    }

    @Test
    /**
     * Gets file metadata.
     */
    void getsMetadata() {
        AiFileRef ref = adapter.get(new AiFileCommand(baseUrl, "sk", "stepfun", "file_1"));

        assertEquals("file_1", ref.fileId());
        assertEquals("GET", lastMethod.get());
    }

    @Test
    /**
     * Lists files with a purpose filter.
     */
    void listsFiles() {
        List<AiFileRef> refs = adapter.list(new AiFileListCommand(baseUrl, "sk", "stepfun",
                AiFilePurpose.TEXT_EXTRACT));

        assertEquals(1, refs.size());
        assertEquals("file_1", refs.get(0).fileId());
        assertTrue(lastPath.get().contains("purpose=file-extract"));
    }

    @Test
    /**
     * Deletes a file.
     */
    void deletesFile() {
        adapter.delete(new AiFileCommand(baseUrl, "sk", "stepfun", "file_1"));

        assertEquals("DELETE", lastMethod.get());
    }

    @Test
    /**
     * Downloads file content.
     */
    void downloadsContent() {
        byte[] bytes = adapter.content(new AiFileCommand(baseUrl, "sk", "stepfun", "file_1"));

        assertEquals("file-content", new String(bytes, StandardCharsets.UTF_8));
        assertTrue(lastPath.get().endsWith("/content"));
    }

    private void handle(HttpExchange exchange) throws IOException {
        lastMethod.set(exchange.getRequestMethod());
        lastPath.set(exchange.getRequestURI().toString());
        lastBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));

        String path = exchange.getRequestURI().getPath();

        if (path.endsWith("/content")) {
            respond(exchange, 200, "file-content".getBytes(StandardCharsets.UTF_8), "application/octet-stream");
        } else if ("DELETE".equals(exchange.getRequestMethod())) {
            respond(exchange, 200, "{}".getBytes(StandardCharsets.UTF_8), "application/json");
        } else if ("GET".equals(exchange.getRequestMethod()) && path.equals("/v1/files")) {
            respond(exchange, 200, ("{\"object\":\"list\",\"data\":[{\"id\":\"file_1\",\"filename\":\"a.pdf\","
                    + "\"bytes\":3,\"purpose\":\"file-extract\",\"status\":\"processed\"}]}")
                    .getBytes(StandardCharsets.UTF_8), "application/json");
        } else {
            respond(exchange, 200, ("{\"id\":\"file_1\",\"object\":\"file\",\"bytes\":3,\"filename\":\"a.pdf\","
                    + "\"purpose\":\"file-extract\",\"status\":\"processed\",\"created_at\":1}")
                    .getBytes(StandardCharsets.UTF_8), "application/json");
        }
    }

    /**
     * Writes one response.
     */
    private void respond(HttpExchange exchange, int status, byte[] body, String contentType) throws IOException {
        exchange.getResponseHeaders().add("Content-Type", contentType);
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }
}
