package com.klsjnh.domain.system011.dictionary;

/*                JulyDictionaryRepository interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july dictionary repository interface
 *
 */

import java.util.List;

/**
 * Repository port for the JulyDictionary aggregate. {@link #findEnabledByCode}
 * is the program read entry; the paged queries serve the management view.
 */

public interface JulyDictionaryRepository {

    /**
     * Insert a new aggregate.
     *
     * @param dictionary aggregate
     */
    void insert(JulyDictionary dictionary);

    /**
     * Update an existing aggregate.
     *
     * @param dictionary aggregate with id
     */
    void update(JulyDictionary dictionary);

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return aggregate or null
     */
    JulyDictionary findById(String id);

    /**
     * Find by dictionary code, enabled or not (management read entry).
     *
     * @param dictionaryCode dictionary code
     * @return aggregate or null
     */
    JulyDictionary findByCode(String dictionaryCode);

    /**
     * Find by dictionary code, ENABLED only (program read entry).
     *
     * @param dictionaryCode dictionary code
     * @return aggregate or null
     */
    JulyDictionary findEnabledByCode(String dictionaryCode);

    /**
     * Every ENABLED dictionary, ordered by sort order.
     *
     * @return enabled aggregates, never null
     */
    List<JulyDictionary> findAllEnabled();

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
    List<JulyDictionary> findPage(int offset, int pageSize, JulyDictionaryQuerySpec spec);

    /**
     * Count with the same filter as findPage.
     *
     * @param spec query condition
     * @return total row count
     */
    long count(JulyDictionaryQuerySpec spec);
}
