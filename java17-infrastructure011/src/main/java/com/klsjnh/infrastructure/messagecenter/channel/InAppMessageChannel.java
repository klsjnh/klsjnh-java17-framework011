package com.klsjnh.infrastructure.messagecenter.channel;

/*                InAppMessageChannel class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  built-in in-app message channel
 *
 */

import com.klsjnh.common.constant.MessageProviderTypes011;

import com.klsjnh.domain.messagecenter.channel.MessageChannelPort;
import com.klsjnh.domain.messagecenter.channel.MessageCommand;
import com.klsjnh.domain.messagecenter.channel.MessageResult;

import org.springframework.stereotype.Component;

/**
 * Built-in in-app message channel: the persisted outbox row IS the inbox, so a
 * send always succeeds with no external IO.
 */

@Component
public class InAppMessageChannel implements MessageChannelPort {

    /**
     * Channel code served by this port.
     *
     * @return inapp
     */
    @Override
    public String channelCode() {
        return MessageProviderTypes011.INAPP;
    }

    /**
     * Send: no-op, the outbox row is the message.
     *
     * @param command send command
     * @return success result
     */
    @Override
    public MessageResult send(MessageCommand command) {
        return MessageResult.success(null);
    }
}
