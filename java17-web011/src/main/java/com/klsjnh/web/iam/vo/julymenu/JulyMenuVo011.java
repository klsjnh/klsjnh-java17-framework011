package com.klsjnh.web.iam.vo.julymenu;

/*                JulyMenuVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july menu vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Menu response VO (detail / page rows / tree nodes) — the shape mirrors the
 * frontend MenuConfig consumption.
 */

@Data
public class JulyMenuVo011 {

    /** Primary key. */
    @Schema(description = "主键")
    private String id;

    /** Parent menu id, blank for root. */
    @Schema(description = "上级菜单 id（根为空串）")
    private String parentId;

    /** Menu code, unique. */
    @Schema(description = "菜单编码（唯一）")
    private String menuCode;

    /** Menu name (display title). */
    @Schema(description = "菜单名称")
    private String menuName;

    /** Menu type: 1 directory / 2 page / 3 button. */
    @Schema(description = "菜单类型（1 目录 / 2 菜单 / 3 按钮）")
    private String menuType;

    /** Menu icon. */
    @Schema(description = "菜单图标")
    private String menuIcon;

    /** Menu route. */
    @Schema(description = "菜单路由")
    private String menuRoute;

    /** Permission code (module:object:action). */
    @Schema(description = "权限标识")
    private String permissionCode;

    /** Frontend component. */
    @Schema(description = "前端组件")
    private String component;

    /** Sort order within siblings. */
    @Schema(description = "排序（同级内）")
    private Integer sortOrder;

    /** Menu status: 0 disabled / 1 enabled. */
    @Schema(description = "菜单状态（0 停用 / 1 启用）")
    private String status;

    /** Nested children, ordered by sort. */
    @Schema(description = "子菜单（按排序）")
    private List<JulyMenuVo011> children = new ArrayList<>();

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
