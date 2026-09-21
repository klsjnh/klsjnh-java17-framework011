package com.klsjnh.infrastructure.aicenter.prompt.entity;

/*                JulyAiDomainPo class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.21
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.21  july ai domain po class
 *
 */

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.klsjnh.infrastructure.persistence.entity.BasePo011;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * AI prompt business domain persistence PO mapped to july_ai_domain (master
 * tree).
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("july_ai_domain")
public class JulyAiDomainPo extends BasePo011 {

    /** Domain code, globally unique, immutable. */
    private String domainCode;

    /** Domain name. */
    private String domainName;

    /** Parent domain id, empty string for root. */
    private String parentId;

    /** Remark. */
    private String remark;
}
