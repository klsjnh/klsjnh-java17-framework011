package com.klsjnh.domain.storagecenter.storage;

/*                JulyStorageProvider class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july storage provider class
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.storagecenter.object.StorageProviderCodes011;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;
import com.klsjnh.domain.storagecenter.object.ObjectStoragePort;
import com.klsjnh.domain.storagecenter.object.StorageConnectionConfig;

/**
 * JulyStorageProvider aggregate root (storage center context): one registered
 * object storage instance — a storage code resolved by the runtime to a
 * concrete {@link ObjectStoragePort} adapter. The default bucket lives in the
 * july_storage_provider_bucket child table.
 */

public class JulyStorageProvider {

    /**
     * Default sort order when none is given (matches BasePo011 / DDL default).
     */
    private static final int DEFAULT_SORT_ORDER = 9999;

    /**
     * Default presigned URL expiry, seconds.
     */
    private static final int DEFAULT_PRESIGN_SECONDS = 3600;

    /**
     * Mask echoed by the API for accessKey / secretKey; update must treat it
     * (and blank) as "keep the stored value".
     */
    public static final String SECRET_MASK = "******";

    /**
     * Primary key.
     */
    private final EntityId id;

    /**
     * Storage code, unique and immutable.
     */
    private final String storageCode;

    /**
     * Manual sort order, smaller comes first.
     */
    private Integer sortOrder;

    /**
     * Storage instance display name.
     */
    private String storageName;

    /**
     * provider code (open string, e.g. local011 / minio011 / oss011).
     */
    private String provider;

    /**
     * Local root directory (local011).
     */
    private String basePath;

    /**
     * Endpoint (S3 family).
     */
    private String endpoint;

    /**
     * Access key (S3 family); plaintext max 100 at domain boundary;
     * persistence may store ciphertext ({@code enc:v1:}, DB column VARCHAR(512)).
     */
    private String accessKey;

    /**
     * Secret key (S3 family); never echoed back by the web layer.
     * Plaintext max 300 at domain boundary; persistence may store ciphertext
     * ({@code enc:v1:}, DB column VARCHAR(512)).
     */
    private String secretKey;

    /**
     * Whether to use HTTPS (S3 family).
     */
    private boolean secure;

    /**
     * Presigned URL expiry, seconds.
     */
    private int presignExpirySeconds;

    /**
     * Remark, optional.
     */
    private String remark;

    /**
     * Row status: '1' enabled / '0' disabled.
     */
    private String status;

    /**
     * Audit info.
     */
    private AuditInfo audit;

    /**
     * Full constructor (also the rehydration path from persistence).
     *
     * @param id                   primary key
     * @param storageCode          storage code, unique
     * @param sortOrder            manual sort order, null falls back to the default
     * @param storageName          storage instance display name
     * @param provider             storage type code
     * @param basePath             local root, optional
     * @param endpoint             endpoint, optional
     * @param accessKey            access key, optional
     * @param secretKey            secret key, optional
     * @param secure               whether to use HTTPS
     * @param presignExpirySeconds presigned URL expiry seconds
     * @param remark               remark, optional
     * @param status               row status
     * @param audit                audit info
     */
    public JulyStorageProvider(EntityId id, String storageCode, Integer sortOrder, String storageName, String provider,
            String basePath, String endpoint, String accessKey, String secretKey, boolean secure,
            int presignExpirySeconds, String remark, String status, AuditInfo audit) {
        validate(storageCode, storageName, provider, basePath, endpoint, accessKey, secretKey, remark);

        this.id = id;
        this.storageCode = storageCode;
        this.sortOrder = sortOrder == null ? DEFAULT_SORT_ORDER : sortOrder;
        this.storageName = storageName;
        this.provider = provider;
        this.basePath = basePath;
        this.endpoint = endpoint;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.secure = secure;
        this.presignExpirySeconds = presignExpirySeconds > 0 ? presignExpirySeconds : DEFAULT_PRESIGN_SECONDS;
        this.remark = remark;
        this.status = status == null ? Status011.ENABLED.getCode() : status;
        this.audit = audit == null ? AuditInfo.empty() : audit;
    }

    /**
     * Factory for a new storage instance.
     *
     * @param id                   primary key
     * @param storageCode          storage code, unique, max 60
     * @param sortOrder            manual sort order, null falls back to the default
     * @param storageName          display name, max 100
     * @param provider             storage type code
     * @param basePath             local root, optional, max 500
     * @param endpoint             endpoint, optional, max 300
     * @param accessKey            access key, optional, max 100
     * @param secretKey            secret key, optional, max 300
     * @param secure               whether to use HTTPS
     * @param presignExpirySeconds presigned URL expiry seconds
     * @param remark               remark, optional, max 300
     * @param audit                audit info
     * @return new aggregate
     */
    public static JulyStorageProvider create(EntityId id, String storageCode, Integer sortOrder, String storageName,
            String provider, String basePath, String endpoint, String accessKey, String secretKey, boolean secure,
            int presignExpirySeconds, String remark, AuditInfo audit) {
        return new JulyStorageProvider(id, storageCode, sortOrder, storageName, provider, basePath, endpoint, accessKey,
                secretKey, secure, presignExpirySeconds, remark, Status011.ENABLED.getCode(), audit);
    }

    /**
     * Update the mutable fields (storageCode is immutable). Blank or
     * {@link #SECRET_MASK} accessKey / secretKey keep the stored values
     * (same contract as message-channel {@code mergeKeepingSecrets}).
     *
     * @param storageName          display name
     * @param provider             storage type code
     * @param basePath             local root
     * @param endpoint             endpoint
     * @param accessKey            access key; blank / mask keeps the stored one
     * @param secretKey            secret key; blank / mask keeps the stored one
     * @param secure               whether to use HTTPS
     * @param presignExpirySeconds presigned URL expiry seconds
     * @param remark               remark
     * @param status               row status, null keeps the stored one
     */
    public void update(String storageName, String provider, String basePath, String endpoint, String accessKey,
            String secretKey, boolean secure, Integer presignExpirySeconds, String remark, String status) {
        String keepAccess = isMaskedOrBlank(accessKey) ? this.accessKey : accessKey;
        String keepSecret = isMaskedOrBlank(secretKey) ? this.secretKey : secretKey;
        validate(this.storageCode, storageName, provider, basePath, endpoint, keepAccess, keepSecret, remark);

        this.storageName = storageName;
        this.provider = provider;
        this.basePath = basePath;
        this.endpoint = endpoint;
        this.accessKey = keepAccess;
        this.secretKey = keepSecret;
        this.secure = secure;
        this.remark = remark;

        if (presignExpirySeconds != null && presignExpirySeconds > 0) {
            this.presignExpirySeconds = presignExpirySeconds;
        }

        if (!StringUtil011.isBlank(status)) {
            this.status = status;
        }
    }

    /**
     * Whether a credential field from the client means "keep stored value".
     *
     * @param value access or secret key from the request
     * @return true when blank or the API mask
     */
    private static boolean isMaskedOrBlank(String value) {
        return StringUtil011.isBlank(value) || SECRET_MASK.equals(value.trim());
    }

    /**
     * Build the connection config for the adapter factory.
     *
     * @param defaultBucket default bucket resolved from the child table, nullable
     * @return connection config
     */
    public StorageConnectionConfig toConnectionConfig(String defaultBucket) {
        return new StorageConnectionConfig(provider, basePath, endpoint, accessKey, secretKey, secure, defaultBucket,
                presignExpirySeconds);
    }

    /**
     * Validate the shared create / update basics plus provider-specific
     * requirements.
     *
     * @param storageCode          storage code
     * @param storageName          display name
     * @param provider             storage type code
     * @param basePath             local root
     * @param endpoint             endpoint
     * @param accessKey            access key
     * @param secretKey            secret key
     * @param remark               remark
     */
    private static void validate(String storageCode, String storageName, String provider, String basePath,
            String endpoint, String accessKey, String secretKey, String remark) {
        StringUtil011.requirePresent(storageCode, "storage code", 60);

        StringUtil011.requirePresent(storageName, "storage name", 100);

        if (StringUtil011.isBlank(provider)) {
            throw new IllegalArgumentException("storage provider is required");
        }

        if (StorageProviderCodes011.LOCAL.equals(provider) && StringUtil011.isBlank(basePath)) {
            throw new IllegalArgumentException("base path is required for local011");
        }

        if (!StorageProviderCodes011.LOCAL.equals(provider)
                && (StringUtil011.isBlank(endpoint) || StringUtil011.isBlank(accessKey)
                        || StringUtil011.isBlank(secretKey))) {
            throw new IllegalArgumentException("endpoint, access key and secret key are required for " + provider);
        }

        if (StringUtil011.isOver(basePath, 500) || StringUtil011.isOver(endpoint, 300)
                || StringUtil011.isOver(accessKey, 100) || StringUtil011.isOver(secretKey, 300)
                || StringUtil011.isOver(remark, 300)) {
            throw new IllegalArgumentException("a storage field exceeds its maximum length");
        }
    }

    /**
     * Get the primary key.
     *
     * @return id value object
     */
    public EntityId id() {
        return id;
    }

    /**
     * Get the storage code.
     *
     * @return storage code
     */
    public String storageCode() {
        return storageCode;
    }

    /**
     * Get the manual sort order.
     *
     * @return sort order
     */
    public Integer sortOrder() {
        return sortOrder;
    }

    /**
     * Get the display name.
     *
     * @return storage name
     */
    public String storageName() {
        return storageName;
    }

    /**
     * Get the storage type code.
     *
     * @return provider
     */
    public String provider() {
        return provider;
    }

    /**
     * Get the local root directory.
     *
     * @return base path or null
     */
    public String basePath() {
        return basePath;
    }

    /**
     * Get the endpoint.
     *
     * @return endpoint or null
     */
    public String endpoint() {
        return endpoint;
    }

    /**
     * Get the access key.
     *
     * @return access key or null
     */
    public String accessKey() {
        return accessKey;
    }

    /**
     * Get the secret key.
     *
     * @return secret key or null
     */
    public String secretKey() {
        return secretKey;
    }

    /**
     * Whether to use HTTPS.
     *
     * @return true when secure
     */
    public boolean secure() {
        return secure;
    }

    /**
     * Get the presigned URL expiry.
     *
     * @return expiry seconds
     */
    public int presignExpirySeconds() {
        return presignExpirySeconds;
    }

    /**
     * Get the remark.
     *
     * @return remark or null
     */
    public String remark() {
        return remark;
    }

    /**
     * Get the row status.
     *
     * @return '1' enabled / '0' disabled
     */
    public String status() {
        return status;
    }

    /**
     * Get the audit info.
     *
     * @return audit info
     */
    public AuditInfo audit() {
        return audit;
    }
}
