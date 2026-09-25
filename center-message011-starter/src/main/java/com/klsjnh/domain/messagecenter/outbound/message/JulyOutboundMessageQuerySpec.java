package com.klsjnh.domain.messagecenter.outbound.message;

/*                JulyOutboundMessageQuerySpec class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july message query spec class
 *
 */

import com.klsjnh.common.util.StringUtil011;

/**
 * JulyOutboundMessage page query condition: optional channel / status filters and
 * an optional keyword matched against receiver / title / template code.
 *
 * @param channelCode exact channel code filter, nullable for all
 * @param status  exact send status filter, nullable for all
 * @param keyword     receiver / title / template keyword (fuzzy), nullable
 */

public record JulyOutboundMessageQuerySpec(String channelCode, String status, String keyword) {

    /**
     * Normalize the text filters (blank to null).
     *
     * @param channelCode exact channel code filter, nullable for all
     * @param status  exact send status filter, nullable for all
     * @param keyword     receiver / title / template keyword (fuzzy), nullable
     */
    public JulyOutboundMessageQuerySpec {
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
