package com.klsjnh.application.iam;

/*                LoginResult class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  login result class
 *
 */

/**
 * Login use case result: the signed token plus the profile of the user.
 *
 * @param token      signed JWT
 * @param userAccount login account
 * @param userName   user name
 */

public record LoginResult(String token, String userAccount, String userName) {
}
