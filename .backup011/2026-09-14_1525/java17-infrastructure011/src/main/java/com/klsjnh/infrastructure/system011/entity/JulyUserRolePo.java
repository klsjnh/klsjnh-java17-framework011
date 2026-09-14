package com.klsjnh.infrastructure.system011.entity;

import com.klsjnh.infrastructure.persistence.entity.BasePo;
import com.klsjnh.infrastructure.persistence.entity.MasterLinked;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/*                JulyUserRolePo class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july user role po class
 *
 */


/**
 * User-role junction PO mapped to july_user_role (pk_mt = user master link,
 * pk_role = role link).
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("july_user_role")
public class JulyUserRolePo extends BasePo implements MasterLinked {

    /** Master link: user id (pk_mt). */
    private String pkMt;

    /** Role link (pk_role). */
    private String pkRole;
}
