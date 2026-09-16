package com.klsjnh.application.system011.dictionary;

/*                JulyDictionaryExportProvider class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july dictionary export provider class
 *
 */

import com.klsjnh.domain.platform011.export.ExportColumn;
import com.klsjnh.domain.platform011.export.ExportProvider;
import com.klsjnh.domain.system011.dictionary.JulyDictionary;
import com.klsjnh.domain.system011.dictionary.JulyDictionaryRepository;

import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Export provider for julyDictionary (the dictionary type table).
 */

@Component
public class JulyDictionaryExportProvider implements ExportProvider {

    /**
     * Ordered column definitions, matching the row keys of {@link #toRow}.
     */
    private static final List<ExportColumn> COLUMNS = List.of(
            new ExportColumn("id", "主键"),
            new ExportColumn("dictionaryCode", "字典编码"),
            new ExportColumn("dictionaryName", "字典名称"),
            new ExportColumn("sortOrder", "排序"),
            new ExportColumn("status", "状态"),
            new ExportColumn("createTime", "创建时间"));

    /**
     * JulyDictionary repository.
     */
    private final JulyDictionaryRepository repository;

    /**
     * Create the provider.
     *
     * @param repository july dictionary repository
     */
    public JulyDictionaryExportProvider(JulyDictionaryRepository repository) {
        this.repository = repository;
    }

    /**
     * Get the unique object code this provider exports.
     *
     * @return object code
     */
    @Override
    public String objectCode() {
        return "julyDictionary";
    }

    /**
     * Get the ordered column definitions of julyDictionary.
     *
     * @return column definitions
     */
    @Override
    public List<ExportColumn> columns() {
        return COLUMNS;
    }

    /**
     * Fetch one batch of alive dictionary types.
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
     * Map one dictionary aggregate to an ordered row.
     *
     * @param dictionary aggregate
     * @return row
     */
    private Map<String, Object> toRow(JulyDictionary dictionary) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("id", dictionary.id().value());
        row.put("dictionaryCode", dictionary.dictionaryCode());
        row.put("dictionaryName", dictionary.dictionaryName());
        row.put("sortOrder", dictionary.sortOrder());
        row.put("status", dictionary.status());
        row.put("createTime", dictionary.audit().createTime());

        return row;
    }
}
