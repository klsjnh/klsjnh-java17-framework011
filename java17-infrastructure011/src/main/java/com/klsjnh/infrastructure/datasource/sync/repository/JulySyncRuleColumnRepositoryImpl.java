package com.klsjnh.infrastructure.datasource.sync.repository;

/*                JulySyncRuleColumnRepositoryImpl class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  july sync rule column repository impl class
 *
 */

import com.klsjnh.common.enums.Status011;

import com.klsjnh.domain.datasource.sync.JulySyncRuleColumn;
import com.klsjnh.domain.datasource.sync.JulySyncRuleColumnRepository;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import com.klsjnh.infrastructure.datasource.sync.entity.JulySyncRuleColumnPo;
import com.klsjnh.infrastructure.datasource.sync.mapper.JulySyncRuleColumnMapper;
import com.klsjnh.infrastructure.persistence.mapper.CommonMapper;
import com.klsjnh.infrastructure.persistence.repository.BaseRepository;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.util.List;

/**
 * Repository implementation for the JulySyncRuleColumn child entity on the base
 * repository (july_sync_rule_column).
 */

@Repository
public class JulySyncRuleColumnRepositoryImpl
        extends BaseRepository<JulySyncRuleColumnPo, JulySyncRuleColumnMapper>
        implements JulySyncRuleColumnRepository {

    /**
     * Create the repository.
     *
     * @param mapper       mybatis-plus mapper
     * @param commonMapper native sql mapper
     */
    public JulySyncRuleColumnRepositoryImpl(JulySyncRuleColumnMapper mapper, CommonMapper commonMapper) {
        super(mapper, commonMapper);
    }

    /** {@inheritDoc} */
    @Override
    protected String getTableName() {
        return "july_sync_rule_column";
    }

    /** {@inheritDoc} */
    @Override
    protected String getBusinessColumn() {
        return "";
    }

    /** {@inheritDoc} */
    @Override
    public void insert(JulySyncRuleColumn column) {
        insert(toPo(column));
    }

    /** {@inheritDoc} */
    @Override
    public void update(JulySyncRuleColumn column) {
        update(toPo(column));
    }

    /** {@inheritDoc} */
    @Override
    public JulySyncRuleColumn findById(String id) {
        JulySyncRuleColumnPo po = getById(id);

        return po == null ? null : toAggregate(po);
    }

    /** {@inheritDoc} */
    @Override
    public List<JulySyncRuleColumn> findByMaster(String pkMt) {
        QueryWrapper<JulySyncRuleColumnPo> wrapper = new QueryWrapper<>();
        wrapper.eq("pk_mt", pkMt)
                .eq("status", Status011.ENABLED.getCode())
                .orderByAsc("sort_order")
                .orderByAsc("id");

        return mapper.selectList(wrapper).stream()
                .map(this::toAggregate)
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    public long countByMaster(String pkMt, boolean enabledOnly) {
        QueryWrapper<JulySyncRuleColumnPo> wrapper = new QueryWrapper<>();
        wrapper.eq("pk_mt", pkMt);

        if (enabledOnly) {
            wrapper.eq("status", Status011.ENABLED.getCode());
        }

        return mapper.selectCount(wrapper);
    }

    /** {@inheritDoc} */
    @Override
    public boolean logicDeleteById(String id) {
        JulySyncRuleColumnPo po = getById(id);

        if (po == null) {
            return false;
        }

        logicDelete(po);

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void logicDeleteByMaster(String pkMt) {
        QueryWrapper<JulySyncRuleColumnPo> wrapper = new QueryWrapper<>();
        wrapper.eq("pk_mt", pkMt);

        List<JulySyncRuleColumnPo> rows = mapper.selectList(wrapper);

        if (!rows.isEmpty()) {
            batchLogicDelete(rows.stream().map(JulySyncRuleColumnPo::getId).toList());
        }
    }

    /**
     * Map the aggregate to a PO.
     *
     * @param column aggregate
     * @return PO
     */
    private JulySyncRuleColumnPo toPo(JulySyncRuleColumn column) {
        JulySyncRuleColumnPo po = new JulySyncRuleColumnPo();
        po.setId(column.id().value());
        po.setPkMt(column.pkMt());
        po.setSortOrder(column.sortOrder());
        po.setSourceColumn(column.sourceColumn());
        po.setSourceType(column.sourceType());
        po.setTargetColumn(column.targetColumn());
        po.setTargetType(column.targetType());
        po.setTransform(column.transform());
        po.setStatus(column.status());
        po.setRemark(column.remark());

        return po;
    }

    /**
     * Map a PO to the aggregate.
     *
     * @param po PO
     * @return aggregate
     */
    private JulySyncRuleColumn toAggregate(JulySyncRuleColumnPo po) {
        AuditInfo audit = new AuditInfo(po.getCreateBy(), po.getUpdateBy(), po.getCreateTime(), po.getUpdateTime());

        return new JulySyncRuleColumn(EntityId.of(po.getId()), po.getPkMt(), po.getSourceColumn(), po.getSourceType(),
                po.getTargetColumn(), po.getTargetType(), po.getTransform(), po.getSortOrder(), po.getStatus(),
                po.getRemark(), audit);
    }
}
