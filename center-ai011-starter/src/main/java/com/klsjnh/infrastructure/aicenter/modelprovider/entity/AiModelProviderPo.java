package com.klsjnh.infrastructure.aicenter.modelprovider.entity;

/*                AiModelProviderPo class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  ai model provider po class
 *
 */

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.klsjnh.infrastructure.persistence.entity.BasePo011;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * Provider persistence PO mapped to july_ai_model_provider (AI model access
 * registry), a sorted table (BasePo011 adds sort_order).
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("july_ai_model_provider")
public class AiModelProviderPo extends BasePo011 {

    /** Provider code, unique, immutable. */
    private String providerCode;

    /** Provider display name. */
    private String providerName;

    /** OpenAI-compatible base url. */
    private String baseUrl;

    /** Comma separated model list, optional. */
    private String models;

    /** Remark, optional. */
    private String remark;
}
