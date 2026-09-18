package com.klsjnh.domain.lowcode011;

/*                JulyMetadataRepository interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july metadata repository interface
 *
 */

import java.util.List;

/**
 * Repository port for the JulyMetadata aggregate. Insert / update / logic
 * delete persist the whole aggregate (master plus three child collections,
 * replace strategy).
 */

public interface JulyMetadataRepository {

    /**
     * Insert a new aggregate with its children.
     *
     * @param metadata aggregate
     */
    void insert(JulyMetadata metadata);

    /**
     * Update an existing aggregate (children replaced).
     *
     * @param metadata aggregate with id
     */
    void update(JulyMetadata metadata);

    /**
     * Find by primary key together with its children.
     *
     * @param id primary key
     * @return aggregate or null
     */
    JulyMetadata findById(String id);

    /**
     * Find by object name together with its children.
     *
     * @param objectName object name
     * @return aggregate or null
     */
    JulyMetadata findByObjectName(String objectName);

    /**
     * Logic delete the aggregate and cascade to its children.
     *
     * @param id primary key
     * @return true when a row was deleted
     */
    boolean logicDeleteById(String id);

    /**
     * Offset based page query on the master.
     *
     * @param offset   zero-based row offset
     * @param pageSize page size
     * @param spec     query condition
     * @return page rows (without children)
     */
    List<JulyMetadata> findPage(int offset, int pageSize, JulyMetadataQuerySpec spec);

    /**
     * Count with the same filter as findPage.
     *
     * @param spec query condition
     * @return total row count
     */
    long count(JulyMetadataQuerySpec spec);

    /**
     * Update the publish state pointer of an object (the DRAFT row keeps a
     * reference to its latest published version / physical table).
     *
     * @param id            object id
     * @param publishStatus publish status (DRAFT / PUBLISHED)
     * @param version       current published version
     * @param physicalTable physical table name
     */
    void updatePublishState(String id, String publishStatus, String version, String physicalTable);

    /**
     * Mark an object's data as initialized and stamp the last sync time.
     *
     * @param objectName object name
     */
    void markSynced(String objectName);

    /**
     * Whether an object's data has been initialized.
     *
     * @param objectName object name
     * @return true when initialized
     */
    boolean isSynced(String objectName);

    /**
     * Persist the source descriptor (datasource + probe sql) of an object.
     *
     * @param id             object id
     * @param dataSourceCode source datasource code
     * @param probeSql       probe sql
     */
    void updateSource(String id, String dataSourceCode, String probeSql);

    /**
     * Read the source descriptor of an object.
     *
     * @param objectName object name
     * @return source descriptor, null when the object is absent
     */
    JulyMetadataSource findSource(String objectName);
}
