package com.klsjnh.web.aicenter.vo.aidomain;

/*                JulyAiDomainBundleVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.21
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.21  july ai domain bundle vo 011 class
 *
 */

import lombok.Data;

import com.klsjnh.web.aicenter.vo.aidomainprompt.JulyAiDomainPromptVo011;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

/**
 * Master-sub read response: a business domain together with its prompts.
 */

@Data
public class JulyAiDomainBundleVo011 {

    /** The domain (master). */
    @Schema(description = "业务域（主表）")
    private JulyAiDomainVo011 domain;

    /** The prompts of the domain (children), ordered. */
    @Schema(description = "提示词列表（子表，按排序）")
    private List<JulyAiDomainPromptVo011> prompts = new ArrayList<>();
}
