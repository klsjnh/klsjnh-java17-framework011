package com.klsjnh.infrastructure.messagecenter.outbound.channel;

/*                WebhookMessageChannel class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate 2026.09.21
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  built-in webhook message channel
 *      2026.09.21  replace hand-written json escaping with jackson
 *
 */

import com.klsjnh.common.constant.MessageProviderTypes011;
import com.klsjnh.common.util.HttpResponse011;
import com.klsjnh.common.util.HttpUtil011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.messagecenter.outbound.channel.MessageChannelPort;
import com.klsjnh.domain.messagecenter.outbound.channel.MessageCommand;
import com.klsjnh.domain.messagecenter.outbound.channel.MessageResult;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Built-in generic webhook channel: HTTP POST a small JSON body to the channel
 * endpoint (from the channel config url). WeChat Work / Feishu / DingTalk can
 * use it directly today; vendor-specific signing is a future extension point.
 */

@Component
public class WebhookMessageChannel implements MessageChannelPort {

    /**
     * JSON mapper for the webhook request body (static: thread-safe for
     * serialization, isolated from the Spring-wide mapper customization).
     */
    private static final ObjectMapper WEBHOOK_MAPPER = new ObjectMapper();

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
            HttpResponse011 response = HttpUtil011.postJson(endpoint.trim(), toJson(command));

            if (response.isSuccess()) {
                return MessageResult.success("HTTP " + response.status());
            }

            return MessageResult.failure("webhook returned HTTP " + response.status());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();

            return MessageResult.failure("webhook interrupted");
        } catch (Exception ex) {
            return MessageResult.failure(ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
    }

    /**
     * Build the JSON body {to,title,content} via Jackson, so every control
     * character is escaped per RFC 8259. Package-private so the unit test can
     * call it directly without reflection.
     *
     * @param command send command
     * @return json body
     */
    String toJson(MessageCommand command) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("to", Objects.requireNonNullElse(command.to(), ""));
        body.put("title", Objects.requireNonNullElse(command.title(), ""));
        body.put("content", Objects.requireNonNullElse(command.content(), ""));

        try {
            return WEBHOOK_MAPPER.writeValueAsString(body);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("webhook body serialization failed", ex);
        }
    }
}
