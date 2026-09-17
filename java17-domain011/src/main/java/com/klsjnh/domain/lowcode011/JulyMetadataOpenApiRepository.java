package com.klsjnh.domain.lowcode011;

/*                JulyMetadataOpenApiRepository interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  open api config repository interface
 *
 */

/**
 * Repository for the open API configuration of a low-code object (one row per
 * object).
 */

public interface JulyMetadataOpenApiRepository {

    /**
     * Find the configuration of an object.
     *
     * @param objectName object name
     * @return configuration or null
     */
    JulyMetadataOpenApi findByObjectName(String objectName);

    /**
     * Insert a configuration.
     *
     * @param config configuration
     */
    void insert(JulyMetadataOpenApi config);

    /**
     * Update a configuration by object name.
     *
     * @param config configuration
     */
    void update(JulyMetadataOpenApi config);
}
