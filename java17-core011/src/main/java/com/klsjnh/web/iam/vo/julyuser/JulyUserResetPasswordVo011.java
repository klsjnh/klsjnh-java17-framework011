package com.klsjnh.web.iam.vo.julyuser;

/*                JulyUserResetPasswordVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july user reset password vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**
 * Admin password reset request VO for a user.
 */

@Data
public class JulyUserResetPasswordVo011 {

    /** User id. */
    @NotBlank(message = "id is required")
    @Schema(description = "用户 id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    /** New raw password, stored as bcrypt on the server. */
    @NotBlank(message = "password is required")
    @Schema(description = "新密码（明文传输，服务端 bcrypt 存储）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
