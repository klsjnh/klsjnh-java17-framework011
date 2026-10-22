package com.klsjnh.domain.messagecenter.inbound.channel;

/*                MessagePayloadKeys011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.22  standard inbound payload keys
 *
 */

/**
 * Standard keys for {@link InboundMessage#payload()}. Channels may add vendor
 * keys, but these names are the neutral contract shared by listeners, media
 * download and the documentation.
 */

public final class MessagePayloadKeys011 {

    /** Channel-side message id (same as rawMessageId when available). */
    public static final String MESSAGE_ID = "messageId";

    /** Channel-side chat / conversation id. */
    public static final String CHAT_ID = "chatId";

    /** Sender id (open id / user id / phone). */
    public static final String SENDER_ID = "senderId";

    /** Media key for a file / voice / video. */
    public static final String FILE_KEY = "fileKey";

    /** Media key for an image. */
    public static final String IMAGE_KEY = "imageKey";

    /** Original file name. */
    public static final String FILE_NAME = "fileName";

    /** Media type: image / file / audio / video. */
    public static final String MEDIA_TYPE = "mediaType";

    /** Duration in milliseconds (audio / video). */
    public static final String DURATION = "duration";

    /** Raw channel content JSON. */
    public static final String CONTENT = "content";

    private MessagePayloadKeys011() {
    }
}
