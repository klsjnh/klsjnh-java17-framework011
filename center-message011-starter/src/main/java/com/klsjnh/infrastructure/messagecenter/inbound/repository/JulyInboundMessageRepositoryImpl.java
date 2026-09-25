package com.klsjnh.infrastructure.messagecenter.inbound.repository;

/*                JulyInboundMessageRepositoryImpl class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july inbound message repository impl class
 *
 */

import com.klsjnh.domain.messagecenter.inbound.message.JulyInboundMessage;
import com.klsjnh.domain.messagecenter.inbound.message.JulyInboundMessageQuerySpec;
import com.klsjnh.domain.messagecenter.inbound.message.JulyInboundMessageRepository;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import com.klsjnh.infrastructure.messagecenter.inbound.entity.JulyInboundMessagePo;
import com.klsjnh.infrastructure.messagecenter.inbound.mapper.JulyInboundMessageMapper;
import com.klsjnh.infrastructure.persistence.mapper.CommonMapper;
import com.klsjnh.infrastructure.persistence.repository.BaseRepository;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * Repository implementation for the JulyInboundMessage aggregate on the base
 * repository (july_message_inbound, no business unique key).
 */

@Repository
public class JulyInboundMessageRepositoryImpl extends BaseRepository<JulyInboundMessagePo, JulyInboundMessageMapper>
        implements JulyInboundMessageRepository {

    /**
     * Create the repository.
     *
     * @param mapper       mybatis-plus mapper
     * @param commonMapper native sql mapper
     */
    public JulyInboundMessageRepositoryImpl(JulyInboundMessageMapper mapper, CommonMapper commonMapper) {
        super(mapper, commonMapper);
    }

    /**
     * Table name for logs and maintenance statements.
     *
     * @return table name
     */
    @Override
    protected String getTableName() {
        return "july_message_inbound";
    }

    /**
     * Business unique column name; blank — july_message_inbound has no business key.
     *
     * @return empty string
     */
    @Override
    protected String getBusinessColumn() {
        return "";
    }

    /**
     * Insert a new aggregate.
     *
     * @param message aggregate
     */
    @Override
    public void insert(JulyInboundMessage message) {
        insert(toPo(message));
    }

    /**
     * Update an existing aggregate.
     *
     * @param message aggregate with id
     */
    @Override
    public void update(JulyInboundMessage message) {
        update(toPo(message));
    }

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return aggregate or null
     */
    @Override
    public JulyInboundMessage findById(String id) {
        JulyInboundMessagePo po = getById(id);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Find by channel code and channel-side message id (dedupe read).
     *
     * @param channelCode  channel code
     * @param rawMessageId channel-side message id
     * @return aggregate or null
     */
    @Override
    public JulyInboundMessage findByChannelAndRawId(String channelCode, String rawMessageId) {
        QueryWrapper<JulyInboundMessagePo> wrapper = new QueryWrapper<>();
        wrapper.eq("channel_code", channelCode)
                .eq("raw_message_id", rawMessageId)
                .last("LIMIT 1");

        JulyInboundMessagePo po = mapper.selectOne(wrapper);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Logic delete by primary key.
     *
     * @param id primary key
     * @return true when a row was deleted
     */
    @Override
    public boolean logicDeleteById(String id) {
        JulyInboundMessagePo po = getById(id);

        if (po == null) {
            return false;
        }

        logicDelete(po);

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void logicDeleteByIds(List<String> ids) {
        batchLogicDelete(ids);
    }

    /**
     * Offset based page query on the management view.
     *
     * @param offset   zero-based row offset
     * @param pageSize page size
     * @param spec     query condition
     * @return page rows
     */
    @Override
    public List<JulyInboundMessage> findPage(int offset, int pageSize, JulyInboundMessageQuerySpec spec) {
        int current = offset / pageSize + 1;
        Page<JulyInboundMessagePo> page = Page.of(current, pageSize);

        return mapper.selectPage(page, specWrapper(spec)).getRecords().stream()
                .map(this::toAggregate)
                .toList();
    }

    /**
     * Count with the same filter as findPage.
     *
     * @param spec query condition
     * @return total row count
     */
    @Override
    public long count(JulyInboundMessageQuerySpec spec) {
        return mapper.selectCount(specWrapper(spec));
    }

    /**
     * Filter wrapper shared by findPage and count.
     *
     * @param spec query condition
     * @return query wrapper
     */
    private QueryWrapper<JulyInboundMessagePo> specWrapper(JulyInboundMessageQuerySpec spec) {
        QueryWrapper<JulyInboundMessagePo> wrapper = new QueryWrapper<>();
        JulyInboundMessageQuerySpec query = spec == null ? new JulyInboundMessageQuerySpec(null, null, null) : spec;

        if (query.hasChannelCode()) {
            wrapper.eq("channel_code", query.channelCode());
        }

        if (query.hasStatus()) {
            wrapper.eq("status", query.status());
        }

        if (query.hasKeyword()) {
            String keyword = query.keyword();
            wrapper.and(w -> w.like("from_id", keyword)
                    .or().like("content", keyword)
                    .or().like("raw_message_id", keyword));
        }

        wrapper.orderByDesc("create_time").orderByDesc("id");

        return wrapper;
    }

    /**
     * Map the aggregate to a PO.
     *
     * @param message aggregate
     * @return PO
     */
    private JulyInboundMessagePo toPo(JulyInboundMessage message) {
        JulyInboundMessagePo po = new JulyInboundMessagePo();
        po.setId(message.id().value());

        po.setChannelCode(message.channelCode());
        po.setProviderType(message.providerType());
        po.setMessageType(message.messageType());
        po.setPayload(message.payload());
        po.setFromId(message.fromId());
        po.setContent(message.content());
        po.setRawMessageId(message.rawMessageId());

        po.setError(message.error());
        po.setStatus(message.status());
        po.setRemark(message.remark());

        return po;
    }

    /**
     * Map a PO to the aggregate.
     *
     * @param po PO
     * @return aggregate
     */
    private JulyInboundMessage toAggregate(JulyInboundMessagePo po) {
        AuditInfo audit = new AuditInfo(po.getCreateBy(), po.getUpdateBy(), po.getCreateTime(), po.getUpdateTime());

        return new JulyInboundMessage(EntityId.of(po.getId()), po.getChannelCode(), po.getProviderType(),
                po.getMessageType(), po.getPayload(), po.getFromId(), po.getContent(), po.getRawMessageId(),
                po.getError(), po.getRemark(), po.getStatus(), audit);
    }
}
