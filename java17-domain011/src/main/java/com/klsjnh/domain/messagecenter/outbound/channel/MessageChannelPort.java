package com.klsjnh.domain.messagecenter.outbound.channel;

/*                MessageChannelPort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  message channel SPI
 *
 */

/**
 * SPI: one message channel (in-app / webhook / sms / wecom / ...).
 * Implementations are collected by the channel registry (Spring {@code List}
 * injection), so a consumer adds a channel by declaring one more bean; the
 * platform is not modified. Channel codes are an open string vocabulary.
 */

public interface MessageChannelPort {

    /**
     * The channel code this port serves (open string vocabulary).
     *
     * @return channel code, e.g. {@code inapp}
     */
    String channelCode();

    /**
     * Send one message.
     *
     * @param command send command
     * @return send result, never null
     */
    MessageResult send(MessageCommand command);
}
