package com.klsjnh.domain.messagecenter.message;

/*                JulyMessageRepository interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july message repository interface
 *
 */

import java.util.List;

/**
 * Repository port for the JulyMessage aggregate. No business unique key.
 */

public interface JulyMessageRepository {

    /**
     * Insert a new aggregate.
     *
     * @param message aggregate
     */
    void insert(JulyMessage message);

    /**
     * Update an existing aggregate.
     *
     * @param message aggregate with id
     */
    void update(JulyMessage message);

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return aggregate or null
     */
    JulyMessage findById(String id);

    /**
     * Logic delete by primary key.
     *
     * @param id primary key
     * @return true when a row was deleted
     */
    boolean logicDeleteById(String id);

    /**
     * Logic delete many, all-or-nothing (throws when any id is missing).
     *
     * @param ids primary keys
     */
    void logicDeleteByIds(List<String> ids);

    /**
     * Offset based page query on the management view.
     *
     * @param offset   zero-based row offset
     * @param pageSize page size
     * @param spec     query condition
     * @return page rows
     */
    List<JulyMessage> findPage(int offset, int pageSize, JulyMessageQuerySpec spec);

    /**
     * Count with the same filter as findPage.
     *
     * @param spec query condition
     * @return total row count
     */
    long count(JulyMessageQuerySpec spec);
}
