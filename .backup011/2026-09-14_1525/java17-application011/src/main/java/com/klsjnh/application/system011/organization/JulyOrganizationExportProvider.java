package com.klsjnh.application.system011.organization;

/*                JulyOrganizationExportProvider class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july organization export provider class
 *
 */

import com.klsjnh.application.export.ExportProvider;
import com.klsjnh.domain.system011.organization.JulyOrganization;
import com.klsjnh.domain.system011.organization.JulyOrganizationRepository;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Export provider for julyOrganization (flat rows; parent_id preserves the
 * tree).
 */

@Component
public class JulyOrganizationExportProvider implements ExportProvider {

    /**
     * JulyOrganization repository.
     */
    private final JulyOrganizationRepository repository;

    /**
     * Create the provider.
     *
     * @param repository july organization repository
     */
    public JulyOrganizationExportProvider(JulyOrganizationRepository repository) {
        this.repository = repository;
    }

    /**
     * Get the unique object code this provider exports.
     *
     * @return object code
     */
    @Override
    public String objectCode() {
        return "julyOrganization";
    }

    /**
     * Export all alive organizations (flat; parent_id preserves the tree).
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
     * Map one organization aggregate to an ordered row.
     *
     * @param organization aggregate
     * @return row
     */
    private Map<String, Object> toRow(JulyOrganization organization) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", organization.id().value());
        row.put("parentId", organization.parentId());
        row.put("orgCode", organization.orgCode());
        row.put("orgName", organization.orgName());
        row.put("pkUser", organization.pkUser());
        row.put("orgLevel", organization.orgLevel());
        row.put("sortOrder", organization.sortOrder());
        row.put("status", organization.status());
        row.put("createTime", organization.audit().createTime());

        return row;
    }
}
