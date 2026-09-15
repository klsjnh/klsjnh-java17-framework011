package com.klsjnh.web.system011.vo.julyrole;

/*                JulyRoleUpdateVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate 2026.09.15
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july role update vo 011 class
 *      2026.09.15  add status field
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Update request VO for a role (the role code is immutable).
 */

@Data
public class JulyRoleUpdateVo011 {

    /** Primary key. */
    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    /** Role name, max 60. */
    @Schema(description = "角色名称（最长 60）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String roleName;

    /** Remark, max 200. */
    @Schema(description = "备注（最长 200）")
    private String remark;

    /** Role status, '1' enabled / '0' disabled; null means keep current. */
    @Schema(description = "角色状态（1 启用 / 0 停用，可空=不修改）")
    private String status;
}
