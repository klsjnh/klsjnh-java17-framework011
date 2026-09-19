package com.klsjnh.web.messagecenter.vo.template;

/*                JulyMessageTemplateVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july message template vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Template response VO (detail and page rows).
 */

@Data
public class JulyMessageTemplateVo011 {

    /** Primary key. */
    @Schema(description = "主键")
    private String id;

    /** Template code, unique, immutable. */
    @Schema(description = "模板编码（唯一，不可变）")
    private String templateCode;

    /** Manual sort order. */
    @Schema(description = "排序（越小越靠前）")
    private Integer sortOrder;

    /** Template display name. */
    @Schema(description = "模板名称")
    private String templateName;

    /** Channel code the template is bound to. */
    @Schema(description = "渠道编码")
    private String channelCode;

    /** Title with ${var} placeholders. */
    @Schema(description = "标题")
    private String title;

    /** Content body with ${var} placeholders. */
    @Schema(description = "内容")
    private String content;

    /** Row status: 0 disabled / 1 enabled. */
    @Schema(description = "状态（0 停用 / 1 启用）")
    private String status;

    /** Remark. */
    @Schema(description = "备注")
    private String remark;

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
