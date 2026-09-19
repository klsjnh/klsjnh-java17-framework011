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
 * One send command handed to a {@link MessageChannelPort}.
 *
 * @param channelCode  channel code (the SPI provider type)
 * @param to           receiver (user id / phone / webhook target), nullable
 * @param endpoint     channel endpoint from the channel config (webhook url), nullable
 * @param templateCode template code, nullable for a raw text send
 * @param params       template variables, nullable
 * @param title        rendered title, nullable
 * @param content      rendered content, nullable
 */

public record MessageCommand(String channelCode, String to, String endpoint, String templateCode,
        Map<String, String> params, String title, String content) {
}
