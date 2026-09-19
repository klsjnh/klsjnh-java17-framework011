package com.klsjnh.infrastructure.iam.user.repository;

/*                JulyUserRepositoryImpl class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july user repository impl class
 *      2026.09.15  clock from date util 011
 *
 */

import com.klsjnh.common.util.DateUtil011;

import com.klsjnh.domain.iam.user.JulyUser;
import com.klsjnh.domain.iam.user.JulyUserRepository;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import com.klsjnh.infrastructure.iam.user.entity.JulyUserPo;
import com.klsjnh.infrastructure.persistence.mapper.CommonMapper;
import com.klsjnh.infrastructure.persistence.repository.BaseMasterSubRepository021;
import com.klsjnh.infrastructure.persistence.repository.BaseRepository;
import com.klsjnh.infrastructure.iam.user.mapper.JulyUserMapper;
import com.klsjnh.infrastructure.iam.role.repository.JulyUserRoleRepositoryImpl;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository implementation for the JulyUser aggregate: master of the
 * user_role / user_audit children (BaseMasterSubRepository021).
 */

@Repository
public class JulyUserRepositoryImpl
        extends BaseMasterSubRepository021<JulyUserPo, JulyUserMapper>
        implements JulyUserRepository {

    /**
     * User role junction repository (cascade child).
     */
    private final JulyUserRoleRepositoryImpl userRoleRepository;

    /**
     * Create the repository.
     *
     * @param mapper       mybatis-plus mapper
     * @param commonMapper native sql mapper
     * @param userRoleRepository user role junction repository
     */
    public JulyUserRepositoryImpl(JulyUserMapper mapper, CommonMapper commonMapper,
            JulyUserRoleRepositoryImpl userRoleRepository) {
        super(mapper, commonMapper);
        this.userRoleRepository = userRoleRepository;
    }

    /**
     * Child repositories owned by this master.
     *
     * @return child repositories
     */
    @Override
    protected List<BaseRepository<?, ?>> getChildServices() {
        return List.of(userRoleRepository);
    }

    /**
     * Table name for logs and maintenance statements.
     *
     * @return table name
     */
    @Override
    protected String getTableName() {
        return "july_user";
    }

    /**
     * Business unique column name.
     *
     * @return column name in snake_case
     */
    @Override
    protected String getBusinessColumn() {
        return "user_account";
    }

    /**
     * Extract business value from entity.
     *
     * @param entity entity
     * @return field value
     */
    @Override
    protected Object getBusinessValue(JulyUserPo entity) {
        return entity.getUserAccount();
    }

    /**
     * Insert a new aggregate.
     *
     * @param user aggregate in enabled state
     */
    @Override
    public void insert(JulyUser user) {
        insert(toPo(user));
    }

    /**
     * Update an existing aggregate.
     *
     * @param user aggregate with id
     */
    @Override
    public void update(JulyUser user) {
        update(toPo(user));
    }

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return aggregate or null
     */
    @Override
    public JulyUser findById(String id) {
        JulyUserPo po = getById(id);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Find by the unique login account.
     *
     * @param userAccount login account
     * @return aggregate or null
     */
    @Override
    public JulyUser findByAccount(String userAccount) {
        JulyUserPo po = getByBusinessValue(userAccount);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Find aggregates by an id list (flat, no junction assembly).
     *
     * @param ids user ids
     * @return aggregates present in the store, empty when ids is null / empty
     */
    @Override
    public List<JulyUser> findByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }

        return mapper.selectByIds(ids).stream().map(this::toAggregate).toList();
    }

        /** {@inheritDoc} */
    @Override
    public void logicDeleteByIds(List<String> ids) {
        batchLogicDelete(ids);
    }

/**
     * Cascade logic delete: toggle the user_role children, then the user.
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
     * Offset based page query with optional keyword filters.
     *
     * @param offset         zero-based row offset
     * @param pageSize       page size
     * @param accountKeyword login account keyword, nullable
     * @param nameKeyword    user name keyword, nullable
     * @return page rows
     */
    @Override
    public List<JulyUser> findPage(int offset, int pageSize, String accountKeyword, String nameKeyword) {
        int current = offset / pageSize + 1;
        Page<JulyUserPo> page = Page.of(current, pageSize);

        QueryWrapper<JulyUserPo> wrapper = new QueryWrapper<>();

        if (accountKeyword != null && !accountKeyword.isBlank()) {
            wrapper.like("user_account", accountKeyword);
        }

        if (nameKeyword != null && !nameKeyword.isBlank()) {
            wrapper.like("user_name", nameKeyword);
        }

        wrapper.orderByDesc("create_time").orderByAsc("id");

        return mapper.selectPage(page, wrapper).getRecords().stream().map(this::toAggregate).toList();
    }

    /**
     * Count with the same filters as findPage.
     *
     * @param accountKeyword login account keyword, nullable
     * @param nameKeyword    user name keyword, nullable
     * @return total row count
     */
    @Override
    public long count(String accountKeyword, String nameKeyword) {
        QueryWrapper<JulyUserPo> wrapper = new QueryWrapper<>();

        if (accountKeyword != null && !accountKeyword.isBlank()) {
            wrapper.like("user_account", accountKeyword);
        }

        if (nameKeyword != null && !nameKeyword.isBlank()) {
            wrapper.like("user_name", nameKeyword);
        }

        return mapper.selectCount(wrapper);
    }

    /**
     * Refresh the last login time of a user.
     *
     * @param id user id
     */
    @Override
    public void touchLastLoginTime(String id) {
        LambdaUpdateWrapper<JulyUserPo> wrapper = new LambdaUpdateWrapper<>();
        wrapper.set(JulyUserPo::getLastLoginTime, DateUtil011.now()).eq(JulyUserPo::getId, id);
        mapper.update(null, wrapper);
    }

    /**
     * Count the alive users mounted on one organization.
     *
     * @param pkOrg organization id
     * @return member count
     */
    @Override
    public long countByOrg(String pkOrg) {
        QueryWrapper<JulyUserPo> wrapper = new QueryWrapper<>();
        wrapper.eq("pk_org", pkOrg);

        Long count = mapper.selectCount(wrapper);

        return count == null ? 0 : count;
    }

    /**
     * Member counts grouped by organization.
     *
     * @return orgId → member count
     */
    @Override
    public java.util.Map<String, Long> countByOrgGrouped() {
        QueryWrapper<JulyUserPo> wrapper = new QueryWrapper<>();
        wrapper.select("pk_org", "COUNT(*) AS cnt").isNotNull("pk_org").groupBy("pk_org");

        java.util.Map<String, Long> counts = new java.util.HashMap<>();

        for (java.util.Map<String, Object> row : mapper.selectMaps(wrapper)) {
            Object org = row.get("pk_org");
            Object cnt = row.get("cnt");

            if (org != null && cnt != null) {
                counts.put(org.toString(), ((Number) cnt).longValue());
            }
        }

        return counts;
    }

    /**
     * Map the aggregate to a PO.
     *
     * @param user aggregate
     * @return PO
     */
    private JulyUserPo toPo(JulyUser user) {
        JulyUserPo po = new JulyUserPo();
        po.setId(user.id().value());
        po.setUserAccount(user.userAccount());
        po.setUserName(user.userName());
        po.setPassword(user.password());
        po.setMobile(user.mobile());
        po.setEmail(user.email());
        po.setAvatar(user.avatar());
        po.setPkOrg(user.pkOrg());
        po.setLastLoginTime(user.lastLoginTime());
        po.setStatus(user.status());

        return po;
    }

    /**
     * Map a PO to the aggregate.
     *
     * @param po PO
     * @return aggregate
     */
    private JulyUser toAggregate(JulyUserPo po) {
        AuditInfo audit = new AuditInfo(po.getCreateBy(), po.getUpdateBy(), po.getCreateTime(), po.getUpdateTime());

        return new JulyUser(EntityId.of(po.getId()), po.getUserAccount(), po.getUserName(), po.getPassword(),
                po.getMobile(), po.getEmail(), po.getAvatar(), po.getPkOrg(), po.getLastLoginTime(), po.getStatus(),
                audit);
    }
}
