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

import com.klsjnh.application.export.ExportProvider;
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
     * Export all alive menus (flat; parent_id preserves the tree).
     *
     * @return rows
     */
    @Override
    public List<Map<String, Object>> exportRows() {
        return repository.findPage(0, 500, null).stream()
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
