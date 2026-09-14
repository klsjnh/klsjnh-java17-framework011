package com.klsjnh.domain.system011.config;

/*                JulyConfigRepository interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july config repository interface
 *
 */

import java.util.List;

/**
 * Repository port for the JulyConfig aggregate. Readers query the store every
 * time (no cache — user decision).
 */

public interface JulyConfigRepository {

    /**
     * Insert a new aggregate.
     *
     * @param config aggregate
     */
    void insert(JulyConfig config);

    /**
     * Update an existing aggregate.
     *
     * @param config aggregate with id
     */
    void update(JulyConfig config);

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return aggregate or null
     */
    JulyConfig findById(String id);

    /**
     * Find an ENABLED config by its unique key (disabled / missing → null) —
     * the program-facing read entry.
     *
     * @param code config key
     * @return aggregate or null
     */
    JulyConfig findEnabledByCode(String code);

    /**
     * Logic delete by primary key.
     *
     * @param id primary key
     * @return true when a row was deleted
     */
    boolean logicDeleteById(String id);

    /**
     * Offset based page query with an optional keyword filter.
     *
     * @param offset   zero-based row offset
     * @param pageSize page size
     * @param keyword  code / data keyword, nullable
     * @return page rows
     */
    List<JulyConfig> findPage(int offset, int pageSize, String keyword);

    /**
     * Count with the same filter as findPage.
     *
     * @param keyword code / data keyword, nullable
     * @return total row count
     */
    long count(String keyword);
}
