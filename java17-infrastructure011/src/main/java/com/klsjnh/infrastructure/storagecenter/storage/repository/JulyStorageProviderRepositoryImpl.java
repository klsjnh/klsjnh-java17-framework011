package com.klsjnh.infrastructure.storagecenter.storage.repository;

/*                JulyStorageProviderRepositoryImpl class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july storage provider repository impl class
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;
import com.klsjnh.domain.storagecenter.storage.JulyStorageProvider;
import com.klsjnh.domain.storagecenter.storage.JulyStorageProviderQuerySpec;
import com.klsjnh.domain.storagecenter.storage.JulyStorageProviderRepository;

import com.klsjnh.infrastructure.persistence.mapper.CommonMapper;
import com.klsjnh.infrastructure.persistence.repository.BaseMasterSubRepository011;
import com.klsjnh.infrastructure.persistence.repository.BaseRepository;
import com.klsjnh.infrastructure.persistence.sql.QuotedLiteral;
import com.klsjnh.infrastructure.storagecenter.storage.entity.JulyStorageProviderPo;
import com.klsjnh.infrastructure.storagecenter.storage.mapper.JulyStorageProviderMapper;
import com.klsjnh.infrastructure.persistence.support.SortSupport;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * Repository implementation for the JulyStorageProvider aggregate on the
 * master-sub base (july_storage_provider master, july_storage_provider_bucket
 * child; business unique column storage_code).
 */

@Repository
public class JulyStorageProviderRepositoryImpl
        extends BaseMasterSubRepository011<JulyStorageProviderPo, JulyStorageProviderMapper>
        implements JulyStorageProviderRepository {

    /**
     * Bucket (child) repository.
     */
    private final JulyStorageProviderBucketRepositoryImpl bucketRepository;

    /**
     * Create the repository.
     *
     * @param mapper           mybatis-plus mapper
     * @param commonMapper     native sql mapper
     * @param bucketRepository bucket child repository
     */
    public JulyStorageProviderRepositoryImpl(JulyStorageProviderMapper mapper, CommonMapper commonMapper,
            JulyStorageProviderBucketRepositoryImpl bucketRepository) {
        super(mapper, commonMapper);
        this.bucketRepository = bucketRepository;
    }

    /** {@inheritDoc} */
    @Override
    protected List<BaseRepository<?, ?>> getChildServices() {
        return List.of(bucketRepository);
    }

    /**
     * Table name for logs and maintenance statements.
     *
     * @return table name
     */
    @Override
    protected String getTableName() {
        return "july_storage_provider";
    }

    /**
     * Business unique column name.
     *
     * @return column name
     */
    @Override
    protected String getBusinessColumn() {
        return "storage_code";
    }

    /**
     * Extract business value from entity.
     *
     * @param entity entity
     * @return field value
     */
    @Override
    protected Object getBusinessValue(JulyStorageProviderPo entity) {
        return entity.getStorageCode();
    }

    /**
     * Duplicate message for the unique storageCode.
     *
     * @return message
     */
    @Override
    protected String duplicateMessage() {
        return "storage code already exists";
    }

    /**
     * Insert a new aggregate.
     *
     * @param storage aggregate
     */
    @Override
    public void insert(JulyStorageProvider storage) {
        insert(toPo(storage));
    }

    /**
     * Update an existing aggregate.
     *
     * @param storage aggregate with id
     */
    @Override
    public void update(JulyStorageProvider storage) {
        update(toPo(storage));
    }

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return aggregate or null
     */
    @Override
    public JulyStorageProvider findById(String id) {
        JulyStorageProviderPo po = getById(id);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Find by storage code, enabled or not (management read entry).
     *
     * @param storageCode storage code
     * @return aggregate or null
     */
    @Override
    public JulyStorageProvider findByCode(String storageCode) {
        JulyStorageProviderPo po = getByBusinessValue(storageCode);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Find by storage code, ENABLED only (runtime resolver entry).
     *
     * @param storageCode storage code
     * @return aggregate or null
     */
    @Override
    public JulyStorageProvider findEnabledByCode(String storageCode) {
        QueryWrapper<JulyStorageProviderPo> wrapper = new QueryWrapper<>();
        wrapper.eq("storage_code", storageCode)
                .eq("status", Status011.ENABLED.getCode())
                .last("LIMIT 1");

        JulyStorageProviderPo po = mapper.selectOne(wrapper);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Whether a storage code exists including logic-deleted rows (seed
     * idempotency); the storageCode is bound through a QuotedLiteral.
     *
     * @param storageCode storage code
     * @return true when a row exists, deleted or not
     */
    @Override
    public boolean existsIncludingDeleted(String storageCode) {
        if (StringUtil011.isBlank(storageCode)) {
            return false;
        }

        String sql = "SELECT COUNT(*) FROM " + getTableName() + " WHERE " + getBusinessColumn() + " = "
                + QuotedLiteral.of(storageCode);

        return commonMapper.countBy(sql) > 0;
    }

        /** {@inheritDoc} */
    @Override
    public void logicDeleteByIds(List<String> ids) {
        batchLogicDelete(ids);
    }

/**
     * Logic delete by primary key.
     *
     * @param id primary key
     * @return true when a row was deleted
     */
    @Override
    public boolean logicDeleteById(String id) {
        JulyStorageProviderPo po = getById(id);

        if (po == null) {
            return false;
        }

        logicDelete(po);

        return true;
    }

    /**
     * Offset based page query on the management view.
     *
     * @param offset   zero-based row offset
     * @param pageSize page size
     * @param spec     query condition
     * @return page rows
     */
    @Override
    public List<JulyStorageProvider> findPage(int offset, int pageSize, JulyStorageProviderQuerySpec spec) {
        int current = offset / pageSize + 1;
        Page<JulyStorageProviderPo> page = Page.of(current, pageSize);

        return mapper.selectPage(page, specWrapper(spec)).getRecords().stream()
                .map(this::toAggregate)
                .toList();
    }

    /**
     * Count with the same filter as findPage.
     *
     * @param spec query condition
     * @return total row count
     */
    @Override
    public long count(JulyStorageProviderQuerySpec spec) {
        return mapper.selectCount(specWrapper(spec));
    }

    /**
     * Filter wrapper shared by findPage and count.
     *
     * @param spec query condition
     * @return query wrapper
     */
    private QueryWrapper<JulyStorageProviderPo> specWrapper(JulyStorageProviderQuerySpec spec) {
        QueryWrapper<JulyStorageProviderPo> wrapper = new QueryWrapper<>();
        JulyStorageProviderQuerySpec query = spec == null ? new JulyStorageProviderQuerySpec(null, null, null) : spec;

        if (query.hasKeyword()) {
            String keyword = query.keyword();
            wrapper.and(w -> w.like("storage_code", keyword).or().like("storage_name", keyword));
        }

        if (query.hasProvider()) {
            wrapper.eq("provider", query.provider());
        }

        if (query.hasStatus()) {
            wrapper.eq("status", query.status());
        }

        SortSupport.orderBySortThenId(wrapper);

        return wrapper;
    }

    /**
     * Map the aggregate to a PO.
     *
     * @param storage aggregate
     * @return PO
     */
    private JulyStorageProviderPo toPo(JulyStorageProvider storage) {
        JulyStorageProviderPo po = new JulyStorageProviderPo();
        po.setId(storage.id().value());
        po.setSortOrder(storage.sortOrder());
        po.setStorageCode(storage.storageCode());
        po.setStorageName(storage.storageName());
        po.setProvider(storage.provider());
        po.setBasePath(storage.basePath());
        po.setEndpoint(storage.endpoint());
        po.setAccessKey(storage.accessKey());
        po.setSecretKey(storage.secretKey());
        po.setSecure(storage.secure() ? "1" : "0");
        po.setPresignExpirySeconds(storage.presignExpirySeconds());
        po.setRemark(storage.remark());
        po.setStatus(storage.status());

        return po;
    }

    /**
     * Map a PO to the aggregate.
     *
     * @param po PO
     * @return aggregate
     */
    private JulyStorageProvider toAggregate(JulyStorageProviderPo po) {
        AuditInfo audit = new AuditInfo(po.getCreateBy(), po.getUpdateBy(), po.getCreateTime(), po.getUpdateTime());
        int presign = po.getPresignExpirySeconds() == null ? 0 : po.getPresignExpirySeconds();

        return new JulyStorageProvider(EntityId.of(po.getId()), po.getStorageCode(), po.getSortOrder(),
                po.getStorageName(), po.getProvider(), po.getBasePath(), po.getEndpoint(), po.getAccessKey(),
                po.getSecretKey(), "1".equals(po.getSecure()), presign, po.getRemark(), po.getStatus(), audit);
    }
}
