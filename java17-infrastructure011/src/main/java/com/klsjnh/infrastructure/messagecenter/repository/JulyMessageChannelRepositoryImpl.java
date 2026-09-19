package com.klsjnh.infrastructure.messagecenter.repository;

/*                JulyMessageChannelRepositoryImpl class
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

import com.klsjnh.domain.messagecenter.channel.JulyMessageChannel;
import com.klsjnh.domain.messagecenter.channel.JulyMessageChannelQuerySpec;
import com.klsjnh.domain.messagecenter.channel.JulyMessageChannelRepository;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import com.klsjnh.infrastructure.messagecenter.entity.JulyMessageChannelPo;
import com.klsjnh.infrastructure.messagecenter.mapper.JulyMessageChannelMapper;
import com.klsjnh.infrastructure.persistence.mapper.CommonMapper;
import com.klsjnh.infrastructure.persistence.repository.BaseRepository;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * Repository implementation for the JulyMessageChannel aggregate on the base
 * repository (july_message_channel, business unique column channel_code).
 */

@Repository
public class JulyMessageChannelRepositoryImpl
        extends BaseRepository<JulyMessageChannelPo, JulyMessageChannelMapper>
        implements JulyMessageChannelRepository {

    /**
     * Create the repository.
     *
     * @param mapper       mybatis-plus mapper
     * @param commonMapper native sql mapper
     */
    public JulyMessageChannelRepositoryImpl(JulyMessageChannelMapper mapper, CommonMapper commonMapper) {
        super(mapper, commonMapper);
    }

    /**
     * Table name for logs and maintenance statements.
     *
     * @return table name
     */
    @Override
    protected String getTableName() {
        return "july_message_channel";
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
    protected Object getBusinessValue(JulyMessageChannelPo entity) {
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
    public void insert(JulyMessageChannel channel) {
        insert(toPo(channel));
    }

    /**
     * Update an existing aggregate.
     *
     * @param channel aggregate with id
     */
    @Override
    public void update(JulyMessageChannel channel) {
        update(toPo(channel));
    }

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return aggregate or null
     */
    @Override
    public JulyMessageChannel findById(String id) {
        JulyMessageChannelPo po = getById(id);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Find by channel code, enabled or not (management read entry).
     *
     * @param channelCode channel code
     * @return aggregate or null
     */
    @Override
    public JulyMessageChannel findByCode(String channelCode) {
        JulyMessageChannelPo po = getByBusinessValue(channelCode);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Find by channel code, ENABLED only (program read entry).
     *
     * @param channelCode channel code
     * @return aggregate or null
     */
    @Override
    public JulyMessageChannel findEnabledByCode(String channelCode) {
        QueryWrapper<JulyMessageChannelPo> wrapper = new QueryWrapper<>();
        wrapper.eq("channel_code", channelCode)
                .eq("status", Status011.ENABLED.getCode())
                .last("LIMIT 1");

        JulyMessageChannelPo po = mapper.selectOne(wrapper);

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
        JulyMessageChannelPo po = getById(id);

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
    public List<JulyMessageChannel> findPage(int offset, int pageSize, JulyMessageChannelQuerySpec spec) {
        int current = offset / pageSize + 1;
        Page<JulyMessageChannelPo> page = Page.of(current, pageSize);

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
    public long count(JulyMessageChannelQuerySpec spec) {
        return mapper.selectCount(specWrapper(spec));
    }

    /**
     * Filter wrapper shared by findPage and count.
     *
     * @param spec query condition
     * @return query wrapper
     */
    private QueryWrapper<JulyMessageChannelPo> specWrapper(JulyMessageChannelQuerySpec spec) {
        QueryWrapper<JulyMessageChannelPo> wrapper = new QueryWrapper<>();
        JulyMessageChannelQuerySpec query = spec == null ? new JulyMessageChannelQuerySpec(null, null) : spec;

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
    private JulyMessageChannelPo toPo(JulyMessageChannel channel) {
        JulyMessageChannelPo po = new JulyMessageChannelPo();
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
    private JulyMessageChannel toAggregate(JulyMessageChannelPo po) {
        AuditInfo audit = new AuditInfo(po.getCreateBy(), po.getUpdateBy(), po.getCreateTime(), po.getUpdateTime());

        return new JulyMessageChannel(EntityId.of(po.getId()), po.getChannelCode(), po.getSortOrder(),
                po.getChannelName(), po.getProviderType(), po.getConfig(), po.getStatus(), po.getRemark(), audit);
    }
}
