package com.klsjnh.domain.dataservice011;

/*                JulyBusinessModelingQuerySpec class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july business modeling query spec class
 *
 */

import com.klsjnh.common.util.StringUtil011;

/**
 * JulyBusinessModeling page query condition: an optional keyword matched
 * against modelCode / modelName / objectName, an optional datasource filter,
 * plus an optional status filter. A null status means "both enabled and
 * disabled" (management view).
 *
 * @param keyword        modelCode / modelName / objectName keyword (fuzzy), nullable
 * @param dataSourceCode exact datasource code filter, nullable for all
 * @param status         row status filter, nullable for all
 */

public record JulyBusinessModelingQuerySpec(String keyword, String dataSourceCode, String status) {

    /**
     * Normalize the text filters (blank → null) and keep the status as given.
     *
     * @param keyword        modelCode / modelName / objectName keyword (fuzzy), nullable
     * @param dataSourceCode exact datasource code filter, nullable for all
     * @param status         row status filter, nullable for all
     */
    public JulyBusinessModelingQuerySpec {
        keyword = blankToNull(keyword);
        dataSourceCode = blankToNull(dataSourceCode);
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
     * Whether a datasource filter is present.
     *
     * @return true when a datasource code is present
     */
    public boolean hasDataSourceCode() {
        return dataSourceCode != null;
    }

    /**
     * Whether a status filter is present.
     *
     * @return true when a status is present
     */
    public boolean hasStatus() {
        return !StringUtil011.isBlank(status);
    }

    /**
     * Treat a blank text as absent.
     *
     * @param value raw value
     * @return trimmed value, or null when blank
     */
    private static String blankToNull(String value) {
        return StringUtil011.isBlank(value) ? null : value.trim();
    }
}
