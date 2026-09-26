package com.klsjnh.web.iam.vo.julyuser;

/*                JulyUserInsertVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july user insert vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**
 * Insert request VO for a user (the password is stored as a bcrypt hash).
 */

@Data
public class JulyUserInsertVo011 {

    /** Login account, unique, max 30, immutable after create. */
    @NotBlank(message = "userAccount is required")
    @Schema(description = "登录账号（唯一，最长 30，创建后不可修改）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userAccount;

    /** User name, max 60. */
    @NotBlank(message = "userName is required")
    @Schema(description = "用户姓名（最长 60）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userName;

    /** Initial raw password, stored as bcrypt on the server. */
    @NotBlank(message = "password is required")
    @Schema(description = "初始密码（明文传输，服务端 bcrypt 存储）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    /** Mobile number. */
    @Schema(description = "手机号")
    private String mobile;

    /** Email. */
    @Schema(description = "邮箱")
    private String email;

    /** Avatar. */
    private String avatar;

    /** Organization link (pk_org), nullable. */
    private String pkOrg;
}
