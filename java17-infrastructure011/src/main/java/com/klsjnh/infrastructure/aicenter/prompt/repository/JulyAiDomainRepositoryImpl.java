package com.klsjnh.infrastructure.aicenter.prompt.repository;

/*                JulyAiDomainRepositoryImpl class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.21
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.21  july ai domain repository impl class
 *      2026.09.21  extends BaseTreeSubRepository011 (tree + master-sub)
 *
 */

import com.klsjnh.common.enums.Status011;

import com.klsjnh.domain.aicenter.prompt.JulyAiDomain;
import com.klsjnh.domain.aicenter.prompt.JulyAiDomainQuerySpec;
import com.klsjnh.domain.aicenter.prompt.JulyAiDomainRepository;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import com.klsjnh.infrastructure.aicenter.prompt.entity.JulyAiDomainPo;
import com.klsjnh.infrastructure.aicenter.prompt.mapper.JulyAiDomainMapper;
import com.klsjnh.infrastructure.persistence.mapper.CommonMapper;
import com.klsjnh.infrastructure.persistence.repository.BaseRepository;
import com.klsjnh.infrastructure.persistence.repository.BaseTreeSubRepository011;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * Repository implementation for the JulyAiDomain aggregate (july_ai_domain,
 * business unique column domain_code): a tree master (parent_id + sort_order)
 * whose child table is july_ai_domain_prompt (pk_mt), hence
 * {@link BaseTreeSubRepository011}.
 */

@Repository
public class JulyAiDomainRepositoryImpl extends BaseTreeSubRepository011<JulyAiDomainPo, JulyAiDomainMapper>
        implements JulyAiDomainRepository {

    /**
     * Prompt (child) repository.
     */
    private final JulyAiDomainPromptRepositoryImpl promptRepository;

    /**
     * Create the repository.
     *
     * @param mapper           mybatis-plus mapper
     * @param commonMapper     native sql mapper
     * @param promptRepository prompt child repository
     */
    public JulyAiDomainRepositoryImpl(JulyAiDomainMapper mapper, CommonMapper commonMapper,
            JulyAiDomainPromptRepositoryImpl promptRepository) {
        super(mapper, commonMapper);
        this.promptRepository = promptRepository;
    }

    /** {@inheritDoc} */
    @Override
    protected String getTableName() {
        return "july_ai_domain";
    }

    /** {@inheritDoc} */
    @Override
    protected String getBusinessColumn() {
        return "domain_code";
    }

    /** {@inheritDoc} */
    @Override
    protected Object getBusinessValue(JulyAiDomainPo entity) {
        return entity.getDomainCode();
    }

    /** {@inheritDoc} */
    @Override
    protected String duplicateMessage() {
        return "domain code already exists";
    }

    /** {@inheritDoc} */
    @Override
    protected List<BaseRepository<?, ?>> getChildServices() {
        return List.of(promptRepository);
    }

    /**
     * {@inheritDoc}
     *
     * <p>The tree carries only enabled domains, ordered by sort_order / id.</p>
     */
    @Override
    protected QueryWrapper<JulyAiDomainPo> treeWrapper() {
        return super.treeWrapper().eq("status", Status011.ENABLED.getCode());
    }

    /** {@inheritDoc} */
    @Override
    public void insert(JulyAiDomain domain) {
        insert(toPo(domain));
    }

    /** {@inheritDoc} */
    @Override
    public void update(JulyAiDomain domain) {
        update(toPo(domain));
    }

    /** {@inheritDoc} */
    @Override
    public JulyAiDomain findById(String id) {
        JulyAiDomainPo po = getById(id);

        return po == null ? null : toAggregate(po);
    }

    /** {@inheritDoc} */
    @Override
    public JulyAiDomain findByCode(String domainCode) {
        JulyAiDomainPo po = getByBusinessValue(domainCode);

        return po == null ? null : toAggregate(po);
    }

    /** {@inheritDoc} */
    @Override
    public boolean logicDeleteById(String id) {
        JulyAiDomainPo po = getById(id);

        if (po == null) {
            return false;
        }

        logicDelete(po);

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public boolean hasChildren(String parentId) {
        QueryWrapper<JulyAiDomainPo> wrapper = new QueryWrapper<>();
        wrapper.eq("parent_id", parentId);

        return mapper.selectCount(wrapper) > 0;
    }

    /** {@inheritDoc} */
    @Override
    public List<JulyAiDomain> findTree() {
        return selectTree().stream().map(this::toAggregateTree).toList();
    }

    /** {@inheritDoc} */
    @Override
    public List<JulyAiDomain> findPage(int offset, int pageSize, JulyAiDomainQuerySpec spec) {
        int current = offset / pageSize + 1;
        Page<JulyAiDomainPo> page = Page.of(current, pageSize);

        return mapper.selectPage(page, specWrapper(spec)).getRecords().stream()
                .map(this::toAggregate)
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    public long count(JulyAiDomainQuerySpec spec) {
        return mapper.selectCount(specWrapper(spec));
    }

    /**
     * Filter wrapper shared by findPage and count.
     *
     * @param spec query condition
     * @return query wrapper
     */
    private QueryWrapper<JulyAiDomainPo> specWrapper(JulyAiDomainQuerySpec spec) {
        QueryWrapper<JulyAiDomainPo> wrapper = new QueryWrapper<>();
        JulyAiDomainQuerySpec query = spec == null ? new JulyAiDomainQuerySpec(null, null, null) : spec;

        if (query.hasKeyword()) {
            String keyword = query.keyword();
            wrapper.and(w -> w.like("domain_code", keyword).or().like("domain_name", keyword));
        }

        if (query.hasParentId()) {
            wrapper.eq("parent_id", query.parentId());
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
     * @param domain aggregate
     * @return PO
     */
    private JulyAiDomainPo toPo(JulyAiDomain domain) {
        JulyAiDomainPo po = new JulyAiDomainPo();
        po.setId(domain.id().value());
        po.setSortOrder(domain.sortOrder());
        po.setDomainCode(domain.domainCode());
        po.setDomainName(domain.domainName());
        po.setParentId(domain.parentId());
        po.setStatus(domain.status());
        po.setRemark(domain.remark());

        return po;
    }

    /**
     * Map a PO to the aggregate (children not mapped).
     *
     * @param po PO
     * @return aggregate
     */
    private JulyAiDomain toAggregate(JulyAiDomainPo po) {
        AuditInfo audit = new AuditInfo(po.getCreateBy(), po.getUpdateBy(), po.getCreateTime(), po.getUpdateTime());

        return new JulyAiDomain(EntityId.of(po.getId()), po.getDomainCode(), po.getDomainName(), po.getParentId(),
                po.getSortOrder(), po.getStatus(), po.getRemark(), audit);
    }

    /**
     * Map a PO tree to an aggregate tree (children mapped recursively).
     *
     * @param po PO node
     * @return aggregate node with nested children
     */
    private JulyAiDomain toAggregateTree(JulyAiDomainPo po) {
        JulyAiDomain domain = toAggregate(po);

        for (JulyAiDomainPo child : po.getChildren()) {
            domain.addChild(toAggregateTree(child));
        }

        return domain;
    }
}
