package com.klsjnh.domain.messagecenter.inbound.message;

/*                JulyInboundMessageRepository interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july inbound message repository interface
 *
 */

import java.util.List;

/**
 * Repository port for the JulyInboundMessage aggregate. No business unique key;
 * de-duplication is a channel-scoped read on (channelCode, rawMessageId).
 */

public interface JulyInboundMessageRepository {

    /**
     * Insert a new aggregate.
     *
     * @param message aggregate
     */
    void insert(JulyInboundMessage message);

    /**
     * Update an existing aggregate.
     *
     * @param message aggregate with id
     */
    void update(JulyInboundMessage message);

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return aggregate or null
     */
    JulyInboundMessage findById(String id);

    /**
     * Find by channel code and channel-side message id (dedupe read).
     *
     * @param channelCode  channel code
     * @param rawMessageId channel-side message id
     * @return aggregate or null
     */
    JulyInboundMessage findByChannelAndRawId(String channelCode, String rawMessageId);

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
    List<JulyInboundMessage> findPage(int offset, int pageSize, JulyInboundMessageQuerySpec spec);

    /**
     * Count with the same filter as findPage.
     *
     * @param spec query condition
     * @return total row count
     */
    long count(JulyInboundMessageQuerySpec spec);
}
