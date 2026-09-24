package com.klsjnh.domain.iam.menu;

/*                JulyMenu class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july menu class
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import java.util.ArrayList;
import java.util.List;

/**
 * JulyMenu aggregate root (system management context): one node of the
 * navigation menu tree — directory / page / button — carrying the permission
 * code consumed by role authorization.
 */

public class JulyMenu {

    /**
     * Primary key.
     */
    private final EntityId id;

    /**
     * Menu code, unique, immutable after create.
     */
    private final String menuCode;

    /**
     * Menu name (display title).
     */
    private String menuName;

    /**
     * Menu type: '1' directory / '2' page / '3' button.
     */
    private String menuType;

    /**
     * Menu icon.
     */
    private String menuIcon;

    /**
     * Menu route.
     */
    private String menuRoute;

    /**
     * Permission code (module:object:action), nullable.
     */
    private String permissionCode;

    /**
     * Frontend component.
     */
    private String component;

    /**
     * Parent menu id, blank for root.
     */
    private String parentId;

    /**
     * Sort order within siblings.
     */
    private Integer sortOrder;

    /**
     * Menu status: '1' enabled / '0' disabled.
     */
    private String status;

    /**
     * Audit info.
     */
    private AuditInfo audit;

    /**
     * Nested child nodes, not persistent state — filled by tree assembly.
     */
    private final List<JulyMenu> children = new ArrayList<>();

    /**
     * Full constructor (also the rehydration path from persistence).
     *
     * @param id             primary key
     * @param menuCode       menu code, unique
     * @param menuName       menu name
     * @param menuType       menu type
     * @param menuIcon       menu icon
     * @param menuRoute      menu route
     * @param permissionCode permission code
     * @param component      frontend component
     * @param parentId       parent menu id
     * @param sortOrder      sort order
     * @param status         menu status
     * @param audit          audit info
     */
    public JulyMenu(EntityId id, String menuCode, String menuName, String menuType, String menuIcon,
            String menuRoute, String permissionCode, String component, String parentId, Integer sortOrder,
            String status, AuditInfo audit) {
        this.id = id;
        this.menuCode = menuCode;
        this.menuName = menuName;
        this.menuType = menuType;
        this.menuIcon = menuIcon;
        this.menuRoute = menuRoute;
        this.permissionCode = permissionCode;
        this.component = component;
        this.parentId = parentId;
        this.sortOrder = sortOrder;
        this.status = status == null ? Status011.ENABLED.getCode() : status;
        this.audit = audit == null ? AuditInfo.empty() : audit;
    }

    /**
     * Factory for a new menu node.
     *
     * @param id             primary key
     * @param menuCode       menu code, unique, max 30
     * @param menuName       menu name, max 60
     * @param menuType       menu type ('1' / '2' / '3')
     * @param menuIcon       menu icon, nullable
     * @param menuRoute      menu route, nullable
     * @param permissionCode permission code, nullable
     * @param component      frontend component, nullable
     * @param parentId       parent menu id, blank for root
     * @param sortOrder      sort order
     * @param audit          audit info
     * @return new aggregate
     */
    public static JulyMenu create(EntityId id, String menuCode, String menuName, String menuType, String menuIcon,
            String menuRoute, String permissionCode, String component, String parentId, Integer sortOrder,
            AuditInfo audit) {
        validateBasics(menuCode, menuName, menuType);

        return new JulyMenu(id, menuCode, menuName, menuType, menuIcon, menuRoute, permissionCode, component,
                parentId, sortOrder, Status011.ENABLED.getCode(), audit);
    }

    /**
     * Update the mutable basics (code is immutable after create).
     *
     * @param menuName       menu name
     * @param menuType       menu type
     * @param menuIcon       menu icon
     * @param menuRoute      menu route
     * @param permissionCode permission code
     * @param component      frontend component
     * @param parentId       parent menu id (move allowed)
     * @param sortOrder      sort order
     */
    public void updateBasics(String menuName, String menuType, String menuIcon, String menuRoute,
            String permissionCode, String component, String parentId, Integer sortOrder) {
        validateBasics(this.menuCode, menuName, menuType);
        this.menuName = menuName;
        this.menuType = menuType;
        this.menuIcon = menuIcon;
        this.menuRoute = menuRoute;
        this.permissionCode = permissionCode;
        this.component = component;
        this.parentId = parentId;
        this.sortOrder = sortOrder;
    }

    /**
     * Attach a child node during tree assembly.
     *
     * @param child child node
     */
    public void addChild(JulyMenu child) {
        children.add(child);
    }

    /**
     * Validate the create / update basics.
     *
     * @param menuCode menu code
     * @param menuName menu name
     * @param menuType menu type
     */
    private static void validateBasics(String menuCode, String menuName, String menuType) {
        StringUtil011.requirePresent(menuCode, "menu code", 30);

        StringUtil011.requirePresent(menuName, "menu name", 60);

        StringUtil011.requirePresent(menuType, "menu type", 3);
    }

    /**
     * Get the primary key.
     *
     * @return id value object
     */
    public EntityId id() {
        return id;
    }

    /**
     * Get the menu code.
     *
     * @return menu code
     */
    public String menuCode() {
        return menuCode;
    }

    /**
     * Get the menu name.
     *
     * @return menu name
     */
    public String menuName() {
        return menuName;
    }

    /**
     * Get the menu type.
     *
     * @return menu type
     */
    public String menuType() {
        return menuType;
    }

    /**
     * Get the menu icon.
     *
     * @return menu icon
     */
    public String menuIcon() {
        return menuIcon;
    }

    /**
     * Get the menu route.
     *
     * @return menu route
     */
    public String menuRoute() {
        return menuRoute;
    }

    /**
     * Get the permission code.
     *
     * @return permission code
     */
    public String permissionCode() {
        return permissionCode;
    }

    /**
     * Get the frontend component.
     *
     * @return frontend component
     */
    public String component() {
        return component;
    }

    /**
     * Get the parent menu id.
     *
     * @return parent id, blank for root
     */
    public String parentId() {
        return parentId;
    }

    /**
     * Get the sort order.
     *
     * @return sort order
     */
    public Integer sortOrder() {
        return sortOrder;
    }

    /**
     * Get the menu status.
     *
     * @return '1' enabled / '0' disabled
     */
    public String status() {
        return status;
    }

    /**
     * Get the audit info.
     *
     * @return audit info
     */
    public AuditInfo audit() {
        return audit;
    }

    /**
     * Get the nested children.
     *
     * @return children
     */
    public List<JulyMenu> getChildren() {
        return children;
    }
}
