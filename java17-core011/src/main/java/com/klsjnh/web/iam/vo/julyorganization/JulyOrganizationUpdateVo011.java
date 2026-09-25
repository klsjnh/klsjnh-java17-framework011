package com.klsjnh.web.iam.vo.julyorganization;

/*                JulyOrganizationUpdateVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july organization update vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Update request VO for an organization node (code immutable; parent move
 * allowed with cycle guard and subtree re-level).
 */

@Data
public class JulyOrganizationUpdateVo011 {

    /** Primary key. */
    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    /** Organization name, max 60. */
    @Schema(description = "组织名称（最长 60）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orgName;

    /** Leader user id, nullable. */
    @Schema(description = "负责人（pk_user，可空）")
    private String pkUser;

    /** Parent organization id, blank for root (move allowed). */
    @Schema(description = "上级组织 id（根为空串，可移动）")
    private String parentId;

    /** Sort order within siblings. */
    @Schema(description = "排序（同级内）")
    private Integer sortOrder;
}
