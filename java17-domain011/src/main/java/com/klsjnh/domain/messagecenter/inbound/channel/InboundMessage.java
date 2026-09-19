package com.klsjnh.domain.messagecenter.inbound.channel;

/*                InboundMessage record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  inbound message record
 *
 */

import java.util.Map;

/**
 * One inbound message handed to an {@link InboundMessageListener}: the channel
 * that received it, the sender, an optional channel-defined shape and payload,
 * the parsed content and the channel-side message id used for deduplication.
 *
 * @param channelCode  channel code that received the message
 * @param providerType provider type (the SPI channelCode) that parsed it
 * @param fromId       sender (user id / openId / phone), nullable
 * @param messageType  channel-defined message SHAPE (open string), nullable
 * @param payload      channel-defined payload, nullable
 * @param content      parsed content, nullable
 * @param rawMessageId channel-side message id (dedupe key), nullable
 * @param remark       remark, nullable
 */

public record InboundMessage(String channelCode, String providerType, String fromId, String messageType,
        Map<String, String> payload, String content, String rawMessageId, String remark) {
}
