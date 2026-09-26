package com.klsjnh.domain.storagecenter.storage;

/*                JulyStorageProviderSecretMaskTest class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  update keeps accessKey/secretKey on mask or blank
 *
 */

import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;
import com.klsjnh.domain.storagecenter.object.StorageProviderCodes011;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link JulyStorageProvider#update} secret-mask retention.
 */

class JulyStorageProviderSecretMaskTest {

    /**
     * Mask and blank credentials keep the stored accessKey / secretKey.
     */
    @Test
    void updateKeepsSecretsWhenMaskedOrBlank() {
        JulyStorageProvider storage = JulyStorageProvider.create(EntityId.of("s1"), "minio-a", 1, "MinIO A",
                StorageProviderCodes011.MINIO, null, "http://127.0.0.1:9000", "ak-real", "sk-real", false, 3600,
                null, AuditInfo.empty());

        storage.update("MinIO A2", StorageProviderCodes011.MINIO, null, "http://127.0.0.1:9001",
                JulyStorageProvider.SECRET_MASK, JulyStorageProvider.SECRET_MASK, true, 7200, "r", null);

        assertEquals("ak-real", storage.accessKey());
        assertEquals("sk-real", storage.secretKey());
        assertEquals("MinIO A2", storage.storageName());
        assertEquals("http://127.0.0.1:9001", storage.endpoint());

        storage.update("MinIO A3", StorageProviderCodes011.MINIO, null, "http://127.0.0.1:9002", "", "", false, null,
                null, null);

        assertEquals("ak-real", storage.accessKey());
        assertEquals("sk-real", storage.secretKey());
    }

    /**
     * A real new secret replaces the stored one; mask on the other field keeps it.
     */
    @Test
    void updateReplacesOnlyNonMaskedCredentials() {
        JulyStorageProvider storage = JulyStorageProvider.create(EntityId.of("s2"), "minio-b", 1, "MinIO B",
                StorageProviderCodes011.MINIO, null, "http://127.0.0.1:9000", "ak-old", "sk-old", false, 3600, null,
                AuditInfo.empty());

        storage.update("MinIO B", StorageProviderCodes011.MINIO, null, "http://127.0.0.1:9000", "ak-new",
                JulyStorageProvider.SECRET_MASK, false, null, null, null);

        assertEquals("ak-new", storage.accessKey());
        assertEquals("sk-old", storage.secretKey());
    }
}
