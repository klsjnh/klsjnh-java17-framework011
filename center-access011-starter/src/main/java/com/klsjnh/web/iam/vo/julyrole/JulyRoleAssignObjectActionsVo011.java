package com.klsjnh.web.iam.vo.julyrole;

/*                JulyRoleAssignObjectActionsVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.24
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.24  role assign object actions VO
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * Object-action assignment request: replace the role's direct grants under one
 * catalog object with the given action codes.
 */

@Data
public class JulyRoleAssignObjectActionsVo011 {

    /** Role id. */
    @NotBlank(message = "id is required")
    @Schema(description = "角色 id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    /** Catalog object code (e.g. julyScheduler). */
    @NotBlank(message = "objectCode is required")
    @Schema(description = "权限对象编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String objectCode;

    /** Action codes to keep under that object (empty = revoke all of object). */
    @Schema(description = "动作编码列表（空=撤掉该对象下直授）")
    private List<String> actionCodes;
}
