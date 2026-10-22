package com.klsjnh.domain.messagecenter.inbound.channel;

/*                MessageInboundProcessor interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.22  explicit per-channel processor binding
 *
 */

import java.util.Set;

/**
 * SPI: a processor explicitly bound to one or more inbound channel <b>instance
 * codes</b> (bot / account / tenant). Unlike the legacy
 * {@link InboundMessageListener#supports(String)} filter, the binding is
 * declared once and is stable: one application can host many bots, each with
 * its own processor, and one processor may serve several bots.
 * <p>
 * Multiple processors may bind the same channel; they run in bean order and the
 * first non-null reply wins.
 * </p>
 */

public interface MessageInboundProcessor {

    /**
     * Channel instance codes this processor is bound to (non-empty).
     *
     * @return channel codes
     */
    Set<String> channelCodes();

    /**
     * Process one inbound message.
     *
     * @param message inbound message
     * @return optional synchronous reply, null when no reply is required
     */
    InboundReply process(InboundMessage message);
}
