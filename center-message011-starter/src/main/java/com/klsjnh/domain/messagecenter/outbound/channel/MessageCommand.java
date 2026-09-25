package com.klsjnh.domain.messagecenter.outbound.channel;

/*                MessageCommand record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  message command record
 *
 */

import java.util.Map;

/**
 * One send command handed to a {@link MessageChannelPort}. The whole channel
 * config is passed through as a structured map, so a channel reads whatever keys
 * it needs (webhook {@code url}; WeCom {@code corpId} / {@code corpSecret} /
 * {@code agentId}; ...) instead of querying the channel repository itself.
 *
 * @param channelCode  channel code (the SPI provider type)
 * @param to           receiver (user id / phone / webhook target), nullable
 * @param messageType  channel-defined message SHAPE (open string, e.g. WeCom
 *                     {@code text} / {@code textcard}); the platform passes it
 *                     through uninterpreted, nullable
 * @param config       whole channel config as key-value pairs, nullable
 * @param payload      channel-defined payload (shape-specific keys such as
 *                     image_key / file_key / url / btntxt), nullable
 * @param templateCode template code, nullable for a raw text send
 * @param params       template variables, nullable
 * @param title        rendered title, nullable
 * @param content      rendered content, nullable
 */

public record MessageCommand(String channelCode, String to, String messageType, Map<String, String> config,
        Map<String, String> payload, String templateCode, Map<String, String> params, String title, String content) {
}
