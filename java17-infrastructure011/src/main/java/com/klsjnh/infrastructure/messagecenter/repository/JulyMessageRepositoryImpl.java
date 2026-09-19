package com.klsjnh.infrastructure.messagecenter.repository;

/*                JulyMessageRepositoryImpl class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july message repository impl class
 *
 */

import com.klsjnh.domain.messagecenter.message.JulyMessage;
import com.klsjnh.domain.messagecenter.message.JulyMessageQuerySpec;
import com.klsjnh.domain.messagecenter.message.JulyMessageRepository;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import com.klsjnh.infrastructure.messagecenter.entity.JulyMessagePo;
import com.klsjnh.infrastructure.messagecenter.mapper.JulyMessageMapper;
import com.klsjnh.infrastructure.persistence.mapper.CommonMapper;
import com.klsjnh.infrastructure.persistence.repository.BaseRepository;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * Repository implementation for the JulyMessage aggregate on the base
 * repository (july_message, no business unique key).
 */

@Repository
public class JulyMessageRepositoryImpl extends BaseRepository<JulyMessagePo, JulyMessageMapper>
        implements JulyMessageRepository {

    /**
     * Create the repository.
     *
     * @param mapper       mybatis-plus mapper
     * @param commonMapper native sql mapper
     */
    public JulyMessageRepositoryImpl(JulyMessageMapper mapper, CommonMapper commonMapper) {
        super(mapper, commonMapper);
    }

    /**
     * Table name for logs and maintenance statements.
     *
     * @return table name
     */
    @Override
    protected String getTableName() {
        return "july_message";
    }

    /**
     * Business unique column name; blank — july_message has no business key.
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
    public void insert(JulyMessage message) {
        insert(toPo(message));
    }

    /**
     * Update an existing aggregate.
     *
     * @param message aggregate with id
     */
    @Override
    public void update(JulyMessage message) {
        update(toPo(message));
    }

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return aggregate or null
     */
    @Override
    public JulyMessage findById(String id) {
        JulyMessagePo po = getById(id);

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
        JulyMessagePo po = getById(id);

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
    public List<JulyMessage> findPage(int offset, int pageSize, JulyMessageQuerySpec spec) {
        int current = offset / pageSize + 1;
        Page<JulyMessagePo> page = Page.of(current, pageSize);

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
    public long count(JulyMessageQuerySpec spec) {
        return mapper.selectCount(specWrapper(spec));
    }

    /**
     * Filter wrapper shared by findPage and count.
     *
     * @param spec query condition
     * @return query wrapper
     */
    private QueryWrapper<JulyMessagePo> specWrapper(JulyMessageQuerySpec spec) {
        QueryWrapper<JulyMessagePo> wrapper = new QueryWrapper<>();
        JulyMessageQuerySpec query = spec == null ? new JulyMessageQuerySpec(null, null, null) : spec;

        if (query.hasChannelCode()) {
            wrapper.eq("channel_code", query.channelCode());
        }

        if (query.hasSendStatus()) {
            wrapper.eq("send_status", query.sendStatus());
        }

        if (query.hasKeyword()) {
            String keyword = query.keyword();
            wrapper.and(w -> w.like("msg_to", keyword)
                    .or().like("title", keyword)
                    .or().like("template_code", keyword));
        }

        wrapper.orderByAsc("sort_order").orderByAsc("id");

        return wrapper;
    }

    /**
     * Map the aggregate to a PO.
     *
     * @param message aggregate
     * @return PO
     */
    private JulyMessagePo toPo(JulyMessage message) {
        JulyMessagePo po = new JulyMessagePo();
        po.setId(message.id().value());
        po.setSortOrder(message.sortOrder());
        po.setChannelCode(message.channelCode());
        po.setProviderType(message.providerType());
        po.setMsgTo(message.msgTo());
        po.setTemplateCode(message.templateCode());
        po.setTitle(message.title());
        po.setContent(message.content());
        po.setSendStatus(message.sendStatus());
        po.setRetryCount(message.retryCount());
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
    private JulyMessage toAggregate(JulyMessagePo po) {
        AuditInfo audit = new AuditInfo(po.getCreateBy(), po.getUpdateBy(), po.getCreateTime(), po.getUpdateTime());

        return new JulyMessage(EntityId.of(po.getId()), po.getSortOrder(), po.getChannelCode(), po.getProviderType(),
                po.getMsgTo(), po.getTemplateCode(), po.getTitle(), po.getContent(), po.getSendStatus(),
                po.getRetryCount() == null ? 0 : po.getRetryCount(), po.getError(), po.getStatus(), po.getRemark(),
                audit);
    }
}
