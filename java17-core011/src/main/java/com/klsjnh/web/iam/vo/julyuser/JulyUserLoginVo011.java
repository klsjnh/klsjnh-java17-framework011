package com.klsjnh.web.iam.vo.julyuser;

/*                JulyUserLoginVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july user login vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Login request VO. Two kinds: account + password (any environment), or
 * account only (passwordless — debug / development runtime modes only).
 */

@Data
public class JulyUserLoginVo011 {

    /** Login account. */
    @Schema(description = "登录账号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userAccount;

    /** Login password; omitted for passwordless login. */
    @Schema(description = "登录密码；免密登录（loginByUserName）时不传")
    private String password;
}
