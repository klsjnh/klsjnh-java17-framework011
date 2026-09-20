package com.klsjnh.domain.datasource.sync;

/*                JulySyncRuleColumnRepository interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  sync rule column repository port
 *
 */

import java.util.List;

/**
 * Repository port for the {@link JulySyncRuleColumn} aggregate (child table).
 */

public interface JulySyncRuleColumnRepository {

    /**
     * Insert a new column row.
     *
     * @param column column row
     */
    void insert(JulySyncRuleColumn column);

    /**
     * Update an existing column row.
     *
     * @param column column row with id
     */
    void update(JulySyncRuleColumn column);

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return column row or null
     */
    JulySyncRuleColumn findById(String id);

    /**
     * Find all enabled rows of a rule, ordered.
     *
     * @param pkMt master id
     * @return column rows
     */
    List<JulySyncRuleColumn> findByMaster(String pkMt);

    /**
     * Count rows of a rule (including deleted when requested).
     *
     * @param pkMt        master id
     * @param enabledOnly enabled only
     * @return row count
     */
    long countByMaster(String pkMt, boolean enabledOnly);

    /**
     * Logic delete by primary key.
     *
     * @param id primary key
     * @return true when deleted
     */
    boolean logicDeleteById(String id);

    /**
     * Logic delete all rows of a rule.
     *
     * @param pkMt master id
     */
    void logicDeleteByMaster(String pkMt);
}
