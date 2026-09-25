package com.klsjnh.domain.datasource.management;

/*                JulyDatasourceRepository interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july datasource repository interface
 *
 */

import com.klsjnh.domain.datasource.management.JulyDatasourceQuerySpec;

import java.util.List;

/**
 * Repository port for the JulyDatasource aggregate. {@link #findAllEnabled()} is
 * the runtime read entry feeding the dynamic datasource registry; the paged
 * queries serve the management view and include disabled rows.
 */

public interface JulyDatasourceRepository {

    /**
     * Insert a new aggregate.
     *
     * @param datasource aggregate
     */

    void insert(JulyDatasource datasource);

    /**
     * Update an existing aggregate.
     *
     * @param datasource aggregate with id
     */

    void update(JulyDatasource datasource);

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return aggregate or null
     */

    JulyDatasource findById(String id);

    /**
     * Find by datasource code, enabled or not (management read entry).
     *
     * @param dsCode datasource code
     * @return aggregate or null
     */

    JulyDatasource findByCode(String dsCode);

    /**
     * Whether a dsCode exists at all, INCLUDING logic-deleted rows — the seed
     * idempotency check.
     * <p>
     * The bootstrap yaml is a one-shot seed: on startup a yaml entry is
     * inserted only when its dsCode has never been seen. A row deleted from the
     * management page therefore keeps blocking the seed and stays a tombstone —
     * deleting a datasource must not make it reappear after a restart. This is
     * why the check cannot go through {@link #findByCode}, which the
     * logic-delete interceptor filters down to {@code dr='0'}.
     * </p>
     *
     * @param dsCode datasource code
     * @return true when a row exists under this dsCode, deleted or not
     */

    boolean existsIncludingDeleted(String dsCode);

    /**
     * Runtime read entry: every ENABLED datasource, ordered by dsCode — the
     * snapshot the dynamic datasource registry reloads from.
     *
     * @return enabled aggregates, never null
     */

    List<JulyDatasource> findAllEnabled();

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
     * Offset based page query on the management view.
     *
     * @param offset   zero-based row offset
     * @param pageSize page size
     * @param spec     query condition
     * @return page rows
     */

    List<JulyDatasource> findPage(int offset, int pageSize, JulyDatasourceQuerySpec spec);

    /**
     * Count with the same filter as findPage.
     *
     * @param spec query condition
     * @return total row count
     */

    long count(JulyDatasourceQuerySpec spec);
}
