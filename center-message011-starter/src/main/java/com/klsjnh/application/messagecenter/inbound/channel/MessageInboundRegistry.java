package com.klsjnh.application.messagecenter.inbound.channel;

/*                MessageInboundRegistry class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  message inbound registry class
 *
 */

import com.klsjnh.domain.messagecenter.inbound.channel.InboundMessageListener;
import com.klsjnh.domain.messagecenter.inbound.channel.MessageInboundPort;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Collects every {@link MessageInboundPort} bean of the context into a
 * channelCode to port map (immutable snapshot at startup) and keeps the
 * listener beans. A consumer adds a channel by declaring one more port bean and
 * a listener by declaring one more listener bean; the platform is not modified.
 */

@Component
public class MessageInboundRegistry {

    /**
     * Channel code to port mapping.
     */
    private final Map<String, MessageInboundPort> ports;

    /**
     * Inbound listener beans.
     */
    private final List<InboundMessageListener> listeners;

    /**
     * Create the registry from all inbound port and listener beans.
     *
     * @param ports     inbound port beans found in the context
     * @param listeners inbound listener beans found in the context
     */
    public MessageInboundRegistry(List<MessageInboundPort> ports, List<InboundMessageListener> listeners) {
        this.ports = ports.stream()
                .collect(Collectors.toUnmodifiableMap(MessageInboundPort::channelCode, Function.identity()));
        this.listeners = List.copyOf(listeners);
    }

    /**
     * Resolve an inbound port by channel code.
     *
     * @param channelCode channel code
     * @return port or null when unknown
     */
    public MessageInboundPort port(String channelCode) {
        return channelCode == null ? null : ports.get(channelCode);
    }

    /**
     * Listeners that support the given channel code.
     *
     * @param channelCode channel code
     * @return matching listeners, never null
     */
    public List<InboundMessageListener> listenersFor(String channelCode) {
        return listeners.stream().filter(listener -> listener.supports(channelCode)).toList();
    }
}
