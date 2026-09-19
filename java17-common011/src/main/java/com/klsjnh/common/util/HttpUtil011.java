package com.klsjnh.common.util;

/*                HttpUtil011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  jdk http client helper
 *
 */

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

/**
 * Thin HTTP helper over the JDK {@link HttpClient} (no extra dependency): the
 * single home for the plain GET / POST-JSON calls used by infrastructure
 * adapters (AI probe, webhook channel, vendor channel plug-ins). It carries no
 * vendor semantics — callers read {@link HttpResponse011#status()} and
 * {@link HttpResponse011#body()}.
 *
 * <p>Timeout and client are shared statics; transport failures surface as
 * {@link IOException}, an interrupted call re-sets the interrupt flag and throws
 * {@link InterruptedException}.</p>
 */

public final class HttpUtil011 {

    /**
     * Connect timeout.
     */
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);

    /**
     * Request (read) timeout.
     */
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    /**
     * Shared http client.
     */
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            .build();

    /**
     * Utility holder, no instances.
     */
    private HttpUtil011() {
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
        return send(HttpRequest.newBuilder(URI.create(url)).timeout(REQUEST_TIMEOUT).GET().build());
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
        return send(builder(url, headers).GET().build());
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
        HttpRequest.Builder builder = builder(url, headers)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json == null ? "" : json, StandardCharsets.UTF_8));

        return send(builder.build());
    }

    /**
     * Build a request builder with the shared timeout and optional headers.
     *
     * @param url     request url
     * @param headers extra headers, nullable
     * @return request builder
     */
    private static HttpRequest.Builder builder(String url, Map<String, String> headers) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url)).timeout(REQUEST_TIMEOUT);

        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.header(entry.getKey(), entry.getValue());
            }
        }

        return builder;
    }

    /**
     * Send a request and wrap the response.
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
}
