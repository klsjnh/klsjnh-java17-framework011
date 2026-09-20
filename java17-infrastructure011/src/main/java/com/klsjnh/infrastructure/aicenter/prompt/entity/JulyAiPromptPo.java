package com.klsjnh.infrastructure.aicenter.prompt.entity;

/*                JulyAiPromptPo class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  july ai prompt po class
 *
 */

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.klsjnh.infrastructure.persistence.entity.BasePo011;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * AI prompt persistence PO mapped to july_ai_prompt (master).
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("july_ai_prompt")
public class JulyAiPromptPo extends BasePo011 {

    /** Prompt code, globally unique, immutable. */
    private String promptCode;

    /** Prompt name. */
    private String promptName;

    /** Scene (inference / image / tts). */
    private String scene;

    /** Remark. */
    private String remark;
}
