package com.klsjnh.infrastructure.messagecenter.channel;

/*                WebhookMessageChannel class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  built-in webhook message channel
 *
 */

import com.klsjnh.common.constant.MessageProviderTypes011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.messagecenter.channel.MessageChannelPort;
import com.klsjnh.domain.messagecenter.channel.MessageCommand;
import com.klsjnh.domain.messagecenter.channel.MessageResult;

import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Built-in generic webhook channel: HTTP POST a small JSON body to the channel
 * endpoint (from the channel config url). WeChat Work / Feishu / DingTalk can
 * use it directly today; vendor-specific signing is a future extension point.
 */

@Component
public class WebhookMessageChannel implements MessageChannelPort {

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
     * Channel code served by this port.
     *
     * @return webhook
     */
    @Override
    public String channelCode() {
        return MessageProviderTypes011.WEBHOOK;
    }

    /**
     * Send: POST a JSON body to the configured endpoint.
     *
     * @param command send command (endpoint required)
     * @return send result, never null
     */
    @Override
    public MessageResult send(MessageCommand command) {
        String endpoint = command == null || command.config() == null ? null : command.config().get("url");

        if (StringUtil011.isBlank(endpoint)) {
            return MessageResult.failure("webhook endpoint is not configured");
        }

        try {
            String body = toJson(command);
            HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint.trim()))
                    .timeout(REQUEST_TIMEOUT)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();

            if (status >= 200 && status < 300) {
                return MessageResult.success("HTTP " + status);
            }

            return MessageResult.failure("webhook returned HTTP " + status);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();

            return MessageResult.failure("webhook interrupted");
        } catch (Exception ex) {
            return MessageResult.failure(ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
    }

    /**
     * Build the JSON body {to,title,content} with minimal escaping.
     *
     * @param command send command
     * @return json body
     */
    private String toJson(MessageCommand command) {
        return "{\"to\":\"" + escape(command.to()) + "\",\"title\":\"" + escape(command.title())
                + "\",\"content\":\"" + escape(command.content()) + "\"}";
    }

    /**
     * Escape a value for a JSON string literal.
     *
     * @param value raw value, nullable
     * @return escaped value, never null
     */
    private String escape(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}
