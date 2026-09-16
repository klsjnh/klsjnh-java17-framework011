package com.klsjnh.infrastructure.lowcode011.entity;

/*                JulyMetadataPo class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july metadata po class
 *
 */

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.klsjnh.infrastructure.persistence.entity.BasePo011;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * Object metadata persistence PO mapped to july_metadata.
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("july_metadata")
public class JulyMetadataPo extends BasePo011 {

    /** Object name, unique, immutable. */
    private String objectName;

    /** Object type code. */
    private String objectType;

    /** Object description. */
    private String description;

    /** Business field mapping. */
    private String businessField;

    /** Target package name. */
    private String packageName;

    /** Route path. */
    private String routerPath;

    /** Remark, optional. */
    private String remark;
}
