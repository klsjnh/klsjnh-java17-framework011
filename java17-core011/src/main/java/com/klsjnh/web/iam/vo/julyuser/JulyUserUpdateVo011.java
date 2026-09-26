package com.klsjnh.web.iam.vo.julyuser;

/*                JulyUserUpdateVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july user update vo 011 class
 *      2026.09.26  add status field (bumps tokenVersion)
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

/**
 * Profile update request VO for a user (account and password excluded).
 * Status is optional; a real change bumps {@code token_version} so prior
 * JWTs fail verify.
 */

@Data
public class JulyUserUpdateVo011 {

    /** Primary key. */
    @NotBlank(message = "id is required")
    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    /** User name, max 60. */
    @NotBlank(message = "userName is required")
    @Schema(description = "用户姓名（最长 60）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userName;

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

    /** Account status, '1' enabled / '0' disabled; null means keep current. */
    @Schema(description = "账号状态（1 启用 / 0 停用，可空=不修改；变更后旧 JWT 作废）")
    private String status;
}
