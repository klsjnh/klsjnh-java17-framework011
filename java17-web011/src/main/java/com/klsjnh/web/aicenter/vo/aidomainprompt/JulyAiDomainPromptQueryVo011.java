package com.klsjnh.web.aicenter.vo.aidomainprompt;

/*                JulyAiDomainPromptQueryVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.21
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.21  ai domain prompt query vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Prompt page query request.
 */

@Data
public class JulyAiDomainPromptQueryVo011 {

    /** Page index. */
    @Schema(description = "页码（从 1 起）")
    private Integer pageIndex;

    /** Page size. */
    @Schema(description = "每页条数")
    private Integer pageSize;

    /** Code / name keyword. */
    @Schema(description = "编码/名称关键字")
    private String keyword;

    /** Master domain id filter. */
    @Schema(description = "业务域 id（pk_mt）过滤")
    private String pkMt;

    /** Scene filter. */
    @Schema(description = "适用能力过滤")
    private String scene;

    /** Status filter. */
    @Schema(description = "状态（0 停用 / 1 启用）")
    private String status;
}
