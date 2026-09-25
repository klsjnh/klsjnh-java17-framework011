package com.klsjnh.web.iam.converter;

/*                JulyMenuConverter class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july menu converter class
 *
 */

import com.klsjnh.domain.iam.menu.JulyMenu;

import com.klsjnh.web.iam.vo.julymenu.JulyMenuVo011;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Converter between the JulyMenu aggregate and the response VO (recursive for
 * tree nodes).
 */

@Component
public class JulyMenuConverter {

    /**
     * Map the aggregate to the response VO (children mapped recursively).
     *
     * @param menu aggregate
     * @return response VO
     */
    public JulyMenuVo011 toVo(JulyMenu menu) {
        JulyMenuVo011 vo = new JulyMenuVo011();
        vo.setId(menu.id().value());
        vo.setParentId(menu.parentId());
        vo.setMenuCode(menu.menuCode());
        vo.setMenuName(menu.menuName());
        vo.setMenuType(menu.menuType());
        vo.setMenuIcon(menu.menuIcon());
        vo.setMenuRoute(menu.menuRoute());
        vo.setPermissionCode(menu.permissionCode());
        vo.setComponent(menu.component());
        vo.setSortOrder(menu.sortOrder());
        vo.setStatus(menu.status());
        vo.setCreateBy(menu.audit().createBy());
        vo.setUpdateBy(menu.audit().updateBy());
        vo.setCreateTime(menu.audit().createTime());
        vo.setUpdateTime(menu.audit().updateTime());

        for (JulyMenu child : menu.getChildren()) {
            vo.getChildren().add(toVo(child));
        }

        return vo;
    }

    /**
     * Map aggregates to response VOs (each with its subtree).
     *
     * @param menus aggregates
     * @return response VO list
     */
    public List<JulyMenuVo011> toVoList(List<JulyMenu> menus) {
        List<JulyMenuVo011> result = new ArrayList<>();

        for (JulyMenu menu : menus) {
            result.add(toVo(menu));
        }

        return result;
    }
}
