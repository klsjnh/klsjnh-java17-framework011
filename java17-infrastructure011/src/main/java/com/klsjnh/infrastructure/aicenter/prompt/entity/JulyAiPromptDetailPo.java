package com.klsjnh.infrastructure.aicenter.prompt.entity;

/*                JulyAiPromptDetailPo class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  july ai prompt detail po class
 *
 */

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.klsjnh.infrastructure.persistence.entity.BasePo011;
import com.klsjnh.infrastructure.persistence.entity.MasterLinked;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * AI prompt detail persistence PO mapped to july_ai_prompt_detail (child).
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("july_ai_prompt_detail")
public class JulyAiPromptDetailPo extends BasePo011 implements MasterLinked {

    /** Master link: prompt id (pk_mt). */
    private String pkMt;

    /** Business domain. */
    private String domainCode;

    /** Content mode (inline / storage). */
    private String contentMode;

    /** Inline content. */
    private String content;

    /** Storage instance code. */
    private String storageCode;

    /** Bucket. */
    private String bucket;

    /** Object key. */
    private String objectKey;

    /** Content hash. */
    private String contentHash;

    /** Content size. */
    private Long contentSize;

    /** Variable declarations. */
    private String variables;

    /** Remark. */
    private String remark;

    /**
     * Get the master id.
     *
     * @return master id
     */
    @Override
    public String getPkMt() {
        return pkMt;
    }

    /**
     * Set the master id.
     *
     * @param masterId master id
     */
    @Override
    public void setPkMt(String masterId) {
        this.pkMt = masterId;
    }
}
