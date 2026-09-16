package com.klsjnh.infrastructure.storage.repository;

/*                JulyStorageRepositoryImpl class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july storage repository impl class
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;
import com.klsjnh.domain.storage.JulyStorage;
import com.klsjnh.domain.storage.JulyStorageQuerySpec;
import com.klsjnh.domain.storage.JulyStorageRepository;

import com.klsjnh.infrastructure.persistence.mapper.CommonMapper;
import com.klsjnh.infrastructure.persistence.repository.BaseRepository;
import com.klsjnh.infrastructure.persistence.sql.QuotedLiteral;
import com.klsjnh.infrastructure.storage.entity.JulyStoragePo;
import com.klsjnh.infrastructure.storage.mapper.JulyStorageMapper;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
 * Repository implementation for the JulyStorage aggregate on the base
 * repository (july_storage, business unique column storage_code).
 */

@Repository
public class JulyStorageRepositoryImpl
        extends BaseRepository<JulyStoragePo, JulyStorageMapper>
        implements JulyStorageRepository {

    /**
     * Create the repository.
     *
     * @param mapper       mybatis-plus mapper
     * @param commonMapper native sql mapper
     */
    public JulyStorageRepositoryImpl(JulyStorageMapper mapper, CommonMapper commonMapper) {
        super(mapper, commonMapper);
    }

    /**
     * Table name for logs and maintenance statements.
     *
     * @return table name
     */
    @Override
    protected String getTableName() {
        return "july_storage";
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
    protected Object getBusinessValue(JulyStoragePo entity) {
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
    public void insert(JulyStorage storage) {
        insert(toPo(storage));
    }

    /**
     * Update an existing aggregate.
     *
     * @param storage aggregate with id
     */
    @Override
    public void update(JulyStorage storage) {
        update(toPo(storage));
    }

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return aggregate or null
     */
    @Override
    public JulyStorage findById(String id) {
        JulyStoragePo po = getById(id);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Find by storage code, enabled or not (management read entry).
     *
     * @param storageCode storage code
     * @return aggregate or null
     */
    @Override
    public JulyStorage findByCode(String storageCode) {
        JulyStoragePo po = getByBusinessValue(storageCode);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Find by storage code, ENABLED only (runtime resolver entry).
     *
     * @param storageCode storage code
     * @return aggregate or null
     */
    @Override
    public JulyStorage findEnabledByCode(String storageCode) {
        QueryWrapper<JulyStoragePo> wrapper = new QueryWrapper<>();
        wrapper.eq("storage_code", storageCode)
                .eq("status", Status011.ENABLED.getCode())
                .last("LIMIT 1");

        JulyStoragePo po = mapper.selectOne(wrapper);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Whether a storage code exists including logic-deleted rows (seed
     * idempotency); the dsCode is bound through a QuotedLiteral.
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

    /**
     * Logic delete by primary key.
     *
     * @param id primary key
     * @return true when a row was deleted
     */
    @Override
    public boolean logicDeleteById(String id) {
        JulyStoragePo po = getById(id);

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
    public List<JulyStorage> findPage(int offset, int pageSize, JulyStorageQuerySpec spec) {
        int current = offset / pageSize + 1;
        Page<JulyStoragePo> page = Page.of(current, pageSize);

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
    public long count(JulyStorageQuerySpec spec) {
        return mapper.selectCount(specWrapper(spec));
    }

    /**
     * Filter wrapper shared by findPage and count.
     *
     * @param spec query condition
     * @return query wrapper
     */
    private QueryWrapper<JulyStoragePo> specWrapper(JulyStorageQuerySpec spec) {
        QueryWrapper<JulyStoragePo> wrapper = new QueryWrapper<>();
        JulyStorageQuerySpec query = spec == null ? new JulyStorageQuerySpec(null, null, null) : spec;

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

        wrapper.orderByAsc("sort_order").orderByAsc("id");

        return wrapper;
    }

    /**
     * Map the aggregate to a PO.
     *
     * @param storage aggregate
     * @return PO
     */
    private JulyStoragePo toPo(JulyStorage storage) {
        JulyStoragePo po = new JulyStoragePo();
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
        po.setDefaultBucket(storage.defaultBucket());
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
    private JulyStorage toAggregate(JulyStoragePo po) {
        AuditInfo audit = new AuditInfo(po.getCreateBy(), po.getUpdateBy(), po.getCreateTime(), po.getUpdateTime());
        int presign = po.getPresignExpirySeconds() == null ? 0 : po.getPresignExpirySeconds();

        return new JulyStorage(EntityId.of(po.getId()), po.getStorageCode(), po.getSortOrder(), po.getStorageName(),
                po.getProvider(), po.getBasePath(), po.getEndpoint(), po.getAccessKey(), po.getSecretKey(),
                "1".equals(po.getSecure()), po.getDefaultBucket(), presign, po.getRemark(), po.getStatus(), audit);
    }
}
