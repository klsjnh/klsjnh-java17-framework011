package com.klsjnh.application.ai011.modelprovider;

/*                AiModelProviderExportProvider class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  ai model provider export provider class
 *
 */

import com.klsjnh.domain.ai011.modelprovider.AiModelProvider;
import com.klsjnh.domain.ai011.modelprovider.AiModelProviderRepository;
import com.klsjnh.domain.platform011.export.ExportColumn;
import com.klsjnh.domain.platform011.export.ExportProvider;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Export provider for julyAiModelProvider.
 * <p>
 * Only the provider rows are exported; the api key child table is deliberately
 * out of scope, so no secret can leak through the export path.
 * </p>
 */

@Component
public class AiModelProviderExportProvider implements ExportProvider {

    /**
     * Ordered column definitions, matching the row keys of {@link #toRow}.
     */
    private static final List<ExportColumn> COLUMNS = List.of(
            new ExportColumn("id", "主键"),
            new ExportColumn("providerCode", "提供商编码"),
            new ExportColumn("providerName", "提供商名称"),
            new ExportColumn("baseUrl", "Base URL"),
            new ExportColumn("models", "模型清单"),
            new ExportColumn("sortOrder", "排序"),
            new ExportColumn("status", "状态"),
            new ExportColumn("createTime", "创建时间"));

    /**
     * AiModelProvider repository.
     */
    private final AiModelProviderRepository repository;

    /**
     * Create the provider.
     *
     * @param repository ai model provider repository
     */
    public AiModelProviderExportProvider(AiModelProviderRepository repository) {
        this.repository = repository;
    }

    /**
     * Get the unique object code this provider exports.
     *
     * @return object code
     */
    @Override
    public String objectCode() {
        return "julyAiModelProvider";
    }

    /**
     * Get the ordered column definitions of julyAiModelProvider.
     *
     * @return column definitions
     */
    @Override
    public List<ExportColumn> columns() {
        return COLUMNS;
    }

    /**
     * Fetch one batch of alive providers.
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
     * Map one provider aggregate to an ordered row.
     *
     * @param provider aggregate
     * @return row
     */
    private Map<String, Object> toRow(AiModelProvider provider) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", provider.id().value());
        row.put("providerCode", provider.providerCode());
        row.put("providerName", provider.providerName());
        row.put("baseUrl", provider.baseUrl());
        row.put("models", provider.models());
        row.put("sortOrder", provider.sortOrder());
        row.put("status", provider.status());
        row.put("createTime", provider.audit().createTime());

        return row;
    }
}
