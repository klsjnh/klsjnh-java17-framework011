package com.klsjnh.web.messagecenter.vo.template;

/*                JulyOutboundTemplateInsertVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july message template insert vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Insert request VO for a message template (templateCode required and
 * immutable).
 */

@Data
public class JulyOutboundTemplateInsertVo011 {

    /** Template code, unique, immutable, max 60. */
    @Schema(description = "模板编码（唯一，最长 60，创建后不可修改）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String templateCode;

    /** Manual sort order, smaller comes first; blank falls back to the default. */
    @Schema(description = "排序（越小越靠前，留空取默认 9999）")
    private Integer sortOrder;

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

    /** Remark, max 300. */
    @Schema(description = "备注（最长 300）")
    private String remark;
}
