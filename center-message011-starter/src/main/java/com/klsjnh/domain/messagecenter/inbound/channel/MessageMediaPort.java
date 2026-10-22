package com.klsjnh.domain.messagecenter.inbound.channel;

/*                MessageMediaPort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.22  inbound media download SPI
 *
 */

/**
 * SPI: download one inbound message resource (image / file / voice) for a
 * channel. Vendors differ (Feishu {@code /im/v1/messages/{id}/resources},
 * WeCom media API ...), so the transport stays in the channel plug-in while the
 * framework owns the neutral {@link MessageMedia} shape.
 */

public interface MessageMediaPort {

    /**
     * Provider type this port serves (the inbound port's channel code).
     *
     * @return provider type, e.g. {@code feishu}
     */
    String channelCode();

    /**
     * Download one media resource.
     *
     * @param channelCode channel instance code (for channel config lookup)
     * @param messageId   channel-side message id
     * @param mediaKey    channel-side media key ({@code file_key} / {@code image_key})
     * @param mediaType   neutral type: {@code image} / {@code file} / {@code audio} / {@code video}
     * @return media bytes, never null
     */
    MessageMedia download(String channelCode, String messageId, String mediaKey, String mediaType);
}
