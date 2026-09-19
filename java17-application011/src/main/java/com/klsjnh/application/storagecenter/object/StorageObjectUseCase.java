package com.klsjnh.application.storagecenter.object;

/*                StorageObjectUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  storage object use case class
 *
 */

import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;

import com.klsjnh.domain.storagecenter.storage.EditableTextPolicy;
import com.klsjnh.domain.storagecenter.object.ObjectStat;
import com.klsjnh.domain.storagecenter.object.ObjectStoragePort;
import com.klsjnh.domain.storagecenter.object.StorageResolverPort;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Storage object use cases: list / stat / upload / download / remove, plus the
 * online text editing read/write and the presigned URL.
 */

@Service
public class StorageObjectUseCase {

    /**
     * Storage resolver.
     */
    private final StorageResolverPort resolver;

    /**
     * Create the use case.
     *
     * @param resolver storage resolver
     */
    public StorageObjectUseCase(StorageResolverPort resolver) {
        this.resolver = resolver;
    }

    /**
     * List object keys under a prefix.
     *
     * @param storageCode storage code
     * @param bucketName  bucket name
     * @param prefix      key prefix, nullable
     * @return object keys, never null
     */
    public List<String> selectList(String storageCode, String bucketName, String prefix) {
        return adapter(storageCode).list(bucketName, prefix);
    }

    /**
     * Page objects (key + metadata) under a prefix — the file-list page shows
     * size / last-modified without one stat call per row.
     *
     * @param storageCode storage code
     * @param bucketName  bucket name
     * @param prefix      key prefix, nullable
     * @param pageIndex   page index, 1 based
     * @param pageSize    page size
     * @return page result of object metadata
     */
    public PageResult011<ObjectStat> selectListByPage(String storageCode, String bucketName, String prefix,
            Integer pageIndex, Integer pageSize) {
        PageQuery011 query = new PageQuery011(pageIndex, pageSize);
        List<ObjectStat> all = adapter(storageCode).listStat(bucketName, prefix);

        int from = (int) Math.min(query.offset(), all.size());
        int to = (int) Math.min((long) from + query.pageSize(), all.size());

        return PageResult011.of(query, all.size(), all.subList(from, to));
    }

    /**
     * Object metadata.
     *
     * @param storageCode storage code
     * @param bucketName  bucket name
     * @param objectName  object name
     * @return stat, never null
     */
    public ObjectStat stat(String storageCode, String bucketName, String objectName) {
        ObjectStat stat = adapter(storageCode).stat(bucketName, objectName);

        if (stat == null) {
            throw BusinessException.recordNotFound(objectName);
        }

        return stat;
    }

    /**
     * Store an object.
     *
     * @param storageCode storage code
     * @param bucketName  bucket name
     * @param objectName  object name, blank falls back to a generated key
     * @param content     object bytes
     * @param contentType mime type, nullable
     * @return the final stored key
     */
    public String upload(String storageCode, String bucketName, String objectName, byte[] content, String contentType) {
        if (content == null) {
            throw BusinessException.badRequest("file content is required");
        }

        String key = objectName == null || objectName.isBlank() ? "upload-" + System.currentTimeMillis() : objectName;

        return adapter(storageCode).put(bucketName, key, content, contentType);
    }

    /**
     * Read an object.
     *
     * @param storageCode storage code
     * @param bucketName  bucket name
     * @param objectName  object name
     * @return object bytes, never null
     */
    public byte[] download(String storageCode, String bucketName, String objectName) {
        byte[] content = adapter(storageCode).get(bucketName, objectName);

        if (content == null) {
            throw BusinessException.recordNotFound(objectName);
        }

        return content;
    }

    /**
     * Delete an object.
     *
     * @param storageCode storage code
     * @param bucketName  bucket name
     * @param objectName  object name
     */
    public void remove(String storageCode, String bucketName, String objectName) {
        adapter(storageCode).delete(bucketName, objectName);
    }

    /**
     * Delete objects in batch.
     *
     * @param storageCode storage code
     * @param bucketName  bucket name
     * @param objectNames object names
     */
    public void batchRemove(String storageCode, String bucketName, List<String> objectNames) {
        ObjectStoragePort adapter = adapter(storageCode);

        for (String name : objectNames == null ? List.<String>of() : objectNames) {
            adapter.delete(bucketName, name);
        }
    }

    /**
     * Presigned GET URL of an object.
     *
     * @param storageCode storage code
     * @param bucketName  bucket name
     * @param objectName  object name
     * @return presigned URL / URI
     */
    public String presignedUrl(String storageCode, String bucketName, String objectName) {
        return adapter(storageCode).presignedGetUrl(bucketName, objectName);
    }

    /**
     * Read an object as editable text (≤1MB).
     *
     * @param storageCode storage code
     * @param bucketName  bucket name
     * @param objectName  object name
     * @return text content
     */
    public StorageTextContent readText(String storageCode, String bucketName, String objectName) {
        ObjectStoragePort adapter = adapter(storageCode);
        ObjectStat stat = adapter.stat(bucketName, objectName);

        if (stat == null) {
            throw BusinessException.recordNotFound(objectName);
        }

        try {
            EditableTextPolicy.assertEditable(stat.size());
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }

        byte[] bytes = adapter.get(bucketName, objectName);

        if (bytes == null) {
            throw BusinessException.recordNotFound(objectName);
        }

        return new StorageTextContent(storageCode, bucketName, objectName, bytes.length,
                new String(bytes, StandardCharsets.UTF_8), EditableTextPolicy.resolveEditorKind(objectName),
                EditableTextPolicy.resolveContentType(objectName));
    }

    /**
     * Write an object as editable text (≤1MB).
     *
     * @param storageCode storage code
     * @param bucketName  bucket name
     * @param objectName  object name
     * @param content     text content
     * @return the stored key
     */
    public String saveText(String storageCode, String bucketName, String objectName, String content) {
        if (objectName == null || objectName.isBlank()) {
            throw BusinessException.badRequest("objectName is required");
        }

        if (content == null) {
            throw BusinessException.badRequest("content is required");
        }

        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);

        try {
            EditableTextPolicy.assertContentSize(bytes.length);
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }

        return adapter(storageCode).put(bucketName, objectName, bytes,
                EditableTextPolicy.resolveContentType(objectName));
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
