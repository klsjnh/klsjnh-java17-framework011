package com.klsjnh.application.system011.config;

/*                JulyConfigExportProvider class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july config export provider class
 *
 */

import com.klsjnh.domain.platform011.export.ExportColumn;
import com.klsjnh.domain.platform011.export.ExportProvider;
import com.klsjnh.domain.system011.config.JulyConfig;
import com.klsjnh.domain.system011.config.JulyConfigRepository;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Export provider for julyConfig.
 */

@Component
public class JulyConfigExportProvider implements ExportProvider {

    /**
     * Ordered column definitions, matching the row keys of {@link #toRow}.
     */
    private static final List<ExportColumn> COLUMNS = List.of(
            new ExportColumn("id", "主键"),
            new ExportColumn("code", "配置项"),
            new ExportColumn("data", "配置值"),
            new ExportColumn("status", "状态"),
            new ExportColumn("createTime", "创建时间"));

    /**
     * JulyConfig repository.
     */
    private final JulyConfigRepository repository;

    /**
     * Create the provider.
     *
     * @param repository july config repository
     */
    public JulyConfigExportProvider(JulyConfigRepository repository) {
        this.repository = repository;
    }

    /**
     * Get the unique object code this provider exports.
     *
     * @return object code
     */
    @Override
    public String objectCode() {
        return "julyConfig";
    }

    /**
     * Get the ordered column definitions of julyConfig.
     *
     * @return column definitions
     */
    @Override
    public List<ExportColumn> columns() {
        return COLUMNS;
    }

    /**
     * Fetch one batch of alive config entries.
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
     * Map one config aggregate to an ordered row.
     *
     * @param config aggregate
     * @return row
     */
    private Map<String, Object> toRow(JulyConfig config) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", config.id().value());
        row.put("code", config.code());
        row.put("data", config.data());
        row.put("status", config.status());
        row.put("createTime", config.audit().createTime());

        return row;
    }
}
