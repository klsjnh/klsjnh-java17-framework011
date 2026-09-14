package com.klsjnh.web.system011.vo.julyuser;

/*                JulyUserAssignRolesVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july user assign roles vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Role assignment request VO: the given role id list becomes the user's full
 * role set (toggle semantics, replace strategy).
 */

@Data
public class JulyUserAssignRolesVo011 {

    /** User id. */
    @Schema(description = "用户 id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    /** Full list of granted role ids. */
    @Schema(description = "授予的角色 id 全量列表")
    private List<String> pkRoles;
}
