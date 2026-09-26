package com.klsjnh.web.aicenter.vo.aidomain;

/*                JulyAiDomainSaveWholeVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.21
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.21  july ai domain save whole vo 011 class
 *
 */

import lombok.Data;

import com.klsjnh.web.aicenter.vo.aidomainprompt.JulyAiDomainPromptSaveVo011;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;

/**
 * Whole save request (master + children): the domain is inserted when id is
 * blank and updated otherwise, then the prompt list replaces the old children.
 */

@Data
public class JulyAiDomainSaveWholeVo011 {

    /** Domain id; blank inserts a new domain. */
    @Schema(description = "业务域主键（留空=新增；非空=修改）")
    private String id;

    /** Domain code, unique, immutable; used on insert only. */
    @Schema(description = "域编码（全局唯一，创建后不可修改；仅新增时用）")
    private String domainCode;

    /** Domain name, max 100. */
    @NotBlank(message = "domainName is required")
    @Schema(description = "域名称（最长 100）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String domainName;

    /** Parent domain id, blank for root. */
    @Schema(description = "上级域 id（空=根）")
    private String parentId;

    /** Sort order, smaller comes first. */
    @Schema(description = "排序（越小越靠前）")
    private Integer sortOrder;

    /** Remark, max 300. */
    @Schema(description = "备注（最长 300）")
    private String remark;

    /** Row status: 0 disabled / 1 enabled; blank keeps the stored one. */
    @Schema(description = "状态（0 停用 / 1 启用；留空保持）")
    private String status;

    /** Prompt rows replacing the old children; blank means none. */
    @Valid
    @Schema(description = "提示词列表（整存替换：旧子表逻辑删 + 新列表插入）")
    private List<JulyAiDomainPromptSaveVo011> prompts = new ArrayList<>();
}
