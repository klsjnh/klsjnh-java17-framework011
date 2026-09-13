package com.klsjnh.demo11.domain;

/*                Demo011Repository interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  demo 011 repository interface
 *
 */

import java.util.List;

/**
 * Repository port for the Demo011 aggregate — the reference port shape for
 * third-party business systems.
 */

public interface Demo011Repository {

    /**
     * Insert a new aggregate.
     *
     * @param demo aggregate
     */
    void insert(Demo011 demo);

    /**
     * Update an existing aggregate.
     *
     * @param demo aggregate with id
     */
    void update(Demo011 demo);

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return aggregate or null
     */
    Demo011 findById(String id);

    /**
     * Find by the unique code.
     *
     * @param code demo code
     * @return aggregate or null
     */
    Demo011 findByCode(String code);

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
     * @param keyword  code / name keyword, nullable
     * @return page rows
     */
    List<Demo011> findPage(int offset, int pageSize, String keyword);

    /**
     * Count with the same filter as findPage.
     *
     * @param keyword code / name keyword, nullable
     * @return total row count
     */
    long count(String keyword);
}
