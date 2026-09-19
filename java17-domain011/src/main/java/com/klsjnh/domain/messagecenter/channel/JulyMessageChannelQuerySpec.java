package com.klsjnh.domain.messagecenter.channel;

/*                JulyMessageChannelQuerySpec class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july message channel query spec class
 *
 */

import com.klsjnh.common.util.StringUtil011;

/**
 * JulyMessageChannel page query condition: an optional keyword matched against
 * code / name / provider type, plus an optional status filter.
 *
 * @param keyword channel code / name / provider keyword (fuzzy), nullable
 * @param status  row status filter, nullable for all
 */

public record JulyMessageChannelQuerySpec(String keyword, String status) {

    /**
     * Normalize the text filters (blank to null).
     *
     * @param keyword channel code / name / provider keyword (fuzzy), nullable
     * @param status  row status filter, nullable for all
     */
    public JulyMessageChannelQuerySpec {
        keyword = StringUtil011.blankToNull(keyword);
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
     * Whether a status filter is present.
     *
     * @return true when a status is present
     */
    public boolean hasStatus() {
        return !StringUtil011.isBlank(status);
    }
}
