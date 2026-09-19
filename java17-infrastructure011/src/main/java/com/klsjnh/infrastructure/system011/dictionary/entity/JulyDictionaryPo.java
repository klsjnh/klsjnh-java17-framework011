package com.klsjnh.infrastructure.system011.dictionary.entity;

/*                JulyDictionaryPo class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july dictionary po class
 *
 */

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.klsjnh.infrastructure.persistence.entity.BasePo011;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * Dictionary persistence PO mapped to july_dictionary, a sorted table (BasePo011
 * adds sort_order).
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("july_dictionary")
public class JulyDictionaryPo extends BasePo011 {

    /** Dictionary code, unique, immutable. */
    private String dictionaryCode;

    /** Dictionary display name. */
    private String dictionaryName;

    /** Remark, optional. */
    private String remark;
}
