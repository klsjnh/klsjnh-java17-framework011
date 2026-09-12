package com.klsjnh.infrastructure.system011.repository;

/*                JulyRoleRepositoryImpl class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july role repository impl class
 *
 */

import com.klsjnh.domain.iam.JulyRole;
import com.klsjnh.domain.iam.JulyRoleRepository;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;
import com.klsjnh.infrastructure.system011.entity.JulyRolePo;
import com.klsjnh.infrastructure.persistence.mapper.CommonMapper;
import com.klsjnh.infrastructure.persistence.repository.BaseMasterSubRepository021;
import com.klsjnh.infrastructure.persistence.repository.BaseRepository;
import com.klsjnh.infrastructure.system011.mapper.JulyRoleMapper;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository implementation for the JulyRole aggregate: master of the
 * role_permissions child (BaseMasterSubRepository021).
 */

@Repository
public class JulyRoleRepositoryImpl
        extends BaseMasterSubRepository021<JulyRolePo, JulyRoleMapper>
        implements JulyRoleRepository {

    /**
     * Create the repository.
     *
     * @param mapper       mybatis-plus mapper
     * @param commonMapper native sql mapper
     */
    public JulyRoleRepositoryImpl(JulyRoleMapper mapper, CommonMapper commonMapper) {
        super(mapper, commonMapper);
    }

    /**
     * Child repositories owned by this master (role_permissions lands with the
     * menu feature).
     *
     * @return child repositories
     */
    @Override
    protected List<BaseRepository<?, ?>> getChildServices() {
        return List.of();
    }

    /**
     * Table name for logs and maintenance statements.
     *
     * @return table name
     */
    @Override
    protected String getTableName() {
        return "july_role";
    }

    /**
     * Business unique column name.
     *
     * @return column name in snake_case
     */
    @Override
    protected String getBusinessColumn() {
        return "role_code";
    }

    /**
     * Extract business value from entity.
     *
     * @param entity entity
     * @return field value
     */
    @Override
    protected Object getBusinessValue(JulyRolePo entity) {
        return entity.getRoleCode();
    }

    /**
     * Insert a new aggregate.
     *
     * @param role aggregate in enabled state
     */
    @Override
    public void insert(JulyRole role) {
        insert(toPo(role));
    }

    /**
     * Update an existing aggregate.
     *
     * @param role aggregate with id
     */
    @Override
    public void update(JulyRole role) {
        update(toPo(role));
    }

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return aggregate or null
     */
    @Override
    public JulyRole findById(String id) {
        JulyRolePo po = getById(id);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Find by the unique role code.
     *
     * @param roleCode role code
     * @return aggregate or null
     */
    @Override
    public JulyRole findByCode(String roleCode) {
        JulyRolePo po = getByBusinessValue(roleCode);

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
        if (getById(id) == null) {
            return false;
        }

        cascadeDelete(id);

        return true;
    }

    /**
     * Offset based page query with an optional keyword filter.
     *
     * @param offset   zero-based row offset
     * @param pageSize page size
     * @param keyword  role code / name keyword, nullable
     * @return page rows
     */
    @Override
    public List<JulyRole> findPage(int offset, int pageSize, String keyword) {
        int current = offset / pageSize + 1;
        Page<JulyRolePo> page = Page.of(current, pageSize);

        return mapper.selectPage(page, keywordWrapper(keyword)).getRecords().stream()
                .map(this::toAggregate)
                .toList();
    }

    /**
     * Count with the same filter as findPage.
     *
     * @param keyword role code / name keyword, nullable
     * @return total row count
     */
    @Override
    public long count(String keyword) {
        return mapper.selectCount(keywordWrapper(keyword));
    }

    /**
     * Keyword filter wrapper shared by findPage and count.
     *
     * @param keyword role code / name keyword, nullable
     * @return query wrapper
     */
    private QueryWrapper<JulyRolePo> keywordWrapper(String keyword) {
        QueryWrapper<JulyRolePo> wrapper = new QueryWrapper<>();

        if (keyword != null && !keyword.isBlank()) {
            wrapper.like("role_code", keyword).or().like("role_name", keyword);
        }

        wrapper.orderByDesc("create_time").orderByAsc("id");

        return wrapper;
    }

    /**
     * Map the aggregate to a PO.
     *
     * @param role aggregate
     * @return PO
     */
    private JulyRolePo toPo(JulyRole role) {
        JulyRolePo po = new JulyRolePo();
        po.setId(role.id().value());
        po.setRoleCode(role.roleCode());
        po.setRoleName(role.roleName());
        po.setIsBuiltin(role.isBuiltin());
        po.setRemark(role.remark());
        po.setStatus(role.status());

        return po;
    }

    /**
     * Map a PO to the aggregate.
     *
     * @param po PO
     * @return aggregate
     */
    private JulyRole toAggregate(JulyRolePo po) {
        AuditInfo audit = new AuditInfo(po.getCreateBy(), po.getUpdateBy(), po.getCreateTime(), po.getUpdateTime());

        return new JulyRole(EntityId.of(po.getId()), po.getRoleCode(), po.getRoleName(), po.getIsBuiltin(),
                po.getRemark(), po.getStatus(), audit);
    }
}
