package com.klsjnh.domain.system011.dictionary;

/*                JulyDictionaryItemRepository interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july dictionary item repository interface
 *
 */

import java.util.List;

/**
 * Repository port for the JulyDictionaryItem child entity: item reads by master
 * and the per-master uniqueness support.
 */

public interface JulyDictionaryItemRepository {

    /**
     * Insert a new item.
     *
     * @param item entity
     */
    void insert(JulyDictionaryItem item);

    /**
     * Update an existing item.
     *
     * @param item entity with id
     */
    void update(JulyDictionaryItem item);

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return entity or null
     */
    JulyDictionaryItem findById(String id);

    /**
     * Find the ENABLED items of a dictionary, ordered by sort order.
     *
     * @param dictionaryId dictionary id
     * @return ordered entities, never null
     */
    List<JulyDictionaryItem> findByMaster(String dictionaryId);

    /**
     * Find the items of a dictionary (management view, optional status filter).
     *
     * @param dictionaryId dictionary id
     * @param status       optional status filter, null for all
     * @return ordered entities, never null
     */
    List<JulyDictionaryItem> findAllByMaster(String dictionaryId, String status);

    /**
     * Find one item by dictionary id and item code.
     *
     * @param dictionaryId dictionary id
     * @param itemCode     item code
     * @return entity or null
     */
    JulyDictionaryItem findByMasterAndCode(String dictionaryId, String itemCode);

    /**
     * Count the alive items of a dictionary.
     *
     * @param dictionaryId dictionary id
     * @return alive item count
     */
    long countByMaster(String dictionaryId);

    /**
     * Logic delete by primary key.
     *
     * @param id primary key
     * @return true when a row was deleted
     */
    boolean logicDeleteById(String id);
}
