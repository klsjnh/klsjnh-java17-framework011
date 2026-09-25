package com.klsjnh.infrastructure.iam.role.entity;

/*                JulyRolePermissionsPo class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july role permissions po class
 *
 */

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.klsjnh.infrastructure.persistence.entity.BasePo;
import com.klsjnh.infrastructure.persistence.entity.MasterLinked;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * Role-permission junction PO mapped to july_role_permissions (pk_mt = role
 * master link, pk_menu = menu link, permission_code = snapshot).
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("july_role_permissions")
public class JulyRolePermissionsPo extends BasePo implements MasterLinked {

    /** Master link: role id (pk_mt). */
    private String pkMt;

    /** Menu link (pk_menu). */
    private String pkMenu;

    /** Permission code snapshot, blank for pure visibility. */
    private String permissionCode;
}
