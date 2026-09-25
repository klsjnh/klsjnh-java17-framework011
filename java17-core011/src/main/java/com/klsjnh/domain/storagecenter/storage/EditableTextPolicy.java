package com.klsjnh.domain.storagecenter.storage;

/*                EditableTextPolicy class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  editable text policy class
 *
 */

import java.util.Locale;

/**
 * Domain rules for editing a storage object as plain text in the UI: a size
 * ceiling and the editor kind resolved from the object extension.
 */

public final class EditableTextPolicy {

    /**
     * Maximum editable size, 1MB.
     */
    public static final long MAX_SIZE_BYTES = 1024L * 1024L;

    /**
     * SQL editor kind.
     */
    public static final String EDITOR_SQL = "sql";

    /**
     * Markdown editor kind.
     */
    public static final String EDITOR_MARKDOWN = "markdown";

    /**
     * Private constructor: static utility.
     */
    private EditableTextPolicy() {
    }

    /**
     * Assert a size is known and within the edit ceiling.
     *
     * @param sizeBytes object size
     */
    public static void assertEditable(long sizeBytes) {
        if (sizeBytes < 0) {
            throw new IllegalArgumentException("object size unknown");
        }

        if (sizeBytes > MAX_SIZE_BYTES) {
            throw new IllegalArgumentException("file exceeds 1MB edit limit");
        }
    }

    /**
     * Assert a content length is within the edit ceiling.
     *
     * @param contentBytes content size in bytes
     */
    public static void assertContentSize(long contentBytes) {
        assertEditable(contentBytes);
    }

    /**
     * Resolve the editor kind from the object key.
     *
     * @param objectKey object key
     * @return {@link #EDITOR_SQL} for {@code .sql}, otherwise {@link #EDITOR_MARKDOWN}
     */
    public static String resolveEditorKind(String objectKey) {
        if (objectKey != null && objectKey.trim().toLowerCase(Locale.ROOT).endsWith(".sql")) {
            return EDITOR_SQL;
        }

        return EDITOR_MARKDOWN;
    }

    /**
     * Resolve the content type from the object key.
     *
     * @param objectKey object key
     * @return content type
     */
    public static String resolveContentType(String objectKey) {
        if (EDITOR_SQL.equals(resolveEditorKind(objectKey))) {
            return "text/plain;charset=UTF-8";
        }

        return "text/markdown;charset=UTF-8";
    }
}
