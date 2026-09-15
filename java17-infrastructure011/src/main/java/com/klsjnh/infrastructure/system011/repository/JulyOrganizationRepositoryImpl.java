package com.klsjnh.infrastructure.system011.repository;

/*                JulyOrganizationRepositoryImpl class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july organization repository impl class
 *      2026.09.15  tree method renamed to getTree
 *
 */

import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;
import com.klsjnh.domain.system011.organization.JulyOrganization;
import com.klsjnh.domain.system011.organization.JulyOrganizationRepository;

import com.klsjnh.infrastructure.persistence.mapper.CommonMapper;
import com.klsjnh.infrastructure.persistence.repository.BaseTreeRepository011;
import com.klsjnh.infrastructure.system011.entity.JulyOrganizationPo;
import com.klsjnh.infrastructure.system011.mapper.JulyOrganizationMapper;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository implementation for the JulyOrganization aggregate on the tree +
 * sort base (BaseTreeRepository011).
 */

@Repository
public class JulyOrganizationRepositoryImpl
        extends BaseTreeRepository011<JulyOrganizationPo, JulyOrganizationMapper>
        implements JulyOrganizationRepository {

    /**
     * Create the repository.
     *
     * @param mapper       mybatis-plus mapper
     * @param commonMapper native sql mapper
     */
    public JulyOrganizationRepositoryImpl(JulyOrganizationMapper mapper, CommonMapper commonMapper) {
        super(mapper, commonMapper);
    }

    /**
     * Table name for logs and maintenance statements.
     *
     * @return table name
     */
    @Override
    protected String getTableName() {
        return "july_organization";
    }

    /**
     * Business unique column name.
     *
     * @return column name
     */
    @Override
    protected String getBusinessColumn() {
        return "org_code";
    }

    /**
     * Extract business value from entity.
     *
     * @param entity entity
     * @return field value
     */
    @Override
    protected Object getBusinessValue(JulyOrganizationPo entity) {
        return entity.getOrgCode();
    }

    /**
     * Insert a new aggregate.
     *
     * @param organization aggregate
     */
    @Override
    public void insert(JulyOrganization organization) {
        insert(toPo(organization));
    }

    /**
     * Update an existing aggregate.
     *
     * @param organization aggregate with id
     */
    @Override
    public void update(JulyOrganization organization) {
        update(toPo(organization));
    }

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return aggregate or null
     */
    @Override
    public JulyOrganization findById(String id) {
        JulyOrganizationPo po = getById(id);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Find by the unique organization code.
     *
     * @param orgCode organization code
     * @return aggregate or null
     */
    @Override
    public JulyOrganization findByCode(String orgCode) {
        JulyOrganizationPo po = getByBusinessValue(orgCode);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Whether an organization has alive children.
     *
     * @param id organization id
     * @return true when children exist
     */
    @Override
    public boolean hasChildren(String id) {
        QueryWrapper<JulyOrganizationPo> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", id);

        return mapper.selectCount(wrapper) > 0;
    }

    /**
     * Find the alive children of one organization (flat).
     *
     * @param parentId parent organization id
     * @return child aggregates
     */
    @Override
    public List<JulyOrganization> findChildren(String parentId) {
        QueryWrapper<JulyOrganizationPo> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", parentId).orderByAsc("sort_order").orderByAsc("id");

        return mapper.selectList(wrapper).stream().map(this::toAggregate).toList();
    }

    /**
     * Logic delete by primary key.
     *
     * @param id primary key
     * @return true when a row was deleted
     */
    @Override
    public boolean logicDeleteById(String id) {
        JulyOrganizationPo po = getById(id);

        if (po == null) {
            return false;
        }

        logicDelete(po);

        return true;
    }

    /**
     * Load the full alive organization tree (children assembled by the tree
     * base).
     *
     * @return root nodes with nested children
     */
    @Override
    public List<JulyOrganization> getTree() {
        return toAggregateTree(super.selectTree());
    }

    /**
     * {@inheritDoc}
     *
     * <p>Organization tree orders by sort_order first, then id.</p>
     */
    @Override
    protected QueryWrapper<JulyOrganizationPo> treeWrapper() {
        QueryWrapper<JulyOrganizationPo> wrapper = new QueryWrapper<>();
        wrapper.orderByAsc("sort_order").orderByAsc("id");

        return wrapper;
    }

    /**
     * Offset based page query with an optional keyword filter.
     *
     * @param offset   zero-based row offset
     * @param pageSize page size
     * @param keyword  org code / name keyword, nullable
     * @return page rows
     */
    @Override
    public List<JulyOrganization> findPage(int offset, int pageSize, String keyword) {
        int current = offset / pageSize + 1;
        Page<JulyOrganizationPo> page = Page.of(current, pageSize);

        return mapper.selectPage(page, keywordWrapper(keyword)).getRecords().stream()
                .map(this::toAggregate)
                .toList();
    }

    /**
     * Count with the same filter as findPage.
     *
     * @param keyword org code / name keyword, nullable
     * @return total row count
     */
    @Override
    public long count(String keyword) {
        return mapper.selectCount(keywordWrapper(keyword));
    }

    /**
     * Keyword filter wrapper shared by findPage and count.
     *
     * @param keyword org code / name keyword, nullable
     * @return query wrapper
     */
    private QueryWrapper<JulyOrganizationPo> keywordWrapper(String keyword) {
        QueryWrapper<JulyOrganizationPo> wrapper = new QueryWrapper<>();

        if (keyword != null && !keyword.isBlank()) {
            wrapper.like("org_code", keyword).or().like("org_name", keyword);
        }

        return wrapper;
    }

    /**
     * Map a Po tree to the aggregate tree.
     *
     * @param roots root Po nodes with nested children
     * @return root aggregates with nested children
     */
    private List<JulyOrganization> toAggregateTree(List<JulyOrganizationPo> roots) {
        List<JulyOrganization> result = new ArrayList<>();

        for (JulyOrganizationPo root : roots) {
            result.add(toAggregateRecursive(root));
        }

        return result;
    }

    /**
     * Recursive Po → aggregate mapping keeping the children tree.
     *
     * @param po Po node
     * @return aggregate node with children
     */
    private JulyOrganization toAggregateRecursive(JulyOrganizationPo po) {
        JulyOrganization organization = toAggregate(po);

        for (JulyOrganizationPo child : po.getChildren()) {
            organization.addChild(toAggregateRecursive(child));
        }

        return organization;
    }

    /**
     * Map the aggregate to a PO.
     *
     * @param organization aggregate
     * @return PO
     */
    private JulyOrganizationPo toPo(JulyOrganization organization) {
        JulyOrganizationPo po = new JulyOrganizationPo();
        po.setId(organization.id().value());
        po.setOrgCode(organization.orgCode());
        po.setOrgName(organization.orgName());
        po.setPkUser(organization.pkUser());
        po.setOrgLevel(organization.orgLevel());
        po.setParentId(organization.parentId());
        po.setSortOrder(organization.sortOrder());
        po.setStatus(organization.status());

        return po;
    }

    /**
     * Map a PO to the aggregate.
     *
     * @param po PO
     * @return aggregate
     */
    private JulyOrganization toAggregate(JulyOrganizationPo po) {
        AuditInfo audit = new AuditInfo(po.getCreateBy(), po.getUpdateBy(), po.getCreateTime(), po.getUpdateTime());

        return new JulyOrganization(EntityId.of(po.getId()), po.getOrgCode(), po.getOrgName(), po.getPkUser(),
                po.getOrgLevel(), po.getParentId(), po.getSortOrder(), po.getStatus(), audit);
    }
}
