package com.klsjnh.domain.system011.scheduler;

/*                JulySchedulerRepository interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july scheduler repository interface
 *
 */

import java.util.List;

/**
 * Repository port for the JulyScheduler aggregate. Implemented in
 * infrastructure on MyBatis-Plus.
 */

public interface JulySchedulerRepository {

    /**
     * Insert a new aggregate.
     *
     * @param scheduler aggregate in stopped state
     */
    void insert(JulyScheduler scheduler);

    /**
     * Update an existing aggregate.
     *
     * @param scheduler aggregate with id
     */
    void update(JulyScheduler scheduler);

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return aggregate or null
     */
    JulyScheduler findById(String id);

    /**
     * Find by the unique scheduler code.
     *
     * @param code scheduler code
     * @return aggregate or null
     */
    JulyScheduler findByCode(String code);

    /**
     * Logic delete by primary key.
     *
     * @param id primary key
     * @return true when a row was deleted
     */
    boolean logicDeleteById(String id);

/**
     * Logic delete many aggregates, all-or-nothing: a single missing id fails
     * the whole call (throws) so the caller transaction rolls back.
     *
     * @param ids primary keys
     */
    void logicDeleteByIds(List<String> ids);

    /**
     * Offset based page query with optional keyword filters.
     *
     * @param offset      zero-based row offset
     * @param pageSize    page size
     * @param codeKeyword scheduler code keyword, nullable
     * @param nameKeyword scheduler name keyword, nullable
     * @return page rows
     */
    List<JulyScheduler> findPage(int offset, int pageSize, String codeKeyword, String nameKeyword);

    /**
     * Count with the same filters as findPage.
     *
     * @param codeKeyword scheduler code keyword, nullable
     * @param nameKeyword scheduler name keyword, nullable
     * @return total row count
     */
    long count(String codeKeyword, String nameKeyword);
}
