package com.klsjnh.web.iam.vo.julyuser;

/*                JulyUserChangePasswordVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july user change password vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**
 * Self password change request VO: the operator id comes from the auth filter
 * request attribute, the old password is verified before the update.
 */

@Data
public class JulyUserChangePasswordVo011 {

    /** Old raw password, verified against bcrypt on the server. */
    @NotBlank(message = "oldPassword is required")
    @Schema(description = "旧密码（明文传输，服务端 bcrypt 校验）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String oldPassword;

    /** New raw password, stored as bcrypt on the server. */
    @NotBlank(message = "newPassword is required")
    @Schema(description = "新密码（明文传输，服务端 bcrypt 存储）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String newPassword;
}
