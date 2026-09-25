package com.klsjnh.infrastructure.system011.dictionary.entity;

/*                JulyDictionaryItemPo class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july dictionary item po class
 *
 */

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.klsjnh.infrastructure.persistence.entity.BasePo011;
import com.klsjnh.infrastructure.persistence.entity.MasterLinked;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * Dictionary item persistence PO mapped to july_dictionary_item (child of the
 * dictionary, master link column pk_mt; BasePo011 adds sort_order).
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("july_dictionary_item")
public class JulyDictionaryItemPo extends BasePo011 implements MasterLinked {

    /** Master link: dictionary id (pk_mt). */
    private String pkMt;

    /** Item code, unique within the dictionary, immutable. */
    private String itemCode;

    /** Item display name. */
    private String itemLabel;

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
