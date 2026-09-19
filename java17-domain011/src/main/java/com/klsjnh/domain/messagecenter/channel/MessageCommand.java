package com.klsjnh.domain.messagecenter.channel;

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
 * @param config       whole channel config as key-value pairs, nullable
 * @param templateCode template code, nullable for a raw text send
 * @param params       template variables, nullable
 * @param title        rendered title, nullable
 * @param content      rendered content, nullable
 */

public record MessageCommand(String channelCode, String to, Map<String, String> config, String templateCode,
        Map<String, String> params, String title, String content) {
}
