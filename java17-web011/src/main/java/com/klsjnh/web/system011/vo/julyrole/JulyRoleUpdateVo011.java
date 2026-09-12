package com.klsjnh.web.system011.vo.julyrole;

/*                JulyRoleUpdateVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july role update vo 011 class
 *
 */

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

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
}
