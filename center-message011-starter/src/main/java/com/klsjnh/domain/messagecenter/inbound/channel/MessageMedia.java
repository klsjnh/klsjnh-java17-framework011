package com.klsjnh.domain.messagecenter.inbound.channel;

/*                MessageMedia record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.22  inbound media bytes (file / image / voice)
 *
 */

/**
 * One downloaded inbound media artifact (image / file / voice).
 *
 * @param fileName    original file name, nullable
 * @param contentType mime type, nullable
 * @param bytes       media bytes
 */

public record MessageMedia(String fileName, String contentType, byte[] bytes) {
}
