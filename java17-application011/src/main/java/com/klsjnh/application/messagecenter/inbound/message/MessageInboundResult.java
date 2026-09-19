package com.klsjnh.application.messagecenter.inbound.message;

/*                MessageInboundResult record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  message inbound result record
 *
 */

/**
 * Outcome of one inbound receive: the persisted record id, whether the message
 * was a duplicate and the optional encoded response body.
 *
 * @param messageId    received record id
 * @param duplicate    whether the message was already received
 * @param responseBody encoded response body, nullable
 */

public record MessageInboundResult(String messageId, boolean duplicate, String responseBody) {
}
