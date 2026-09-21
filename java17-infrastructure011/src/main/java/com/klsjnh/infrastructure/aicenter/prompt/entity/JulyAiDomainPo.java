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
 *      2026.09.21  extends TreePo011 (parent_id / sort_order from the base)
 *
 */

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.klsjnh.infrastructure.persistence.entity.TreePo011;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * AI prompt business domain persistence PO mapped to july_ai_domain (master
 * tree): {@code parent_id} and {@code sort_order} come from
 * {@link TreePo011}.
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("july_ai_domain")
public class JulyAiDomainPo extends TreePo011<JulyAiDomainPo> {

    /** Domain code, globally unique, immutable. */
    private String domainCode;

    /** Domain name. */
    private String domainName;

    /** Remark. */
    private String remark;
}
