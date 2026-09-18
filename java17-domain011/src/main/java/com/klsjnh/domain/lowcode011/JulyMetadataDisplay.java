package com.klsjnh.domain.lowcode011;

/*                JulyMetadataDisplay class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july metadata display class
 *
 */

import com.klsjnh.common.util.StringUtil011;

/**
 * Low-code core display column definition (child of {@link JulyMetadata}),
 * binding a rendered column to a field code.
 *
 * @param displayCode   column code binding a field, unique within the object
 * @param displayName   column display name
 * @param align         text alignment (left / center / right)
 * @param width         column width in pixels
 * @param componentType component type
 * @param displayType   display type code (DisplayType011)
 * @param param011      extra parameter, nullable
 * @param sortOrder     manual sort order, null falls back to the default
 */

public record JulyMetadataDisplay(String displayCode, String displayName, String align, int width, String componentType,
        String displayType, String param011, Integer sortOrder) {

    /**
     * Normalize and validate.
     */
    public JulyMetadataDisplay {
        StringUtil011.requirePresent(displayCode, "display code", 60);

        StringUtil011.requirePresent(displayName, "display name", 60);

        if (StringUtil011.isBlank(align) || !(align.equals("left") || align.equals("center") || align.equals("right"))) {
            align = "left";
        }

        if (width < 0) {
            width = 0;
        }

        StringUtil011.requirePresent(componentType, "component type", 30);

        if (StringUtil011.isBlank(displayType)) {
            displayType = "all";
        }

        if (sortOrder == null) {
            sortOrder = 9999;
        }
    }
}
