package com.klsjnh.domain.aicenter.prompt;

/*                JulyAiDomainQuerySpec record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.21
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.21  ai domain query spec
 *
 */

import com.klsjnh.common.util.StringUtil011;

/**
 * Domain page query condition: keyword (code / name), parent and status
 * filters.
 *
 * @param keyword  domain code / name keyword (fuzzy), nullable
 * @param parentId parent domain id filter, nullable
 * @param status   row status filter, nullable
 */

public record JulyAiDomainQuerySpec(String keyword, String parentId, String status) {

    /**
     * Normalize the filters (blank → null).
     *
     * @param keyword  domain code / name keyword (fuzzy), nullable
     * @param parentId parent domain id filter, nullable
     * @param status   row status filter, nullable
     */
    public JulyAiDomainQuerySpec {
        keyword = StringUtil011.blankToNull(keyword);
        parentId = StringUtil011.blankToNull(parentId);
    }

    /** @return true when a keyword is present */
    public boolean hasKeyword() {
        return keyword != null;
    }

    /** @return true when a parent id is present */
    public boolean hasParentId() {
        return parentId != null;
    }

    /** @return true when a status is present */
    public boolean hasStatus() {
        return !StringUtil011.isBlank(status);
    }
}
