package com.klsjnh.infrastructure.messagecenter.outbound.repository;

/*                JulyOutboundTemplateRepositoryImpl class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july message template repository impl class
 *
 */

import com.klsjnh.common.enums.Status011;

import com.klsjnh.domain.messagecenter.outbound.template.JulyOutboundTemplate;
import com.klsjnh.domain.messagecenter.outbound.template.JulyOutboundTemplateQuerySpec;
import com.klsjnh.domain.messagecenter.outbound.template.JulyOutboundTemplateRepository;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import com.klsjnh.infrastructure.messagecenter.outbound.entity.JulyOutboundTemplatePo;
import com.klsjnh.infrastructure.messagecenter.outbound.mapper.JulyOutboundTemplateMapper;
import com.klsjnh.infrastructure.persistence.mapper.CommonMapper;
import com.klsjnh.infrastructure.persistence.repository.BaseRepository;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * Repository implementation for the JulyOutboundTemplate aggregate on the base
 * repository (july_message_outbound_template, business unique column template_code).
 */

@Repository
public class JulyOutboundTemplateRepositoryImpl
        extends BaseRepository<JulyOutboundTemplatePo, JulyOutboundTemplateMapper>
        implements JulyOutboundTemplateRepository {

    /**
     * Create the repository.
     *
     * @param mapper       mybatis-plus mapper
     * @param commonMapper native sql mapper
     */
    public JulyOutboundTemplateRepositoryImpl(JulyOutboundTemplateMapper mapper, CommonMapper commonMapper) {
        super(mapper, commonMapper);
    }

    /**
     * Table name for logs and maintenance statements.
     *
     * @return table name
     */
    @Override
    protected String getTableName() {
        return "july_message_outbound_template";
    }

    /**
     * Business unique column name.
     *
     * @return column name
     */
    @Override
    protected String getBusinessColumn() {
        return "template_code";
    }

    /**
     * Extract business value from entity.
     *
     * @param entity entity
     * @return field value
     */
    @Override
    protected Object getBusinessValue(JulyOutboundTemplatePo entity) {
        return entity.getTemplateCode();
    }

    /**
     * Duplicate message for the unique templateCode.
     *
     * @return message
     */
    @Override
    protected String duplicateMessage() {
        return "template code already exists";
    }

    /**
     * Insert a new aggregate.
     *
     * @param template aggregate
     */
    @Override
    public void insert(JulyOutboundTemplate template) {
        insert(toPo(template));
    }

    /**
     * Update an existing aggregate.
     *
     * @param template aggregate with id
     */
    @Override
    public void update(JulyOutboundTemplate template) {
        update(toPo(template));
    }

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return aggregate or null
     */
    @Override
    public JulyOutboundTemplate findById(String id) {
        JulyOutboundTemplatePo po = getById(id);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Find by template code, enabled or not (management read entry).
     *
     * @param templateCode template code
     * @return aggregate or null
     */
    @Override
    public JulyOutboundTemplate findByCode(String templateCode) {
        JulyOutboundTemplatePo po = getByBusinessValue(templateCode);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Find by template code, ENABLED only (program read entry).
     *
     * @param templateCode template code
     * @return aggregate or null
     */
    @Override
    public JulyOutboundTemplate findEnabledByCode(String templateCode) {
        QueryWrapper<JulyOutboundTemplatePo> wrapper = new QueryWrapper<>();
        wrapper.eq("template_code", templateCode)
                .eq("status", Status011.ENABLED.getCode())
                .last("LIMIT 1");

        JulyOutboundTemplatePo po = mapper.selectOne(wrapper);

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
        JulyOutboundTemplatePo po = getById(id);

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
    public List<JulyOutboundTemplate> findPage(int offset, int pageSize, JulyOutboundTemplateQuerySpec spec) {
        int current = offset / pageSize + 1;
        Page<JulyOutboundTemplatePo> page = Page.of(current, pageSize);

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
    public long count(JulyOutboundTemplateQuerySpec spec) {
        return mapper.selectCount(specWrapper(spec));
    }

    /**
     * Filter wrapper shared by findPage and count.
     *
     * @param spec query condition
     * @return query wrapper
     */
    private QueryWrapper<JulyOutboundTemplatePo> specWrapper(JulyOutboundTemplateQuerySpec spec) {
        QueryWrapper<JulyOutboundTemplatePo> wrapper = new QueryWrapper<>();
        JulyOutboundTemplateQuerySpec query = spec == null ? new JulyOutboundTemplateQuerySpec(null, null, null) : spec;

        if (query.hasKeyword()) {
            String keyword = query.keyword();
            wrapper.and(w -> w.like("template_code", keyword)
                    .or().like("template_name", keyword));
        }

        if (query.hasChannelCode()) {
            wrapper.eq("channel_code", query.channelCode());
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
     * @param template aggregate
     * @return PO
     */
    private JulyOutboundTemplatePo toPo(JulyOutboundTemplate template) {
        JulyOutboundTemplatePo po = new JulyOutboundTemplatePo();
        po.setId(template.id().value());
        po.setSortOrder(template.sortOrder());
        po.setTemplateCode(template.templateCode());
        po.setTemplateName(template.templateName());
        po.setChannelCode(template.channelCode());
        po.setTitle(template.title());
        po.setContent(template.content());
        po.setStatus(template.status());
        po.setRemark(template.remark());

        return po;
    }

    /**
     * Map a PO to the aggregate.
     *
     * @param po PO
     * @return aggregate
     */
    private JulyOutboundTemplate toAggregate(JulyOutboundTemplatePo po) {
        AuditInfo audit = new AuditInfo(po.getCreateBy(), po.getUpdateBy(), po.getCreateTime(), po.getUpdateTime());

        return new JulyOutboundTemplate(EntityId.of(po.getId()), po.getTemplateCode(), po.getSortOrder(),
                po.getTemplateName(), po.getChannelCode(), po.getTitle(), po.getContent(), po.getStatus(),
                po.getRemark(), audit);
    }
}
