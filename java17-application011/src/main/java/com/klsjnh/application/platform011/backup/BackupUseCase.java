package com.klsjnh.application.platform011.backup;

/*                BackupUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  backup use case class
 *      2026.09.15  moved into the platform011.backup package
 *      2026.09.15  object key stamp from date util 011
 *
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.klsjnh.common.enums.AuditType011;
import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.identity.Operator011;
import com.klsjnh.common.util.DateUtil011;

import com.klsjnh.domain.iam.UserAuditPort;
import com.klsjnh.domain.platform011.export.ExportResult;
import com.klsjnh.domain.storagecenter.ObjectStoragePort;
import com.klsjnh.domain.storagecenter.StorageDefaultsPort;
import com.klsjnh.domain.storagecenter.StorageResolverPort;
import com.klsjnh.application.platform011.export.ExportUseCase;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Object backup use case: reuses the export collection verbatim and redirects
 * the outcome into the storage center instead of an HTTP response.
 * <p>
 * This is the whole reason the export result carries no transport concern —
 * export and backup differ only in where the finished payload goes, never in
 * how the rows are collected.
 * </p>
 * <p>
 * The target bucket is the active adapter's configured default
 * ({@code krt.storage-center.local011.default-bucket} /
 * {@code minio011.default-bucket}), falling back to the resolved instance's
 * own {@code july_storage.default_bucket}; no bucket name is hard-coded here.
 * </p>
 */

@Service
public class BackupUseCase {

    /**
     * Logger.
     */
    private static final Logger logger = LoggerFactory.getLogger(BackupUseCase.class);

    /**
     * Export use case, the collection engine shared with the download path.
     */
    private final ExportUseCase exportUseCase;

    /**
     * Storage resolver (table-driven adapter lookup).
     */
    private final StorageResolverPort storageResolver;

    /**
     * Configuration-declared storage defaults (active adapter default bucket).
     */
    private final StorageDefaultsPort storageDefaults;

    /**
     * User audit port.
     */
    private final UserAuditPort userAuditPort;

    /**
     * JSON mapper, shared and thread safe.
     */
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    /**
     * Create the use case.
     *
     * @param exportUseCase   export use case
     * @param storageResolver storage resolver
     * @param storageDefaults configuration-declared storage defaults
     * @param userAuditPort   user audit port
     */
    public BackupUseCase(ExportUseCase exportUseCase, StorageResolverPort storageResolver,
            StorageDefaultsPort storageDefaults, UserAuditPort userAuditPort) {
        this.exportUseCase = exportUseCase;
        this.storageResolver = storageResolver;
        this.storageDefaults = storageDefaults;
        this.userAuditPort = userAuditPort;
    }

    /**
     * Back one object up into the storage center.
     *
     * @param objectCode      object code (registered provider)
     * @param operatorId      current operator id
     * @param operatorAccount current operator account
     * @param ip              client IP
     * @return the stored object key
     */
    public String backup(String objectCode, String operatorId, String operatorAccount, String ip) {
        return backup(objectCode, new Operator011(operatorId, operatorAccount, ip));
    }

    /**
     * Back one object up into the storage center, with the operator carried
     * as one value.
     *
     * @param objectCode object code (registered provider)
     * @param operator   current operator (id + account + ip)
     * @return the stored object key
     */
    public String backup(String objectCode, Operator011 operator) {
        String funcName = "backup";

        if (operator == null || !operator.authenticated()) {
            throw BusinessException.unauthorized(funcName + ": not authenticated");
        }

        ExportResult result = exportUseCase.export(objectCode, operator);
        ObjectStoragePort storagePort = storageResolver.resolve(null);
        String bucket = resolveBucket(storagePort);
        String key = objectKey(objectCode);

        storagePort.put(bucket, key, toJsonBytes(result), "application/json");

        userAuditPort.record(operator.id(), operator.userAccount(), AuditType011.BACKUP, objectCode,
                "backup " + result.metaInfo().rowCount() + " rows to " + bucket + "/" + key, operator.ip());

        logger.info("{} {} stored {} rows at {}/{}", funcName, objectCode, result.metaInfo().rowCount(), bucket, key);

        return key;
    }

    /**
     * Build the object key: {@code objectCode/yyyyMMdd_HHmmss.json}, the stamp
     * coming from {@link DateUtil011}.
     *
     * @param objectCode object code
     * @return object key
     */
    private String objectKey(String objectCode) {
        return objectCode + "/" + DateUtil011.nowStamp() + ".json";
    }

    /**
     * Resolve the backup target bucket: the active adapter's configured default
     * first, then the resolved instance's own default bucket.
     *
     * @param storagePort resolved storage adapter
     * @return bucket name
     */
    private String resolveBucket(ObjectStoragePort storagePort) {
        String bucket = storageDefaults.defaultBucket();

        return bucket == null || bucket.isBlank() ? storagePort.defaultBucket() : bucket;
    }

    /**
     * Serialize the result as JSON bytes.
     *
     * @param result export result
     * @return json bytes
     */
    private byte[] toJsonBytes(ExportResult result) {
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(result);
        } catch (JsonProcessingException ex) {
            throw BusinessException.badRequest("backup serialize failed: " + ex.getMessage());
        }
    }
}
