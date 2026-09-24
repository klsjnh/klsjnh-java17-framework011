package com.klsjnh.web.iam.vo.julyorganization;

/*                JulyOrganizationVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july organization vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Organization response VO (detail / page rows / tree nodes) with the
 * member-count badge.
 */

@Data
public class JulyOrganizationVo011 {

    /** Primary key. */
    @Schema(description = "主键")
    private String id;

    /** Parent organization id, blank for root. */
    @Schema(description = "上级组织 id（根为空串）")
    private String parentId;

    /** Organization code, unique. */
    @Schema(description = "组织编码（唯一）")
    private String orgCode;

    /** Organization name. */
    @Schema(description = "组织名称")
    private String orgName;

    /** Leader user id (pk_user), nullable. */
    @Schema(description = "负责人（pk_user，可空）")
    private String pkUser;

    /** Tree level (root = 1). */
    @Schema(description = "组织层级（根为 1）")
    private Integer orgLevel;

    /** Sort order within siblings. */
    @Schema(description = "排序（同级内）")
    private Integer sortOrder;

    /** Org status: 0 disabled / 1 enabled. */
    @Schema(description = "组织状态（0 停用 / 1 启用）")
    private String status;

    /** Member count (alive users with pk_org = this org). */
    @Schema(description = "组织人数（pk_org 统计）")
    private Long memberCount;

    /** Nested children, ordered by sort. */
    @Schema(description = "子组织（按排序）")
    private List<JulyOrganizationVo011> children = new ArrayList<>();

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
