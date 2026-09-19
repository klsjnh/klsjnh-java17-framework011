package com.klsjnh.domain.messagecenter.message;

/*                JulyMessageQuerySpec class
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
 * JulyMessage page query condition: optional channel / send-status filters and
 * an optional keyword matched against receiver / title / template code.
 *
 * @param channelCode exact channel code filter, nullable for all
 * @param sendStatus  exact send status filter, nullable for all
 * @param keyword     receiver / title / template keyword (fuzzy), nullable
 */

public record JulyMessageQuerySpec(String channelCode, String sendStatus, String keyword) {

    /**
     * Normalize the text filters (blank to null).
     *
     * @param channelCode exact channel code filter, nullable for all
     * @param sendStatus  exact send status filter, nullable for all
     * @param keyword     receiver / title / template keyword (fuzzy), nullable
     */
    public JulyMessageQuerySpec {
        channelCode = StringUtil011.blankToNull(channelCode);
        sendStatus = StringUtil011.blankToNull(sendStatus);
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
     * Whether a send status filter is present.
     *
     * @return true when a send status is present
     */
    public boolean hasSendStatus() {
        return sendStatus != null;
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
