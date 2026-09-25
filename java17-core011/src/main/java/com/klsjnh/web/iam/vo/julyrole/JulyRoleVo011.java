package com.klsjnh.web.iam.vo.julyrole;

/*                JulyRoleVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july role vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Role response VO (detail and page rows).
 */

@Data
public class JulyRoleVo011 {

    /** Primary key. */
    @Schema(description = "主键")
    private String id;

    /** Role code, unique. */
    @Schema(description = "角色编码（唯一）")
    private String roleCode;

    /** Role name. */
    @Schema(description = "角色名称")
    private String roleName;

    /** Built-in flag (1 yes / 0 no). */
    @Schema(description = "内置角色（1 是 / 0 否）")
    private String isBuiltin;

    /** Remark. */
    @Schema(description = "备注")
    private String remark;

    /** Role status: 0 disabled / 1 enabled. */
    @Schema(description = "角色状态（0 禁用 / 1 启用）")
    private String status;

    /** Creator. */
    @Schema(description = "创建人")
    private String createBy;

    /** Last modifier. */
    @Schema(description = "最后修改人")
    private String updateBy;

    /** Create time. */
    @Schema(description = "创建日期")
    private LocalDateTime createTime;

    /** Update time. */
    @Schema(description = "最后修改日期")
    private LocalDateTime updateTime;
}
