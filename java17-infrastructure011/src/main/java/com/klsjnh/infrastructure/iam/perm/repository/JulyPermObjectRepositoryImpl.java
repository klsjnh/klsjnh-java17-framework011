package com.klsjnh.infrastructure.iam.perm.repository;

/*                JulyPermObjectRepositoryImpl class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.24
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.24  permission object repository impl
 *
 */

import com.klsjnh.common.enums.Status011;

import com.klsjnh.domain.iam.perm.JulyPermObject;
import com.klsjnh.domain.iam.perm.JulyPermObjectRepository;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import com.klsjnh.infrastructure.iam.perm.entity.JulyPermObjectPo;
import com.klsjnh.infrastructure.iam.perm.mapper.JulyPermObjectMapper;
import com.klsjnh.infrastructure.persistence.mapper.CommonMapper;
import com.klsjnh.infrastructure.persistence.repository.BaseRepository;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.util.List;

/**
 * Repository implementation for july_perm_object.
 */

@Repository
public class JulyPermObjectRepositoryImpl
        extends BaseRepository<JulyPermObjectPo, JulyPermObjectMapper>
        implements JulyPermObjectRepository {

    /**
     * Create the repository.
     *
     * @param mapper       mybatis-plus mapper
     * @param commonMapper native sql mapper
     */
    public JulyPermObjectRepositoryImpl(JulyPermObjectMapper mapper, CommonMapper commonMapper) {
        super(mapper, commonMapper);
    }

    /** {@inheritDoc} */
    @Override
    protected String getTableName() {
        return "july_perm_object";
    }

    /** {@inheritDoc} */
    @Override
    protected String getBusinessColumn() {
        return "object_code";
    }

    /** {@inheritDoc} */
    @Override
    protected Object getBusinessValue(JulyPermObjectPo entity) {
        return entity.getObjectCode();
    }

    /** {@inheritDoc} */
    @Override
    public void insert(JulyPermObject object) {
        insert(toPo(object));
    }

    /** {@inheritDoc} */
    @Override
    public JulyPermObject findByCode(String objectCode) {
        JulyPermObjectPo po = getByBusinessValue(objectCode);

        return po == null ? null : toAggregate(po);
    }

    /** {@inheritDoc} */
    @Override
    public List<JulyPermObject> findAllEnabled() {
        QueryWrapper<JulyPermObjectPo> wrapper = new QueryWrapper<>();
        wrapper.eq("status", Status011.ENABLED.getCode()).orderByAsc("sort_order").orderByAsc("object_code");

        return mapper.selectList(wrapper).stream().map(this::toAggregate).toList();
    }

    /**
     * Domain → PO.
     *
     * @param object aggregate
     * @return po
     */
    private JulyPermObjectPo toPo(JulyPermObject object) {
        JulyPermObjectPo po = new JulyPermObjectPo();
        po.setId(object.id().value());
        po.setObjectCode(object.objectCode());
        po.setObjectName(object.objectName());
        po.setModuleCode(object.moduleCode());
        po.setSortOrder(object.sortOrder());
        po.setStatus(object.status());
        po.setRemark(object.remark());

        return po;
    }

    /**
     * PO → domain.
     *
     * @param po po
     * @return aggregate
     */
    private JulyPermObject toAggregate(JulyPermObjectPo po) {
        return new JulyPermObject(EntityId.of(po.getId()), po.getObjectCode(), po.getObjectName(), po.getModuleCode(),
                po.getSortOrder(), po.getStatus(), po.getRemark(),
                new AuditInfo(po.getCreateBy(), po.getUpdateBy(), po.getCreateTime(), po.getUpdateTime()));
    }
}
