package com.klsjnh.application.system011.iam;

/*                JulyUserExportProvider class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july user export provider class
 *
 */

import com.klsjnh.application.export.ExportProvider;
import com.klsjnh.domain.iam.JulyUser;
import com.klsjnh.domain.iam.JulyUserRepository;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Export provider for julyUser — the password hash is deliberately excluded.
 */

@Component
public class JulyUserExportProvider implements ExportProvider {

    /**
     * JulyUser repository.
     */
    private final JulyUserRepository repository;

    /**
     * Create the provider.
     *
     * @param repository july user repository
     */
    public JulyUserExportProvider(JulyUserRepository repository) {
        this.repository = repository;
    }

    /**
     * Get the unique object code this provider exports.
     *
     * @return object code
     */
    @Override
    public String objectCode() {
        return "julyUser";
    }

    /**
     * Export all alive users (password hash excluded by design).
     *
     * @return rows
     */
    @Override
    public List<Map<String, Object>> exportRows() {
        return repository.findPage(0, 500, null, null).stream()
                .map(this::toRow)
                .toList();
    }

    /**
     * Map one user aggregate to an ordered row (no password).
     *
     * @param user aggregate
     * @return row
     */
    private Map<String, Object> toRow(JulyUser user) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", user.id().value());
        row.put("userAccount", user.userAccount());
        row.put("userName", user.userName());
        row.put("mobile", user.mobile());
        row.put("email", user.email());
        row.put("pkOrg", user.pkOrg());
        row.put("lastLoginTime", user.lastLoginTime());
        row.put("status", user.status());
        row.put("createBy", user.audit().createBy());
        row.put("createTime", user.audit().createTime());

        return row;
    }
}
