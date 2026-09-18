package com.klsjnh.infrastructure.lowcode011.modeling.entity;

/*                JulyBusinessModelingPo class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july business modeling po class
 *
 */

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.klsjnh.infrastructure.persistence.entity.BasePo;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * Business modeling persistence PO mapped to july_business_modeling (a flat
 * table; the model_data column carries the low-code description as JSON).
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("july_business_modeling")
public class JulyBusinessModelingPo extends BasePo {

    /** Modeling code, unique, immutable. */
    private String modelCode;

    /** Modeling name. */
    private String modelName;

    /** Low-code object name, globally unique, immutable. */
    private String objectName;

    /** Datasource code, mandatory. */
    private String dataSourceCode;

    /** Take-out SQL, optional. */
    private String sqlContent;

    /** Description JSON (metaData + fieldData). */
    private String modelData;

    /** Remark, optional. */
    private String remark;
}
