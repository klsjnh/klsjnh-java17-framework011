package com.klsjnh.domain.messagecenter.inbound.channel;

/*                JulyInboundChannelRepository interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july message channel repository interface
 *
 */

import java.util.List;

/**
 * Repository port for the JulyInboundChannel aggregate.
 */

public interface JulyInboundChannelRepository {

    /**
     * Insert a new aggregate.
     *
     * @param channel aggregate
     */
    void insert(JulyInboundChannel channel);

    /**
     * Update an existing aggregate.
     *
     * @param channel aggregate with id
     */
    void update(JulyInboundChannel channel);

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return aggregate or null
     */
    JulyInboundChannel findById(String id);

    /**
     * Find by channel code, enabled or not (management read entry).
     *
     * @param channelCode channel code
     * @return aggregate or null
     */
    JulyInboundChannel findByCode(String channelCode);

    /**
     * Find by channel code, ENABLED only (program read entry).
     *
     * @param channelCode channel code
     * @return aggregate or null
     */
    JulyInboundChannel findEnabledByCode(String channelCode);

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
    List<JulyInboundChannel> findPage(int offset, int pageSize, JulyInboundChannelQuerySpec spec);

    /**
     * Count with the same filter as findPage.
     *
     * @param spec query condition
     * @return total row count
     */
    long count(JulyInboundChannelQuerySpec spec);
}
