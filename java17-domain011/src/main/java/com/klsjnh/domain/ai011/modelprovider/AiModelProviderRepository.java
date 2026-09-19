package com.klsjnh.domain.ai011.modelprovider;

/*                AiModelProviderRepository interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  ai model provider repository interface
 *
 */

import java.util.List;

/**
 * Repository port for the AiModelProvider aggregate. {@link #findEnabledByCode}
 * is the program read entry; the paged queries serve the management view and
 * include disabled rows.
 */

public interface AiModelProviderRepository {

    /**
     * Insert a new aggregate.
     *
     * @param provider aggregate
     */
    void insert(AiModelProvider provider);

    /**
     * Update an existing aggregate.
     *
     * @param provider aggregate with id
     */
    void update(AiModelProvider provider);

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return aggregate or null
     */
    AiModelProvider findById(String id);

    /**
     * Find by provider code, enabled or not (management read entry).
     *
     * @param providerCode provider code
     * @return aggregate or null
     */
    AiModelProvider findByCode(String providerCode);

    /**
     * Find by provider code, ENABLED only (program read entry).
     *
     * @param providerCode provider code
     * @return aggregate or null
     */
    AiModelProvider findEnabledByCode(String providerCode);

    /**
     * Every ENABLED provider, ordered by sort order.
     *
     * @return enabled aggregates, never null
     */
    List<AiModelProvider> findAllEnabled();

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
    List<AiModelProvider> findPage(int offset, int pageSize, AiModelProviderQuerySpec spec);

    /**
     * Count with the same filter as findPage.
     *
     * @param spec query condition
     * @return total row count
     */
    long count(AiModelProviderQuerySpec spec);
}
