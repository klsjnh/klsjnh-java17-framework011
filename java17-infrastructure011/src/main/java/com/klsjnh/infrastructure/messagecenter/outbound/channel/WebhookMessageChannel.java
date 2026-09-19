package com.klsjnh.infrastructure.messagecenter.outbound.channel;

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
import com.klsjnh.common.util.HttpResponse011;
import com.klsjnh.common.util.HttpUtil011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.messagecenter.outbound.channel.MessageChannelPort;
import com.klsjnh.domain.messagecenter.outbound.channel.MessageCommand;
import com.klsjnh.domain.messagecenter.outbound.channel.MessageResult;

import org.springframework.stereotype.Component;

/**
 * Built-in generic webhook channel: HTTP POST a small JSON body to the channel
 * endpoint (from the channel config url). WeChat Work / Feishu / DingTalk can
 * use it directly today; vendor-specific signing is a future extension point.
 */

@Component
public class WebhookMessageChannel implements MessageChannelPort {

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
