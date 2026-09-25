package com.klsjnh.infrastructure.iam.perm.repository;

/*                JulyPermActionRepositoryImpl class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.24
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.24  permission action repository impl
 *
 */

import com.klsjnh.common.enums.Status011;

import com.klsjnh.domain.iam.perm.JulyPermAction;
import com.klsjnh.domain.iam.perm.JulyPermActionRepository;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import com.klsjnh.infrastructure.iam.perm.entity.JulyPermActionPo;
import com.klsjnh.infrastructure.iam.perm.mapper.JulyPermActionMapper;
import com.klsjnh.infrastructure.persistence.mapper.CommonMapper;
import com.klsjnh.infrastructure.persistence.repository.BaseRepository;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.util.List;

/**
 * Repository implementation for july_perm_action.
 */

@Repository
public class JulyPermActionRepositoryImpl
        extends BaseRepository<JulyPermActionPo, JulyPermActionMapper>
        implements JulyPermActionRepository {

    /**
     * Create the repository.
     *
     * @param mapper       mybatis-plus mapper
     * @param commonMapper native sql mapper
     */
    public JulyPermActionRepositoryImpl(JulyPermActionMapper mapper, CommonMapper commonMapper) {
        super(mapper, commonMapper);
    }

    /** {@inheritDoc} */
    @Override
    protected String getTableName() {
        return "july_perm_action";
    }

    /** {@inheritDoc} */
    @Override
    protected String getBusinessColumn() {
        return "permission_code";
    }

    /** {@inheritDoc} */
    @Override
    protected Object getBusinessValue(JulyPermActionPo entity) {
        return entity.getPermissionCode();
    }

    /** {@inheritDoc} */
    @Override
    public void insert(JulyPermAction action) {
        insert(toPo(action));
    }

    /** {@inheritDoc} */
    @Override
    public JulyPermAction findByPermissionCode(String permissionCode) {
        JulyPermActionPo po = getByBusinessValue(permissionCode);

        return po == null ? null : toAggregate(po);
    }

    /** {@inheritDoc} */
    @Override
    public JulyPermAction findByObjectAndAction(String objectCode, String actionCode) {
        QueryWrapper<JulyPermActionPo> wrapper = new QueryWrapper<>();
        wrapper.eq("object_code", objectCode).eq("action_code", actionCode);

        JulyPermActionPo po = mapper.selectOne(wrapper);

        return po == null ? null : toAggregate(po);
    }

    /** {@inheritDoc} */
    @Override
    public List<JulyPermAction> findEnabledByObjectCode(String objectCode) {
        QueryWrapper<JulyPermActionPo> wrapper = new QueryWrapper<>();
        wrapper.eq("object_code", objectCode).eq("status", Status011.ENABLED.getCode())
                .orderByAsc("sort_order").orderByAsc("action_code");

        return mapper.selectList(wrapper).stream().map(this::toAggregate).toList();
    }

    /**
     * Domain → PO.
     *
     * @param action aggregate
     * @return po
     */
    private JulyPermActionPo toPo(JulyPermAction action) {
        JulyPermActionPo po = new JulyPermActionPo();
        po.setId(action.id().value());
        po.setObjectCode(action.objectCode());
        po.setActionCode(action.actionCode());
        po.setActionName(action.actionName());
        po.setPermissionCode(action.permissionCode());
        po.setSortOrder(action.sortOrder());
        po.setStatus(action.status());
        po.setRemark(action.remark());

        return po;
    }

    /**
     * PO → domain.
     *
     * @param po po
     * @return aggregate
     */
    private JulyPermAction toAggregate(JulyPermActionPo po) {
        return new JulyPermAction(EntityId.of(po.getId()), po.getObjectCode(), po.getActionCode(), po.getActionName(),
                po.getPermissionCode(), po.getSortOrder(), po.getStatus(), po.getRemark(),
                new AuditInfo(po.getCreateBy(), po.getUpdateBy(), po.getCreateTime(), po.getUpdateTime()));
    }
}
