package com.klsjnh.infrastructure.system011.entity;

/*                JulyMenuPo class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july menu po class
 *
 */

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.klsjnh.infrastructure.persistence.entity.TreePo011;

import com.baomidou.mybatisplus.annotation.TableName;

/**
 * Menu persistence PO mapped to july_menu (tree + sibling order + permission
 * code).
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("july_menu")
public class JulyMenuPo extends TreePo011<JulyMenuPo> {

    /** Menu code, unique. */
    private String menuCode;

    /** Menu name (display title). */
    private String menuName;

    /** Menu type: 1 directory / 2 page / 3 button. */
    private String menuType;

    /** Menu icon. */
    private String menuIcon;

    /** Menu route. */
    private String menuRoute;

    /** Permission code (module:object:action). */
    private String permissionCode;

    /** Frontend component. */
    private String component;
}
