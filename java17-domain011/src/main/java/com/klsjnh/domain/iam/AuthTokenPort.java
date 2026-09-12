package com.klsjnh.domain.iam;

/*                AuthTokenPort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  auth token port interface
 *
 */

/**
 * Auth token port: the JWT adapter lives in infrastructure (jjwt +
 * krt.jwt.secret from KrtConfig011).
 */

public interface AuthTokenPort {

    /**
     * Issue a signed token for a user.
     *
     * @param id          user id
     * @param userAccount login account
     * @return signed JWT string
     */
    String issue(String id, String userAccount);
}
