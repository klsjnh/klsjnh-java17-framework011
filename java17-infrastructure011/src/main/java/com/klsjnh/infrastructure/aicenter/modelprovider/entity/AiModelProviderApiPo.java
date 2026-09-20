package com.klsjnh.infrastructure.aicenter.modelprovider.entity;

/*                AiModelProviderApiPo class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  ai model provider api po class
 *
 */

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.klsjnh.infrastructure.persistence.entity.BasePo011;
import com.klsjnh.infrastructure.persistence.entity.MasterLinked;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * Api key persistence PO mapped to july_ai_model_provider_api (child of the
 * provider, master link column pk_mt; BasePo011 adds sort_order). The api key
 * column is a secret and never leaves the infrastructure/application layers.
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("july_ai_model_provider_api")
public class AiModelProviderApiPo extends BasePo011 implements MasterLinked {

    /** Master link: provider id (pk_mt). */
    private String pkMt;

    /** Api key code, unique within the provider, immutable. */
    private String apiCode;

    /** Api key display name. */
    private String apiName;

    /** API key secret. */
    private String apiKey;

    /** Remark, optional. */
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
