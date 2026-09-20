package com.klsjnh.domain.datasource.sync;

/*                JulySyncRuleQuerySpec record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  sync rule query spec
 *
 */

import com.klsjnh.common.util.StringUtil011;

/**
 * Sync rule page query condition: an optional keyword (code / name) plus an
 * optional status filter.
 *
 * @param keyword sync code / name keyword (fuzzy), nullable
 * @param status  row status filter, nullable
 */

public record JulySyncRuleQuerySpec(String keyword, String status) {

    /**
     * Normalize the keyword (blank → null).
     *
     * @param keyword sync code / name keyword (fuzzy), nullable
     * @param status  row status filter, nullable
     */
    public JulySyncRuleQuerySpec {
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
