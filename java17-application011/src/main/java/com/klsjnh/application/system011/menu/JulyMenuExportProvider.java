package com.klsjnh.application.system011.menu;

/*                JulyMenuExportProvider class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july menu export provider class
 *
 */

import com.klsjnh.domain.platform011.export.ExportColumn;
import com.klsjnh.domain.platform011.export.ExportProvider;
import com.klsjnh.domain.system011.menu.JulyMenu;
import com.klsjnh.domain.system011.menu.JulyMenuRepository;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Export provider for julyMenu (flat rows; parent_id preserves the tree).
 */

@Component
public class JulyMenuExportProvider implements ExportProvider {

    /**
     * Ordered column definitions, matching the row keys of {@link #toRow}.
     */
    private static final List<ExportColumn> COLUMNS = List.of(
            new ExportColumn("id", "主键"),
            new ExportColumn("parentId", "上级菜单"),
            new ExportColumn("menuCode", "菜单编码"),
            new ExportColumn("menuName", "菜单名称"),
            new ExportColumn("menuType", "菜单类型"),
            new ExportColumn("menuIcon", "菜单图标"),
            new ExportColumn("menuRoute", "菜单路由"),
            new ExportColumn("permissionCode", "权限编码"),
            new ExportColumn("sortOrder", "排序号"),
            new ExportColumn("status", "状态"),
            new ExportColumn("createTime", "创建时间"));

    /**
     * JulyMenu repository.
     */
    private final JulyMenuRepository repository;

    /**
     * Create the provider.
     *
     * @param repository july menu repository
     */
    public JulyMenuExportProvider(JulyMenuRepository repository) {
        this.repository = repository;
    }

    /**
     * Get the unique object code this provider exports.
     *
     * @return object code
     */
    @Override
    public String objectCode() {
        return "julyMenu";
    }

    /**
     * Get the ordered column definitions of julyMenu.
     *
     * @return column definitions
     */
    @Override
    public List<ExportColumn> columns() {
        return COLUMNS;
    }

    /**
     * Fetch one batch of alive menus (flat; parent_id preserves the tree).
     *
     * @param offset row offset, 0 based
     * @param limit  max rows to return
     * @return rows
     */
    @Override
    public List<Map<String, Object>> exportRows(int offset, int limit) {
        return repository.findPage(offset, limit, null).stream()
                .map(this::toRow)
                .toList();
    }

    /**
     * Map one menu aggregate to an ordered row.
     *
     * @param menu aggregate
     * @return row
     */
    private Map<String, Object> toRow(JulyMenu menu) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", menu.id().value());
        row.put("parentId", menu.parentId());
        row.put("menuCode", menu.menuCode());
        row.put("menuName", menu.menuName());
        row.put("menuType", menu.menuType());
        row.put("menuIcon", menu.menuIcon());
        row.put("menuRoute", menu.menuRoute());
        row.put("permissionCode", menu.permissionCode());
        row.put("sortOrder", menu.sortOrder());
        row.put("status", menu.status());
        row.put("createTime", menu.audit().createTime());

        return row;
    }
}
