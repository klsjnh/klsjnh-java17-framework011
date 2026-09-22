package com.klsjnh.infrastructure.datasource.sync.repository;

/*                JulySyncRuleRepositoryImpl class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  july sync rule repository impl class
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.datasource.sync.JulySyncRule;
import com.klsjnh.domain.datasource.sync.JulySyncRuleQuerySpec;
import com.klsjnh.domain.datasource.sync.JulySyncRuleRepository;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import com.klsjnh.infrastructure.datasource.sync.entity.JulySyncRulePo;
import com.klsjnh.infrastructure.datasource.sync.mapper.JulySyncRuleMapper;
import com.klsjnh.infrastructure.persistence.mapper.CommonMapper;
import com.klsjnh.infrastructure.persistence.repository.BaseMasterSubRepository011;
import com.klsjnh.infrastructure.persistence.repository.BaseRepository;
import com.klsjnh.infrastructure.persistence.sql.QuotedLiteral;
import com.klsjnh.infrastructure.persistence.support.SortSupport;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * Repository implementation for the JulySyncRule aggregate on the master-sub
 * base (july_sync_rule master, july_sync_rule_column child; business unique
 * column sync_code).
 */

@Repository
public class JulySyncRuleRepositoryImpl extends BaseMasterSubRepository011<JulySyncRulePo, JulySyncRuleMapper>
        implements JulySyncRuleRepository {

    /**
     * Column (child) repository.
     */
    private final JulySyncRuleColumnRepositoryImpl columnRepository;

    /**
     * Create the repository.
     *
     * @param mapper           mybatis-plus mapper
     * @param commonMapper     native sql mapper
     * @param columnRepository column child repository
     */
    public JulySyncRuleRepositoryImpl(JulySyncRuleMapper mapper, CommonMapper commonMapper,
            JulySyncRuleColumnRepositoryImpl columnRepository) {
        super(mapper, commonMapper);
        this.columnRepository = columnRepository;
    }

    /** {@inheritDoc} */
    @Override
    protected List<BaseRepository<?, ?>> getChildServices() {
        return List.of(columnRepository);
    }

    /** {@inheritDoc} */
    @Override
    protected String getTableName() {
        return "july_sync_rule";
    }

    /** {@inheritDoc} */
    @Override
    protected String getBusinessColumn() {
        return "sync_code";
    }

    /** {@inheritDoc} */
    @Override
    protected Object getBusinessValue(JulySyncRulePo entity) {
        return entity.getSyncCode();
    }

    /** {@inheritDoc} */
    @Override
    protected String duplicateMessage() {
        return "sync code already exists";
    }

    /** {@inheritDoc} */
    @Override
    public void insert(JulySyncRule rule) {
        insert(toPo(rule));
    }

    /** {@inheritDoc} */
    @Override
    public void update(JulySyncRule rule) {
        update(toPo(rule));
    }

    /** {@inheritDoc} */
    @Override
    public JulySyncRule findById(String id) {
        JulySyncRulePo po = getById(id);

        return po == null ? null : toAggregate(po);
    }

    /** {@inheritDoc} */
    @Override
    public JulySyncRule findByCode(String syncCode) {
        JulySyncRulePo po = getByBusinessValue(syncCode);

        return po == null ? null : toAggregate(po);
    }

    /** {@inheritDoc} */
    @Override
    public boolean logicDeleteById(String id) {
        JulySyncRulePo po = getById(id);

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

    /** {@inheritDoc} */
    @Override
    public List<JulySyncRule> findPage(int offset, int pageSize, JulySyncRuleQuerySpec spec) {
        int current = offset / pageSize + 1;
        Page<JulySyncRulePo> page = Page.of(current, pageSize);

        return mapper.selectPage(page, specWrapper(spec)).getRecords().stream()
                .map(this::toAggregate)
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    public long count(JulySyncRuleQuerySpec spec) {
        return mapper.selectCount(specWrapper(spec));
    }

    /**
     * Filter wrapper shared by findPage and count.
     *
     * @param spec query condition
     * @return query wrapper
     */
    private QueryWrapper<JulySyncRulePo> specWrapper(JulySyncRuleQuerySpec spec) {
        QueryWrapper<JulySyncRulePo> wrapper = new QueryWrapper<>();
        JulySyncRuleQuerySpec query = spec == null ? new JulySyncRuleQuerySpec(null, null) : spec;

        if (query.hasKeyword()) {
            String keyword = query.keyword();
            wrapper.and(w -> w.like("sync_code", keyword).or().like("sync_name", keyword));
        }

        if (query.hasStatus()) {
            wrapper.eq("status", query.status());
        }

        SortSupport.orderBySortThenId(wrapper);

        return wrapper;
    }

    /**
     * Map the aggregate to a PO.
     *
     * @param rule aggregate
     * @return PO
     */
    private JulySyncRulePo toPo(JulySyncRule rule) {
        JulySyncRulePo po = new JulySyncRulePo();
        po.setId(rule.id().value());
        po.setSortOrder(rule.sortOrder());
        po.setSyncCode(rule.syncCode());
        po.setSyncName(rule.syncName());
        po.setSourceDsCode(rule.sourceDsCode());
        po.setSourceKind(rule.sourceKind());
        po.setSourceData(rule.sourceData());
        po.setTargetDsCode(rule.targetDsCode());
        po.setTargetKind(rule.targetKind());
        po.setTargetData(rule.targetData());
        po.setMode(rule.mode());
        po.setSyncKey(rule.syncKey());
        po.setConflict(rule.conflict());
        po.setOptions(rule.options());
        po.setPageSize(rule.pageSize());
        po.setStatus(rule.status());
        po.setRemark(rule.remark());

        return po;
    }

    /**
     * Map a PO to the aggregate.
     *
     * @param po PO
     * @return aggregate
     */
    private JulySyncRule toAggregate(JulySyncRulePo po) {
        AuditInfo audit = new AuditInfo(po.getCreateBy(), po.getUpdateBy(), po.getCreateTime(), po.getUpdateTime());

        return new JulySyncRule(EntityId.of(po.getId()), po.getSyncCode(), po.getSyncName(), po.getSourceDsCode(),
                po.getSourceKind(), po.getSourceData(), po.getTargetDsCode(), po.getTargetKind(), po.getTargetData(),
                po.getMode(), po.getSyncKey(), po.getConflict(), po.getOptions(), po.getPageSize(), po.getSortOrder(),
                po.getStatus(), po.getRemark(), audit);
    }

    /**
     * Whether a sync code exists including logic-deleted rows.
     *
     * @param syncCode sync code
     * @return true when a row exists, deleted or not
     */
    public boolean existsIncludingDeleted(String syncCode) {
        if (StringUtil011.isBlank(syncCode)) {
            return false;
        }

        String sql = "SELECT COUNT(*) FROM " + getTableName() + " WHERE " + getBusinessColumn() + " = "
                + QuotedLiteral.of(syncCode);

        return commonMapper.countBy(sql) > 0;
    }

    /**
     * Enabled-only lookup by sync code (runtime entry).
     *
     * @param syncCode sync code
     * @return aggregate or null
     */
    public JulySyncRule findEnabledByCode(String syncCode) {
        QueryWrapper<JulySyncRulePo> wrapper = new QueryWrapper<>();
        wrapper.eq("sync_code", syncCode)
                .eq("status", Status011.ENABLED.getCode())
                .last("LIMIT 1");

        JulySyncRulePo po = mapper.selectOne(wrapper);

        return po == null ? null : toAggregate(po);
    }
}
