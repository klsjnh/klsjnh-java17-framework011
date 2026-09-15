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

import com.klsjnh.domain.iam.JulyUser;
import com.klsjnh.domain.iam.JulyUserRepository;
import com.klsjnh.domain.platform011.export.ExportColumn;
import com.klsjnh.domain.platform011.export.ExportProvider;

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
     * Ordered column definitions, matching the row keys of {@link #toRow}.
     */
    private static final List<ExportColumn> COLUMNS = List.of(
            new ExportColumn("id", "主键"),
            new ExportColumn("userAccount", "用户账号"),
            new ExportColumn("userName", "用户姓名"),
            new ExportColumn("mobile", "手机号"),
            new ExportColumn("email", "邮箱"),
            new ExportColumn("pkOrg", "所属组织"),
            new ExportColumn("lastLoginTime", "最后登录时间"),
            new ExportColumn("status", "状态"),
            new ExportColumn("createBy", "创建人"),
            new ExportColumn("createTime", "创建时间"));

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
     * Get the ordered column definitions of julyUser.
     *
     * @return column definitions
     */
    @Override
    public List<ExportColumn> columns() {
        return COLUMNS;
    }

    /**
     * Fetch one batch of alive users (password hash excluded by design).
     *
     * @param offset row offset, 0 based
     * @param limit  max rows to return
     * @return rows
     */
    @Override
    public List<Map<String, Object>> exportRows(int offset, int limit) {
        return repository.findPage(offset, limit, null, null).stream()
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
