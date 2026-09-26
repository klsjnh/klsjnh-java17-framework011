package com.klsjnh.web.iam.vo.julyrole;

/*                JulyRoleAssignMenusVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july role assign menus vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * Menu assignment request VO: the given menu id list becomes the role's full
 * menu grant (toggle semantics, replace strategy).
 */

@Data
public class JulyRoleAssignMenusVo011 {

    /** Role id. */
    @NotBlank(message = "id is required")
    @Schema(description = "角色 id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    /** Granted menu id full list. */
    @Schema(description = "授予的菜单 id 全量列表")
    private List<String> pkMenus;
}
