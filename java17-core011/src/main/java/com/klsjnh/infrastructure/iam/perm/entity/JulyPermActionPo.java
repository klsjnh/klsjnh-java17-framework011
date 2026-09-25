package com.klsjnh.infrastructure.iam.perm.entity;

/*                JulyPermActionPo class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.24
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.24  permission action PO
 *
 */

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.klsjnh.infrastructure.persistence.entity.BasePo011;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * Permission action catalog PO mapped to july_perm_action.
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("july_perm_action")
public class JulyPermActionPo extends BasePo011 {

    /** Parent object code. */
    private String objectCode;

    /** Action code. */
    private String actionCode;

    /** Display name. */
    private String actionName;

    /** Full permission code. */
    private String permissionCode;

    /** Remark. */
    private String remark;
}
