package com.klsjnh.web.system011.vo.julyorganization;

/*                JulyOrganizationInsertVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july organization insert vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Insert request VO for an organization node.
 */

@Data
public class JulyOrganizationInsertVo011 {

    /** Organization code, unique, max 30, immutable after create. */
    @Schema(description = "组织编码（唯一，最长 30，创建后不可修改）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orgCode;

    /** Organization name, max 60. */
    @Schema(description = "组织名称（最长 60）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orgName;

    /** Leader user id, nullable. */
    @Schema(description = "负责人（pk_user，可空）")
    private String pkUser;

    /** Parent organization id, blank for root. */
    @Schema(description = "上级组织 id（根为空串）")
    private String parentId;

    /** Sort order within siblings. */
    @Schema(description = "排序（同级内）")
    private Integer sortOrder;
}
