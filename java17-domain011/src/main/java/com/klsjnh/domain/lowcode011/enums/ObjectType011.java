package com.klsjnh.domain.lowcode011.enums;

/*                ObjectType011 enum
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.11
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.11  object type 011 class
 *      2026.09.15  migrated to domain lowcode011
 *
 */

import java.util.Locale;

/**
 * Low-code object type value object: classifies the metadata definition by the
 * shape of the business object it describes.
 * <p>
 * The code is the persistence-facing value and the label is the
 * presentation-facing display name. Immutable and compared by value.
 * </p>
 * <p>
 * Each type implies the repository base class a generated module extends:
 * {@code type011} → {@code BaseRepository}, {@code type013} →
 * {@code BaseRepository011}, {@code type_tree} → {@code BaseTreeRepository},
 * {@code type_tree011} → {@code BaseTreeRepository011}, {@code type021} →
 * {@code BaseMasterSubRepository021}. The {@code 011}
 * variants are the sortable track (PO carries {@code sortOrder}).
 * </p>
 */

public enum ObjectType011 {

    /** Standard flat object, type 011, base repository. */
    TYPE011("type011", "type 011"),

    /** Standard flat object, type 013, sortable repository. */
    TYPE013("type013", "type 013"),

    /** Tree structured object. */
    TREE("type_tree", "tree type"),

    /** Tree structured object, sortable. */
    TREE011("type_tree011", "tree type 011"),

    /** Master-sub table object with cascade persistence. */
    TYPE021("type021", "type 021");

    /**
     * Persistence code, string.
     */
    private final String code;

    /**
     * Display label, string.
     */
    private final String label;

    /**
     * Create an object type constant.
     *
     * @param code  persistence code
     * @param label display label
     */
    ObjectType011(String code, String label) {
        this.code = code;
        this.label = label;
    }

    /**
     * Get the persistence code.
     *
     * @return persistence code
     */
    public String getCode() {
        return code;
    }

    /**
     * Get the display label.
     *
     * @return display label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Resolve from a raw code; null when blank or unknown.
     *
     * @param value raw code
     * @return resolved type, null when blank or unknown
     */
    public static ObjectType011 fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        String v = value.trim().toLowerCase(Locale.ROOT);

        for (ObjectType011 type : values()) {
            if (type.code.equals(v)) {
                return type;
            }
        }

        return null;
    }
}
