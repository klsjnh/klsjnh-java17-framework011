package com.klsjnh.infrastructure.storagecenter.storage.repository;

/*                JulyStorageProviderBucketRepositoryImpl class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july storage provider bucket repository impl class
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;
import com.klsjnh.domain.storagecenter.storage.JulyStorageProviderBucket;
import com.klsjnh.domain.storagecenter.storage.JulyStorageProviderBucketRepository;

import com.klsjnh.infrastructure.persistence.mapper.CommonMapper;
import com.klsjnh.infrastructure.persistence.repository.BaseRepository;
import com.klsjnh.infrastructure.persistence.sql.QuotedLiteral;
import com.klsjnh.infrastructure.storagecenter.storage.entity.JulyStorageProviderBucketPo;
import com.klsjnh.infrastructure.storagecenter.storage.mapper.JulyStorageProviderBucketMapper;
import com.klsjnh.infrastructure.persistence.support.SortSupport;

import org.springframework.stereotype.Repository;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.util.List;

/**
 * Repository implementation for the JulyStorageProviderBucket child entity on
 * the base repository (july_storage_provider_bucket). Uniqueness is composite
 * (pk_mt, bucket_code), enforced in the use case and by the DDL unique key.
 */

@Repository
public class JulyStorageProviderBucketRepositoryImpl
        extends BaseRepository<JulyStorageProviderBucketPo, JulyStorageProviderBucketMapper>
        implements JulyStorageProviderBucketRepository {

    /**
     * Create the repository.
     *
     * @param mapper       mybatis-plus mapper
     * @param commonMapper native sql mapper
     */
    public JulyStorageProviderBucketRepositoryImpl(JulyStorageProviderBucketMapper mapper, CommonMapper commonMapper) {
        super(mapper, commonMapper);
    }

    /**
     * Table name for logs and maintenance statements.
     *
     * @return table name
     */
    @Override
    protected String getTableName() {
        return "july_storage_provider_bucket";
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
     * Insert a new bucket.
     *
     * @param bucket entity
     */
    @Override
    public void insert(JulyStorageProviderBucket bucket) {
        insert(toPo(bucket));
    }

    /**
     * Update an existing bucket.
     *
     * @param bucket entity with id
     */
    @Override
    public void update(JulyStorageProviderBucket bucket) {
        update(toPo(bucket));
    }

    /**
     * Find by primary key.
     *
     * @param id primary key
     * @return entity or null
     */
    @Override
    public JulyStorageProviderBucket findById(String id) {
        JulyStorageProviderBucketPo po = getById(id);

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
        JulyStorageProviderBucketPo po = getById(id);

        if (po == null) {
            return false;
        }

        logicDelete(po);

        return true;
    }

    /**
     * Find the buckets of a storage instance, ordered by sort order.
     *
     * @param pkMt storage provider id
     * @return ordered entities, never null
     */
    @Override
    public List<JulyStorageProviderBucket> findByPkMt(String pkMt) {
        QueryWrapper<JulyStorageProviderBucketPo> wrapper = new QueryWrapper<>();
        wrapper.eq("pk_mt", pkMt);
        SortSupport.orderBySortThenId(wrapper);

        return mapper.selectList(wrapper).stream()
                .map(this::toAggregate)
                .toList();
    }

    /**
     * Find the default ENABLED bucket of a storage instance.
     *
     * @param pkMt storage provider id
     * @return entity or null when none
     */
    @Override
    public JulyStorageProviderBucket findDefault(String pkMt) {
        QueryWrapper<JulyStorageProviderBucketPo> wrapper = new QueryWrapper<>();
        wrapper.eq("pk_mt", pkMt)
                .eq("is_default", "1")
                .eq("status", Status011.ENABLED.getCode())
                .last("LIMIT 1");

        JulyStorageProviderBucketPo po = mapper.selectOne(wrapper);

        return po == null ? null : toAggregate(po);
    }

    /**
     * Whether a bucket code exists in an instance including logic-deleted rows;
     * the values are bound through QuotedLiterals.
     *
     * @param pkMt       storage provider id
     * @param bucketCode bucket code
     * @return true when a row exists, deleted or not
     */
    @Override
    public boolean existsIncludingDeleted(String pkMt, String bucketCode) {
        if (StringUtil011.isBlank(pkMt) || StringUtil011.isBlank(bucketCode)) {
            return false;
        }

        String sql = "SELECT COUNT(*) FROM " + getTableName() + " WHERE pk_mt = " + QuotedLiteral.of(pkMt)
                + " AND bucket_code = " + QuotedLiteral.of(bucketCode);

        return commonMapper.countBy(sql) > 0;
    }

    /**
     * Whether an instance has any bucket row at all, including logic-deleted
     * rows; the pk_mt is bound through a QuotedLiteral.
     *
     * @param pkMt storage provider id
     * @return true when a row exists, deleted or not
     */
    @Override
    public boolean hasAnyIncludingDeleted(String pkMt) {
        if (StringUtil011.isBlank(pkMt)) {
            return false;
        }

        String sql = "SELECT COUNT(*) FROM " + getTableName() + " WHERE pk_mt = " + QuotedLiteral.of(pkMt);

        return commonMapper.countBy(sql) > 0;
    }

    /**
     * Map the entity to a PO.
     *
     * @param bucket entity
     * @return PO
     */
    private JulyStorageProviderBucketPo toPo(JulyStorageProviderBucket bucket) {
        JulyStorageProviderBucketPo po = new JulyStorageProviderBucketPo();
        po.setId(bucket.id().value());
        po.setPkMt(bucket.pkMt());
        po.setSortOrder(bucket.sortOrder());
        po.setBucketCode(bucket.bucketCode());
        po.setBucketName(bucket.bucketName());
        po.setIsDefault(bucket.isDefault() ? "1" : "0");
        po.setStatus(bucket.status());
        po.setRemark(bucket.remark());

        return po;
    }

    /**
     * Map a PO to the entity.
     *
     * @param po PO
     * @return entity
     */
    private JulyStorageProviderBucket toAggregate(JulyStorageProviderBucketPo po) {
        AuditInfo audit = new AuditInfo(po.getCreateBy(), po.getUpdateBy(), po.getCreateTime(), po.getUpdateTime());

        return new JulyStorageProviderBucket(EntityId.of(po.getId()), po.getPkMt(), po.getBucketCode(),
                po.getBucketName(), "1".equals(po.getIsDefault()), po.getSortOrder(), po.getRemark(), po.getStatus(),
                audit);
    }
}
