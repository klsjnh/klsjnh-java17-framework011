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
 *
 */

import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.aicenter.capability.AiMedia;
import com.klsjnh.domain.aicenter.media.AiMediaRef;
import com.klsjnh.domain.aicenter.media.AiMediaStorePort;
import com.klsjnh.domain.storagecenter.object.ObjectStoragePort;
import com.klsjnh.domain.storagecenter.object.StorageResolverPort;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.UUID;

/**
 * Stores a generated artifact through the storage center (no storage code
 * here — the storage center is not modified). The AI center never manages the
 * artifact lifecycle; when to delete is the consumer's decision.
 */

@Component
public class AiMediaStore011 implements AiMediaStorePort {

    /**
     * Storage instance code, blank for the default instance.
     */
    private final String storageCode;

    /**
     * Bucket for generated media.
     */
    private final String bucket;

    /**
     * Key prefix for generated media.
     */
    private final String keyPrefix;

    /**
     * Storage resolver.
     */
    private final StorageResolverPort storageResolver;

    /**
     * Create the store.
     *
     * @param storageCode     storage instance code (blank = default)
     * @param bucket          target bucket
     * @param keyPrefix       key prefix
     * @param storageResolver storage resolver port
     */
    public AiMediaStore011(@Value("${krt.ai-center.media.storage-code:}") String storageCode,
            @Value("${krt.ai-center.media.bucket:ai-media}") String bucket,
            @Value("${krt.ai-center.media.key-prefix:ai/}") String keyPrefix, StorageResolverPort storageResolver) {
        this.storageCode = storageCode;
        this.bucket = bucket;
        this.keyPrefix = keyPrefix;
        this.storageResolver = storageResolver;
    }

    /** {@inheritDoc} */
    @Override
    public AiMediaRef save(AiMedia media) {
        if (media == null) {
            throw new IllegalStateException("ai media is required");
        }

        byte[] bytes = bytes(media);

        if (bytes == null || bytes.length == 0) {
            // Already a remote url: nothing to store, hand the reference back.
            return new AiMediaRef(null, media.url(), media.mimeType(), 0);
        }

        String key = keyPrefix + UUID.randomUUID().toString().replace("-", "") + extension(media.mimeType());
        ObjectStoragePort adapter = storageResolver.resolve(storageCode);
        adapter.put(bucket, key, bytes, media.mimeType());

        return new AiMediaRef(key, adapter.presignedGetUrl(bucket, key), media.mimeType(), bytes.length);
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
}
