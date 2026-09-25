package com.klsjnh.web.aicenter.vo.aidomain;

/*                JulyAiDomainVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.21
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.21  ai domain vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain response VO (detail / page rows / tree nodes).
 */

@Data
public class JulyAiDomainVo011 {

    /** Primary key. */
    @Schema(description = "主键")
    private String id;

    /** Domain code, globally unique. */
    @Schema(description = "域编码（全局唯一）")
    private String domainCode;

    /** Domain name. */
    @Schema(description = "域名称")
    private String domainName;

    /** Parent domain id, empty string for root. */
    @Schema(description = "上级域 id（根为空串）")
    private String parentId;

    /** Sort order. */
    @Schema(description = "排序")
    private Integer sortOrder;

    /** Row status. */
    @Schema(description = "状态（0 停用 / 1 启用）")
    private String status;

    /** Remark. */
    @Schema(description = "备注")
    private String remark;

    /** Nested children, ordered by sort. */
    @Schema(description = "子域（按排序）")
    private List<JulyAiDomainVo011> children = new ArrayList<>();

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
