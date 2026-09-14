package com.klsjnh.infrastructure.system011.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.klsjnh.infrastructure.persistence.entity.BasePo;

import com.baomidou.mybatisplus.annotation.TableName;

/*                JulyRolePo class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july role po class
 *
 */


/**
 * Role persistence PO mapped to july_role (system management - role table).
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("july_role")
public class JulyRolePo extends BasePo {

    /** Role code, unique. */
    private String roleCode;

    /** Role name. */
    private String roleName;

    /** Built-in flag: 1 built-in / 0 custom. */
    private String isBuiltin;

    /** Remark. */
    private String remark;
}
