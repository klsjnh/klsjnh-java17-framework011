package com.klsjnh.infrastructure.aicenter.modelprovider.repository;

/*                AiModelProviderApiRepositoryImpl class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  ai model provider api repository impl class
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.aicenter.modelprovider.AiModelProviderApi;
import com.klsjnh.domain.aicenter.modelprovider.AiModelProviderApiRepository;
import com.klsjnh.domain.crypto.SecretCipherPort;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import com.klsjnh.infrastructure.aicenter.modelprovider.entity.AiModelProviderApiPo;
import com.klsjnh.infrastructure.aicenter.modelprovider.mapper.AiModelProviderApiMapper;
import com.klsjnh.infrastructure.persistence.mapper.CommonMapper;
import com.klsjnh.infrastructure.persistence.repository.BaseRepository;
import com.klsjnh.infrastructure.persistence.support.SortSupport;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.util.List;

/**
 * Repository implementation for the AiModelProviderApi child entity on the base
 * repository (july_ai_model_provider_api). Uniqueness is composite
 * (pk_mt, api_code), enforced in the use case and by the DDL unique key.
 */

@Repository
public class AiModelProviderApiRepositoryImpl
        extends BaseRepository<AiModelProviderApiPo, AiModelProviderApiMapper>
        implements AiModelProviderApiRepository {

    /**
     * Reversible cipher for the stored api key.
     */
    private final SecretCipherPort secretCipher;

    /**
     * Create the repository.
     *
     * @param mapper       mybatis-plus mapper
     * @param commonMapper native sql mapper
     * @param secretCipher secret cipher port
     */
    public AiModelProviderApiRepositoryImpl(AiModelProviderApiMapper mapper, CommonMapper commonMapper,
            SecretCipherPort secretCipher) {
        super(mapper, commonMapper);
        this.secretCipher = secretCipher;
    }

    /**
     * Table name for logs and maintenance statements.
     *
     * @return table name
     */
    @Override
    protected String getTableName() {
        return "july_ai_model_provider_api";
    }

    /**
     * Business unique column name; blank — uniqueness is composite and handled
     * by the use case plus the DDL unique key.
     *
     * @return empty string
     */
    @Override
    protected String getBusinessColumn() {
        return "";
    }

    /**
     * Insert a new api key.
     *
     * @param api entity
     */
    @Override
    public void insert(AiModelProviderApi api) {
        insert(toPo(api));
    }

    /**
     * Update an existing api key.
     *
     * @param api entity with id
     */
    @Override
    public void update(AiModelProviderApi api) {
        update(toPo(api));
    }

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return entity or null
     */
    @Override
    public AiModelProviderApi findById(String id) {
        AiModelProviderApiPo po = getById(id);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Find the ENABLED keys of a provider, ordered by sort order.
     *
     * @param providerId provider id
     * @return ordered entities, never null
     */
    @Override
    public List<AiModelProviderApi> findByMaster(String providerId) {
        return findAllByMaster(providerId, Status011.ENABLED.getCode());
    }

    /**
     * Find the keys of a provider (management view, optional status filter).
     *
     * @param providerId provider id
     * @param status     optional status filter, null for all
     * @return ordered entities, never null
     */
    @Override
    public List<AiModelProviderApi> findAllByMaster(String providerId, String status) {
        QueryWrapper<AiModelProviderApiPo> wrapper = new QueryWrapper<>();
        wrapper.eq("pk_mt", providerId);

        if (!StringUtil011.isBlank(status)) {
            wrapper.eq("status", status);
        }

        SortSupport.orderBySortThenId(wrapper);

        return mapper.selectList(wrapper).stream()
                .map(this::toAggregate)
                .toList();
    }

    /**
     * Find one key by provider id and api code.
     *
     * @param providerId provider id
     * @param apiCode    api code
     * @return entity or null
     */
    @Override
    public AiModelProviderApi findByMasterAndCode(String providerId, String apiCode) {
        QueryWrapper<AiModelProviderApiPo> wrapper = new QueryWrapper<>();
        wrapper.eq("pk_mt", providerId)
                .eq("api_code", apiCode)
                .last("LIMIT 1");

        AiModelProviderApiPo po = mapper.selectOne(wrapper);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Count the alive keys of a provider.
     *
     * @param providerId provider id
     * @return alive key count
     */
    @Override
    public long countByMaster(String providerId) {
        QueryWrapper<AiModelProviderApiPo> wrapper = new QueryWrapper<>();
        wrapper.eq("pk_mt", providerId);

        return mapper.selectCount(wrapper);
    }

    /**
     * Logic delete by primary key.
     *
     * @param id primary key
     * @return true when a row was deleted
     */
    @Override
    public boolean logicDeleteById(String id) {
        AiModelProviderApiPo po = getById(id);

        if (po == null) {
            return false;
        }

        logicDelete(po);

        return true;
    }

    /**
     * Map the entity to a PO.
     *
     * @param api entity
     * @return PO
     */
    private AiModelProviderApiPo toPo(AiModelProviderApi api) {
        AiModelProviderApiPo po = new AiModelProviderApiPo();
        po.setId(api.id().value());
        po.setPkMt(api.pkMt());
        po.setSortOrder(api.sortOrder());
        po.setApiCode(api.apiCode());
        po.setApiName(api.apiName());
        po.setApiKey(secretCipher.encrypt(api.apiKey()));
        po.setStatus(api.status());
        po.setRemark(api.remark());

        return po;
    }

    /**
     * Map a PO to the entity.
     *
     * @param po PO
     * @return entity
     */
    private AiModelProviderApi toAggregate(AiModelProviderApiPo po) {
        AuditInfo audit = new AuditInfo(po.getCreateBy(), po.getUpdateBy(), po.getCreateTime(), po.getUpdateTime());

        return new AiModelProviderApi(EntityId.of(po.getId()), po.getPkMt(), po.getSortOrder(), po.getApiCode(),
                po.getApiName(), secretCipher.decrypt(po.getApiKey()), po.getStatus(), po.getRemark(), audit);
    }
}
