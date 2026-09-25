package com.klsjnh.domain.messagecenter.inbound.channel;

/*                InboundReply record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  inbound reply record
 *
 */

import java.util.Map;

/**
 * Optional synchronous reply returned by an {@link InboundMessageListener}: the
 * channel encodes it into the platform's HTTP response body. A null reply means
 * the platform answers with an empty body.
 *
 * @param messageType channel-defined reply shape (open string), nullable
 * @param payload     channel-defined reply payload, nullable
 * @param content     reply content, nullable
 */

public record InboundReply(String messageType, Map<String, String> payload, String content) {
}
