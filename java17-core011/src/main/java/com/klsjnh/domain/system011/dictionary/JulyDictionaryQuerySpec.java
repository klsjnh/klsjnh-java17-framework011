package com.klsjnh.domain.system011.dictionary;

/*                JulyDictionaryQuerySpec class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july dictionary query spec class
 *
 */

import com.klsjnh.common.util.StringUtil011;

/**
 * JulyDictionary page query condition: an optional keyword matched against
 * dictionaryCode / dictionaryName, plus an optional status filter.
 *
 * @param keyword dictionary code / name keyword (fuzzy), nullable
 * @param status  row status filter, nullable for all
 */

public record JulyDictionaryQuerySpec(String keyword, String status) {

    /**
     * Normalize the keyword (blank → null) and keep the status as given.
     *
     * @param keyword dictionary code / name keyword (fuzzy), nullable
     * @param status  row status filter, nullable for all
     */
    public JulyDictionaryQuerySpec {
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
