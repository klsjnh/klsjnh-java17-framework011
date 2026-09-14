package com.klsjnh.infrastructure.storage;

/*                StorageProperties class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  storage properties class
 *
 */

import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Storage center properties bound to {@code krt.storage-center} (values use
 * the 011-suffix convention: local011 / minio011, cloud vendors reserved).
 */

@Data
@Component
@ConfigurationProperties(prefix = "krt.storage-center")
public class StorageProperties {

    /**
     * Active adapter type: local011 / minio011 (cos011 / tos011 / oss011 /
     * s3011 reserved).
     */
    private String defaultType = "local011";

    /**
     * Local011 adapter settings.
     */
    private Local011 local011 = new Local011();

    /**
     * Minio011 adapter settings.
     */
    private Minio011 minio011 = new Minio011();

    /**
     * Local011 settings.
     */
    @Data
    public static class Local011 {

        /**
         * Base directory for objects.
         */
        private String basePath = "./storage011";
    }

    /**
     * Minio011 settings.
     */
    @Data
    public static class Minio011 {

        /**
         * MinIO endpoint, e.g. http://192.168.1.88:9000.
         */
        private String endpoint;

        /**
         * Access key.
         */
        private String accessKey;

        /**
         * Secret key.
         */
        private String secretKey;

        /**
         * Default bucket, auto-created when missing.
         */
        private String defaultBucket = "klsjnh011";
    }
}
