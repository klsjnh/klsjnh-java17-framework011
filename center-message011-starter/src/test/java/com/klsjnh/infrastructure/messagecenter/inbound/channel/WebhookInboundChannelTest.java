package com.klsjnh.infrastructure.messagecenter.inbound.channel;

/*                WebhookInboundChannelTest class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.05
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.05  webhook inbound channel unit test (HMAC fail-closed)
 *
 */

import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.domain.messagecenter.inbound.channel.InboundMessage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Unit tests for {@link WebhookInboundChannel}: the HMAC-SHA256 signature is
 * mandatory (fail-closed), a mismatched signature is a 400, and a verified
 * body maps into the neutral event.
 */

@ExtendWith(MockitoExtension.class)
class WebhookInboundChannelTest {

    /**
     * Shared test secret.
     */
    private static final String SECRET = "unit-test-secret";

    /**
     * Channel under test.
     */
    private final WebhookInboundChannel channel = new WebhookInboundChannel();

    /**
     * Compute the test signature.
     *
     * @param body raw body
     * @return hex digest
     */
    private String sign(String body) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();

            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * A valid signature + body maps into the event.
     */
    @Test
    void validSignatureParsesBody() {
        String body = "{\"fromId\":\"u1\",\"messageType\":\"event\",\"content\":\"hello\","
                + "\"rawMessageId\":\"m1\",\"payload\":{\"k\":\"v\"}}";
        Map<String, String> config = Map.of("secret", SECRET);
        Map<String, String> headers = Map.of("X-Signature", sign(body));

        InboundMessage event = channel.parse(config, headers, body);

        assertEquals("webhook", event.providerType());
        assertEquals("u1", event.fromId());
        assertEquals("hello", event.content());
        assertEquals("m1", event.rawMessageId());
        assertEquals("v", event.payload().get("k"));
    }

    /**
     * A wrong signature is rejected with 400.
     */
    @Test
    void wrongSignatureIsRejected() {
        Map<String, String> config = Map.of("secret", SECRET);
        Map<String, String> headers = Map.of("X-Signature", "deadbeef");

        assertThrows(BusinessException.class, () -> channel.parse(config, headers, "{\"x\":1}"));
    }

    /**
     * A missing signature header is rejected with 400.
     */
    @Test
    void missingSignatureIsRejected() {
        Map<String, String> config = Map.of("secret", SECRET);

        assertThrows(BusinessException.class, () -> channel.parse(config, Map.of(), "{\"x\":1}"));
    }

    /**
     * Fail closed: without a configured secret every request is rejected.
     */
    @Test
    void unconfiguredSecretIsRejected() {
        assertThrows(BusinessException.class, () -> channel.parse(Map.of(), Map.of(), "{\"x\":1}"));
    }

    /**
     * The reply encodes to a JSON object.
     */
    @Test
    void replyEncodesToJson() {
        String body = channel.encodeReply(
                new com.klsjnh.domain.messagecenter.inbound.channel.InboundReply("ack", null, "ok"));

        assertEquals("{\"messageType\":\"ack\",\"content\":\"ok\"}", body);
    }
}
