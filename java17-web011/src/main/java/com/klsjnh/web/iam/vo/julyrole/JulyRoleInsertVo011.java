package com.klsjnh.web.iam.vo.julyrole;

/*                JulyRoleInsertVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july role insert vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Insert request VO for a role (custom roles only; built-in roles are seeded
 * separately).
 */

@Data
public class JulyRoleInsertVo011 {

    /** Role code, unique, max 30, immutable after create. */
    @Schema(description = "角色编码（唯一，最长 30，创建后不可修改）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String roleCode;

    /** Role name, max 60. */
    @Schema(description = "角色名称（最长 60）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String roleName;

    /** Remark, max 200. */
    @Schema(description = "备注（最长 200）")
    private String remark;
}
