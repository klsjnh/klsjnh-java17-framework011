package com.klsjnh.domain.dataservice011;

/*                JulyBusinessModelingRepository interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july business modeling repository interface
 *
 */

import java.util.List;

/**
 * Repository port for the JulyBusinessModeling aggregate. The lookup methods
 * serve the uniqueness checks the use case performs before a write (modeling
 * code and object name are both globally unique), while the paged queries
 * serve the management view.
 */

public interface JulyBusinessModelingRepository {

    /**
     * Insert a new aggregate.
     *
     * @param modeling aggregate
     */
    void insert(JulyBusinessModeling modeling);

    /**
     * Update an existing aggregate.
     *
     * @param modeling aggregate with id
     */
    void update(JulyBusinessModeling modeling);

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return aggregate or null
     */
    JulyBusinessModeling findById(String id);

    /**
     * Find by modeling code, the uniqueness key of the management view.
     *
     * @param modelCode modeling code
     * @return aggregate or null
     */
    JulyBusinessModeling findByCode(String modelCode);

    /**
     * Find by low-code object name — the key the description is handed over
     * under, so it must stay globally unique.
     *
     * @param objectName object name
     * @return aggregate or null
     */
    JulyBusinessModeling findByObjectName(String objectName);

    /**
     * Count rows carrying an object name other than the given one — the
     * "already taken by another row" check used on update, where the current
     * row must not collide with itself.
     *
     * @param objectName     object name
     * @param excludeId      primary key to exclude, nullable for none
     * @return matching row count
     */
    long countByObjectNameExcluding(String objectName, String excludeId);

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
    List<JulyBusinessModeling> findPage(int offset, int pageSize, JulyBusinessModelingQuerySpec spec);

    /**
     * Count with the same filter as findPage.
     *
     * @param spec query condition
     * @return total row count
     */
    long count(JulyBusinessModelingQuerySpec spec);
}
