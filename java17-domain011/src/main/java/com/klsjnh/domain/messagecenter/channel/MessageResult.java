package com.klsjnh.domain.messagecenter.channel;

/*                MessageResult record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  message result record
 *
 */

/**
 * Outcome of one {@link MessageChannelPort#send(MessageCommand)} call.
 *
 * @param success          whether the send succeeded
 * @param channelMessageId channel-side message id, nullable
 * @param error            failure reason, nullable
 */

public record MessageResult(boolean success, String channelMessageId, String error) {

    /**
     * A successful result.
     *
     * @param channelMessageId channel-side message id, nullable
     * @return result
     */
    public static MessageResult success(String channelMessageId) {
        return new MessageResult(true, channelMessageId, null);
    }

    /**
     * A failed result.
     *
     * @param error failure reason
     * @return result
     */
    public static MessageResult failure(String error) {
        return new MessageResult(false, null, error);
    }
}
