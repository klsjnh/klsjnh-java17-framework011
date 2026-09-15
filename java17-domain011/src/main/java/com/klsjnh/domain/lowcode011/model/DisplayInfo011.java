package com.klsjnh.domain.lowcode011.model;

/*                DisplayInfo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.11
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.11  display info 011 class
 *      2026.09.15  migrated to domain lowcode011, rebuilt as a setter-free entity
 *
 */

import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.lowcode011.enums.DisplayType011;

import java.util.List;
import java.util.Locale;

/**
 * Low-code display column definition: an entity inside the {@link MetaData011}
 * aggregate boundary, binding a rendered column to a business field.
 * <p>
 * The binding target ({@code displayCode} pointing at a {@link FieldInfo011}
 * code) is validated by the aggregate root, because only the root can see the
 * whole field set. No setters: every change goes through an intent method owned
 * by the aggregate.
 * </p>
 */

public class DisplayInfo011 {

    /**
     * Max length of the column code.
     */
    private static final int CODE_MAX = 60;

    /**
     * Allowed text alignment values.
     */
    private static final List<String> ALIGNMENTS = List.of("left", "center", "right");

    /**
     * Alignment used when the raw value is blank or unsupported.
     */
    private static final String DEFAULT_ALIGN = "left";

    /**
     * Column code (field binding), unique within the aggregate, immutable after create.
     */
    private final String displayCode;

    /**
     * Column display name.
     */
    private String displayName;

    /**
     * Text alignment (left / center / right).
     */
    private String align;

    /**
     * Column width in pixels.
     */
    private int width;

    /**
     * Component type (input / select / date / upload).
     */
    private String componentType;

    /**
     * Display type (all / table / form / custom).
     */
    private DisplayType011 displayType;

    /**
     * Extra parameter 011.
     */
    private String param011;

    /**
     * Full constructor, also the rehydration path from persistence.
     *
     * @param displayCode   column code binding a field
     * @param displayName   column display name
     * @param align         text alignment
     * @param width         column width in pixels
     * @param componentType component type
     * @param displayType   display type
     * @param param011      extra parameter, nullable
     */
    public DisplayInfo011(String displayCode, String displayName, String align, int width, String componentType,
            DisplayType011 displayType, String param011) {
        this.displayCode = displayCode;
        this.displayName = displayName;
        this.align = align;
        this.width = width;
        this.componentType = componentType;
        this.displayType = displayType;
        this.param011 = param011;
    }

    /**
     * Factory for a new display column.
     *
     * @param displayCode   column code, required, max 60
     * @param displayName   column display name, required, max 60
     * @param align         text alignment, defaults to left when unknown or blank
     * @param width         column width, negative falls back to 0
     * @param componentType component type, required, max 30
     * @param displayType   display type, defaults to all when null
     * @param param011      extra parameter, nullable
     * @return new display column
     */
    public static DisplayInfo011 create(String displayCode, String displayName, String align, int width,
            String componentType, DisplayType011 displayType, String param011) {
        validateBasics(displayCode, displayName, componentType);

        return new DisplayInfo011(displayCode, displayName, normalizeAlign(align), Math.max(width, 0), componentType,
                displayType == null ? DisplayType011.ALL : displayType, param011);
    }

    /**
     * Update the mutable descriptive attributes; the code is immutable.
     *
     * @param displayName   column display name, required, max 60
     * @param align         text alignment, defaults to left when unknown or blank
     * @param width         column width, negative falls back to 0
     * @param componentType component type, required, max 30
     * @param displayType   display type, defaults to all when null
     * @param param011      extra parameter, nullable
     */
    public void updateBasics(String displayName, String align, int width, String componentType,
            DisplayType011 displayType, String param011) {
        validateBasics(this.displayCode, displayName, componentType);
        this.displayName = displayName;
        this.align = normalizeAlign(align);
        this.width = Math.max(width, 0);
        this.componentType = componentType;
        this.displayType = displayType == null ? DisplayType011.ALL : displayType;
        this.param011 = param011;
    }

    /**
     * Normalize an alignment to a supported value, falling back to left.
     *
     * @param align raw alignment
     * @return supported alignment
     */
    private static String normalizeAlign(String align) {
        if (StringUtil011.isBlank(align)) {
            return DEFAULT_ALIGN;
        }

        String v = align.trim().toLowerCase(Locale.ROOT);

        return ALIGNMENTS.contains(v) ? v : DEFAULT_ALIGN;
    }

    /**
     * Validate the mandatory attributes.
     *
     * @param code      column code
     * @param name      column display name
     * @param component component type
     */
    private static void validateBasics(String code, String name, String component) {
        if (StringUtil011.isMissing(code, CODE_MAX)) {
            throw new IllegalArgumentException("display code is required (max " + CODE_MAX + ")");
        }

        if (StringUtil011.isMissing(name, CODE_MAX)) {
            throw new IllegalArgumentException("display name is required (max " + CODE_MAX + ")");
        }

        if (StringUtil011.isMissing(component, 30)) {
            throw new IllegalArgumentException("component type is required (max 30)");
        }
    }

    /**
     * Get the column code.
     *
     * @return column code binding a field
     */
    public String displayCode() {
        return displayCode;
    }

    /**
     * Get the column display name.
     *
     * @return column display name
     */
    public String displayName() {
        return displayName;
    }

    /**
     * Get the text alignment.
     *
     * @return text alignment
     */
    public String align() {
        return align;
    }

    /**
     * Get the column width.
     *
     * @return column width in pixels
     */
    public int width() {
        return width;
    }

    /**
     * Get the component type.
     *
     * @return component type
     */
    public String componentType() {
        return componentType;
    }

    /**
     * Get the display type.
     *
     * @return display type
     */
    public DisplayType011 displayType() {
        return displayType;
    }

    /**
     * Get the extra parameter.
     *
     * @return extra parameter, nullable
     */
    public String param011() {
        return param011;
    }
}
