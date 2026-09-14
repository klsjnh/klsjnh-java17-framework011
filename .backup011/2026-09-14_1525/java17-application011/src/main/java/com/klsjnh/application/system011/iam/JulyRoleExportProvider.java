package com.klsjnh.application.system011.iam;

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

import com.klsjnh.application.export.ExportProvider;
import com.klsjnh.domain.iam.JulyRole;
import com.klsjnh.domain.iam.JulyRoleRepository;

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
     * Export all alive roles.
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
