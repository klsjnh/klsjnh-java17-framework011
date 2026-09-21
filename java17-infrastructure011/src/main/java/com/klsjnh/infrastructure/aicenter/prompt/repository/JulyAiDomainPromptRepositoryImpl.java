package com.klsjnh.infrastructure.aicenter.prompt.repository;

/*                JulyAiDomainPromptRepositoryImpl class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.21
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.21  july ai domain prompt repository impl class
 *
 */

import com.klsjnh.common.enums.Status011;

import com.klsjnh.domain.aicenter.prompt.JulyAiDomainPrompt;
import com.klsjnh.domain.aicenter.prompt.JulyAiDomainPromptQuerySpec;
import com.klsjnh.domain.aicenter.prompt.JulyAiDomainPromptRepository;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import com.klsjnh.infrastructure.aicenter.prompt.entity.JulyAiDomainPromptPo;
import com.klsjnh.infrastructure.aicenter.prompt.mapper.JulyAiDomainPromptMapper;
import com.klsjnh.infrastructure.persistence.mapper.CommonMapper;
import com.klsjnh.infrastructure.persistence.repository.BaseRepository;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * Repository implementation for the JulyAiDomainPrompt child entity
 * (july_ai_domain_prompt, business unique column prompt_code).
 */

@Repository
public class JulyAiDomainPromptRepositoryImpl
        extends BaseRepository<JulyAiDomainPromptPo, JulyAiDomainPromptMapper>
        implements JulyAiDomainPromptRepository {

    /**
     * Create the repository.
     *
     * @param mapper       mybatis-plus mapper
     * @param commonMapper native sql mapper
     */
    public JulyAiDomainPromptRepositoryImpl(JulyAiDomainPromptMapper mapper, CommonMapper commonMapper) {
        super(mapper, commonMapper);
    }

    /** {@inheritDoc} */
    @Override
    protected String getTableName() {
        return "july_ai_domain_prompt";
    }

    /** {@inheritDoc} */
    @Override
    protected String getBusinessColumn() {
        return "prompt_code";
    }

    /** {@inheritDoc} */
    @Override
    protected Object getBusinessValue(JulyAiDomainPromptPo entity) {
        return entity.getPromptCode();
    }

    /** {@inheritDoc} */
    @Override
    protected String duplicateMessage() {
        return "prompt code already exists";
    }

    /** {@inheritDoc} */
    @Override
    public void insert(JulyAiDomainPrompt prompt) {
        insert(toPo(prompt));
    }

    /** {@inheritDoc} */
    @Override
    public void update(JulyAiDomainPrompt prompt) {
        update(toPo(prompt));
    }

    /** {@inheritDoc} */
    @Override
    public JulyAiDomainPrompt findById(String id) {
        JulyAiDomainPromptPo po = getById(id);

        return po == null ? null : toAggregate(po);
    }

    /** {@inheritDoc} */
    @Override
    public JulyAiDomainPrompt findByCode(String promptCode) {
        JulyAiDomainPromptPo po = getByBusinessValue(promptCode);

        return po == null ? null : toAggregate(po);
    }

    /** {@inheritDoc} */
    @Override
    public List<JulyAiDomainPrompt> findByMaster(String pkMt) {
        QueryWrapper<JulyAiDomainPromptPo> wrapper = new QueryWrapper<>();
        wrapper.eq("pk_mt", pkMt)
                .eq("status", Status011.ENABLED.getCode())
                .orderByAsc("sort_order")
                .orderByAsc("id");

        return mapper.selectList(wrapper).stream().map(this::toAggregate).toList();
    }

    /** {@inheritDoc} */
    @Override
    public long countByMaster(String pkMt, boolean enabledOnly) {
        QueryWrapper<JulyAiDomainPromptPo> wrapper = new QueryWrapper<>();
        wrapper.eq("pk_mt", pkMt);

        if (enabledOnly) {
            wrapper.eq("status", Status011.ENABLED.getCode());
        }

        return mapper.selectCount(wrapper);
    }

    /** {@inheritDoc} */
    @Override
    public boolean logicDeleteById(String id) {
        JulyAiDomainPromptPo po = getById(id);

        if (po == null) {
            return false;
        }

        logicDelete(po);

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void logicDeleteByMaster(String pkMt) {
        QueryWrapper<JulyAiDomainPromptPo> wrapper = new QueryWrapper<>();
        wrapper.eq("pk_mt", pkMt);

        List<JulyAiDomainPromptPo> rows = mapper.selectList(wrapper);

        if (!rows.isEmpty()) {
            batchLogicDelete(rows.stream().map(JulyAiDomainPromptPo::getId).toList());
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<JulyAiDomainPrompt> findPage(int offset, int pageSize, JulyAiDomainPromptQuerySpec spec) {
        int current = offset / pageSize + 1;
        Page<JulyAiDomainPromptPo> page = Page.of(current, pageSize);

        return mapper.selectPage(page, specWrapper(spec)).getRecords().stream()
                .map(this::toAggregate)
                .toList();
    }

    /** {@inheritDoc} */
    @Override
    public long count(JulyAiDomainPromptQuerySpec spec) {
        return mapper.selectCount(specWrapper(spec));
    }

    /**
     * Filter wrapper shared by findPage and count.
     *
     * @param spec query condition
     * @return query wrapper
     */
    private QueryWrapper<JulyAiDomainPromptPo> specWrapper(JulyAiDomainPromptQuerySpec spec) {
        QueryWrapper<JulyAiDomainPromptPo> wrapper = new QueryWrapper<>();
        JulyAiDomainPromptQuerySpec query = spec == null ? new JulyAiDomainPromptQuerySpec(null, null, null, null) : spec;

        if (query.hasKeyword()) {
            String keyword = query.keyword();
            wrapper.and(w -> w.like("prompt_code", keyword).or().like("prompt_name", keyword));
        }

        if (query.hasPkMt()) {
            wrapper.eq("pk_mt", query.pkMt());
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
    private JulyAiDomainPromptPo toPo(JulyAiDomainPrompt prompt) {
        JulyAiDomainPromptPo po = new JulyAiDomainPromptPo();
        po.setId(prompt.id().value());
        po.setPkMt(prompt.pkMt());
        po.setSortOrder(prompt.sortOrder());
        po.setPromptCode(prompt.promptCode());
        po.setPromptName(prompt.promptName());
        po.setScene(prompt.scene());
        po.setContentMode(prompt.contentMode());
        po.setContent(prompt.content());
        po.setStorageCode(prompt.storageCode());
        po.setBucket(prompt.bucket());
        po.setObjectKey(prompt.objectKey());
        po.setContentHash(prompt.contentHash());
        po.setContentSize(prompt.contentSize());
        po.setVariables(prompt.variables());
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
    private JulyAiDomainPrompt toAggregate(JulyAiDomainPromptPo po) {
        AuditInfo audit = new AuditInfo(po.getCreateBy(), po.getUpdateBy(), po.getCreateTime(), po.getUpdateTime());

        return new JulyAiDomainPrompt(EntityId.of(po.getId()), po.getPkMt(), po.getPromptCode(), po.getPromptName(),
                po.getScene(), po.getContentMode(), po.getContent(), po.getStorageCode(), po.getBucket(),
                po.getObjectKey(), po.getContentHash(), po.getContentSize(), po.getVariables(), po.getSortOrder(),
                po.getStatus(), po.getRemark(), audit);
    }
}
