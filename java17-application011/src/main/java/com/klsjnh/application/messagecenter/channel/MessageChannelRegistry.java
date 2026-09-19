package com.klsjnh.application.messagecenter.channel;

/*                MessageChannelRegistry class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  message channel registry class
 *
 */

import com.klsjnh.domain.messagecenter.channel.MessageChannelPort;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Collects every {@link MessageChannelPort} bean of the context into a
 * channelCode to port map (immutable snapshot at startup) — the same
 * registration pattern as the export provider registry. A consumer adds a
 * channel by declaring one more port bean; the platform is not modified.
 */

@Component
public class MessageChannelRegistry {

    /**
     * Channel code to port mapping.
     */
    private final Map<String, MessageChannelPort> ports;

    /**
     * Create the registry from all channel port beans.
     *
     * @param portBeans channel port beans found in the context
     */
    public MessageChannelRegistry(List<MessageChannelPort> portBeans) {
        this.ports = portBeans.stream()
                .collect(Collectors.toUnmodifiableMap(MessageChannelPort::channelCode, Function.identity()));
    }

    /**
     * Resolve a channel port by channel code.
     *
     * @param channelCode channel code
     * @return port or null when unknown
     */
    public MessageChannelPort get(String channelCode) {
        return channelCode == null ? null : ports.get(channelCode);
    }

    /**
     * All registered channel codes.
     *
     * @return channel code list
     */
    public List<String> channelCodes() {
        return List.copyOf(ports.keySet());
    }
}
