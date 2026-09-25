package com.klsjnh.application.iam.role;

/*                JulyRoleExportProvider class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july role export provider class
 *
 */

import com.klsjnh.domain.iam.role.JulyRole;
import com.klsjnh.domain.iam.role.JulyRoleRepository;
import com.klsjnh.domain.platform011.export.ExportColumn;
import com.klsjnh.domain.platform011.export.ExportProvider;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Export provider for julyRole.
 */

@Component
public class JulyRoleExportProvider implements ExportProvider {

    /**
     * Ordered column definitions, matching the row keys of {@link #toRow}.
     */
    private static final List<ExportColumn> COLUMNS = List.of(
            new ExportColumn("id", "主键"),
            new ExportColumn("roleCode", "角色编码"),
            new ExportColumn("roleName", "角色名称"),
            new ExportColumn("isBuiltin", "是否内置"),
            new ExportColumn("remark", "备注"),
            new ExportColumn("status", "状态"),
            new ExportColumn("createBy", "创建人"),
            new ExportColumn("createTime", "创建时间"));

    /**
     * JulyRole repository.
     */
    private final JulyRoleRepository repository;

    /**
     * Create the provider.
     *
     * @param repository july role repository
     */
    public JulyRoleExportProvider(JulyRoleRepository repository) {
        this.repository = repository;
    }

    /**
     * Get the unique object code this provider exports.
     *
     * @return object code
     */
    @Override
    public String objectCode() {
        return "julyRole";
    }

    /**
     * Get the ordered column definitions of julyRole.
     *
     * @return column definitions
     */
    @Override
    public List<ExportColumn> columns() {
        return COLUMNS;
    }

    /**
     * Fetch one batch of alive roles.
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
     * Map one role aggregate to an ordered row.
     *
     * @param role aggregate
     * @return row
     */
    private Map<String, Object> toRow(JulyRole role) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", role.id().value());
        row.put("roleCode", role.roleCode());
        row.put("roleName", role.roleName());
        row.put("isBuiltin", role.isBuiltin());
        row.put("remark", role.remark());
        row.put("status", role.status());
        row.put("createBy", role.audit().createBy());
        row.put("createTime", role.audit().createTime());

        return row;
    }
}
