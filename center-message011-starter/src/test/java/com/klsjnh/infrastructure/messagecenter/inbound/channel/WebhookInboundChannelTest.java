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
 *      2026.10.11  timestamp replay-window tests
 *
 */

import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.domain.messagecenter.inbound.channel.InboundMessage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
     * Fresh epoch-seconds timestamp.
     *
     * @param offsetSeconds offset from now, negative for the past
     * @return timestamp header value
     */
    private String epochSeconds(long offsetSeconds) {
        return String.valueOf(System.currentTimeMillis() / 1000 + offsetSeconds);
    }

    /**
     * A valid signature + timestamp + body maps into the event.
     */
    @Test
    void validSignatureParsesBody() {
        String body = "{\"fromId\":\"u1\",\"messageType\":\"event\",\"content\":\"hello\","
                + "\"rawMessageId\":\"m1\",\"payload\":{\"k\":\"v\"}}";
        Map<String, String> config = Map.of("secret", SECRET);
        Map<String, String> headers = Map.of("X-Signature", sign(body), "X-Timestamp", epochSeconds(0));

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
        Map<String, String> headers = Map.of("X-Signature", "deadbeef", "X-Timestamp", epochSeconds(0));

        assertThrows(BusinessException.class, () -> channel.parse(config, headers, "{\"x\":1}"));
    }

    /**
     * A missing signature header is rejected with 400.
     */
    @Test
    void missingSignatureIsRejected() {
        Map<String, String> config = Map.of("secret", SECRET);
        Map<String, String> headers = Map.of("X-Timestamp", epochSeconds(0));

        assertThrows(BusinessException.class, () -> channel.parse(config, headers, "{\"x\":1}"));
    }

    /**
     * Fail closed: without a configured secret every request is rejected.
     */
    @Test
    void unconfiguredSecretIsRejected() {
        assertThrows(BusinessException.class, () -> channel.parse(Map.of(), Map.of(), "{\"x\":1}"));
    }

    /**
     * A missing timestamp header is rejected (replay window is on by default).
     */
    @Test
    void missingTimestampIsRejected() {
        String body = "{\"x\":1}";
        Map<String, String> config = Map.of("secret", SECRET);
        Map<String, String> headers = Map.of("X-Signature", sign(body));

        assertThrows(BusinessException.class, () -> channel.parse(config, headers, body));
    }

    /**
     * A timestamp older than the window is rejected.
     */
    @Test
    void staleTimestampIsRejected() {
        String body = "{\"x\":1}";
        Map<String, String> config = Map.of("secret", SECRET);
        Map<String, String> headers = Map.of("X-Signature", sign(body), "X-Timestamp", epochSeconds(-3600));

        assertThrows(BusinessException.class, () -> channel.parse(config, headers, body));
    }

    /**
     * A timestamp too far in the future is rejected.
     */
    @Test
    void futureTimestampIsRejected() {
        String body = "{\"x\":1}";
        Map<String, String> config = Map.of("secret", SECRET);
        Map<String, String> headers = Map.of("X-Signature", sign(body), "X-Timestamp", epochSeconds(3600));

        assertThrows(BusinessException.class, () -> channel.parse(config, headers, body));
    }

    /**
     * A non-numeric timestamp is rejected.
     */
    @Test
    void malformedTimestampIsRejected() {
        String body = "{\"x\":1}";
        Map<String, String> config = Map.of("secret", SECRET);
        Map<String, String> headers = Map.of("X-Signature", sign(body), "X-Timestamp", "yesterday");

        assertThrows(BusinessException.class, () -> channel.parse(config, headers, body));
    }

    /**
     * Epoch-millis timestamps are accepted (fresh) and a wider configured
     * window accepts an hour-old timestamp.
     */
    @Test
    void timestampFormatsAndWindowAreHandled() {
        String body = "{\"x\":1}";
        Map<String, String> config = Map.of("secret", SECRET, "maxAgeSeconds", "7200");
        Map<String, String> millisHeaders = Map.of("X-Signature", sign(body),
                "X-Timestamp", String.valueOf(System.currentTimeMillis()));
        Map<String, String> hourOldHeaders = Map.of("X-Signature", sign(body), "X-Timestamp", epochSeconds(-3600));

        assertDoesNotThrow(() -> channel.parse(config, millisHeaders, body));
        assertDoesNotThrow(() -> channel.parse(config, hourOldHeaders, body));
    }

    /**
     * {@code requireTimestamp=false} opts out of the replay window.
     */
    @Test
    void timestampCanBeOptedOut() {
        String body = "{\"x\":1}";
        Map<String, String> config = Map.of("secret", SECRET, "requireTimestamp", "false");
        Map<String, String> headers = Map.of("X-Signature", sign(body));

        assertDoesNotThrow(() -> channel.parse(config, headers, body));
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
