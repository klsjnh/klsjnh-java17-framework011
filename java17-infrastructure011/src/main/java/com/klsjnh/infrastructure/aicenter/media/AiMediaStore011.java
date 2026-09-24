package com.klsjnh.infrastructure.aicenter.media;

/*                AiMediaStore011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  ai media persistence via storage center
 *      2026.09.24  call-time locator only; no default storage / bucket
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.aicenter.capability.AiMedia;
import com.klsjnh.domain.aicenter.media.AiMediaLocation;
import com.klsjnh.domain.aicenter.media.AiMediaRef;
import com.klsjnh.domain.aicenter.media.AiMediaStorePort;
import com.klsjnh.domain.storagecenter.object.ObjectStoragePort;
import com.klsjnh.domain.storagecenter.object.StorageResolverPort;
import com.klsjnh.domain.storagecenter.storage.JulyStorageProvider;
import com.klsjnh.domain.storagecenter.storage.JulyStorageProviderBucket;
import com.klsjnh.domain.storagecenter.storage.JulyStorageProviderBucketRepository;
import com.klsjnh.domain.storagecenter.storage.JulyStorageProviderRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.UUID;

/**
 * Thin AI media store: resolves the call-time locator to a storage instance +
 * bucket, then writes through {@link StorageResolverPort} →
 * {@link ObjectStoragePort}. No pre-bound default storage or default bucket;
 * missing locator fields are a parameter error. Does not use storage-center
 * management use cases.
 */

@Component
public class AiMediaStore011 implements AiMediaStorePort {

    /**
     * Key prefix for generated media (not a storage binding).
     */
    private final String keyPrefix;

    /**
     * Storage resolver port.
     */
    private final StorageResolverPort storageResolver;

    /**
     * Storage instance repository (read-only resolve by id / code).
     */
    private final JulyStorageProviderRepository storageRepository;

    /**
     * Bucket repository (read-only resolve by id / code).
     */
    private final JulyStorageProviderBucketRepository bucketRepository;

    /**
     * Create the store.
     *
     * @param keyPrefix         key prefix
     * @param storageResolver   storage resolver port
     * @param storageRepository storage instance repository
     * @param bucketRepository  bucket repository
     */
    public AiMediaStore011(@Value("${krt.ai-center.media.key-prefix:ai/}") String keyPrefix,
            StorageResolverPort storageResolver, JulyStorageProviderRepository storageRepository,
            JulyStorageProviderBucketRepository bucketRepository) {
        this.keyPrefix = keyPrefix;
        this.storageResolver = storageResolver;
        this.storageRepository = storageRepository;
        this.bucketRepository = bucketRepository;
    }

    /** {@inheritDoc} */
    @Override
    public AiMediaRef save(AiMedia media, AiMediaLocation location) {
        if (media == null) {
            throw BusinessException.badRequest("ai media is required");
        }

        ResolvedTarget target = resolve(location);
        byte[] bytes = bytes(media);

        if (bytes == null || bytes.length == 0) {
            // Already a remote url: nothing to store, hand the reference back.
            return new AiMediaRef(target.storageCode(), target.bucketName(), null, media.url(), media.mimeType(), 0);
        }

        String key = keyPrefix + UUID.randomUUID().toString().replace("-", "") + extension(media.mimeType());
        ObjectStoragePort adapter = storageResolver.resolve(target.storageCode());
        adapter.put(target.bucketName(), key, bytes, media.mimeType());

        return new AiMediaRef(target.storageCode(), target.bucketName(), key,
                adapter.presignedGetUrl(target.bucketName(), key), media.mimeType(), bytes.length);
    }

    /**
     * Resolve and validate the call-time locator to a storage code + physical
     * bucket name. Prefer code over id for both sides.
     *
     * @param location call-time locator
     * @return resolved target
     */
    private ResolvedTarget resolve(AiMediaLocation location) {
        if (location == null) {
            throw BusinessException.badRequest("storage location is required");
        }

        if (StringUtil011.isBlank(location.storageCode()) && StringUtil011.isBlank(location.storageId())) {
            throw BusinessException.badRequest("storageCode or storageId is required");
        }

        if (StringUtil011.isBlank(location.bucketCode()) && StringUtil011.isBlank(location.bucketId())) {
            throw BusinessException.badRequest("bucketCode or bucketId is required");
        }

        JulyStorageProvider storage = resolveStorage(location);
        JulyStorageProviderBucket bucket = resolveBucket(storage, location);

        return new ResolvedTarget(storage.storageCode(), bucket.bucketName());
    }

    /**
     * Resolve the storage instance: code preferred, else id. Must be enabled.
     *
     * @param location call-time locator
     * @return enabled storage instance
     */
    private JulyStorageProvider resolveStorage(AiMediaLocation location) {
        if (!StringUtil011.isBlank(location.storageCode())) {
            JulyStorageProvider byCode = storageRepository.findEnabledByCode(location.storageCode().trim());
            if (byCode == null) {
                throw BusinessException.badRequest("storage is not available: " + location.storageCode().trim());
            }
            return byCode;
        }

        JulyStorageProvider byId = storageRepository.findById(location.storageId().trim());

        if (byId == null || !Status011.ENABLED.getCode().equals(byId.status())) {
            throw BusinessException.badRequest("storage is not available: " + location.storageId().trim());
        }

        return byId;
    }

    /**
     * Resolve the bucket within the storage instance: code preferred, else id.
     * Must be enabled and belong to the instance. Never falls back to the
     * instance default bucket.
     *
     * @param storage  resolved storage instance
     * @param location call-time locator
     * @return enabled bucket
     */
    private JulyStorageProviderBucket resolveBucket(JulyStorageProvider storage, AiMediaLocation location) {
        String masterId = storage.id().value();

        if (!StringUtil011.isBlank(location.bucketCode())) {
            String code = location.bucketCode().trim();
            for (JulyStorageProviderBucket row : bucketRepository.findByPkMt(masterId)) {
                if (code.equals(row.bucketCode()) && Status011.ENABLED.getCode().equals(row.status())) {
                    return row;
                }
            }
            throw BusinessException.badRequest("bucket is not available: " + code);
        }

        JulyStorageProviderBucket byId = bucketRepository.findById(location.bucketId().trim());

        if (byId == null || !masterId.equals(byId.pkMt()) || !Status011.ENABLED.getCode().equals(byId.status())) {
            throw BusinessException.badRequest("bucket is not available: " + location.bucketId().trim());
        }

        return byId;
    }

    /**
     * Raw bytes of an artifact (direct bytes or decoded base64).
     *
     * @param media artifact
     * @return bytes, null when only a url is present
     */
    private byte[] bytes(AiMedia media) {
        if (media.bytes() != null && media.bytes().length > 0) {
            return media.bytes();
        }

        if (!StringUtil011.isBlank(media.base64())) {
            return Base64.getDecoder().decode(media.base64());
        }

        return null;
    }

    /**
     * File extension for a mime type.
     *
     * @param mimeType mime type
     * @return extension including the dot
     */
    private String extension(String mimeType) {
        if (mimeType == null) {
            return ".bin";
        }

        if (mimeType.contains("png")) {
            return ".png";
        }

        if (mimeType.contains("jpeg") || mimeType.contains("jpg")) {
            return ".jpg";
        }

        if (mimeType.contains("wav")) {
            return ".wav";
        }

        if (mimeType.contains("mpeg") || mimeType.contains("mp3")) {
            return ".mp3";
        }

        return ".bin";
    }

    /**
     * Resolved write target.
     *
     * @param storageCode storage instance code
     * @param bucketName  physical bucket name
     */
    private record ResolvedTarget(String storageCode, String bucketName) {
    }
}
