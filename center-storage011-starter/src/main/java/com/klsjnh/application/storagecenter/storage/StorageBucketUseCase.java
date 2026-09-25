package com.klsjnh.application.storagecenter.storage;

/*                StorageBucketUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  storage bucket use case class
 *      2026.09.26  explicit permission checks (julyStorageProviderBucket)
 *
 */

import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;
import com.klsjnh.domain.storagecenter.storage.JulyStorageProvider;
import com.klsjnh.domain.storagecenter.storage.JulyStorageProviderBucket;
import com.klsjnh.domain.storagecenter.storage.JulyStorageProviderBucketPermissionCodes011;
import com.klsjnh.domain.storagecenter.storage.JulyStorageProviderBucketRepository;
import com.klsjnh.domain.storagecenter.storage.JulyStorageProviderRepository;
import com.klsjnh.domain.iam.auth.AuthorizationPort;
import com.klsjnh.domain.storagecenter.object.ObjectStoragePort;
import com.klsjnh.domain.storagecenter.object.StorageProbe;
import com.klsjnh.domain.storagecenter.object.StorageResolverPort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Storage bucket use cases: list / get / create / remove / probe, backed by the
 * july_storage_provider_bucket child table and resolved per storage code.
 */

@Service
public class StorageBucketUseCase {

    /**
     * Default instance code.
     */
    private static final String DEFAULT_CODE = "default";

    /**
     * Storage provider repository.
     */
    private final JulyStorageProviderRepository providerRepository;

    /**
     * Storage bucket repository.
     */
    private final JulyStorageProviderBucketRepository bucketRepository;

    /**
     * Storage resolver.
     */
    private final StorageResolverPort resolver;

    private final AuthorizationPort authorizationPort;

    public StorageBucketUseCase(JulyStorageProviderRepository providerRepository,
            JulyStorageProviderBucketRepository bucketRepository, StorageResolverPort resolver,
            AuthorizationPort authorizationPort) {
        this.providerRepository = providerRepository;
        this.bucketRepository = bucketRepository;
        this.resolver = resolver;
        this.authorizationPort = authorizationPort;
    }

    /**
     * List the bucket rows of an instance.
     *
     * @param storageCode storage code, blank for the default instance
     * @return ordered bucket entities, never null
     */
    public List<JulyStorageProviderBucket> selectList(String operatorId, String storageCode) {
        authorizationPort.assertHas(operatorId, JulyStorageProviderBucketPermissionCodes011.SELECT);
        return bucketRepository.findByPkMt(requireMasterId(storageCode));
    }

    /**
     * Page the bucket rows of an instance, filtered by a name keyword.
     *
     * @param storageCode storage code
     * @param pageIndex   page index, 1 based
     * @param pageSize    page size
     * @param keyword     name keyword, nullable
     * @return page result of bucket entities
     */
    public PageResult011<JulyStorageProviderBucket> selectListByPage(String operatorId, String storageCode,
            Integer pageIndex, Integer pageSize, String keyword) {
        authorizationPort.assertHas(operatorId, JulyStorageProviderBucketPermissionCodes011.SELECT);

        PageQuery011 query = new PageQuery011(pageIndex, pageSize);
        String name = StringUtil011.blankToNull(keyword);
        List<JulyStorageProviderBucket> all = bucketRepository.findByPkMt(requireMasterId(storageCode));
        List<JulyStorageProviderBucket> filtered = name == null ? all : all.stream()
                .filter(bucket -> bucket.bucketName() != null && bucket.bucketName().contains(name))
                .toList();

        int from = (int) Math.min(query.offset(), filtered.size());
        int to = (int) Math.min((long) from + query.pageSize(), filtered.size());

        return PageResult011.of(query, filtered.size(), filtered.subList(from, to));
    }

    /**
     * Get a bucket row by code; a missing bucket is a 404.
     *
     * @param storageCode storage code
     * @param bucketCode  bucket code
     * @return bucket entity
     */
    public JulyStorageProviderBucket getBucket(String operatorId, String storageCode, String bucketCode) {
        authorizationPort.assertHas(operatorId, JulyStorageProviderBucketPermissionCodes011.SELECT);

        JulyStorageProviderBucket bucket = findBucketByCode(requireMasterId(storageCode), bucketCode);

        if (bucket == null) {
            throw BusinessException.recordNotFound(bucketCode);
        }

        return bucket;
    }

    /**
     * Create a bucket: ensure it on the backend, then persist the child row.
     *
     * @param storageCode storage code
     * @param bucketCode  bucket code, unique within the instance
     * @param bucketName  bucket name
     * @param isDefault   whether this becomes the default bucket
     * @param region      region, ignored by local / MinIO
     */
    @Transactional
    public void insert(String operatorId, String storageCode, String bucketCode, String bucketName, boolean isDefault,
            String region) {
        authorizationPort.assertHas(operatorId, JulyStorageProviderBucketPermissionCodes011.INSERT);

        String masterId = requireMasterId(storageCode);
        JulyStorageProviderBucket bucket = newBucket(masterId, bucketCode, bucketName, isDefault);

        if (bucketRepository.existsIncludingDeleted(masterId, bucketCode)) {
            throw BusinessException.badRequest("bucket code already exists in storage: " + bucketCode);
        }

        try {
            adapter(storageCode).createBucket(bucketName);
        } catch (IllegalStateException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }

        if (isDefault) {
            clearDefault(masterId, null);
        }

        bucketRepository.insert(bucket);
    }

    /**
     * Remove a bucket: delete it from the backend, then logic-delete the child
     * row. A missing bucket is a 404.
     *
     * @param storageCode storage code
     * @param bucketCode  bucket code
     */
    @Transactional
    public void remove(String operatorId, String storageCode, String bucketCode) {
        authorizationPort.assertHas(operatorId, JulyStorageProviderBucketPermissionCodes011.LOGIC_DELETE);

        JulyStorageProviderBucket bucket = findBucketByCode(requireMasterId(storageCode), bucketCode);

        if (bucket == null) {
            throw BusinessException.recordNotFound(bucketCode);
        }

        try {
            adapter(storageCode).deleteBucket(bucket.bucketName());
        } catch (IllegalStateException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }

        if (!bucketRepository.logicDeleteById(bucket.id().value())) {
            throw BusinessException.recordNotFound(bucketCode);
        }
    }

    /**
     * Probe the resolved instance.
     *
     * @param storageCode storage code
     * @return probe result, never null
     */
    public StorageProbe testConnection(String operatorId, String storageCode) {
        authorizationPort.assertHas(operatorId, JulyStorageProviderBucketPermissionCodes011.TEST_CONNECTION);
        return adapter(storageCode).testConnection();
    }

    /**
     * Build a new bucket entity, translating domain validation into 400.
     *
     * @param masterId   storage provider id
     * @param bucketCode bucket code
     * @param bucketName bucket name
     * @param isDefault  whether this is the default bucket
     * @return new entity
     */
    private JulyStorageProviderBucket newBucket(String masterId, String bucketCode, String bucketName,
            boolean isDefault) {
        try {
            return JulyStorageProviderBucket.create(EntityId.generate(), masterId, bucketCode, bucketName, isDefault,
                    null, null, AuditInfo.empty());
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }
    }

    /**
     * Resolve the master id of a storage code (blank for the default instance).
     *
     * @param storageCode storage code
     * @return storage provider id
     */
    private String requireMasterId(String storageCode) {
        String key = StringUtil011.isBlank(storageCode) ? DEFAULT_CODE : storageCode.trim();
        JulyStorageProvider provider = providerRepository.findEnabledByCode(key);

        if (provider == null) {
            throw BusinessException.recordNotFound(key);
        }

        return provider.id().value();
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
            if (bucketCode != null && bucketCode.equals(bucket.bucketCode())) {
                return bucket;
            }
        }

        return null;
    }

    /**
     * Clear the default flag on every bucket of the instance.
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
     * Resolve the adapter, translating resolution failures into 400.
     *
     * @param storageCode storage code
     * @return adapter
     */
    private ObjectStoragePort adapter(String storageCode) {
        try {
            return resolver.resolve(storageCode);
        } catch (IllegalStateException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }
    }
}
