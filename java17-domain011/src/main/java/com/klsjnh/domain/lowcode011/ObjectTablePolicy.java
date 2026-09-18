package com.klsjnh.domain.lowcode011;

/*                ObjectTablePolicy class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  object table policy (pure helpers)
 *
 */

import com.klsjnh.common.util.DateUtil011;

import com.klsjnh.domain.lowcode011.records.BaseColumn011;

import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Pure, IO-free policy for generated object tables: column filtering, platform
 * base-column defaults, the update/delete key, logic-delete detection and page
 * clamping. Extracted from three runtime/open-api/data-sync use cases that each
 * carried a near-identical copy.
 */

public final class ObjectTablePolicy {

    /**
     * Default page index.
     */
    private static final int DEFAULT_PAGE_INDEX = 1;

    /**
     * Default page size.
     */
    private static final int DEFAULT_PAGE_SIZE = 20;

    /**
     * Max page size (aligned with the 033 paged execute).
     */
    private static final int MAX_PAGE_SIZE = 500;

    /**
     * Utility: no instances.
     */
    private ObjectTablePolicy() {
    }

    /**
     * Keep only request keys that are real target columns (case-insensitive).
     *
     * @param raw     raw request map (may be null)
     * @param columns target columns
     * @return filtered map
     */
    public static Map<String, Object> filter(Object raw, Set<String> columns) {
        Map<String, Object> filtered = new LinkedHashMap<>();

        if (raw instanceof Map) {
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) raw).entrySet()) {
                String key = String.valueOf(entry.getKey()).toLowerCase(Locale.ROOT);
                if (columns.contains(key)) {
                    filtered.put(key, entry.getValue());
                }
            }
        }

        return filtered;
    }

    /**
     * Fill platform base columns that are absent (id / status / dr / audit
     * time) so a generated table's NOT NULL columns never block an insert.
     *
     * @param values  column values (mutated)
     * @param columns target columns
     */
    public static void fillBaseDefaults(Map<String, Object> values, Set<String> columns) {
        if (columns.contains(BaseColumn011.ID) && !values.containsKey(BaseColumn011.ID)) {
            values.put(BaseColumn011.ID, UUID.randomUUID().toString().replace("-", ""));
        }

        if (columns.contains(BaseColumn011.STATUS) && !values.containsKey(BaseColumn011.STATUS)) {
            values.put(BaseColumn011.STATUS, "1");
        }

        if (columns.contains(BaseColumn011.DR) && !values.containsKey(BaseColumn011.DR)) {
            values.put(BaseColumn011.DR, "0");
        }

        Timestamp now = Timestamp.valueOf(DateUtil011.now());

        if (columns.contains(BaseColumn011.CREATE_TIME) && !values.containsKey(BaseColumn011.CREATE_TIME)) {
            values.put(BaseColumn011.CREATE_TIME, now);
        }

        if (columns.contains(BaseColumn011.UPDATE_TIME) && !values.containsKey(BaseColumn011.UPDATE_TIME)) {
            values.put(BaseColumn011.UPDATE_TIME, now);
        }
    }

    /**
     * Refresh the update-time column on an update.
     *
     * @param values  column values (mutated)
     * @param columns target columns
     */
    public static void stampUpdate(Map<String, Object> values, Set<String> columns) {
        if (columns.contains(BaseColumn011.UPDATE_TIME)) {
            values.put(BaseColumn011.UPDATE_TIME, Timestamp.valueOf(DateUtil011.now()));
        }
    }

    /**
     * The update/delete key column: the business key {@code sid} when present,
     * else the primary key {@code id}.
     *
     * @param body request body
     * @return key column
     */
    public static String keyColumn(Map<String, Object> body) {
        return body.containsKey(BusinessKey011.FIELD) ? BusinessKey011.FIELD : BaseColumn011.ID;
    }

    /**
     * Whether the table supports logic delete.
     *
     * @param columns target columns
     * @return true when {@code dr} exists
     */
    public static boolean logicDelete(Set<String> columns) {
        return columns.contains(BaseColumn011.DR);
    }

    /**
     * Clamp the page request: index >= 1, size in [1, 500].
     *
     * @param pageIndex raw page index, nullable
     * @param pageSize  raw page size, nullable
     * @return {index, size}
     */
    public static int[] page(Integer pageIndex, Integer pageSize) {
        int index = pageIndex == null || pageIndex < 1 ? DEFAULT_PAGE_INDEX : pageIndex;
        int size = pageSize == null || pageSize < 1 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);

        return new int[] { index, size };
    }

    /**
     * Add the logic-delete filter when the table has a {@code dr} column.
     *
     * @param filters equality filters (mutated)
     * @param columns target columns
     * @return the same filters, for chaining
     */
    public static Map<String, Object> withDrFilter(Map<String, Object> filters, Set<String> columns) {
        if (columns.contains(BaseColumn011.DR) && !filters.containsKey(BaseColumn011.DR)) {
            filters.put(BaseColumn011.DR, "0");
        }

        return filters;
    }
}
