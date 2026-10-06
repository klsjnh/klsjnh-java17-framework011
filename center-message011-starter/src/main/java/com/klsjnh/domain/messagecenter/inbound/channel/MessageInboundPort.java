package com.klsjnh.domain.messagecenter.inbound.channel;

/*                MessageInboundPort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate 2026.10.05
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  message inbound SPI
 *      2026.10.05  headers-aware parse (signature verification at the port)
 *
 */

import java.util.Map;

/**
 * SPI: one inbound message channel. Implementations are collected by the
 * inbound registry (Spring {@code List} injection), so a consumer adds a
 * channel by declaring one more bean; the platform is not modified. The port
 * parses the raw request body into the neutral {@link InboundMessage} event and
 * encodes an optional reply back into the platform response body.
 * <p>
 * Signature / authenticity verification belongs HERE (in {@code parse}), not in
 * the platform: the receive endpoint hands the port the channel config, the
 * request headers and the raw body.
 * </p>
 */

public interface MessageInboundPort {

    /**
     * The channel code this port serves (open string vocabulary).
     *
     * @return channel code, e.g. {@code webhook}
     */
    String channelCode();

    /**
     * Parse the raw request body into a neutral inbound event, with the request
     * headers available for signature verification. Channels that do not need
     * headers keep implementing the two-arg variant; the platform always calls
     * this one.
     *
     * @param config  whole channel config as key-value pairs, nullable
     * @param headers request headers (first value per name), never null
     * @param rawBody raw request body, nullable
     * @return inbound event, never null
     */
    default InboundMessage parse(Map<String, String> config, Map<String, String> headers, String rawBody) {
        return parse(config, rawBody);
    }

    /**
     * Parse the raw request body into a neutral inbound event.
     *
     * @param config  whole channel config as key-value pairs, nullable
     * @param rawBody raw request body, nullable
     * @return inbound event, never null
     */
    InboundMessage parse(Map<String, String> config, String rawBody);

    /**
     * Encode a synchronous reply into the platform HTTP response body.
     *
     * @param reply reply to encode, nullable
     * @return response body, nullable when no body should be written
     */
    String encodeReply(InboundReply reply);
}
