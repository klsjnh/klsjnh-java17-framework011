package com.klsjnh.domain.storagecenter;

/*                JulyStorageRepository interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july storage repository interface
 *
 */

import java.util.List;

/**
 * Repository port for the JulyStorage aggregate.
 */

public interface JulyStorageRepository {

    /**
     * Insert a new aggregate.
     *
     * @param storage aggregate
     */
    void insert(JulyStorage storage);

    /**
     * Update an existing aggregate.
     *
     * @param storage aggregate with id
     */
    void update(JulyStorage storage);

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return aggregate or null
     */
    JulyStorage findById(String id);

    /**
     * Find by storage code, enabled or not (management read entry).
     *
     * @param storageCode storage code
     * @return aggregate or null
     */
    JulyStorage findByCode(String storageCode);

    /**
     * Find by storage code, ENABLED only (runtime resolver entry).
     *
     * @param storageCode storage code
     * @return aggregate or null
     */
    JulyStorage findEnabledByCode(String storageCode);

    /**
     * Whether a storage code exists at all, INCLUDING logic-deleted rows — the
     * seed idempotency check.
     *
     * @param storageCode storage code
     * @return true when a row exists, deleted or not
     */
    boolean existsIncludingDeleted(String storageCode);

    /**
     * Logic delete by primary key.
     *
     * @param id primary key
     * @return true when a row was deleted
     */
    boolean logicDeleteById(String id);

    /**
     * Offset based page query on the management view.
     *
     * @param offset   zero-based row offset
     * @param pageSize page size
     * @param spec     query condition
     * @return page rows
     */
    List<JulyStorage> findPage(int offset, int pageSize, JulyStorageQuerySpec spec);

    /**
     * Count with the same filter as findPage.
     *
     * @param spec query condition
     * @return total row count
     */
    long count(JulyStorageQuerySpec spec);
}
