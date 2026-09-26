package com.klsjnh.application.system011.dictionary;

/*                JulyDictionaryImportSupport class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  extracted workbook import from JulyDictionaryUseCase
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.platform011.importdata.ImportBundle;
import com.klsjnh.domain.platform011.importdata.ImportResult;
import com.klsjnh.domain.platform011.importdata.ImportSheet;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;
import com.klsjnh.domain.system011.dictionary.JulyDictionary;
import com.klsjnh.domain.system011.dictionary.JulyDictionaryItem;
import com.klsjnh.domain.system011.dictionary.JulyDictionaryItemRepository;
import com.klsjnh.domain.system011.dictionary.JulyDictionaryRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Workbook import support for julyDictionary: upsert masters by code and
 * replace children. Extracted from {@link JulyDictionaryUseCase} to keep the
 * use case focused on CRUD / program reads.
 */

@Component
public class JulyDictionaryImportSupport {

    private final JulyDictionaryRepository repository;
    private final JulyDictionaryItemRepository itemRepository;

    /**
     * Create the support.
     *
     * @param repository     dictionary repository
     * @param itemRepository dictionary item repository
     */
    public JulyDictionaryImportSupport(JulyDictionaryRepository repository,
            JulyDictionaryItemRepository itemRepository) {
        this.repository = repository;
        this.itemRepository = itemRepository;
    }

    /**
     * Import a workbook bundle: upsert masters by dictionaryCode; for every
     * code that appears on the master sheet, replace that master's items from
     * the children sheet (other dictionaries untouched).
     *
     * @param bundle decoded sheets
     * @return import counts
     */
    @Transactional
    public ImportResult importWorkbook(ImportBundle bundle) {
        String funcName = "import workbook";

        ImportSheet masterSheet = bundle.sheet("master")
                .orElseThrow(() -> BusinessException.badRequest(funcName + ": missing master sheet"));
        ImportSheet childSheet = bundle.sheet("children").orElse(new ImportSheet("children", List.of()));

        int mastersInserted = 0;
        int mastersUpdated = 0;
        int childrenWritten = 0;

        Map<String, List<Map<String, Object>>> childrenByCode = new HashMap<>();

        for (int i = 0; i < childSheet.rows().size(); i++) {
            Map<String, Object> row = childSheet.rows().get(i);
            String dictionaryCode = stringVal(row.get("dictionaryCode"));

            if (StringUtil011.isBlank(dictionaryCode)) {
                throw BusinessException.badRequest(funcName + ": children row " + (i + 2) + " missing dictionaryCode");
            }

            childrenByCode.computeIfAbsent(dictionaryCode, key -> new ArrayList<>()).add(row);
        }

        Set<String> seenCodes = new HashSet<>();

        for (int i = 0; i < masterSheet.rows().size(); i++) {
            Map<String, Object> row = masterSheet.rows().get(i);
            int excelRow = i + 2;
            String dictionaryCode = stringVal(row.get("dictionaryCode"));
            String dictionaryName = stringVal(row.get("dictionaryName"));
            Integer sortOrder = intVal(row.get("sortOrder"));
            String status = blankToNull(stringVal(row.get("status")));
            String remark = blankToNull(stringVal(row.get("remark")));

            if (StringUtil011.isBlank(dictionaryCode)) {
                throw BusinessException.badRequest(funcName + ": master row " + excelRow + " missing dictionaryCode");
            }

            if (StringUtil011.isBlank(dictionaryName)) {
                throw BusinessException.badRequest(funcName + ": master row " + excelRow + " missing dictionaryName");
            }

            if (!seenCodes.add(dictionaryCode)) {
                throw BusinessException.badRequest(funcName + ": duplicate dictionaryCode on master: " + dictionaryCode);
            }

            requireStatus(status);

            JulyDictionary existing = repository.findByCode(dictionaryCode);
            String dictionaryId;

            if (existing == null) {
                JulyDictionary created = newDictionary(dictionaryCode, sortOrder, dictionaryName, status, remark);
                repository.insert(created);
                dictionaryId = created.id().value();
                mastersInserted++;
            } else {
                applyUpdate(existing, dictionaryName, sortOrder, status, remark);
                repository.update(existing);
                dictionaryId = existing.id().value();
                mastersUpdated++;
            }

            List<Map<String, Object>> childRows = childrenByCode.getOrDefault(dictionaryCode, List.of());
            childrenWritten += replaceItems(dictionaryId, childRows, excelRow);
        }

        for (String code : childrenByCode.keySet()) {
            if (!seenCodes.contains(code)) {
                throw BusinessException.badRequest(
                        funcName + ": children reference dictionaryCode not on master sheet: " + code);
            }
        }

        return new ImportResult("julyDictionary", mastersInserted, mastersUpdated, childrenWritten);
    }

    /**
     * Replace all items under a dictionary with the given rows.
     *
     * @param dictionaryId   dictionary id
     * @param childRows      child rows
     * @param masterExcelRow master excel row for error context
     * @return number of items written
     */
    private int replaceItems(String dictionaryId, List<Map<String, Object>> childRows, int masterExcelRow) {
        for (JulyDictionaryItem old : itemRepository.findAllByMaster(dictionaryId, null)) {
            itemRepository.logicDeleteById(old.id().value());
        }

        Set<String> itemCodes = new HashSet<>();
        int written = 0;

        for (int i = 0; i < childRows.size(); i++) {
            Map<String, Object> row = childRows.get(i);
            String itemCode = stringVal(row.get("itemCode"));
            String itemLabel = stringVal(row.get("itemLabel"));
            Integer sortOrder = intVal(row.get("sortOrder"));
            String status = blankToNull(stringVal(row.get("status")));
            String remark = blankToNull(stringVal(row.get("remark")));

            if (StringUtil011.isBlank(itemCode)) {
                throw BusinessException.badRequest(
                        "import workbook: children under master row " + masterExcelRow + " missing itemCode");
            }

            if (StringUtil011.isBlank(itemLabel)) {
                throw BusinessException.badRequest(
                        "import workbook: children itemCode " + itemCode + " missing itemLabel");
            }

            if (!itemCodes.add(itemCode)) {
                throw BusinessException.badRequest(
                        "import workbook: duplicate itemCode " + itemCode + " under dictionary");
            }

            requireStatus(status);
            JulyDictionaryItem item = newItem(dictionaryId, sortOrder, itemCode, itemLabel, status, remark);
            itemRepository.insert(item);
            written++;
        }

        return written;
    }

    /**
     * Coerce a cell value to string.
     *
     * @param value raw value
     * @return string, never null
     */
    private static String stringVal(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    /**
     * Blank string to null.
     *
     * @param value string
     * @return null when blank
     */
    private static String blankToNull(String value) {
        return StringUtil011.isBlank(value) ? null : value;
    }

    /**
     * Parse an integer cell.
     *
     * @param value raw value
     * @return integer or null
     */
    private static Integer intVal(Object value) {
        if (value == null) {
            return null;
        }

        String text = String.valueOf(value).trim();

        if (text.isEmpty()) {
            return null;
        }

        try {
            if (text.contains(".")) {
                return (int) Double.parseDouble(text);
            }

            return Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            throw BusinessException.badRequest("import workbook: invalid integer " + text);
        }
    }

    /**
     * Apply an update on the aggregate.
     *
     * @param dictionary     aggregate
     * @param dictionaryName display name
     * @param sortOrder      sort order
     * @param status         status
     * @param remark         remark
     */
    private void applyUpdate(JulyDictionary dictionary, String dictionaryName, Integer sortOrder, String status,
            String remark) {
        try {
            dictionary.update(dictionaryName, sortOrder, status, remark);
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }
    }

    /**
     * Build a new dictionary aggregate.
     *
     * @param dictionaryCode code
     * @param sortOrder      sort order
     * @param dictionaryName display name
     * @param status         status
     * @param remark         remark
     * @return new aggregate
     */
    private JulyDictionary newDictionary(String dictionaryCode, Integer sortOrder, String dictionaryName, String status,
            String remark) {
        try {
            return JulyDictionary.create(EntityId.generate(), dictionaryCode, sortOrder, dictionaryName, status, remark,
                    AuditInfo.empty());
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }
    }

    /**
     * Build a new item entity.
     *
     * @param dictionaryId dictionary id
     * @param sortOrder    sort order
     * @param itemCode     item code
     * @param itemLabel    item label
     * @param status       status
     * @param remark       remark
     * @return new entity
     */
    private JulyDictionaryItem newItem(String dictionaryId, Integer sortOrder, String itemCode, String itemLabel,
            String status, String remark) {
        try {
            return JulyDictionaryItem.create(EntityId.generate(), dictionaryId, sortOrder, itemCode, itemLabel, status,
                    remark, AuditInfo.empty());
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }
    }

    /**
     * Reject an unknown status when one is supplied.
     *
     * @param status raw status, nullable
     */
    private void requireStatus(String status) {
        if (status != null && !status.isBlank() && Status011.of(status) == null) {
            throw BusinessException.badRequest("unknown status: " + status);
        }
    }
}
