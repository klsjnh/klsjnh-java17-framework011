package com.klsjnh.domain.dataservice011;

/*                JulyDatasourceQuerySpec class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july datasource query spec class
 *
 */

import com.klsjnh.common.util.StringUtil011;

/**
 * JulyDatasource page query condition: an optional keyword matched against
 * dsCode / dsName / jdbcUrl, plus an optional status filter. A null status
 * means "both enabled and disabled" (management view), while the runtime
 * registry only ever reads enabled rows.
 *
 * @param keyword dsCode / dsName / jdbcUrl keyword (fuzzy), nullable
 * @param status  row status filter, nullable for all
 */

public record JulyDatasourceQuerySpec(String keyword, String status) {

    /**
     * Normalize the keyword (blank → null) and keep the status as given.
     *
     * @param keyword dsCode / dsName / jdbcUrl keyword (fuzzy), nullable
     * @param status  row status filter, nullable for all
     */
    public JulyDatasourceQuerySpec {
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
