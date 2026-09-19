package com.klsjnh.domain.messagecenter.inbound.channel;

/*                InboundMessageListener interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  inbound message listener SPI
 *
 */

/**
 * SPI: a consumer listener for inbound messages. Implementations declare the
 * channels they support and are collected by the inbound registry. A listener
 * may return a synchronous {@link InboundReply} (the first non-null reply wins)
 * or null when no reply is required.
 */

public interface InboundMessageListener {

    /**
     * Whether this listener handles the given channel code.
     *
     * @param channelCode channel code
     * @return true when supported
     */
    boolean supports(String channelCode);

    /**
     * Handle one inbound message.
     *
     * @param message inbound event
     * @return reply, or null when no reply is required
     */
    InboundReply onMessage(InboundMessage message);
}
