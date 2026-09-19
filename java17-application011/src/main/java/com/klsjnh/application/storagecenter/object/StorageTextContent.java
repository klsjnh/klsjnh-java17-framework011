package com.klsjnh.application.storagecenter.object;

/*                StorageTextContent class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  storage text content class
 *
 */

/**
 * Editable text content of a storage object.
 *
 * @param storageCode storage code
 * @param bucketName  bucket name
 * @param objectName  object name
 * @param size        content size in bytes
 * @param content     text content
 * @param editorKind  editor kind (sql / markdown)
 * @param contentType content type
 */

public record StorageTextContent(String storageCode, String bucketName, String objectName, long size, String content,
        String editorKind, String contentType) {
}
