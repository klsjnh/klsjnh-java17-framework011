package com.klsjnh.domain.ai011;

/*                AiModelProviderApiRepository interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  ai model provider api repository interface
 *
 */

import java.util.List;

/**
 * Repository port for the AiModelProviderApi child entity: key reads by master
 * and the per-master uniqueness support.
 */

public interface AiModelProviderApiRepository {

    /**
     * Insert a new api key.
     *
     * @param api entity
     */
    void insert(AiModelProviderApi api);

    /**
     * Update an existing api key.
     *
     * @param api entity with id
     */
    void update(AiModelProviderApi api);

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return entity or null
     */
    AiModelProviderApi findById(String id);

    /**
     * Find the ENABLED keys of a provider, ordered by sort order.
     *
     * @param providerId provider id
     * @return ordered entities, never null
     */
    List<AiModelProviderApi> findByMaster(String providerId);

    /**
     * Find the keys of a provider (management view, optional status filter),
     * ordered by sort order.
     *
     * @param providerId provider id
     * @param status     optional status filter, null for all
     * @return ordered entities, never null
     */
    List<AiModelProviderApi> findAllByMaster(String providerId, String status);

    /**
     * Find one key by provider id and api code.
     *
     * @param providerId provider id
     * @param apiCode    api code
     * @return entity or null
     */
    AiModelProviderApi findByMasterAndCode(String providerId, String apiCode);

    /**
     * Count the alive keys of a provider.
     *
     * @param providerId provider id
     * @return alive key count
     */
    long countByMaster(String providerId);

    /**
     * Logic delete by primary key.
     *
     * @param id primary key
     * @return true when a row was deleted
     */
    boolean logicDeleteById(String id);
}
