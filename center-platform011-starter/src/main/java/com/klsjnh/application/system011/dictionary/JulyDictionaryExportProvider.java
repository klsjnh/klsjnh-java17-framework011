package com.klsjnh.application.system011.dictionary;

/*                JulyDictionaryExportProvider class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate 2026.09.22
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july dictionary export provider class
 *      2026.09.22  master + children sheets for xlsx
 *
 */

import com.klsjnh.domain.platform011.export.ExportColumn;
import com.klsjnh.domain.platform011.export.ExportProvider;
import com.klsjnh.domain.platform011.export.ExportSheetSpec;
import com.klsjnh.domain.system011.dictionary.JulyDictionary;
import com.klsjnh.domain.system011.dictionary.JulyDictionaryItem;
import com.klsjnh.domain.system011.dictionary.JulyDictionaryItemRepository;
import com.klsjnh.domain.system011.dictionary.JulyDictionaryRepository;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Export provider for julyDictionary: master sheet + children sheet (items
 * keyed by dictionaryCode).
 */

@Component
public class JulyDictionaryExportProvider implements ExportProvider {

    /**
     * Master sheet columns.
     */
    private static final List<ExportColumn> MASTER_COLUMNS = List.of(
            new ExportColumn("dictionaryCode", "字典编码"),
            new ExportColumn("dictionaryName", "字典名称"),
            new ExportColumn("sortOrder", "排序"),
            new ExportColumn("status", "状态"),
            new ExportColumn("remark", "备注"));

    /**
     * Children sheet columns.
     */
    private static final List<ExportColumn> CHILD_COLUMNS = List.of(
            new ExportColumn("dictionaryCode", "字典编码"),
            new ExportColumn("itemCode", "字典项编码"),
            new ExportColumn("itemLabel", "字典项名称"),
            new ExportColumn("sortOrder", "排序"),
            new ExportColumn("status", "状态"),
            new ExportColumn("remark", "备注"));

    /**
     * JulyDictionary repository.
     */
    private final JulyDictionaryRepository repository;

    /**
     * JulyDictionaryItem repository.
     */
    private final JulyDictionaryItemRepository itemRepository;

    /**
     * Create the provider.
     *
     * @param repository     dictionary repository
     * @param itemRepository item repository
     */
    public JulyDictionaryExportProvider(JulyDictionaryRepository repository,
            JulyDictionaryItemRepository itemRepository) {
        this.repository = repository;
        this.itemRepository = itemRepository;
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
     * Master columns (JSON/CSV path).
     *
     * @return column definitions
     */
    @Override
    public List<ExportColumn> columns() {
        return MASTER_COLUMNS;
    }

    /**
     * Fetch one batch of alive dictionary types (master).
     *
     * @param offset row offset, 0 based
     * @param limit  max rows to return
     * @return rows
     */
    @Override
    public List<Map<String, Object>> exportRows(int offset, int limit) {
        return repository.findPage(offset, limit, null).stream()
                .map(this::toMasterRow)
                .toList();
    }

    /**
     * Two sheets: master + children.
     *
     * @return sheet specs
     */
    @Override
    public List<ExportSheetSpec> sheetSpecs() {
        return List.of(new ExportSheetSpec("master", MASTER_COLUMNS), new ExportSheetSpec("children", CHILD_COLUMNS));
    }

    /**
     * Fetch one batch for a named sheet.
     *
     * @param sheetName sheet name
     * @param offset    offset
     * @param limit     limit
     * @return rows
     */
    @Override
    public List<Map<String, Object>> exportSheetRows(String sheetName, int offset, int limit) {
        if ("master".equals(sheetName)) {
            return exportRows(offset, limit);
        }

        if ("children".equals(sheetName)) {
            return exportChildren(offset, limit);
        }

        return List.of();
    }

    /**
     * Page children by walking masters then their items (stable order for
     * batched export).
     *
     * @param offset offset into the flat children list
     * @param limit  max rows
     * @return child rows
     */
    private List<Map<String, Object>> exportChildren(int offset, int limit) {
        List<Map<String, Object>> all = new ArrayList<>();
        int masterOffset = 0;
        int masterBatch = 100;

        while (true) {
            List<JulyDictionary> masters = repository.findPage(masterOffset, masterBatch, null);

            if (masters.isEmpty()) {
                break;
            }

            for (JulyDictionary dictionary : masters) {
                for (JulyDictionaryItem item : itemRepository.findAllByMaster(dictionary.id().value(), null)) {
                    all.add(toChildRow(dictionary.dictionaryCode(), item));
                }
            }

            masterOffset += masters.size();

            if (masters.size() < masterBatch) {
                break;
            }
        }

        if (offset >= all.size()) {
            return List.of();
        }

        int end = Math.min(all.size(), offset + limit);

        return all.subList(offset, end);
    }

    /**
     * Map one dictionary to a master row.
     *
     * @param dictionary aggregate
     * @return row
     */
    private Map<String, Object> toMasterRow(JulyDictionary dictionary) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("dictionaryCode", dictionary.dictionaryCode());
        row.put("dictionaryName", dictionary.dictionaryName());
        row.put("sortOrder", dictionary.sortOrder());
        row.put("status", dictionary.status());
        row.put("remark", dictionary.remark());

        return row;
    }

    /**
     * Map one item to a children row.
     *
     * @param dictionaryCode dictionary code
     * @param item           item
     * @return row
     */
    private Map<String, Object> toChildRow(String dictionaryCode, JulyDictionaryItem item) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("dictionaryCode", dictionaryCode);
        row.put("itemCode", item.itemCode());
        row.put("itemLabel", item.itemLabel());
        row.put("sortOrder", item.sortOrder());
        row.put("status", item.status());
        row.put("remark", item.remark());

        return row;
    }
}
