package com.klsjnh.infrastructure.aicenter.prompt.repository;

/*                JulyAiPromptRepositoryImpl class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  july ai prompt repository impl class
 *
 */

import com.klsjnh.domain.aicenter.prompt.JulyAiPrompt;
import com.klsjnh.domain.aicenter.prompt.JulyAiPromptQuerySpec;
import com.klsjnh.domain.aicenter.prompt.JulyAiPromptRepository;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import com.klsjnh.infrastructure.aicenter.prompt.entity.JulyAiPromptPo;
import com.klsjnh.infrastructure.aicenter.prompt.mapper.JulyAiPromptMapper;
import com.klsjnh.infrastructure.persistence.mapper.CommonMapper;
import com.klsjnh.infrastructure.persistence.repository.BaseRepository;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * Repository implementation for the JulyAiPrompt aggregate (july_ai_prompt,
 * business unique column prompt_code).
 */

@Repository
public class JulyAiPromptRepositoryImpl extends BaseRepository<JulyAiPromptPo, JulyAiPromptMapper>
        implements JulyAiPromptRepository {

    /**
     * Create the repository.
     *
     * @param mapper       mybatis-plus mapper
     * @param commonMapper native sql mapper
     */
    public JulyAiPromptRepositoryImpl(JulyAiPromptMapper mapper, CommonMapper commonMapper) {
        super(mapper, commonMapper);
    }

    /** {@inheritDoc} */
    @Override
    protected String getTableName() {
        return "july_ai_prompt";
    }

    /** {@inheritDoc} */
    @Override
    protected String getBusinessColumn() {
        return "prompt_code";
    }

    /** {@inheritDoc} */
    @Override
    protected Object getBusinessValue(JulyAiPromptPo entity) {
        return entity.getPromptCode();
    }

    /** {@inheritDoc} */
    @Override
    protected String duplicateMessage() {
        return "prompt code already exists";
    }

    /** {@inheritDoc} */
    @Override
    public void insert(JulyAiPrompt prompt) {
        insert(toPo(prompt));
    }

    /** {@inheritDoc} */
    @Override
    public void update(JulyAiPrompt prompt) {
        update(toPo(prompt));
    }

    /** {@inheritDoc} */
    @Override
    public JulyAiPrompt findById(String id) {
        JulyAiPromptPo po = getById(id);

        return po == null ? null : toAggregate(po);
    }

    /** {@inheritDoc} */
    @Override
    public JulyAiPrompt findByCode(String promptCode) {
        JulyAiPromptPo po = getByBusinessValue(promptCode);

        return po == null ? null : toAggregate(po);
    }

    /** {@inheritDoc} */
    @Override
    public boolean logicDeleteById(String id) {
        JulyAiPromptPo po = getById(id);

        if (po == null) {
            return false;
        }

        logicDelete(po);

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public List<JulyAiPrompt> findPage(int offset, int pageSize, JulyAiPromptQuerySpec spec) {
        int current = offset / pageSize + 1;
        Page<JulyAiPromptPo> page = Page.of(current, pageSize);

        return mapper.selectPage(page, specWrapper(spec)).getRecords().stream()
                .map(this::toAggregate)
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    public long count(JulyAiPromptQuerySpec spec) {
        return mapper.selectCount(specWrapper(spec));
    }

    /**
     * Filter wrapper shared by findPage and count.
     *
     * @param spec query condition
     * @return query wrapper
     */
    private QueryWrapper<JulyAiPromptPo> specWrapper(JulyAiPromptQuerySpec spec) {
        QueryWrapper<JulyAiPromptPo> wrapper = new QueryWrapper<>();
        JulyAiPromptQuerySpec query = spec == null ? new JulyAiPromptQuerySpec(null, null, null) : spec;

        if (query.hasKeyword()) {
            String keyword = query.keyword();
            wrapper.and(w -> w.like("prompt_code", keyword).or().like("prompt_name", keyword));
        }

        if (query.hasScene()) {
            wrapper.eq("scene", query.scene());
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
     * @param prompt aggregate
     * @return PO
     */
    private JulyAiPromptPo toPo(JulyAiPrompt prompt) {
        JulyAiPromptPo po = new JulyAiPromptPo();
        po.setId(prompt.id().value());
        po.setSortOrder(prompt.sortOrder());
        po.setPromptCode(prompt.promptCode());
        po.setPromptName(prompt.promptName());
        po.setScene(prompt.scene());
        po.setStatus(prompt.status());
        po.setRemark(prompt.remark());

        return po;
    }

    /**
     * Map a PO to the aggregate.
     *
     * @param po PO
     * @return aggregate
     */
    private JulyAiPrompt toAggregate(JulyAiPromptPo po) {
        AuditInfo audit = new AuditInfo(po.getCreateBy(), po.getUpdateBy(), po.getCreateTime(), po.getUpdateTime());

        return new JulyAiPrompt(EntityId.of(po.getId()), po.getPromptCode(), po.getPromptName(), po.getScene(),
                po.getSortOrder(), po.getStatus(), po.getRemark(), audit);
    }
}
