package com.klsjnh.infrastructure.ai011.probe;

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
 *
 */

import com.klsjnh.domain.ai011.AiModelProbePort;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Connectivity probe over the JDK HTTP client: GET {@code {baseUrl}/models}
 * with {@code Authorization: Bearer <apiKey>}. Only the status line is read;
 * the key never appears in any message.
 */

@Component
public class AiModelProbe011 implements AiModelProbePort {

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
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(CONNECT_TIMEOUT)
            .build();

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
            HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                    .timeout(REQUEST_TIMEOUT)
                    .header("Authorization", "Bearer " + (apiKey == null ? "" : apiKey))
                    .GET()
                    .build();

            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            int status = response.statusCode();

            return new ProbeResult(status >= 200 && status < 300, "HTTP " + status, status);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();

            return new ProbeResult(false, "probe interrupted", null);
        } catch (Exception ex) {
            return new ProbeResult(false, ex.getClass().getSimpleName() + ": " + ex.getMessage(), null);
        }
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
