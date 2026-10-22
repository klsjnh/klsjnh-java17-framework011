package com.klsjnh.infrastructure.aicenter.provider.openai;

/*                OpenAiCompatFileAdapter class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.22  generic OpenAI-compatible provider file adapter
 *
 */

import com.klsjnh.common.util.HttpResponse011;
import com.klsjnh.common.util.HttpResponseBytes011;
import com.klsjnh.common.util.HttpUtil011;

import com.klsjnh.domain.aicenter.file.AiFileCommand;
import com.klsjnh.domain.aicenter.file.AiFileListCommand;
import com.klsjnh.domain.aicenter.file.AiFilePort;
import com.klsjnh.domain.aicenter.file.AiFileRef;
import com.klsjnh.domain.aicenter.file.AiFileUploadCommand;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic OpenAI-compatible provider file adapter over {@code /files}:
 * multipart upload (or a remote URL), metadata get / list, delete and content
 * download. Bytes stay at the provider; the framework keeps metadata only.
 */

@Component
public class OpenAiCompatFileAdapter implements AiFilePort {

    /**
     * JSON mapper.
     */
    private static final ObjectMapper MAPPER = new ObjectMapper();

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
     * Generic fallback: a vendor-specific file port wins over this.
     *
     * @return always true
     */
    @Override
    public boolean generic() {
        return true;
    }

    /**
     * Upload one file (bytes or remote URL).
     *
     * @param command upload command
     * @return file metadata
     */
    @Override
    public AiFileRef upload(AiFileUploadCommand command) {
        String url = normalize(command.baseUrl()) + "/files";
        Map<String, String> headers = Map.of("Authorization", "Bearer " + command.apiKey());

        try {
            HttpResponse011 response;

            if (command.bytes() != null && command.bytes().length > 0) {
                Map<String, String> fields = new LinkedHashMap<>();
                fields.put("purpose", command.purpose().getCode());
                List<HttpUtil011.Part> parts = List.of(new HttpUtil011.Part("file",
                        command.fileName() == null || command.fileName().isBlank() ? "file" : command.fileName(),
                        "application/octet-stream", command.bytes()));
                response = HttpUtil011.postMultipart(url, headers, fields, parts, timeout());
            } else {
                ObjectNode body = MAPPER.createObjectNode();
                body.put("purpose", command.purpose().getCode());
                body.put("url", command.url());
                response = HttpUtil011.postJson(url, body.toString(), headers, timeout());
            }

            return ref(unwrap(response), command.providerCode());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("ai file upload interrupted");
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("ai file upload failed: " + ex.getClass().getSimpleName(), ex);
        }
    }

    /**
     * Get one file's metadata.
     *
     * @param command lookup command
     * @return file metadata
     */
    @Override
    public AiFileRef get(AiFileCommand command) {
        try {
            HttpResponse011 response = HttpUtil011.get(url(command), auth(command), timeout());

            return ref(unwrap(response), command.providerCode());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("ai file get interrupted");
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("ai file get failed: " + ex.getClass().getSimpleName(), ex);
        }
    }

    /**
     * List files, optionally filtered by purpose.
     *
     * @param command list command
     * @return file metadata list
     */
    @Override
    public List<AiFileRef> list(AiFileListCommand command) {
        String path = "/files";

        if (command.purpose() != null) {
            path = path + "?purpose=" + encode(command.purpose().getCode());
        }

        try {
            HttpResponse011 response = HttpUtil011.get(normalize(command.baseUrl()) + path,
                    Map.of("Authorization", "Bearer " + command.apiKey()), timeout());
            JsonNode data = unwrap(response);
            JsonNode items = data.isArray() ? data : data.path("data");
            List<AiFileRef> refs = new ArrayList<>();

            if (items.isArray()) {
                for (JsonNode item : items) {
                    refs.add(ref(item, command.providerCode()));
                }
            }

            return refs;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("ai file list interrupted");
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("ai file list failed: " + ex.getClass().getSimpleName(), ex);
        }
    }

    /**
     * Delete one file.
     *
     * @param command lookup command
     */
    @Override
    public void delete(AiFileCommand command) {
        try {
            HttpResponse011 response = HttpUtil011.delete(url(command), auth(command), timeout());

            if (!response.isSuccess()) {
                throw new IllegalStateException("ai file delete failed: " + HttpUtil011.describeStatus(response.status()));
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("ai file delete interrupted");
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("ai file delete failed: " + ex.getClass().getSimpleName(), ex);
        }
    }

    /**
     * Download one file's bytes.
     *
     * @param command lookup command
     * @return bytes
     */
    @Override
    public byte[] content(AiFileCommand command) {
        try {
            HttpResponseBytes011 response = HttpUtil011.getBytes(url(command) + "/content", auth(command),
                    Duration.ofSeconds(60));

            if (!response.isSuccess()) {
                throw new IllegalStateException("ai file content failed: "
                        + HttpUtil011.describeStatus(response.status()));
            }

            return response.body();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("ai file content interrupted");
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("ai file content failed: " + ex.getClass().getSimpleName(), ex);
        }
    }

    /**
     * Unwrap the provider envelope: {@code data} when it carries the object,
     * otherwise the root.
     *
     * @param response http response
     * @return payload node
     */
    private JsonNode unwrap(HttpResponse011 response) throws Exception {
        if (!response.isSuccess()) {
            throw new IllegalStateException("ai file failed: " + HttpUtil011.describeStatus(response.status()));
        }

        JsonNode root = MAPPER.readTree(response.body());
        JsonNode data = root.path("data");

        if (data.isObject() && (data.has("id") || data.has("data"))) {
            return data;
        }

        return root;
    }

    /**
     * Map one provider file object.
     *
     * @param node         provider node
     * @param providerCode provider code
     * @return file metadata
     */
    private AiFileRef ref(JsonNode node, String providerCode) {
        Long createdAt = node.path("created_at").isNumber() ? node.path("created_at").asLong() : null;

        return new AiFileRef(providerCode, node.path("id").asText(null), node.path("filename").asText(null),
                node.path("bytes").asLong(0), node.path("purpose").asText(null), node.path("status").asText(null),
                createdAt);
    }

    /**
     * File resource url.
     *
     * @param command lookup command
     * @return url
     */
    private String url(AiFileCommand command) {
        return normalize(command.baseUrl()) + "/files/" + encode(command.fileId());
    }

    /**
     * Bearer headers.
     *
     * @param command lookup command
     * @return headers
     */
    private Map<String, String> auth(AiFileCommand command) {
        return Map.of("Authorization", "Bearer " + command.apiKey());
    }

    /**
     * Request timeout.
     *
     * @return timeout
     */
    private Duration timeout() {
        return Duration.ofSeconds(60);
    }

    /**
     * Normalize the base url.
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

    /**
     * Encode one path segment.
     *
     * @param value value
     * @return encoded value
     */
    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
