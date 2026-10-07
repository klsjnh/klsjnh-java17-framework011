package com.klsjnh.infrastructure.messagecenter.inbound.channel;

/*                WebhookInboundChannel class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.05
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.05  built-in generic webhook inbound channel (HMAC-SHA256)
 *
 */

import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.messagecenter.inbound.channel.InboundMessage;
import com.klsjnh.domain.messagecenter.inbound.channel.InboundReply;
import com.klsjnh.domain.messagecenter.inbound.channel.MessageInboundPort;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Built-in generic webhook inbound channel: verifies an HMAC-SHA256 signature
 * over the raw body (fail-closed — without a configured secret every request is
 * rejected) and parses a flat JSON body into the neutral inbound event.
 * <p>
 * Channel config keys: {@code secret} (HMAC key, REQUIRED),
 * {@code signatureHeader} (default {@code X-Signature}, hex digest),
 * {@code channelCode} (stamped on the event, optional). Body shape:
 * {@code {"fromId":…, "messageType":…, "content":…, "rawMessageId":…,
 * "payload":{…}}} — every field optional. A missing / mismatched signature is
 * a 400, never a silently accepted message.
 * </p>
 */

@Component
public class WebhookInboundChannel implements MessageInboundPort {

    /**
     * Default signature header name.
     */
    public static final String DEFAULT_SIGNATURE_HEADER = "X-Signature";

    /**
     * Config key: HMAC key.
     */
    public static final String CONFIG_SECRET = "secret";

    /**
     * Config key: signature header name override.
     */
    public static final String CONFIG_SIGNATURE_HEADER = "signatureHeader";

    /**
     * JSON mapper for the vendor body (static: thread-safe).
     */
    private static final ObjectMapper BODY_MAPPER = new ObjectMapper();

    /**
     * The channel code this port serves.
     *
     * @return webhook
     */
    @Override
    public String channelCode() {
        return "webhook";
    }

    /**
     * Parse with headers: verify the HMAC-SHA256 signature, then map the JSON
     * body.
     *
     * @param config  whole channel config, nullable
     * @param headers request headers (first value per name), never null
     * @param rawBody raw request body, nullable
     * @return inbound event, never null
     */
    @Override
    public InboundMessage parse(Map<String, String> config, Map<String, String> headers, String rawBody) {
        Map<String, String> safeConfig = config == null ? Map.of() : config;
        String secret = safeConfig.get(CONFIG_SECRET);

        if (StringUtil011.isBlank(secret)) {
            throw BusinessException.badRequest("webhook inbound: secret is not configured");
        }

        String headerName = safeConfig.getOrDefault(CONFIG_SIGNATURE_HEADER, DEFAULT_SIGNATURE_HEADER);
        String signature = header(headers, headerName);

        if (StringUtil011.isBlank(signature)) {
            throw BusinessException.badRequest("webhook inbound: missing signature header " + headerName);
        }

        String expected = hmacSha256Hex(secret, rawBody == null ? "" : rawBody);

        if (!constantTimeEquals(expected, signature.trim().toLowerCase(Locale.ROOT))) {
            throw BusinessException.badRequest("webhook inbound: signature mismatch");
        }

        return toEvent(safeConfig, rawBody);
    }

    /**
     * Two-arg compatibility entry (no headers → no signature to check): the
     * built-in channel requires the signature, so anonymous parsing is refused.
     *
     * @param config  whole channel config, nullable
     * @param rawBody raw request body, nullable
     * @return inbound event, never null
     */
    @Override
    public InboundMessage parse(Map<String, String> config, String rawBody) {
        throw BusinessException.badRequest("webhook inbound: signature header required (parse with headers)");
    }

    /**
     * Encode the reply as a flat JSON object.
     *
     * @param reply reply to encode, nullable
     * @return json body, nullable
     */
    @Override
    public String encodeReply(InboundReply reply) {
        if (reply == null) {
            return null;
        }

        try {
            Map<String, Object> body = new LinkedHashMap<>();

            if (reply.messageType() != null) {
                body.put("messageType", reply.messageType());
            }

            if (reply.payload() != null) {
                body.put("payload", reply.payload());
            }

            if (reply.content() != null) {
                body.put("content", reply.content());
            }

            return BODY_MAPPER.writeValueAsString(body);
        } catch (Exception ex) {
            throw new IllegalStateException("webhook inbound reply serialization failed", ex);
        }
    }

    /**
     * Map the verified JSON body into the neutral event.
     *
     * @param config  channel config
     * @param rawBody raw request body
     * @return inbound event
     */
    private InboundMessage toEvent(Map<String, String> config, String rawBody) {
        if (rawBody == null || rawBody.isBlank()) {
            throw BusinessException.badRequest("webhook inbound: body is required");
        }

        try {
            JsonNode root = BODY_MAPPER.readTree(rawBody);
            String channelCode = config.get("channelCode");
            Map<String, String> payload = new LinkedHashMap<>();

            if (root.has("payload") && root.get("payload").isObject()) {
                root.get("payload").fields().forEachRemaining(entry -> {
                    JsonNode value = entry.getValue();
                    // nested objects / arrays keep their JSON text (asText
                    // would silently flatten them to "")
                    payload.put(entry.getKey(), value.isNull() ? null
                            : value.isValueNode() ? value.asText() : value.toString());
                });
            }

            return new InboundMessage(channelCode, channelCode(), text(root, "fromId"), text(root, "messageType"),
                    payload, text(root, "content"), text(root, "rawMessageId"), null);
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw BusinessException.badRequest("webhook inbound: body is not valid JSON");
        }
    }

    /**
     * Read a text field.
     *
     * @param root json root
     * @param name field name
     * @return text value, nullable
     */
    private String text(JsonNode root, String name) {
        return root.has(name) && !root.get(name).isNull() ? root.get(name).asText() : null;
    }

    /**
     * Look up a header case-insensitively.
     *
     * @param headers request headers
     * @param name    header name
     * @return first value, nullable
     */
    private String header(Map<String, String> headers, String name) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * HMAC-SHA256 over the raw body, hex-encoded.
     *
     * @param secret  hmac key
     * @param rawBody raw body
     * @return hex digest
     */
    private String hmacSha256Hex(String secret, String rawBody) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder(digest.length * 2);

            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("webhook inbound: hmac unavailable", ex);
        }
    }

    /**
     * Constant-time string comparison (no early exit on the first differing
     * char).
     *
     * @param expected computed digest
     * @param actual   presented signature
     * @return true when equal
     */
    private boolean constantTimeEquals(String expected, String actual) {
        byte[] left = expected.getBytes(StandardCharsets.UTF_8);
        byte[] right = actual.getBytes(StandardCharsets.UTF_8);

        return java.security.MessageDigest.isEqual(left, right);
    }
}
