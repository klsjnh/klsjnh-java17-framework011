package com.klsjnh.infrastructure.crypto;

/*                AesGcmSecretCipher011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  AES-256-GCM SecretCipherPort adapter
 *
 */

import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.crypto.SecretCipherPort;

import com.klsjnh.infrastructure.config.KrtSecurityConfig011;

import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * AES-256-GCM adapter for {@link SecretCipherPort}. Ciphertext format:
 * {@code enc:v1:<base64(iv || ciphertext || tag)>}. Master key is taken from
 * {@code krt.crypto.master-key} and hashed with SHA-256 to a 32-byte key.
 */

@Component
public class AesGcmSecretCipher011 implements SecretCipherPort {

    /**
     * Versioned ciphertext prefix (v1).
     */
    public static final String PREFIX_V1 = "enc:v1:";

    private static final int IV_LENGTH_BYTES = 12;
    private static final int TAG_LENGTH_BITS = 128;
    private static final int MIN_MASTER_KEY_BYTES = 32;

    private final SecretKey secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Create the cipher from framework security config.
     *
     * @param krtConfig security config binding
     */
    public AesGcmSecretCipher011(KrtSecurityConfig011 krtConfig) {
        String masterKey = krtConfig.getCrypto() == null ? null : krtConfig.getCrypto().getMasterKey();

        if (StringUtil011.isBlank(masterKey)) {
            throw new IllegalStateException("krt.crypto.master-key is required for SecretCipherPort");
        }

        byte[] raw = masterKey.getBytes(StandardCharsets.UTF_8);

        if (raw.length < MIN_MASTER_KEY_BYTES) {
            throw new IllegalStateException(
                    "krt.crypto.master-key must be at least " + MIN_MASTER_KEY_BYTES + " bytes");
        }

        this.secretKey = new SecretKeySpec(sha256(raw), "AES");
    }

    /**
     * Encrypt plaintext; blank values pass through unchanged.
     *
     * @param plain plaintext
     * @return versioned ciphertext or blank
     */
    @Override
    public String encrypt(String plain) {
        if (StringUtil011.isBlank(plain)) {
            return plain;
        }

        if (isEncrypted(plain)) {
            return plain;
        }

        try {
            byte[] iv = new byte[IV_LENGTH_BYTES];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] cipherBytes = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));

            ByteBuffer buffer = ByteBuffer.allocate(iv.length + cipherBytes.length);
            buffer.put(iv);
            buffer.put(cipherBytes);

            return PREFIX_V1 + Base64.getEncoder().encodeToString(buffer.array());
        } catch (GeneralSecurityException ex) {
            throw BusinessException.badRequest("secret encrypt failed: " + ex.getMessage());
        }
    }

    /**
     * Decrypt a stored value; legacy plaintext (no prefix) is returned as-is.
     *
     * @param stored database value
     * @return plaintext
     */
    @Override
    public String decrypt(String stored) {
        if (StringUtil011.isBlank(stored) || !isEncrypted(stored)) {
            return stored;
        }

        try {
            byte[] decoded = Base64.getDecoder().decode(stored.substring(PREFIX_V1.length()));

            if (decoded.length <= IV_LENGTH_BYTES) {
                throw BusinessException.badRequest("secret decrypt failed: ciphertext too short");
            }

            byte[] iv = new byte[IV_LENGTH_BYTES];
            System.arraycopy(decoded, 0, iv, 0, IV_LENGTH_BYTES);
            byte[] cipherBytes = new byte[decoded.length - IV_LENGTH_BYTES];
            System.arraycopy(decoded, IV_LENGTH_BYTES, cipherBytes, 0, cipherBytes.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] plain = cipher.doFinal(cipherBytes);

            return new String(plain, StandardCharsets.UTF_8);
        } catch (BusinessException ex) {
            throw ex;
        } catch (GeneralSecurityException | IllegalArgumentException ex) {
            throw BusinessException.badRequest("secret decrypt failed: " + ex.getMessage());
        }
    }

    /**
     * Whether the value carries the v1 cipher prefix.
     *
     * @param stored database value
     * @return true when encrypted
     */
    @Override
    public boolean isEncrypted(String stored) {
        return stored != null && stored.startsWith(PREFIX_V1);
    }

    /**
     * SHA-256 digest for key derivation.
     *
     * @param raw master key bytes
     * @return 32-byte digest
     */
    private static byte[] sha256(byte[] raw) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(raw);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }
}
