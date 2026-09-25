package com.klsjnh.domain.messagecenter.outbound.template;

/*                JulyOutboundTemplateRepository interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july message template repository interface
 *
 */

import java.util.List;

/**
 * Repository port for the JulyOutboundTemplate aggregate.
 */

public interface JulyOutboundTemplateRepository {

    /**
     * Insert a new aggregate.
     *
     * @param template aggregate
     */
    void insert(JulyOutboundTemplate template);

    /**
     * Update an existing aggregate.
     *
     * @param template aggregate with id
     */
    void update(JulyOutboundTemplate template);

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return aggregate or null
     */
    JulyOutboundTemplate findById(String id);

    /**
     * Find by template code, enabled or not (management read entry).
     *
     * @param templateCode template code
     * @return aggregate or null
     */
    JulyOutboundTemplate findByCode(String templateCode);

    /**
     * Find by template code, ENABLED only (program read entry).
     *
     * @param templateCode template code
     * @return aggregate or null
     */
    JulyOutboundTemplate findEnabledByCode(String templateCode);

    /**
     * Logic delete by primary key.
     *
     * @param id primary key
     * @return true when a row was deleted
     */
    boolean logicDeleteById(String id);

    /**
     * Logic delete many, all-or-nothing (throws when any id is missing).
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
    List<JulyOutboundTemplate> findPage(int offset, int pageSize, JulyOutboundTemplateQuerySpec spec);

    /**
     * Count with the same filter as findPage.
     *
     * @param spec query condition
     * @return total row count
     */
    long count(JulyOutboundTemplateQuerySpec spec);
}
