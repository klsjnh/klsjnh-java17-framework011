package com.klsjnh.domain.ai011.modelprovider;

/*                AiModelProviderQuerySpec class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  ai model provider query spec class
 *
 */

import com.klsjnh.common.util.StringUtil011;

/**
 * AiModelProvider page query condition: an optional keyword matched against
 * providerCode / providerName / baseUrl, plus an optional status filter.
 *
 * @param keyword provider code / name / base url keyword (fuzzy), nullable
 * @param status  row status filter, nullable for all
 */

public record AiModelProviderQuerySpec(String keyword, String status) {

    /**
     * Normalize the keyword (blank → null) and keep the status as given.
     *
     * @param keyword provider code / name / base url keyword (fuzzy), nullable
     * @param status  row status filter, nullable for all
     */
    public AiModelProviderQuerySpec {
        if (StringUtil011.isBlank(keyword)) {
            keyword = null;
        } else {
            keyword = keyword.trim();
        }
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
