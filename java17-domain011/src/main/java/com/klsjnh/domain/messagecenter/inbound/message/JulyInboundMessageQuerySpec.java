package com.klsjnh.domain.messagecenter.inbound.message;

/*                JulyInboundMessageQuerySpec class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july inbound message query spec class
 *
 */

import com.klsjnh.common.util.StringUtil011;

/**
 * JulyInboundMessage page query condition: optional channel / status filters
 * and an optional keyword matched against sender / content / raw message id.
 *
 * @param channelCode exact channel code filter, nullable for all
 * @param status      exact handling status filter, nullable for all
 * @param keyword     sender / content / raw message id keyword (fuzzy), nullable
 */

public record JulyInboundMessageQuerySpec(String channelCode, String status, String keyword) {

    /**
     * Normalize the text filters (blank to null).
     *
     * @param channelCode exact channel code filter, nullable for all
     * @param status      exact handling status filter, nullable for all
     * @param keyword     sender / content / raw message id keyword (fuzzy), nullable
     */
    public JulyInboundMessageQuerySpec {
        channelCode = StringUtil011.blankToNull(channelCode);
        status = StringUtil011.blankToNull(status);
        keyword = StringUtil011.blankToNull(keyword);
    }

    /**
     * Whether a channel filter is present.
     *
     * @return true when a channel code is present
     */
    public boolean hasChannelCode() {
        return channelCode != null;
    }

    /**
     * Whether a status filter is present.
     *
     * @return true when a status is present
     */
    public boolean hasStatus() {
        return status != null;
    }

    /**
     * Whether a keyword filter is present.
     *
     * @return true when a keyword is present
     */
    public boolean hasKeyword() {
        return keyword != null;
    }
}
