package com.klsjnh.infrastructure.iam.perm.entity;

/*                JulyPermObjectPo class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.24
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.24  permission object PO
 *
 */

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.klsjnh.infrastructure.persistence.entity.BasePo011;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * Permission object catalog PO mapped to july_perm_object.
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("july_perm_object")
public class JulyPermObjectPo extends BasePo011 {

    /** Object code, unique. */
    private String objectCode;

    /** Display name. */
    private String objectName;

    /** Module code. */
    private String moduleCode;

    /** Remark. */
    private String remark;
}
