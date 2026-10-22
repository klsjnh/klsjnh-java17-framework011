package com.klsjnh.application.messagecenter.inbound.message;

/*                MessageMediaService class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.22  neutral inbound media download
 *
 */

import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.application.messagecenter.inbound.channel.MessageMediaRegistry;
import com.klsjnh.domain.messagecenter.inbound.channel.JulyInboundChannel;
import com.klsjnh.domain.messagecenter.inbound.channel.JulyInboundChannelRepository;
import com.klsjnh.domain.messagecenter.inbound.channel.MessageMedia;
import com.klsjnh.domain.messagecenter.inbound.channel.MessageMediaPort;

import org.springframework.stereotype.Service;

/**
 * Neutral inbound media download: resolves the channel instance to its provider
 * type, then delegates to the vendor's {@link MessageMediaPort}. Listeners call
 * this instead of implementing vendor download APIs.
 */

@Service
public class MessageMediaService {

    /**
     * Inbound channel repository.
     */
    private final JulyInboundChannelRepository channelRepository;

    /**
     * Media port registry.
     */
    private final MessageMediaRegistry registry;

    /**
     * Create the service.
     *
     * @param channelRepository channel repository
     * @param registry          media port registry
     */
    public MessageMediaService(JulyInboundChannelRepository channelRepository, MessageMediaRegistry registry) {
        this.channelRepository = channelRepository;
        this.registry = registry;
    }

    /**
     * Download one inbound media resource.
     *
     * @param channelCode channel instance code
     * @param messageId   channel-side message id
     * @param mediaKey    channel-side media key
     * @param mediaType   image / file / audio / video
     * @return media bytes
     */
    public MessageMedia download(String channelCode, String messageId, String mediaKey, String mediaType) {
        if (StringUtil011.isBlank(channelCode)) {
            throw BusinessException.badRequest("channel code is required");
        }

        if (StringUtil011.isBlank(messageId) || StringUtil011.isBlank(mediaKey)) {
            throw BusinessException.badRequest("messageId and mediaKey are required");
        }

        JulyInboundChannel channel = channelRepository.findEnabledByCode(channelCode);

        if (channel == null) {
            throw BusinessException.badRequest("channel not enabled or not found: " + channelCode);
        }

        MessageMediaPort port = registry.port(channel.providerType());

        if (port == null) {
            throw BusinessException.badRequest("no media port for provider type: " + channel.providerType());
        }

        return port.download(channelCode, messageId, mediaKey,
                StringUtil011.isBlank(mediaType) ? "file" : mediaType);
    }
}
