package com.klsjnh.infrastructure.aicenter.prompt.repository;

/*                JulyAiPromptDetailRepositoryImpl class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  july ai prompt detail repository impl class
 *
 */

import com.klsjnh.common.enums.Status011;

import com.klsjnh.domain.aicenter.prompt.JulyAiPromptDetail;
import com.klsjnh.domain.aicenter.prompt.JulyAiPromptDetailRepository;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import com.klsjnh.infrastructure.aicenter.prompt.entity.JulyAiPromptDetailPo;
import com.klsjnh.infrastructure.aicenter.prompt.mapper.JulyAiPromptDetailMapper;
import com.klsjnh.infrastructure.persistence.mapper.CommonMapper;
import com.klsjnh.infrastructure.persistence.repository.BaseRepository;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.util.List;

/**
 * Repository implementation for the JulyAiPromptDetail child entity
 * (july_ai_prompt_detail).
 */

@Repository
public class JulyAiPromptDetailRepositoryImpl
        extends BaseRepository<JulyAiPromptDetailPo, JulyAiPromptDetailMapper>
        implements JulyAiPromptDetailRepository {

    /**
     * Create the repository.
     *
     * @param mapper       mybatis-plus mapper
     * @param commonMapper native sql mapper
     */
    public JulyAiPromptDetailRepositoryImpl(JulyAiPromptDetailMapper mapper, CommonMapper commonMapper) {
        super(mapper, commonMapper);
    }

    /** {@inheritDoc} */
    @Override
    protected String getTableName() {
        return "july_ai_prompt_detail";
    }

    /** {@inheritDoc} */
    @Override
    protected String getBusinessColumn() {
        return "";
    }

    /** {@inheritDoc} */
    @Override
    public void insert(JulyAiPromptDetail detail) {
        insert(toPo(detail));
    }

    /** {@inheritDoc} */
    @Override
    public void update(JulyAiPromptDetail detail) {
        update(toPo(detail));
    }

    /** {@inheritDoc} */
    @Override
    public JulyAiPromptDetail findById(String id) {
        JulyAiPromptDetailPo po = getById(id);

        return po == null ? null : toAggregate(po);
    }

    /** {@inheritDoc} */
    @Override
    public List<JulyAiPromptDetail> findByMaster(String pkMt) {
        QueryWrapper<JulyAiPromptDetailPo> wrapper = new QueryWrapper<>();
        wrapper.eq("pk_mt", pkMt)
                .eq("status", Status011.ENABLED.getCode())
                .orderByAsc("sort_order")
                .orderByAsc("id");

        return mapper.selectList(wrapper).stream().map(this::toAggregate).toList();
    }

    /** {@inheritDoc} */
    @Override
    public JulyAiPromptDetail findByDomain(String pkMt, String domainCode) {
        QueryWrapper<JulyAiPromptDetailPo> wrapper = new QueryWrapper<>();
        wrapper.eq("pk_mt", pkMt)
                .eq("domain_code", domainCode)
                .eq("status", Status011.ENABLED.getCode())
                .last("LIMIT 1");

        JulyAiPromptDetailPo po = mapper.selectOne(wrapper);

        return po == null ? null : toAggregate(po);
    }

    /** {@inheritDoc} */
    @Override
    public long countByMaster(String pkMt, boolean enabledOnly) {
        QueryWrapper<JulyAiPromptDetailPo> wrapper = new QueryWrapper<>();
        wrapper.eq("pk_mt", pkMt);

        if (enabledOnly) {
            wrapper.eq("status", Status011.ENABLED.getCode());
        }

        return mapper.selectCount(wrapper);
    }

    /** {@inheritDoc} */
    @Override
    public boolean logicDeleteById(String id) {
        JulyAiPromptDetailPo po = getById(id);

        if (po == null) {
            return false;
        }

        logicDelete(po);

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void logicDeleteByMaster(String pkMt) {
        QueryWrapper<JulyAiPromptDetailPo> wrapper = new QueryWrapper<>();
        wrapper.eq("pk_mt", pkMt);

        List<JulyAiPromptDetailPo> rows = mapper.selectList(wrapper);

        if (!rows.isEmpty()) {
            batchLogicDelete(rows.stream().map(JulyAiPromptDetailPo::getId).toList());
        }
    }

    /**
     * Map the aggregate to a PO.
     *
     * @param detail aggregate
     * @return PO
     */
    private JulyAiPromptDetailPo toPo(JulyAiPromptDetail detail) {
        JulyAiPromptDetailPo po = new JulyAiPromptDetailPo();
        po.setId(detail.id().value());
        po.setPkMt(detail.pkMt());
        po.setSortOrder(detail.sortOrder());
        po.setDomainCode(detail.domainCode());
        po.setContentMode(detail.contentMode());
        po.setContent(detail.content());
        po.setStorageCode(detail.storageCode());
        po.setBucket(detail.bucket());
        po.setObjectKey(detail.objectKey());
        po.setContentHash(detail.contentHash());
        po.setContentSize(detail.contentSize());
        po.setVariables(detail.variables());
        po.setStatus(detail.status());
        po.setRemark(detail.remark());

        return po;
    }

    /**
     * Map a PO to the aggregate.
     *
     * @param po PO
     * @return aggregate
     */
    private JulyAiPromptDetail toAggregate(JulyAiPromptDetailPo po) {
        AuditInfo audit = new AuditInfo(po.getCreateBy(), po.getUpdateBy(), po.getCreateTime(), po.getUpdateTime());

        return new JulyAiPromptDetail(EntityId.of(po.getId()), po.getPkMt(), po.getDomainCode(), po.getContentMode(),
                po.getContent(), po.getStorageCode(), po.getBucket(), po.getObjectKey(), po.getContentHash(),
                po.getContentSize(), po.getVariables(), po.getSortOrder(), po.getStatus(), po.getRemark(), audit);
    }
}
