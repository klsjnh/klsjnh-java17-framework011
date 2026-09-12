package com.klsjnh.infrastructure.iam;

/*                PasswordEncoderAdapter class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  password encoder adapter class
 *
 */

import com.klsjnh.domain.iam.PasswordPort;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * bcrypt adapter for the PasswordPort (spring-security-crypto).
 */

@Component
public class PasswordEncoderAdapter implements PasswordPort {

    /**
     * bcrypt encoder.
     */
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * Hash a raw password (bcrypt).
     *
     * @param rawPassword raw password
     * @return bcrypt hash
     */
    @Override
    public String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    /**
     * Verify a raw password against a stored hash.
     *
     * @param rawPassword     raw password
     * @param encodedPassword stored bcrypt hash
     * @return true when they match
     */
    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
}
