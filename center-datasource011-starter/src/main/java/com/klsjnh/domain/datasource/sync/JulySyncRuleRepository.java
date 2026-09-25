package com.klsjnh.domain.datasource.sync;

/*                JulySyncRuleRepository interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  sync rule repository port
 *
 */

import java.util.List;

/**
 * Repository port for the {@link JulySyncRule} aggregate.
 */

public interface JulySyncRuleRepository {

    /**
     * Insert a new rule.
     *
     * @param rule rule
     */
    void insert(JulySyncRule rule);

    /**
     * Update an existing rule.
     *
     * @param rule rule with id
     */
    void update(JulySyncRule rule);

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return rule or null
     */
    JulySyncRule findById(String id);

    /**
     * Find by sync code, enabled or not.
     *
     * @param syncCode sync code
     * @return rule or null
     */
    JulySyncRule findByCode(String syncCode);

    /**
     * Logic delete by primary key.
     *
     * @param id primary key
     * @return true when deleted
     */
    boolean logicDeleteById(String id);

    /**
     * Logic delete many, all-or-nothing.
     *
     * @param ids primary keys
     */
    void logicDeleteByIds(List<String> ids);

    /**
     * Offset page query.
     *
     * @param offset   zero-based offset
     * @param pageSize page size
     * @param spec     query condition
     * @return page rows
     */
    List<JulySyncRule> findPage(int offset, int pageSize, JulySyncRuleQuerySpec spec);

    /**
     * Count with the same filter as findPage.
     *
     * @param spec query condition
     * @return total count
     */
    long count(JulySyncRuleQuerySpec spec);
}
