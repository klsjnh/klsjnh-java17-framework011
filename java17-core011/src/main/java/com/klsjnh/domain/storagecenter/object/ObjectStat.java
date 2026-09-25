package com.klsjnh.domain.storagecenter.object;

/*                ObjectStat record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  object stat record
 *
 */

import java.time.LocalDateTime;

/**
 * Object metadata read model (stat result): the file-list page needs size /
 * last-modified / content-type without downloading the object.
 *
 * @param bucket       bucket name
 * @param key          object key
 * @param size         object size in bytes
 * @param lastModified last modification time
 * @param contentType  mime type, adapter-dependent (local infers from the
 *                     file extension)
 */

public record ObjectStat(String bucket, String key, long size, LocalDateTime lastModified, String contentType) {
}
