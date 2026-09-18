package com.klsjnh.domain.lowcode011.records;

/*                BaseColumn011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  platform base column constants
 *
 */

import java.util.Set;

/**
 * Platform base column names shared by the low-code engine, the runtime and the
 * templates. Centralised so the string literals never drift between the DDL
 * generator, the table policy, the value validator and the template validator.
 */

public final class BaseColumn011 {

    /** Primary key. */
    public static final String ID = "id";

    /** Row status / enable flag (also carried by {@link #DR}). */
    public static final String STATUS = "status";

    /** Logic delete marker. */
    public static final String DR = "dr";

    /** Creator audit column. */
    public static final String CREATE_BY = "create_by";

    /** Last modifier audit column. */
    public static final String UPDATE_BY = "update_by";

    /** Creation time audit column. */
    public static final String CREATE_TIME = "create_time";

    /** Last update time audit column. */
    public static final String UPDATE_TIME = "update_time";

    /**
     * Every base column.
     */
    public static final Set<String> ALL = Set.of(ID, STATUS, DR, CREATE_BY, UPDATE_BY, CREATE_TIME, UPDATE_TIME);

    /**
     * Audit columns filled by the framework (never required from callers).
     */
    public static final Set<String> AUDIT = Set.of(CREATE_BY, UPDATE_BY, CREATE_TIME, UPDATE_TIME);

    /**
     * Utility: no instances.
     */
    private BaseColumn011() {
    }
}
