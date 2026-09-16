package com.klsjnh.domain.lowcode011;

/*                JulyMetadataQuerySpec class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july metadata query spec class
 *
 */

import com.klsjnh.common.util.StringUtil011;

/**
 * JulyMetadata page query condition.
 *
 * @param keyword    object name keyword (fuzzy), nullable
 * @param objectType exact object type filter, nullable for all
 * @param status     row status filter, nullable for all
 */

public record JulyMetadataQuerySpec(String keyword, String objectType, String status) {

    /**
     * Normalize the text filters (blank → null).
     *
     * @param keyword    object name keyword (fuzzy), nullable
     * @param objectType exact object type filter, nullable for all
     * @param status     row status filter, nullable for all
     */
    public JulyMetadataQuerySpec {
        keyword = StringUtil011.isBlank(keyword) ? null : keyword.trim();
        objectType = StringUtil011.isBlank(objectType) ? null : objectType.trim();
    }

    /**
     * Whether a keyword filter is present.
     *
     * @return true when present
     */
    public boolean hasKeyword() {
        return keyword != null;
    }

    /**
     * Whether an object type filter is present.
     *
     * @return true when present
     */
    public boolean hasObjectType() {
        return objectType != null;
    }

    /**
     * Whether a status filter is present.
     *
     * @return true when present
     */
    public boolean hasStatus() {
        return !StringUtil011.isBlank(status);
    }
}
