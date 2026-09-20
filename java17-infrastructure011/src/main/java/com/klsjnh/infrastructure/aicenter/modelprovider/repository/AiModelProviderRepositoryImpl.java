package com.klsjnh.infrastructure.aicenter.modelprovider.repository;

/*                AiModelProviderRepositoryImpl class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  ai model provider repository impl class
 *
 */

import com.klsjnh.common.enums.Status011;

import com.klsjnh.domain.aicenter.modelprovider.AiModelProvider;
import com.klsjnh.domain.aicenter.modelprovider.AiModelProviderQuerySpec;
import com.klsjnh.domain.aicenter.modelprovider.AiModelProviderRepository;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import com.klsjnh.infrastructure.aicenter.modelprovider.entity.AiModelProviderPo;
import com.klsjnh.infrastructure.aicenter.modelprovider.mapper.AiModelProviderMapper;
import com.klsjnh.infrastructure.persistence.mapper.CommonMapper;
import com.klsjnh.infrastructure.persistence.repository.BaseRepository;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * Repository implementation for the AiModelProvider aggregate on the base
 * repository (july_ai_model_provider, business unique column provider_code).
 */

@Repository
public class AiModelProviderRepositoryImpl
        extends BaseRepository<AiModelProviderPo, AiModelProviderMapper>
        implements AiModelProviderRepository {

    /**
     * Create the repository.
     *
     * @param mapper       mybatis-plus mapper
     * @param commonMapper native sql mapper
     */
    public AiModelProviderRepositoryImpl(AiModelProviderMapper mapper, CommonMapper commonMapper) {
        super(mapper, commonMapper);
    }

    /**
     * Table name for logs and maintenance statements.
     *
     * @return table name
     */
    @Override
    protected String getTableName() {
        return "july_ai_model_provider";
    }

    /**
     * Business unique column name.
     *
     * @return column name
     */
    @Override
    protected String getBusinessColumn() {
        return "provider_code";
    }

    /**
     * Extract business value from entity.
     *
     * @param entity entity
     * @return field value
     */
    @Override
    protected Object getBusinessValue(AiModelProviderPo entity) {
        return entity.getProviderCode();
    }

    /**
     * Duplicate message for the unique providerCode.
     *
     * @return message
     */
    @Override
    protected String duplicateMessage() {
        return "provider code already exists";
    }

    /**
     * Insert a new aggregate.
     *
     * @param provider aggregate
     */
    @Override
    public void insert(AiModelProvider provider) {
        insert(toPo(provider));
    }

    /**
     * Update an existing aggregate.
     *
     * @param provider aggregate with id
     */
    @Override
    public void update(AiModelProvider provider) {
        update(toPo(provider));
    }

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return aggregate or null
     */
    @Override
    public AiModelProvider findById(String id) {
        AiModelProviderPo po = getById(id);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Find by provider code, enabled or not (management read entry).
     *
     * @param providerCode provider code
     * @return aggregate or null
     */
    @Override
    public AiModelProvider findByCode(String providerCode) {
        AiModelProviderPo po = getByBusinessValue(providerCode);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Find by provider code, ENABLED only (program read entry).
     *
     * @param providerCode provider code
     * @return aggregate or null
     */
    @Override
    public AiModelProvider findEnabledByCode(String providerCode) {
        QueryWrapper<AiModelProviderPo> wrapper = new QueryWrapper<>();
        wrapper.eq("provider_code", providerCode)
                .eq("status", Status011.ENABLED.getCode())
                .last("LIMIT 1");

        AiModelProviderPo po = mapper.selectOne(wrapper);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Every ENABLED provider, ordered by sort order.
     *
     * @return enabled aggregates, never null
     */
    @Override
    public List<AiModelProvider> findAllEnabled() {
        QueryWrapper<AiModelProviderPo> wrapper = new QueryWrapper<>();
        wrapper.eq("status", Status011.ENABLED.getCode())
                .orderByAsc("sort_order")
                .orderByAsc("id");

        return mapper.selectList(wrapper).stream()
                .map(this::toAggregate)
                .toList();
    }

    /**
     * Logic delete by primary key.
     *
     * @param id primary key
     * @return true when a row was deleted
     */
    @Override
    public boolean logicDeleteById(String id) {
        AiModelProviderPo po = getById(id);

        if (po == null) {
            return false;
        }

        logicDelete(po);

        return true;
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
    public List<AiModelProvider> findPage(int offset, int pageSize, AiModelProviderQuerySpec spec) {
        int current = offset / pageSize + 1;
        Page<AiModelProviderPo> page = Page.of(current, pageSize);

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
    public long count(AiModelProviderQuerySpec spec) {
        return mapper.selectCount(specWrapper(spec));
    }

    /**
     * Filter wrapper shared by findPage and count.
     *
     * @param spec query condition
     * @return query wrapper
     */
    private QueryWrapper<AiModelProviderPo> specWrapper(AiModelProviderQuerySpec spec) {
        QueryWrapper<AiModelProviderPo> wrapper = new QueryWrapper<>();
        AiModelProviderQuerySpec query = spec == null ? new AiModelProviderQuerySpec(null, null) : spec;

        if (query.hasKeyword()) {
            String keyword = query.keyword();
            wrapper.and(w -> w.like("provider_code", keyword)
                    .or().like("provider_name", keyword)
                    .or().like("base_url", keyword));
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
     * @param provider aggregate
     * @return PO
     */
    private AiModelProviderPo toPo(AiModelProvider provider) {
        AiModelProviderPo po = new AiModelProviderPo();
        po.setId(provider.id().value());
        po.setSortOrder(provider.sortOrder());
        po.setProviderCode(provider.providerCode());
        po.setProviderName(provider.providerName());
        po.setBaseUrl(provider.baseUrl());
        po.setModels(provider.models());
        po.setStatus(provider.status());
        po.setRemark(provider.remark());

        return po;
    }

    /**
     * Map a PO to the aggregate.
     *
     * @param po PO
     * @return aggregate
     */
    private AiModelProvider toAggregate(AiModelProviderPo po) {
        AuditInfo audit = new AuditInfo(po.getCreateBy(), po.getUpdateBy(), po.getCreateTime(), po.getUpdateTime());

        return new AiModelProvider(EntityId.of(po.getId()), po.getProviderCode(), po.getSortOrder(),
                po.getProviderName(), po.getBaseUrl(), po.getModels(), po.getStatus(), po.getRemark(), audit);
    }
}
