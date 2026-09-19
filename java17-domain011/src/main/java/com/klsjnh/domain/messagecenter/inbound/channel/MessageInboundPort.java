package com.klsjnh.domain.messagecenter.inbound.channel;

/*                MessageInboundPort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  message inbound SPI
 *
 */

import java.util.Map;

/**
 * SPI: one inbound message channel. Implementations are collected by the
 * inbound registry (Spring {@code List} injection), so a consumer adds a
 * channel by declaring one more bean; the platform is not modified. The port
 * parses the raw request body into the neutral {@link InboundMessage} event and
 * encodes an optional reply back into the platform response body.
 */

public interface MessageInboundPort {

    /**
     * The channel code this port serves (open string vocabulary).
     *
     * @return channel code, e.g. {@code webhook}
     */
    String channelCode();

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
