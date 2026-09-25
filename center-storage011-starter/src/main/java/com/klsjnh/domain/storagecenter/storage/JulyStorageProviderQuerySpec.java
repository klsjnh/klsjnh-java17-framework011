package com.klsjnh.domain.storagecenter.storage;

/*                JulyStorageProviderQuerySpec class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july storage provider query spec class
 *
 */

import com.klsjnh.common.util.StringUtil011;

/**
 * JulyStorageProvider page query condition: an optional keyword matched against
 * code / name, an optional provider filter, plus an optional status filter.
 *
 * @param keyword  storage code / name keyword (fuzzy), nullable
 * @param provider exact provider filter, nullable for all
 * @param status   row status filter, nullable for all
 */

public record JulyStorageProviderQuerySpec(String keyword, String provider, String status) {

    /**
     * Normalize the text filters (blank → null).
     *
     * @param keyword  storage code / name keyword (fuzzy), nullable
     * @param provider exact provider filter, nullable for all
     * @param status   row status filter, nullable for all
     */
    public JulyStorageProviderQuerySpec {
        keyword = StringUtil011.blankToNull(keyword);
        provider = StringUtil011.blankToNull(provider);
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
     * Whether a provider filter is present.
     *
     * @return true when a provider is present
     */
    public boolean hasProvider() {
        return provider != null;
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
