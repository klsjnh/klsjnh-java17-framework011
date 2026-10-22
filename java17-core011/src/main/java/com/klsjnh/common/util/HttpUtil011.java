package com.klsjnh.common.util;

/*                HttpUtil011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  jdk http client helper
 *      2026.09.19  add binary GET and multipart upload
 *      2026.09.26  outbound url guard + response size cap + no redirects
 *      2026.10.22  shared upstream status classification (auth / 413 / 429 / 5xx)
 *      2026.10.22  DELETE for provider resource cleanup
 *
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Thin HTTP helper over the JDK {@link HttpClient} (no extra dependency): the
 * single home for the plain GET / GET-bytes / POST-JSON / POST-multipart calls
 * used by infrastructure adapters (AI probe, webhook channel, vendor channel
 * plug-ins). It carries no vendor semantics — callers read the response status
 * and body; token / auth go through {@code headers}, never into this utility.
 *
 * <p>Timeout and client are shared statics; transport failures surface as
 * {@link IOException}, an interrupted call re-sets the interrupt flag and throws
 * {@link InterruptedException}. Download / upload use a longer media timeout by
 * default. Every request URL is checked by {@link OutboundUrlGuard011}; the client
 * never follows redirects; byte downloads are capped by the active outbound
 * policy.</p>
 */

public final class HttpUtil011 {

    /**
     * Connect timeout.
     */
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);

    /**
     * Default request (read) timeout.
     */
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    /**
     * Default media (download / upload) read timeout.
     */
    private static final Duration MEDIA_TIMEOUT = Duration.ofSeconds(30);

    /**
     * Upstream status: missing / invalid credentials.
     */
    public static final int STATUS_UNAUTHORIZED = 401;

    /**
     * Upstream status: authenticated but not allowed.
     */
    public static final int STATUS_FORBIDDEN = 403;

    /**
     * Upstream status: request body over the provider limit.
     */
    public static final int STATUS_PAYLOAD_TOO_LARGE = 413;

    /**
     * Upstream status: throttled.
     */
    public static final int STATUS_TOO_MANY_REQUESTS = 429;

    /**
     * First upstream 5xx status.
     */
    public static final int STATUS_SERVER_ERROR = 500;

    /**
     * Hint: authentication failed.
     */
    private static final String HINT_AUTH = "authentication failed";

    /**
     * Hint: payload too large.
     */
    private static final String HINT_TOO_LARGE = "request payload too large";

    /**
     * Hint: throttled.
     */
    private static final String HINT_RATE_LIMITED = "rate limited";

    /**
     * Hint: upstream 5xx.
     */
    private static final String HINT_UPSTREAM = "upstream error";

    /**
     * Hint: any other non-2xx.
     */
    private static final String HINT_REJECTED = "request rejected";

    /**
     * Shared http client (never follow redirects — blocks open-redirect SSRF).
     */
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            .followRedirects(HttpClient.Redirect.NEVER)
            .build();

    /**
     * Utility holder, no instances.
     */
    private HttpUtil011() {
    }

    /**
     * Classify a non-2xx upstream status into a safe hint. Shared by every
     * outbound adapter so auth / size / throttling / 5xx failures read the same
     * way; never echoes headers or the request body.
     *
     * @param status upstream http status
     * @return hint, never null
     */
    public static String failureHint(int status) {
        if (status == STATUS_UNAUTHORIZED || status == STATUS_FORBIDDEN) {
            return HINT_AUTH;
        }

        if (status == STATUS_PAYLOAD_TOO_LARGE) {
            return HINT_TOO_LARGE;
        }

        if (status == STATUS_TOO_MANY_REQUESTS) {
            return HINT_RATE_LIMITED;
        }

        if (status >= STATUS_SERVER_ERROR) {
            return HINT_UPSTREAM;
        }

        return HINT_REJECTED;
    }

    /**
     * Describe a non-2xx upstream status as {@code HTTP <status> (<hint>)}.
     *
     * @param status upstream http status
     * @return description, never null
     */
    public static String describeStatus(int status) {
        return "HTTP " + status + " (" + failureHint(status) + ")";
    }

    /**
     * HTTP GET returning status and body as text.
     *
     * @param url request url
     * @return response
     * @throws IOException          on transport failure
     * @throws InterruptedException when the call is interrupted
     */
    public static HttpResponse011 get(String url) throws IOException, InterruptedException {
        return get(url, null);
    }

    /**
     * HTTP GET with extra headers.
     *
     * @param url     request url
     * @param headers extra headers, nullable
     * @return response
     * @throws IOException          on transport failure
     * @throws InterruptedException when the call is interrupted
     */
    public static HttpResponse011 get(String url, Map<String, String> headers) throws IOException, InterruptedException {
        return send(builder(url, headers, REQUEST_TIMEOUT).GET().build());
    }

    /**
     * HTTP GET with extra headers and an explicit timeout.
     *
     * @param url     request url
     * @param headers extra headers, nullable
     * @param timeout read timeout
     * @return response
     * @throws IOException          on transport failure
     * @throws InterruptedException when the call is interrupted
     */
    public static HttpResponse011 get(String url, Map<String, String> headers, Duration timeout)
            throws IOException, InterruptedException {
        return send(builder(url, headers, timeout).GET().build());
    }

    /**
     * HTTP GET returning status and body as bytes (media download).
     *
     * @param url request url
     * @return bytes response
     * @throws IOException          on transport failure
     * @throws InterruptedException when the call is interrupted
     */
    public static HttpResponseBytes011 getBytes(String url) throws IOException, InterruptedException {
        return getBytes(url, null, MEDIA_TIMEOUT);
    }

    /**
     * HTTP GET returning status and body as bytes, with extra headers.
     *
     * @param url     request url
     * @param headers extra headers, nullable
     * @return bytes response
     * @throws IOException          on transport failure
     * @throws InterruptedException when the call is interrupted
     */
    public static HttpResponseBytes011 getBytes(String url, Map<String, String> headers)
            throws IOException, InterruptedException {
        return getBytes(url, headers, MEDIA_TIMEOUT);
    }

    /**
     * HTTP GET returning status and body as bytes, with extra headers and an
     * explicit timeout.
     *
     * @param url     request url
     * @param headers extra headers, nullable
     * @param timeout read timeout
     * @return bytes response
     * @throws IOException          on transport failure
     * @throws InterruptedException when the call is interrupted
     */
    public static HttpResponseBytes011 getBytes(String url, Map<String, String> headers, Duration timeout)
            throws IOException, InterruptedException {
        HttpResponse<InputStream> response = HTTP_CLIENT.send(builder(url, headers, timeout).GET().build(),
                HttpResponse.BodyHandlers.ofInputStream());

        try (InputStream body = response.body()) {
            return new HttpResponseBytes011(response.statusCode(), readLimited(body));
        }
    }

    /**
     * HTTP POST a JSON body returning status and body as text.
     *
     * @param url  request url
     * @param json JSON request body
     * @return response
     * @throws IOException          on transport failure
     * @throws InterruptedException when the call is interrupted
     */
    public static HttpResponse011 postJson(String url, String json) throws IOException, InterruptedException {
        return postJson(url, json, null);
    }

    /**
     * HTTP POST a JSON body with extra headers.
     *
     * @param url     request url
     * @param json    JSON request body
     * @param headers extra headers, nullable
     * @return response
     * @throws IOException          on transport failure
     * @throws InterruptedException when the call is interrupted
     */
    public static HttpResponse011 postJson(String url, String json, Map<String, String> headers)
            throws IOException, InterruptedException {
        return postJson(url, json, headers, REQUEST_TIMEOUT);
    }

    /**
     * HTTP POST a JSON body with extra headers and an explicit timeout.
     *
     * @param url     request url
     * @param json    JSON request body
     * @param headers extra headers, nullable
     * @param timeout read timeout
     * @return response
     * @throws IOException          on transport failure
     * @throws InterruptedException when the call is interrupted
     */
    public static HttpResponse011 postJson(String url, String json, Map<String, String> headers, Duration timeout)
            throws IOException, InterruptedException {
        HttpRequest.Builder builder = builder(url, headers, timeout)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json == null ? "" : json, StandardCharsets.UTF_8));

        return send(builder.build());
    }

    /**
     * HTTP DELETE with extra headers.
     *
     * @param url     request url
     * @param headers extra headers, nullable
     * @param timeout read timeout
     * @return response
     * @throws IOException          on transport failure
     * @throws InterruptedException when the call is interrupted
     */
    public static HttpResponse011 delete(String url, Map<String, String> headers, Duration timeout)
            throws IOException, InterruptedException {
        return send(builder(url, headers, timeout).DELETE().build());
    }

    /**
     * HTTP POST a JSON body returning status and body as bytes (binary media
     * responses such as synthesized audio), with the default media timeout.
     *
     * @param url     request url
     * @param json    JSON request body
     * @param headers extra headers, nullable
     * @return bytes response
     * @throws IOException          on transport failure
     * @throws InterruptedException when the call is interrupted
     */
    public static HttpResponseBytes011 postJsonBytes(String url, String json, Map<String, String> headers)
            throws IOException, InterruptedException {
        HttpRequest.Builder builder = builder(url, headers, MEDIA_TIMEOUT)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json == null ? "" : json, StandardCharsets.UTF_8));

        HttpResponse<InputStream> response = HTTP_CLIENT.send(builder.build(), HttpResponse.BodyHandlers.ofInputStream());

        try (InputStream body = response.body()) {
            return new HttpResponseBytes011(response.statusCode(), readLimited(body));
        }
    }

    /**
     * HTTP POST a JSON body and hand back the response as a stream response
     * (status + open body) — the streaming entry for SSE / chunked responses.
     * The caller MUST check {@link HttpResponseStream011#isSuccess()} and MUST
     * close the body stream.
     *
     * @param url     request url
     * @param json    JSON request body
     * @param headers extra headers, nullable
     * @param timeout read timeout
     * @return stream response
     * @throws IOException          on transport failure
     * @throws InterruptedException when the call is interrupted
     */
    public static HttpResponseStream011 postJsonStream(String url, String json, Map<String, String> headers,
            Duration timeout) throws IOException, InterruptedException {
        HttpRequest.Builder builder = builder(url, headers, timeout)
                .header("Content-Type", "application/json")
                .header("Accept", "text/event-stream")
                .POST(HttpRequest.BodyPublishers.ofString(json == null ? "" : json, StandardCharsets.UTF_8));

        HttpResponse<InputStream> response = HTTP_CLIENT.send(builder.build(),
                HttpResponse.BodyHandlers.ofInputStream());

        return new HttpResponseStream011(response.statusCode(), response.body());
    }

    /**
     * HTTP POST a multipart/form-data body (text fields + file parts) with the
     * default media timeout. The boundary and the {@code Content-Type} header are
     * generated here; callers must NOT pass a Content-Type of their own.
     *
     * @param url     request url
     * @param headers extra headers (e.g. Authorization), nullable
     * @param fields  text form fields, nullable
     * @param files   file parts, nullable for no file
     * @return response
     * @throws IOException          on transport failure
     * @throws InterruptedException when the call is interrupted
     */
    public static HttpResponse011 postMultipart(String url, Map<String, String> headers, Map<String, String> fields,
            List<Part> files) throws IOException, InterruptedException {
        return postMultipart(url, headers, fields, files, MEDIA_TIMEOUT);
    }

    /**
     * HTTP POST a multipart/form-data body with an explicit timeout.
     *
     * @param url     request url
     * @param headers extra headers, nullable
     * @param fields  text form fields, nullable
     * @param files   file parts, nullable for no file
     * @param timeout read timeout
     * @return response
     * @throws IOException          on transport failure
     * @throws InterruptedException when the call is interrupted
     */
    public static HttpResponse011 postMultipart(String url, Map<String, String> headers, Map<String, String> fields,
            List<Part> files, Duration timeout) throws IOException, InterruptedException {
        String boundary = "----klsjnh011" + System.nanoTime();
        byte[] body = multipartBody(boundary, fields, files);

        HttpRequest.Builder builder = builder(url, headers, timeout)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body));

        return send(builder.build());
    }

    /**
     * Build the multipart/form-data body bytes.
     *
     * @param boundary multipart boundary
     * @param fields   text fields, nullable
     * @param files    file parts, nullable
     * @return body bytes
     * @throws IOException on write failure
     */
    private static byte[] multipartBody(String boundary, Map<String, String> fields, List<Part> files)
            throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        if (fields != null) {
            for (Map.Entry<String, String> entry : fields.entrySet()) {
                write(out, "--" + boundary + "\r\n");
                write(out, "Content-Disposition: form-data; name=\"" + entry.getKey() + "\"\r\n\r\n");
                write(out, entry.getValue() == null ? "" : entry.getValue());
                write(out, "\r\n");
            }
        }

        if (files != null) {
            for (Part part : files) {
                write(out, "--" + boundary + "\r\n");
                write(out, "Content-Disposition: form-data; name=\"" + part.fieldName()
                        + "\"; filename=\"" + asciiFallback(part.fileName()) + "\"; filename*=UTF-8''"
                        + encode(part.fileName()) + "\r\n");
                write(out, "Content-Type: " + safeType(part.contentType()) + "\r\n\r\n");
                out.write(part.bytes());
                write(out, "\r\n");
            }
        }

        write(out, "--" + boundary + "--\r\n");

        return out.toByteArray();
    }

    /**
     * Build a request builder with an explicit timeout and optional headers.
     *
     * @param url     request url
     * @param headers extra headers, nullable
     * @param timeout read timeout
     * @return request builder
     */
    private static HttpRequest.Builder builder(String url, Map<String, String> headers, Duration timeout) {
        OutboundUrlGuard011.assertSafe(url);
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url)).timeout(timeout);

        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.header(entry.getKey(), entry.getValue());
            }
        }

        return builder;
    }

    /**
     * Read at most the configured outbound max response bytes.
     *
     * @param in response body stream
     * @return body bytes
     * @throws IOException when the body exceeds the cap or read fails
     */
    private static byte[] readLimited(InputStream in) throws IOException {
        int max = OutboundUrlGuard011.policy().maxResponseBytes();
        if (max <= 0) {
            max = OutboundUrlGuard011.Policy.defaults().maxResponseBytes();
        }

        byte[] chunk = in.readNBytes(max + 1);
        if (chunk.length > max) {
            throw new IOException("outbound response exceeds max bytes: " + max);
        }

        return chunk;
    }

    /**
     * Send a request and wrap the text response.
     *
     * @param request request
     * @return response
     * @throws IOException          on transport failure
     * @throws InterruptedException when the call is interrupted
     */
    private static HttpResponse011 send(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        return new HttpResponse011(response.statusCode(), response.body());
    }

    /**
     * Write a UTF-8 string to a byte stream.
     *
     * @param out   target stream
     * @param value text
     * @throws IOException on write failure
     */
    private static void write(ByteArrayOutputStream out, String value) throws IOException {
        out.write(value.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Percent-encode a file name for the RFC 5987 {@code filename*} parameter.
     *
     * @param fileName raw file name
     * @return encoded file name
     */
    private static String encode(String fileName) {
        return URLEncoder.encode(fileName == null ? "" : fileName, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /**
     * ASCII fallback file name; non-ASCII characters become underscores so the
     * legacy {@code filename} parameter stays header-safe.
     *
     * @param fileName raw file name
     * @return ascii-safe file name
     */
    private static String asciiFallback(String fileName) {
        if (fileName == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder(fileName.length());

        for (int i = 0; i < fileName.length(); i++) {
            char c = fileName.charAt(i);
            sb.append(c < 0x80 ? c : '_');
        }

        return sb.toString();
    }

    /**
     * Content type with a default when blank.
     *
     * @param contentType raw content type, nullable
     * @return content type, never blank
     */
    private static String safeType(String contentType) {
        return contentType == null || contentType.isBlank() ? "application/octet-stream" : contentType;
    }

    /**
     * One multipart file part.
     *
     * @param fieldName   form field name (image / file / media)
     * @param fileName    file name
     * @param contentType file content type
     * @param bytes       file bytes
     */
    public record Part(String fieldName, String fileName, String contentType, byte[] bytes) {
    }
}
