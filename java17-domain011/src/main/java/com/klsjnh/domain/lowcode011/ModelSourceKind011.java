package com.klsjnh.domain.lowcode011;

/*                ModelSourceKind011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.18
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.18  modeling intake source kind constants
 *
 */

/**
 * Modeling intake source kinds (the {@code kind} key of an intake request).
 * Centralised so the engine, the adapters and the request contract never drift.
 */

public final class ModelSourceKind011 {

    /** Probe a read-only SQL on a business datasource. */
    public static final String SQL = "sql";

    /** Read an existing table structure. */
    public static final String TABLE = "table";

    /** A MetaDTO / template value. */
    public static final String TEMPLATE = "template";

    /** A natural-language prompt handled by AI. */
    public static final String AI = "ai";

    /** A 033 business-modeling record hand-off. */
    public static final String MODELING = "modeling";

    /** Copy an existing object. */
    public static final String COPY = "copy";

    /**
     * Utility: no instances.
     */
    private ModelSourceKind011() {
    }
}
