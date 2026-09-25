package com.klsjnh.common.constant;

/*                MessageProviderTypes011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  message provider type codes (open string vocabulary)
 *
 */

/**
 * Built-in message channel provider types: the SPI {@code channelCode} an
 * outbound / inbound channel row binds to. Plain string constants, not an
 * enum — a consumer may implement any other code through the
 * {@code MessageChannelPort} SPI without extending the platform (016 §6.5).
 */

public final class MessageProviderTypes011 {

    /** In-app message: the outbox row is the inbox, no external IO. */
    public static final String INAPP = "inapp";

    /** Generic webhook: HTTP POST JSON to the channel's configured url. */
    public static final String WEBHOOK = "webhook";

    private MessageProviderTypes011() {
    }
}
