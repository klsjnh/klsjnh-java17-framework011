package com.klsjnh.application.storagecenter.storage;

/*                JulyStorageProviderUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july storage provider use case class
 *      2026.09.26  explicit permission checks (julyStorageProvider)
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.util.StringUtil011;
import com.klsjnh.common.vo.BatchDeleteResultVo011;

import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;
import com.klsjnh.domain.storagecenter.storage.JulyStorageProvider;
import com.klsjnh.domain.storagecenter.storage.JulyStorageProviderBucket;
import com.klsjnh.domain.storagecenter.storage.JulyStorageProviderBucketRepository;
import com.klsjnh.domain.storagecenter.storage.JulyStorageProviderPermissionCodes011;
import com.klsjnh.domain.storagecenter.storage.JulyStorageProviderQuerySpec;
import com.klsjnh.domain.storagecenter.storage.JulyStorageProviderRepository;
import com.klsjnh.domain.iam.auth.AuthorizationPort;
import com.klsjnh.domain.storagecenter.object.ObjectStorageFactoryPort;
import com.klsjnh.domain.storagecenter.object.StorageConnectionConfig;
import com.klsjnh.domain.storagecenter.object.StorageProbe;
import com.klsjnh.domain.storagecenter.object.StorageResolverPort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * JulyStorageProvider use cases: storage provider CRUD plus the connectivity
 * probe (saved instance or draft config). The default bucket is persisted as a
 * child row of july_storage_provider_bucket.
 */

@Service
public class JulyStorageProviderUseCase {

    /**
     * Storage provider repository.
     */
    private final JulyStorageProviderRepository repository;

    /**
     * Storage bucket repository.
     */
    private final JulyStorageProviderBucketRepository bucketRepository;

    /**
     * Adapter factory (draft probe).
     */
    private final ObjectStorageFactoryPort factory;

    /**
     * Resolver (cache eviction on change).
     */
    private final StorageResolverPort resolver;

    private final AuthorizationPort authorizationPort;

    public JulyStorageProviderUseCase(JulyStorageProviderRepository repository,
            JulyStorageProviderBucketRepository bucketRepository, ObjectStorageFactoryPort factory,
            StorageResolverPort resolver, AuthorizationPort authorizationPort) {
        this.repository = repository;
        this.bucketRepository = bucketRepository;
        this.factory = factory;
        this.resolver = resolver;
        this.authorizationPort = authorizationPort;
    }

    /**
     * Insert a new storage instance and its default bucket child.
     *
     * @param storageCode          storage code, unique, immutable
     * @param sortOrder            manual sort order, null falls back to the default
     * @param storageName          display name
     * @param provider             storage type code
     * @param basePath             local root
     * @param endpoint             endpoint
     * @param accessKey            access key
     * @param secretKey            secret key
     * @param secure               whether to use HTTPS
     * @param defaultBucket        default bucket, optional
     * @param presignExpirySeconds presigned URL expiry seconds
     * @param remark               remark
     * @return new storage id
     */
    @Transactional
    public String insert(String operatorId, String storageCode, Integer sortOrder, String storageName, String provider,
            String basePath, String endpoint, String accessKey, String secretKey, boolean secure, String defaultBucket,
            Integer presignExpirySeconds, String remark) {
        authorizationPort.assertHas(operatorId, JulyStorageProviderPermissionCodes011.INSERT);
        if (repository.findByCode(storageCode) != null) {
            throw BusinessException.badRequest("storage code already exists: " + storageCode);
        }

        JulyStorageProvider storage = newStorage(storageCode, sortOrder, storageName, provider, basePath, endpoint,
                accessKey, secretKey, secure, presignExpirySeconds, remark);
        repository.insert(storage);
        ensureDefaultBucket(storage.id().value(), defaultBucket);

        return storage.id().value();
    }

    /**
     * Update a storage instance; a blank secretKey keeps the stored one. A
     * non-blank defaultBucket upserts the default bucket child row.
     *
     * @param id                   storage id
     * @param storageName          display name
     * @param provider             storage type code
     * @param basePath             local root
     * @param endpoint             endpoint
     * @param accessKey            access key
     * @param secretKey            secret key, blank keeps the stored one
     * @param secure               whether to use HTTPS
     * @param defaultBucket        default bucket, optional
     * @param presignExpirySeconds presigned URL expiry seconds
     * @param remark               remark
     * @param status               row status, null keeps the stored one
     * @return storage id
     */
    @Transactional
    public String update(String operatorId, String id, String storageName, String provider, String basePath,
            String endpoint, String accessKey, String secretKey, boolean secure, String defaultBucket,
            Integer presignExpirySeconds, String remark, String status) {
        authorizationPort.assertHas(operatorId, JulyStorageProviderPermissionCodes011.UPDATE);
        JulyStorageProvider storage = require(id);
        requireStatus(status);

        try {
            storage.update(storageName, provider, basePath, endpoint, accessKey, secretKey, secure,
                    presignExpirySeconds, remark, status);
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }

        repository.update(storage);
        ensureDefaultBucket(storage.id().value(), defaultBucket);
        resolver.evict(storage.storageCode());

        return storage.id().value();
    }

    /**
     * Logic delete a storage instance.
     *
     * @param id storage id
     * @return deleted storage id
     */
    @Transactional
    public String logicDelete(String operatorId, String id) {
        authorizationPort.assertHas(operatorId, JulyStorageProviderPermissionCodes011.LOGIC_DELETE);

        JulyStorageProvider storage = require(id);

        if (!repository.logicDeleteById(id)) {
            throw BusinessException.recordNotFound(id);
        }

        resolver.evict(storage.storageCode());

        return id;
    }

    /**
     * Batch logic delete, all-or-nothing: a missing id fails the whole batch
     * (404) so the transaction rolls back; the resolver cache is then evicted
     * for every deleted instance.
     *
     * @param ids storage ids
     * @return batch delete summary
     */
    @Transactional
    public BatchDeleteResultVo011 logicDeleteBatch(String operatorId, List<String> ids) {
        authorizationPort.assertHas(operatorId, JulyStorageProviderPermissionCodes011.LOGIC_DELETE);
        List<String> normalized = normalize(ids);

        if (normalized.isEmpty()) {
            throw BusinessException.badRequest("batch logic delete: ids is required");
        }

        for (String id : normalized) {
            JulyStorageProvider storage = repository.findById(id);

            if (storage != null) {
                resolver.evict(storage.storageCode());
            }
        }

        repository.logicDeleteByIds(normalized);

        BatchDeleteResultVo011 result = new BatchDeleteResultVo011();
        result.setTotal(normalized.size());
        result.setSuccess(normalized.size());
        result.setFailed(0);

        return result;
    }

    /**
     * Find a storage instance by primary key.
     *
     * @param id storage id
     * @return aggregate
     */
    public JulyStorageProvider getById(String operatorId, String id) {
        authorizationPort.assertHas(operatorId, JulyStorageProviderPermissionCodes011.SELECT);
        return require(id);
    }

    /**
     * Find a storage instance by code.
     *
     * @param operatorId  operator user id
     * @param storageCode storage code
     * @return aggregate
     */
    public JulyStorageProvider getByCode(String operatorId, String storageCode) {
        authorizationPort.assertHas(operatorId, JulyStorageProviderPermissionCodes011.SELECT);

        JulyStorageProvider storage = repository.findByCode(storageCode);

        if (storage == null) {
            throw BusinessException.recordNotFound(storageCode);
        }

        return storage;
    }

    /**
     * Page query on the management view.
     *
     * @param pageQuery page query, null falls back to page 1 / size 10
     * @param spec      query condition, null means no filter
     * @return page result
     */
    public PageResult011<JulyStorageProvider> selectListByPage(String operatorId, PageQuery011 pageQuery,
            JulyStorageProviderQuerySpec spec) {
        authorizationPort.assertHas(operatorId, JulyStorageProviderPermissionCodes011.SELECT);
        PageQuery011 query = pageQuery == null ? new PageQuery011(1, 10) : pageQuery;
        JulyStorageProviderQuerySpec condition = spec == null ? new JulyStorageProviderQuerySpec(null, null, null) : spec;
        List<JulyStorageProvider> rows = repository.findPage(query.offset(), query.pageSize(), condition);
        long total = repository.count(condition);

        return PageResult011.of(query, total, rows);
    }

    /**
     * The buckets of one storage instance (children, ordered) — the read-side
     * counterpart used by the master + children query.
     *
     * @param id storage id
     * @return bucket rows
     */
    public List<JulyStorageProviderBucket> buckets(String operatorId, String id) {
        authorizationPort.assertHas(operatorId, JulyStorageProviderPermissionCodes011.SELECT);
        require(id);

        return bucketRepository.findByPkMt(id);
    }

    /**
     * Probe a saved storage instance, using its default bucket.
     *
     * @param id storage id
     * @return probe result, never null
     */
    public StorageProbe testSaved(String operatorId, String id) {
        authorizationPort.assertHas(operatorId, JulyStorageProviderPermissionCodes011.TEST_CONNECTION);

        JulyStorageProvider storage = require(id);
        JulyStorageProviderBucket defaultBucket = bucketRepository.findDefault(storage.id().value());
        String defaultBucketName = defaultBucket == null ? null : defaultBucket.bucketName();

        return factory.create(storage.toConnectionConfig(defaultBucketName)).testConnection();
    }

    /**
     * Probe a draft config (before saving).
     *
     * @param provider             storage type code
     * @param basePath             local root
     * @param endpoint             endpoint
     * @param accessKey            access key
     * @param secretKey            secret key
     * @param secure               whether to use HTTPS
     * @param defaultBucket        default bucket
     * @param presignExpirySeconds presigned URL expiry seconds
     * @return probe result, never null
     */
    public StorageProbe testDraft(String operatorId, String provider, String basePath, String endpoint, String accessKey,
            String secretKey, boolean secure, String defaultBucket, Integer presignExpirySeconds) {
        authorizationPort.assertHas(operatorId, JulyStorageProviderPermissionCodes011.TEST_CONNECTION);

        StorageConnectionConfig config = new StorageConnectionConfig(provider, basePath, endpoint, accessKey,
                secretKey, secure, defaultBucket, presignExpirySeconds == null ? 3600 : presignExpirySeconds);

        try {
            return factory.create(config).testConnection();
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }
    }

    /**
     * Ensure a default bucket child row exists for the instance; the row is
     * created when missing, promoted to default when it already exists.
     *
     * @param masterId storage provider id
     * @param bucket   default bucket code / name, blank is a no-op
     */
    private void ensureDefaultBucket(String masterId, String bucket) {
        if (StringUtil011.isBlank(bucket)) {
            return;
        }

        JulyStorageProviderBucket existing = findBucketByCode(masterId, bucket);

        if (existing == null) {
            clearDefault(masterId, null);
            JulyStorageProviderBucket created = JulyStorageProviderBucket.create(EntityId.generate(), masterId, bucket,
                    bucket, true, null, null, AuditInfo.empty());
            bucketRepository.insert(created);
            return;
        }

        if (!existing.isDefault()) {
            clearDefault(masterId, existing.id().value());
            existing.update(existing.bucketName(), true, existing.sortOrder(), existing.status(), existing.remark());
            bucketRepository.update(existing);
        }
    }

    /**
     * Clear the default flag on every other bucket of the instance.
     *
     * @param masterId storage provider id
     * @param keepId   bucket id to keep untouched, nullable
     */
    private void clearDefault(String masterId, String keepId) {
        for (JulyStorageProviderBucket bucket : bucketRepository.findByPkMt(masterId)) {
            if (bucket.isDefault() && (keepId == null || !keepId.equals(bucket.id().value()))) {
                bucket.update(bucket.bucketName(), false, bucket.sortOrder(), bucket.status(), bucket.remark());
                bucketRepository.update(bucket);
            }
        }
    }

    /**
     * Find a bucket of an instance by its code.
     *
     * @param masterId   storage provider id
     * @param bucketCode bucket code
     * @return entity or null
     */
    private JulyStorageProviderBucket findBucketByCode(String masterId, String bucketCode) {
        for (JulyStorageProviderBucket bucket : bucketRepository.findByPkMt(masterId)) {
            if (bucketCode.equals(bucket.bucketCode())) {
                return bucket;
            }
        }

        return null;
    }

    /**
     * Build a new aggregate, translating domain validation failures into 400.
     *
     * @param storageCode          storage code
     * @param sortOrder            manual sort order
     * @param storageName          display name
     * @param provider             storage type code
     * @param basePath             local root
     * @param endpoint             endpoint
     * @param accessKey            access key
     * @param secretKey            secret key
     * @param secure               whether to use HTTPS
     * @param presignExpirySeconds presigned URL expiry seconds
     * @param remark               remark
     * @return new aggregate
     */
    private JulyStorageProvider newStorage(String storageCode, Integer sortOrder, String storageName, String provider,
            String basePath, String endpoint, String accessKey, String secretKey, boolean secure,
            Integer presignExpirySeconds, String remark) {
        try {
            return JulyStorageProvider.create(EntityId.generate(), storageCode, sortOrder, storageName, provider,
                    basePath, endpoint, accessKey, secretKey, secure,
                    presignExpirySeconds == null ? 3600 : presignExpirySeconds, remark, AuditInfo.empty());
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }
    }

    /**
     * Require an existing storage instance.
     *
     * @param id storage id
     * @return aggregate
     */
    private JulyStorageProvider require(String id) {
        JulyStorageProvider storage = repository.findById(id);

        if (storage == null) {
            throw BusinessException.recordNotFound(id);
        }

        return storage;
    }

    /**
     * Reject an unknown status when one is supplied.
     *
     * @param status raw status, nullable
     */
    private void requireStatus(String status) {
        if (status != null && !status.isBlank() && Status011.of(status) == null) {
            throw BusinessException.badRequest("unknown status: " + status);
        }
    }

    /**
     * Normalize a batch id list.
     *
     * @param ids raw ids
     * @return normalized ids, never null
     */
    private List<String> normalize(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }

        Set<String> seen = new LinkedHashSet<>();

        for (String id : ids) {
            if (id != null && !id.isBlank()) {
                seen.add(id.trim());
            }
        }

        return new ArrayList<>(seen);
    }
}
