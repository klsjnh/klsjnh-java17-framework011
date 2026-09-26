package com.klsjnh.domain.crypto;

/*                SecretCipherPort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  reversible secret cipher port (AES-GCM)
 *
 */

/**
 * Reversible cipher for platform secrets stored in the database (AI api keys,
 * storage credentials, managed datasource passwords). Login passwords stay on
 * {@code PasswordPort} (bcrypt) and must never go through this port.
 * <p>
 * Ciphertext carries a version prefix so legacy plaintext rows remain
 * readable; the next write upgrades them.
 * </p>
 */

public interface SecretCipherPort {

    /**
     * Encrypt plaintext for durable storage. Blank input is returned as-is
     * (empty / null keep the "leave unchanged" semantics upstream).
     *
     * @param plain plaintext secret, may be null or blank
     * @return versioned ciphertext, or the original blank value
     */
    String encrypt(String plain);

    /**
     * Decrypt a stored value. Values without the cipher prefix are treated as
     * legacy plaintext and returned unchanged.
     *
     * @param stored value from the database, may be null or blank
     * @return plaintext secret
     */
    String decrypt(String stored);

    /**
     * Whether the stored value already carries a recognized cipher prefix.
     *
     * @param stored value from the database
     * @return true when encrypted under a known version
     */
    boolean isEncrypted(String stored);
}
