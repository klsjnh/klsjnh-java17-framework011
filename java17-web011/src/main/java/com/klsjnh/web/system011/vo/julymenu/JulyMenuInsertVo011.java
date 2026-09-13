package com.klsjnh.web.system011.vo.julymenu;

/*                JulyMenuInsertVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july menu insert vo 011 class
 *
 */

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Insert request VO for a menu node.
 */

@Data
public class JulyMenuInsertVo011 {

    /** Menu code, unique, max 30, immutable after create. */
    @Schema(description = "菜单编码（唯一，最长 30，创建后不可修改）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String menuCode;

    /** Menu name, max 60. */
    @Schema(description = "菜单名称（最长 60）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String menuName;

    /** Menu type: 1 directory / 2 page / 3 button. */
    @Schema(description = "菜单类型（1 目录 / 2 菜单 / 3 按钮）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String menuType;

    /** Parent menu id, blank for root. */
    @Schema(description = "上级菜单 id（根为空串）")
    private String parentId;

    /** Sort order within siblings. */
    @Schema(description = "排序（同级内）")
    private Integer sortOrder;

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
}
