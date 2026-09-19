package com.klsjnh.application.messagecenter.message;

/*                MessageSendCommand class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  message send command class
 *
 */

import java.util.Map;

/**
 * Application input of a send request: a channel, a receiver, a channel-defined
 * message shape, an optional template plus variables, or a raw title / content.
 *
 * @param channelCode  channel code
 * @param to           receiver, optional
 * @param messageType  channel-defined message SHAPE (open string, passed through
 *                     uninterpreted; e.g. WeCom text / textcard), nullable
 * @param payload      channel-defined payload (shape-specific keys such as
 *                     image_key / file_key / url / btntxt), nullable
 * @param templateCode template code, optional
 * @param params       template variables, nullable
 * @param title        raw title, optional
 * @param content      raw content, optional
 * @param remark       remark, optional
 */

public record MessageSendCommand(String channelCode, String to, String messageType, Map<String, String> payload,
        String templateCode, Map<String, String> params, String title, String content, String remark) {
}
