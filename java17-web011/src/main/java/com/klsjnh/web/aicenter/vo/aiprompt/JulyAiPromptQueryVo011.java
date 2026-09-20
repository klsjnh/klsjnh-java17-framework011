package com.klsjnh.web.aicenter.vo.aiprompt;

/*                JulyAiPromptQueryVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  ai prompt query vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Prompt page query request.
 */

@Data
public class JulyAiPromptQueryVo011 {

    /** Page index. */
    @Schema(description = "页码（从 1 起）")
    private Integer pageIndex;

    /** Page size. */
    @Schema(description = "每页条数")
    private Integer pageSize;

    /** Code / name keyword. */
    @Schema(description = "编码/名称关键字")
    private String keyword;

    /** Scene filter. */
    @Schema(description = "适用能力过滤")
    private String scene;

    /** Status filter. */
    @Schema(description = "状态（0 停用 / 1 启用）")
    private String status;
}
