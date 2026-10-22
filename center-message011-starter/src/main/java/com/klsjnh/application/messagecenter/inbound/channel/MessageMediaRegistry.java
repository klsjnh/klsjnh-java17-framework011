package com.klsjnh.application.messagecenter.inbound.channel;

/*                MessageMediaRegistry class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.22  inbound media port registry
 *
 */

import com.klsjnh.domain.messagecenter.inbound.channel.MessageMediaPort;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Collects every {@link MessageMediaPort} bean into a provider-type map.
 */

@Component
public class MessageMediaRegistry {

    /**
     * Provider type to port mapping.
     */
    private final Map<String, MessageMediaPort> ports;

    /**
     * Create the registry.
     *
     * @param ports media ports found in the context
     */
    public MessageMediaRegistry(List<MessageMediaPort> ports) {
        this.ports = ports.stream()
                .collect(Collectors.toUnmodifiableMap(MessageMediaPort::channelCode, Function.identity()));
    }

    /**
     * Resolve a media port by provider type.
     *
     * @param channelCode provider type
     * @return port or null when unknown
     */
    public MessageMediaPort port(String channelCode) {
        return channelCode == null ? null : ports.get(channelCode);
    }
}
