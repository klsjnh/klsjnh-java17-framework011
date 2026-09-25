package com.klsjnh.domain.messagecenter.outbound.template;

/*                JulyOutboundTemplateQuerySpec class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july message template query spec class
 *
 */

import com.klsjnh.common.util.StringUtil011;

/**
 * JulyOutboundTemplate page query condition: an optional keyword matched against
 * code / name, an optional channel filter, plus an optional status filter.
 *
 * @param keyword     template code / name keyword (fuzzy), nullable
 * @param channelCode exact channel code filter, nullable for all
 * @param status      row status filter, nullable for all
 */

public record JulyOutboundTemplateQuerySpec(String keyword, String channelCode, String status) {

    /**
     * Normalize the text filters (blank to null).
     *
     * @param keyword     template code / name keyword (fuzzy), nullable
     * @param channelCode exact channel code filter, nullable for all
     * @param status      row status filter, nullable for all
     */
    public JulyOutboundTemplateQuerySpec {
        keyword = StringUtil011.blankToNull(keyword);
        channelCode = StringUtil011.blankToNull(channelCode);
    }

    /**
     * Whether a keyword filter is present.
     *
     * @return true when a keyword is present
     */
    public boolean hasKeyword() {
        return keyword != null;
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
        return !StringUtil011.isBlank(status);
    }
}
