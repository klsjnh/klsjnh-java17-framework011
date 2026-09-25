package com.klsjnh.domain.iam.auth;

/*                PasswordPort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  password port interface
 *
 */

/**
 * Password hashing port: the bcrypt adapter lives in infrastructure. Raw
 * passwords never leave the use case boundary and are never stored.
 */

public interface PasswordPort {

    /**
     * Hash a raw password (bcrypt).
     *
     * @param rawPassword raw password
     * @return bcrypt hash
     */
    String encode(String rawPassword);

    /**
     * Verify a raw password against a stored hash.
     *
     * @param rawPassword     raw password
     * @param encodedPassword stored bcrypt hash
     * @return true when they match
     */
    boolean matches(String rawPassword, String encodedPassword);
}
