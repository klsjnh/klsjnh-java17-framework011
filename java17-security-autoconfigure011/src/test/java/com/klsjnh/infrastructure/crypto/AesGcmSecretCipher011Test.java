package com.klsjnh.infrastructure.crypto;

/*                AesGcmSecretCipher011Test class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  AES-GCM cipher roundtrip / legacy plaintext
 *
 */

import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.infrastructure.config.KrtSecurityConfig011;

import org.springframework.core.env.Environment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link AesGcmSecretCipher011}.
 */

@ExtendWith(MockitoExtension.class)
class AesGcmSecretCipher011Test {

    private static final String MASTER_KEY = "dev-crypto-master-key-klsjnh-framework011-change-me-32b";

    @Mock
    private Environment environment;

    private AesGcmSecretCipher011 cipher;

    /**
     * Build a cipher with a valid master key.
     */
    @BeforeEach
    void setUp() {
        KrtSecurityConfig011 config = new KrtSecurityConfig011(environment);
        config.getCrypto().setMasterKey(MASTER_KEY);
        cipher = new AesGcmSecretCipher011(config);
    }

    /**
     * Encrypt then decrypt returns the original plaintext.
     */
    @Test
    void roundTripEncryptDecrypt() {
        String cipherText = cipher.encrypt("plain-secret-011");

        assertTrue(cipher.isEncrypted(cipherText));
        assertTrue(cipherText.startsWith(AesGcmSecretCipher011.PREFIX_V1));
        assertEquals("plain-secret-011", cipher.decrypt(cipherText));
    }

    /**
     * Legacy plaintext (no prefix) passes through on decrypt.
     */
    @Test
    void legacyPlaintextPassesThroughOnDecrypt() {
        assertEquals("legacy-plain", cipher.decrypt("legacy-plain"));
        assertFalse(cipher.isEncrypted("legacy-plain"));
    }

    /**
     * Blank values are not encrypted.
     */
    @Test
    void blankPassesThroughOnEncrypt() {
        assertEquals(null, cipher.encrypt(null));
        assertEquals("", cipher.encrypt(""));
        assertEquals("   ", cipher.encrypt("   "));
    }

    /**
     * Encrypting an already-encrypted value is a no-op.
     */
    @Test
    void encryptIsIdempotentOnAlreadyEncrypted() {
        String once = cipher.encrypt("once");
        assertEquals(once, cipher.encrypt(once));
    }

    /**
     * Corrupt ciphertext raises a business exception.
     */
    @Test
    void corruptCiphertextFails() {
        assertThrows(BusinessException.class,
                () -> cipher.decrypt(AesGcmSecretCipher011.PREFIX_V1 + "not-valid-base64!!!"));
    }

    /**
     * Master keys shorter than 32 bytes are rejected at construction.
     */
    @Test
    void shortMasterKeyRejected() {
        KrtSecurityConfig011 config = new KrtSecurityConfig011(environment);
        config.getCrypto().setMasterKey("too-short");

        assertThrows(IllegalStateException.class, () -> new AesGcmSecretCipher011(config));
    }
}
