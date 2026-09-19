package com.klsjnh.web.iam.vo.julyuser;

/*                JulyUserSessionVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july user session vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Login response VO: the signed JWT plus the user profile.
 */

@Data
public class JulyUserSessionVo011 {

    /** Signed JWT. */
    @Schema(description = "签名的 JWT")
    private String token;

    /** Login account. */
    @Schema(description = "登录账号")
    private String userAccount;

    /** User name. */
    @Schema(description = "用户姓名")
    private String userName;

    /** Granted role codes. */
    @Schema(description = "授予的角色编码列表")
    private List<String> roles;
}
