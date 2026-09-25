package com.klsjnh.web.messagecenter.outbound.vo.template;

/*                JulyOutboundTemplateUpdateVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july message template update vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Update request VO for a message template (templateCode immutable).
 */

@Data
public class JulyOutboundTemplateUpdateVo011 {

    /** Primary key. */
    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    /** Template display name, max 100. */
    @Schema(description = "模板名称（最长 100）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String templateName;

    /** Channel code the template is bound to, max 60. */
    @Schema(description = "渠道编码（最长 60）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String channelCode;

    /** Title with ${var} placeholders, max 300. */
    @Schema(description = "标题（支持 ${var} 占位，最长 300）")
    private String title;

    /** Content body with ${var} placeholders. */
    @Schema(description = "内容（支持 ${var} 占位）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;

    /** Manual sort order, smaller comes first; blank keeps the stored one. */
    @Schema(description = "排序（越小越靠前，留空保持原值）")
    private Integer sortOrder;

    /** Row status: 0 disabled / 1 enabled; blank keeps the stored one. */
    @Schema(description = "状态（0 停用 / 1 启用，留空保持原值）")
    private String status;

    /** Remark, max 300. */
    @Schema(description = "备注（最长 300）")
    private String remark;
}
