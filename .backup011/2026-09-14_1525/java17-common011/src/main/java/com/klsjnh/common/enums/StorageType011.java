package com.klsjnh.common.enums;

/*                StorageType011 enum
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  storage type 011 class
 *
 */

/**
 * Storage center adapter type, bound to {@code krt.storage-center.default-type}
 * (java8 011-suffix value convention).
 * <p>
 * One active adapter per runtime; adapters land per need — the enum declares
 * the full vocabulary up front (local011 / minio011 first, cloud vendors
 * phased).
 * </p>
 */

public enum StorageType011 {

    /** Local disk adapter (java.nio.file). */
    LOCAL011("local011"),

    /** Self-hosted MinIO adapter (MinIO SDK). */
    MINIO011("minio011"),

    /** Tencent Cloud COS adapter (native SDK, phased). */
    COS011("cos011"),

    /** Volcano Engine TOS adapter (native SDK, phased). */
    TOS011("tos011"),

    /** Alibaba OSS adapter (native SDK, phased). */
    OSS011("oss011"),

    /** AWS S3 / other S3-compatible adapter (phased). */
    S3011("s3011");

    /**
     * Config code stored in the yaml value.
     */
    private final String code;

    /**
     * Create a storage type constant.
     *
     * @param code config code
     */
    StorageType011(String code) {
        this.code = code;
    }

    /**
     * Get the config code.
     *
     * @return config code (local011 / minio011 / ...)
     */
    public String getCode() {
        return code;
    }

    /**
     * Resolve from a raw config value; null when blank or unknown.
     *
     * @param value raw config value
     * @return resolved constant, null when blank or unknown
     */
    public static StorageType011 of(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        for (StorageType011 type : values()) {
            if (type.code.equals(value)) {
                return type;
            }
        }

        return null;
    }
}
