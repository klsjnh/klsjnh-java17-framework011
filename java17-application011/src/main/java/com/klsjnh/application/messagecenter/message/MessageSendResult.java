package com.klsjnh.application.messagecenter.message;

/*                MessageSendResult class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  message send result class
 *
 */

/**
 * Outcome of one message send: the persisted record id plus the channel result.
 *
 * @param messageId        send record id
 * @param success          whether the send succeeded
 * @param channelMessageId channel-side message id, nullable
 * @param error            failure reason, nullable
 */

public record MessageSendResult(String messageId, boolean success, String channelMessageId, String error) {
}
