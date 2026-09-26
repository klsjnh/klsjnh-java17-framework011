package com.klsjnh.infrastructure.messagecenter.crypto;

/*                ChannelConfigCipher011Test class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  channel config json field encrypt roundtrip
 *      2026.09.26  vendor secret key names (corpSecret / appSecret)
 *
 */

import com.klsjnh.domain.crypto.SecretCipherPort;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ChannelConfigCipher011}.
 */

class ChannelConfigCipher011Test {

    /**
     * Fake cipher that wraps plaintext with a recognizable prefix.
     */
    private final SecretCipherPort fakeCipher = new SecretCipherPort() {
        /**
         * {@inheritDoc}
         */
        @Override
        public String encrypt(String plain) {
            if (plain == null || plain.isBlank() || plain.startsWith("enc:v1:")) {
                return plain;
            }

            return "enc:v1:" + plain;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String decrypt(String stored) {
            if (stored != null && stored.startsWith("enc:v1:")) {
                return stored.substring("enc:v1:".length());
            }

            return stored;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isEncrypted(String stored) {
            return stored != null && stored.startsWith("enc:v1:");
        }
    };

    /**
     * Encrypt then decrypt restores plaintext; non-secret keys stay plain.
     */
    @Test
    void roundTripEncryptDecryptSensitiveFields() {
        String plain = "{\"url\":\"https://hook.example\",\"secret\":\"s3cr3t\",\"token\":\"tok\"}";
        String stored = ChannelConfigCipher011.encryptFields(plain, fakeCipher);

        assertTrue(stored.contains("enc:v1:s3cr3t"));
        assertTrue(stored.contains("enc:v1:tok"));
        assertTrue(stored.contains("https://hook.example"));
        assertFalse(stored.contains("\"secret\":\"s3cr3t\""));

        String restored = ChannelConfigCipher011.decryptFields(stored, fakeCipher);
        assertTrue(restored.contains("\"secret\":\"s3cr3t\""));
        assertTrue(restored.contains("\"token\":\"tok\""));
        assertTrue(restored.contains("https://hook.example"));
    }

    /**
     * Vendor compound keys (corpSecret / appSecret / clientSecret) encrypt.
     */
    @Test
    void roundTripEncryptDecryptVendorSecretKeys() {
        String plain = "{\"corpId\":\"ww1\",\"corpSecret\":\"cs\",\"appSecret\":\"as\",\"clientSecret\":\"cls\"}";
        String stored = ChannelConfigCipher011.encryptFields(plain, fakeCipher);

        assertTrue(stored.contains("enc:v1:cs"));
        assertTrue(stored.contains("enc:v1:as"));
        assertTrue(stored.contains("enc:v1:cls"));
        assertTrue(stored.contains("\"corpId\":\"ww1\""));
        assertFalse(stored.contains("\"corpSecret\":\"cs\""));

        String restored = ChannelConfigCipher011.decryptFields(stored, fakeCipher);
        assertTrue(restored.contains("\"corpSecret\":\"cs\""));
        assertTrue(restored.contains("\"appSecret\":\"as\""));
    }

    /**
     * Legacy plaintext (no prefix) decrypts as-is.
     */
    @Test
    void legacyPlaintextDecryptsAsIs() {
        String legacy = "{\"secret\":\"legacy\",\"url\":\"https://x\"}";
        assertEquals(legacy, ChannelConfigCipher011.decryptFields(legacy, fakeCipher));
    }

    /**
     * Mask replaces sensitive values for API responses.
     */
    @Test
    void maskSensitiveFields() {
        String plain = "{\"url\":\"https://hook.example\",\"apiKey\":\"ak\",\"password\":\"pw\"}";
        String masked = ChannelConfigCipher011.maskFields(plain);

        assertTrue(masked.contains("******"));
        assertTrue(masked.contains("https://hook.example"));
        assertFalse(masked.contains("ak"));
        assertFalse(masked.contains("pw"));
    }

    /**
     * Mask covers corpSecret / appSecret / privateKey and underscore variants.
     */
    @Test
    void maskVendorAndUnderscoreSecretKeys() {
        String plain = "{\"corpSecret\":\"cs\",\"app_secret\":\"as\",\"privateKey\":\"pk\",\"agentId\":\"1001\"}";
        String masked = ChannelConfigCipher011.maskFields(plain);

        assertTrue(masked.contains("******"));
        assertTrue(masked.contains("\"agentId\":\"1001\""));
        assertFalse(masked.contains("\"cs\""));
        assertFalse(masked.contains("\"as\""));
        assertFalse(masked.contains("\"pk\""));
    }

    /**
     * Update merge keeps stored secrets when the client sends the mask.
     */
    @Test
    void mergeKeepingSecretsPreservesMaskedValues() {
        String existing = "{\"url\":\"https://a\",\"secret\":\"real\"}";
        String incoming = "{\"url\":\"https://b\",\"secret\":\"******\"}";
        String merged = ChannelConfigCipher011.mergeKeepingSecrets(existing, incoming);

        assertTrue(merged.contains("https://b"));
        assertTrue(merged.contains("\"secret\":\"real\""));
    }

    /**
     * Update merge keeps corpSecret when the client echoes the mask or blank.
     */
    @Test
    void mergeKeepingSecretsPreservesCorpSecretMaskAndBlank() {
        String existing = "{\"corpId\":\"ww1\",\"corpSecret\":\"real-cs\"}";
        String masked = ChannelConfigCipher011.mergeKeepingSecrets(existing,
                "{\"corpId\":\"ww2\",\"corpSecret\":\"******\"}");
        String blanked = ChannelConfigCipher011.mergeKeepingSecrets(existing,
                "{\"corpId\":\"ww3\",\"corpSecret\":\"\"}");

        assertTrue(masked.contains("\"corpId\":\"ww2\""));
        assertTrue(masked.contains("\"corpSecret\":\"real-cs\""));
        assertTrue(blanked.contains("\"corpId\":\"ww3\""));
        assertTrue(blanked.contains("\"corpSecret\":\"real-cs\""));
    }

    /**
     * isSensitiveKey recognizes exact and compound vendor names.
     */
    @Test
    void isSensitiveKeyRecognizesVendorNames() {
        assertTrue(ChannelConfigCipher011.isSensitiveKey("corpSecret"));
        assertTrue(ChannelConfigCipher011.isSensitiveKey("app_secret"));
        assertTrue(ChannelConfigCipher011.isSensitiveKey("client-secret"));
        assertTrue(ChannelConfigCipher011.isSensitiveKey("privateKey"));
        assertTrue(ChannelConfigCipher011.isSensitiveKey("wecomSecret"));
        assertFalse(ChannelConfigCipher011.isSensitiveKey("corpId"));
        assertFalse(ChannelConfigCipher011.isSensitiveKey("url"));
    }
}
