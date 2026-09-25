package com.klsjnh.infrastructure.datasource.sync;

/*                SyncEngine011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  sync engine (source -> map -> sink)
 *
 */

import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.datasource.sync.Endpoint;
import com.klsjnh.domain.datasource.sync.JulySyncRule;
import com.klsjnh.domain.datasource.sync.JulySyncRuleColumn;
import com.klsjnh.domain.datasource.sync.JulySyncRuleColumnRepository;
import com.klsjnh.domain.datasource.sync.SyncEnginePort;
import com.klsjnh.domain.datasource.sync.SyncRunResult;
import com.klsjnh.domain.datasource.sync.SyncSinkPort;
import com.klsjnh.domain.datasource.sync.SyncSourcePort;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Sync engine implementation: reads the source page by page, maps each row
 * through the column mapping (source column → target column) and writes it to
 * the target with idempotent upsert by the business key. Single-table sync
 * (S1); master-detail / one-to-many are future phases.
 */

@Component
public class SyncEngine011 implements SyncEnginePort {

    /**
     * Column mapping repository.
     */
    private final JulySyncRuleColumnRepository columnRepository;

    /**
     * Sync source.
     */
    private final SyncSourcePort source;

    /**
     * Sync sink.
     */
    private final SyncSinkPort sink;

    /**
     * Create the engine.
     *
     * @param columnRepository column mapping repository
     * @param source           sync source
     * @param sink             sync sink
     */
    public SyncEngine011(JulySyncRuleColumnRepository columnRepository, SyncSourcePort source, SyncSinkPort sink) {
        this.columnRepository = columnRepository;
        this.source = source;
        this.sink = sink;
    }

    /** {@inheritDoc} */
    @Override
    public SyncRunResult run(JulySyncRule rule) {
        List<JulySyncRuleColumn> columns = columnRepository.findByMaster(rule.id().value());

        if (columns.isEmpty()) {
            throw new IllegalStateException("no column mapping for sync rule: " + rule.syncCode());
        }

        List<String> keyColumns = split(rule.syncKey());
        boolean upsert = !"append".equalsIgnoreCase(rule.conflict());
        int pageSize = rule.pageSize() == null || rule.pageSize() <= 0 ? 500 : rule.pageSize();
        Endpoint sourceEndpoint = rule.sourceEndpoint();

        long read = 0;
        long written = 0;
        int pages = 0;
        int pageIndex = 1;

        while (true) {
            List<Map<String, Object>> rows = source.readPage(sourceEndpoint, pageSize, pageIndex);

            if (rows == null || rows.isEmpty()) {
                break;
            }

            read += rows.size();
            pages++;

            List<Map<String, Object>> mapped = new ArrayList<>();

            for (Map<String, Object> row : rows) {
                mapped.add(mapRow(row, columns));
            }

            written += sink.writePage(rule.targetEndpoint(), rule.targetData(), keyColumns, upsert, mapped);

            if (rows.size() < pageSize) {
                break;
            }

            pageIndex++;
        }

        return new SyncRunResult(rule.syncCode(), read, written, pages, "ok");
    }

    /**
     * Map one source row to a target row.
     *
     * @param row     source row
     * @param columns column mapping
     * @return target row
     */
    private Map<String, Object> mapRow(Map<String, Object> row, List<JulySyncRuleColumn> columns) {
        Map<String, Object> mapped = new LinkedHashMap<>();

        for (JulySyncRuleColumn column : columns) {
            mapped.put(column.targetColumn(), value(row, column.sourceColumn()));
        }

        return mapped;
    }

    /**
     * Read a value case-insensitively from a row.
     *
     * @param row    source row
     * @param column source column
     * @return value or null
     */
    private Object value(Map<String, Object> row, String column) {
        if (row.containsKey(column)) {
            return row.get(column);
        }

        for (Map.Entry<String, Object> entry : row.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(column)) {
                return entry.getValue();
            }
        }

        return null;
    }

    /**
     * Split a CSV key list.
     *
     * @param csv csv text
     * @return trimmed non-blank items
     */
    private List<String> split(String csv) {
        List<String> items = new ArrayList<>();

        if (StringUtil011.isBlank(csv)) {
            return items;
        }

        for (String part : csv.split(",")) {
            if (!StringUtil011.isBlank(part)) {
                items.add(part.trim());
            }
        }

        return items;
    }
}
