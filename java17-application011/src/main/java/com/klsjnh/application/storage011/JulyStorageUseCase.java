package com.klsjnh.application.storage011;

/*                JulyStorageUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july storage use case class
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.vo.BatchDeleteErrorVo011;
import com.klsjnh.common.vo.BatchDeleteResultVo011;

import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;
import com.klsjnh.domain.storage.JulyStorage;
import com.klsjnh.domain.storage.JulyStorageQuerySpec;
import com.klsjnh.domain.storage.JulyStorageRepository;
import com.klsjnh.domain.storage.ObjectStorageFactoryPort;
import com.klsjnh.domain.storage.ObjectStoragePort;
import com.klsjnh.domain.storage.StorageConnectionConfig;
import com.klsjnh.domain.storage.StorageResolverPort;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * JulyStorage use cases: storage instance CRUD plus the connectivity probe
 * (saved instance or draft config).
 */

@Service
public class JulyStorageUseCase {

    /**
     * Storage repository.
     */
    private final JulyStorageRepository repository;

    /**
     * Adapter factory (draft probe).
     */
    private final ObjectStorageFactoryPort factory;

    /**
     * Resolver (cache eviction on change).
     */
    private final StorageResolverPort resolver;

    /**
     * Create the use case.
     *
     * @param repository storage repository
     * @param factory    adapter factory
     * @param resolver   storage resolver
     */
    public JulyStorageUseCase(JulyStorageRepository repository, ObjectStorageFactoryPort factory,
            StorageResolverPort resolver) {
        this.repository = repository;
        this.factory = factory;
        this.resolver = resolver;
    }

    /**
     * Insert a new storage instance.
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
     * @param defaultBucket        default bucket
     * @param presignExpirySeconds presigned URL expiry seconds
     * @param remark               remark
     * @return new storage id
     */
    @Transactional
    public String insert(String storageCode, Integer sortOrder, String storageName, String provider, String basePath,
            String endpoint, String accessKey, String secretKey, boolean secure, String defaultBucket,
            Integer presignExpirySeconds, String remark) {
        if (repository.findByCode(storageCode) != null) {
            throw BusinessException.badRequest("storage code already exists: " + storageCode);
        }

        JulyStorage storage = newStorage(storageCode, sortOrder, storageName, provider, basePath, endpoint, accessKey,
                secretKey, secure, defaultBucket, presignExpirySeconds, remark);
        repository.insert(storage);

        return storage.id().value();
    }

    /**
     * Update a storage instance; a blank secretKey keeps the stored one.
     *
     * @param id                   storage id
     * @param storageName          display name
     * @param provider             storage type code
     * @param basePath             local root
     * @param endpoint             endpoint
     * @param accessKey            access key
     * @param secretKey            secret key, blank keeps the stored one
     * @param secure               whether to use HTTPS
     * @param defaultBucket        default bucket
     * @param presignExpirySeconds presigned URL expiry seconds
     * @param remark               remark
     * @param status               row status, null keeps the stored one
     * @return storage id
     */
    @Transactional
    public String update(String id, String storageName, String provider, String basePath, String endpoint,
            String accessKey, String secretKey, boolean secure, String defaultBucket, Integer presignExpirySeconds,
            String remark, String status) {
        JulyStorage storage = require(id);
        requireStatus(status);

        try {
            storage.update(storageName, provider, basePath, endpoint, accessKey, secretKey, secure, defaultBucket,
                    presignExpirySeconds, remark, status);
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }

        repository.update(storage);
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
    public String logicDelete(String id) {
        JulyStorage storage = require(id);

        if (!repository.logicDeleteById(id)) {
            throw BusinessException.recordNotFound(id);
        }

        resolver.evict(storage.storageCode());

        return id;
    }

    /**
     * Batch logic delete with a per-id success/failure summary.
     *
     * @param ids storage ids
     * @return per-id summary
     */
    @Transactional
    public BatchDeleteResultVo011 logicDeleteBatch(List<String> ids) {
        List<String> normalized = normalize(ids);

        if (normalized.isEmpty()) {
            throw BusinessException.badRequest("batch logic delete: ids is required");
        }

        int deleted = 0;
        List<String> missing = new ArrayList<>();

        for (String id : normalized) {
            JulyStorage storage = repository.findById(id);

            if (storage != null && repository.logicDeleteById(id)) {
                resolver.evict(storage.storageCode());
                deleted++;
            } else {
                missing.add(id);
            }
        }

        BatchDeleteResultVo011 result = new BatchDeleteResultVo011();
        result.setTotal(normalized.size());
        result.setSuccess(deleted);
        result.setFailed(missing.size());

        for (String id : missing) {
            BatchDeleteErrorVo011 error = new BatchDeleteErrorVo011();
            error.setId(id);
            error.setMessage("record not found");
            result.getErrors().add(error);
        }

        return result;
    }

    /**
     * Find a storage instance by primary key.
     *
     * @param id storage id
     * @return aggregate
     */
    public JulyStorage getById(String id) {
        return require(id);
    }

    /**
     * Find a storage instance by code.
     *
     * @param storageCode storage code
     * @return aggregate
     */
    public JulyStorage getByCode(String storageCode) {
        JulyStorage storage = repository.findByCode(storageCode);

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
    public PageResult011<JulyStorage> selectListByPage(PageQuery011 pageQuery, JulyStorageQuerySpec spec) {
        PageQuery011 query = pageQuery == null ? new PageQuery011(1, 10) : pageQuery;
        JulyStorageQuerySpec condition = spec == null ? new JulyStorageQuerySpec(null, null, null) : spec;
        List<JulyStorage> rows = repository.findPage(query.offset(), query.pageSize(), condition);
        long total = repository.count(condition);

        return PageResult011.of(query, total, rows);
    }

    /**
     * Probe a saved storage instance.
     *
     * @param id storage id
     * @return probe result, never null
     */
    public StorageProbeResult testSaved(String id) {
        JulyStorage storage = require(id);

        return probe(factory.create(storage.toConnectionConfig()));
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
    public StorageProbeResult testDraft(String provider, String basePath, String endpoint, String accessKey,
            String secretKey, boolean secure, String defaultBucket, Integer presignExpirySeconds) {
        StorageConnectionConfig config = new StorageConnectionConfig(provider, basePath, endpoint, accessKey,
                secretKey, secure, defaultBucket, presignExpirySeconds == null ? 3600 : presignExpirySeconds);

        try {
            return probe(factory.create(config));
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }
    }

    /**
     * Probe an adapter: a bucket list is the cheapest end-to-end call.
     *
     * @param adapter storage adapter
     * @return probe result
     */
    private StorageProbeResult probe(ObjectStoragePort adapter) {
        try {
            adapter.listBuckets();

            return StorageProbeResult.ok("connected");
        } catch (Exception ex) {
            return StorageProbeResult.fail(ex.getClass().getSimpleName() + ": " + ex.getMessage());
        }
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
     * @param defaultBucket        default bucket
     * @param presignExpirySeconds presigned URL expiry seconds
     * @param remark               remark
     * @return new aggregate
     */
    private JulyStorage newStorage(String storageCode, Integer sortOrder, String storageName, String provider,
            String basePath, String endpoint, String accessKey, String secretKey, boolean secure, String defaultBucket,
            Integer presignExpirySeconds, String remark) {
        try {
            return JulyStorage.create(EntityId.generate(), storageCode, sortOrder, storageName, provider, basePath,
                    endpoint, accessKey, secretKey, secure, defaultBucket,
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
    private JulyStorage require(String id) {
        JulyStorage storage = repository.findById(id);

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
