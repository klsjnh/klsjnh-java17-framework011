package com.klsjnh.infrastructure.messagecenter.inbound.repository;

/*                JulyInboundChannelRepositoryImpl class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july message channel repository impl class
 *
 */

import com.klsjnh.common.enums.Status011;

import com.klsjnh.domain.messagecenter.inbound.channel.JulyInboundChannel;
import com.klsjnh.domain.messagecenter.inbound.channel.JulyInboundChannelQuerySpec;
import com.klsjnh.domain.messagecenter.inbound.channel.JulyInboundChannelRepository;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import com.klsjnh.infrastructure.messagecenter.inbound.entity.JulyInboundChannelPo;
import com.klsjnh.infrastructure.messagecenter.inbound.mapper.JulyInboundChannelMapper;
import com.klsjnh.infrastructure.persistence.mapper.CommonMapper;
import com.klsjnh.infrastructure.persistence.repository.BaseRepository;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * Repository implementation for the JulyInboundChannel aggregate on the base
 * repository (july_message_inbound_channel, business unique column channel_code).
 */

@Repository
public class JulyInboundChannelRepositoryImpl
        extends BaseRepository<JulyInboundChannelPo, JulyInboundChannelMapper>
        implements JulyInboundChannelRepository {

    /**
     * Create the repository.
     *
     * @param mapper       mybatis-plus mapper
     * @param commonMapper native sql mapper
     */
    public JulyInboundChannelRepositoryImpl(JulyInboundChannelMapper mapper, CommonMapper commonMapper) {
        super(mapper, commonMapper);
    }

    /**
     * Table name for logs and maintenance statements.
     *
     * @return table name
     */
    @Override
    protected String getTableName() {
        return "july_message_inbound_channel";
    }

    /**
     * Business unique column name.
     *
     * @return column name
     */
    @Override
    protected String getBusinessColumn() {
        return "channel_code";
    }

    /**
     * Extract business value from entity.
     *
     * @param entity entity
     * @return field value
     */
    @Override
    protected Object getBusinessValue(JulyInboundChannelPo entity) {
        return entity.getChannelCode();
    }

    /**
     * Duplicate message for the unique channelCode.
     *
     * @return message
     */
    @Override
    protected String duplicateMessage() {
        return "channel code already exists";
    }

    /**
     * Insert a new aggregate.
     *
     * @param channel aggregate
     */
    @Override
    public void insert(JulyInboundChannel channel) {
        insert(toPo(channel));
    }

    /**
     * Update an existing aggregate.
     *
     * @param channel aggregate with id
     */
    @Override
    public void update(JulyInboundChannel channel) {
        update(toPo(channel));
    }

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return aggregate or null
     */
    @Override
    public JulyInboundChannel findById(String id) {
        JulyInboundChannelPo po = getById(id);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Find by channel code, enabled or not (management read entry).
     *
     * @param channelCode channel code
     * @return aggregate or null
     */
    @Override
    public JulyInboundChannel findByCode(String channelCode) {
        JulyInboundChannelPo po = getByBusinessValue(channelCode);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Find by channel code, ENABLED only (program read entry).
     *
     * @param channelCode channel code
     * @return aggregate or null
     */
    @Override
    public JulyInboundChannel findEnabledByCode(String channelCode) {
        QueryWrapper<JulyInboundChannelPo> wrapper = new QueryWrapper<>();
        wrapper.eq("channel_code", channelCode)
                .eq("status", Status011.ENABLED.getCode())
                .last("LIMIT 1");

        JulyInboundChannelPo po = mapper.selectOne(wrapper);

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
        JulyInboundChannelPo po = getById(id);

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
    public List<JulyInboundChannel> findPage(int offset, int pageSize, JulyInboundChannelQuerySpec spec) {
        int current = offset / pageSize + 1;
        Page<JulyInboundChannelPo> page = Page.of(current, pageSize);

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
    public long count(JulyInboundChannelQuerySpec spec) {
        return mapper.selectCount(specWrapper(spec));
    }

    /**
     * Filter wrapper shared by findPage and count.
     *
     * @param spec query condition
     * @return query wrapper
     */
    private QueryWrapper<JulyInboundChannelPo> specWrapper(JulyInboundChannelQuerySpec spec) {
        QueryWrapper<JulyInboundChannelPo> wrapper = new QueryWrapper<>();
        JulyInboundChannelQuerySpec query = spec == null ? new JulyInboundChannelQuerySpec(null, null) : spec;

        if (query.hasKeyword()) {
            String keyword = query.keyword();
            wrapper.and(w -> w.like("channel_code", keyword)
                    .or().like("channel_name", keyword)
                    .or().like("provider_type", keyword));
        }

        if (query.hasStatus()) {
            wrapper.eq("status", query.status());
        }

        wrapper.orderByAsc("sort_order").orderByAsc("id");

        return wrapper;
    }

    /**
     * Map the aggregate to a PO.
     *
     * @param channel aggregate
     * @return PO
     */
    private JulyInboundChannelPo toPo(JulyInboundChannel channel) {
        JulyInboundChannelPo po = new JulyInboundChannelPo();
        po.setId(channel.id().value());
        po.setSortOrder(channel.sortOrder());
        po.setChannelCode(channel.channelCode());
        po.setChannelName(channel.channelName());
        po.setProviderType(channel.providerType());
        po.setConfig(channel.config());
        po.setStatus(channel.status());
        po.setRemark(channel.remark());

        return po;
    }

    /**
     * Map a PO to the aggregate.
     *
     * @param po PO
     * @return aggregate
     */
    private JulyInboundChannel toAggregate(JulyInboundChannelPo po) {
        AuditInfo audit = new AuditInfo(po.getCreateBy(), po.getUpdateBy(), po.getCreateTime(), po.getUpdateTime());

        return new JulyInboundChannel(EntityId.of(po.getId()), po.getChannelCode(), po.getSortOrder(),
                po.getChannelName(), po.getProviderType(), po.getConfig(), po.getStatus(), po.getRemark(), audit);
    }
}
