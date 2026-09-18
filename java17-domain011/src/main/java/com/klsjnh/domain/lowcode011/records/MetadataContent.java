package com.klsjnh.domain.lowcode011.records;

/*                MetadataContent record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  metadata content contract record
 *
 */

import com.klsjnh.domain.lowcode011.JulyMetadataDisplay;
import com.klsjnh.domain.lowcode011.JulyMetadataField;
import com.klsjnh.domain.lowcode011.JulyMetadataService;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * The low-code <b>content contract</b>: what one object's metadata looks like,
 * independent of who owns it (the engine aggregate, modeling hand-off, a
 * template, an AI prompt). It is the single schema shared by designer, template,
 * runtime, open API and 033 — serialized to the MetaDTO JSON (keys unchanged).
 * <p>
 * Immutable: the record copies the collections, and {@link #reconstitute} is the
 * only entry for foreign data (validates before the value enters the domain).
 * </p>
 *
 * @param objectName    object name, identity, immutable
 * @param objectType    object type code (ObjectType011), nullable
 * @param description   description, nullable
 * @param businessField business field code, nullable
 * @param packageName   target package name, nullable
 * @param routerPath    route path, nullable
 * @param fields        field definitions
 * @param displays      display column definitions
 * @param services      service definitions
 */

public record MetadataContent(String objectName, String objectType, String description, String businessField,
        String packageName, String routerPath, List<JulyMetadataField> fields, List<JulyMetadataDisplay> displays,
        List<JulyMetadataService> services) {

    /**
     * Object name shape.
     */
    private static final Pattern OBJECT_NAME = Pattern.compile("^[a-z][a-z0-9_]{0,49}$");

    /**
     * Normalize: copy collections to immutable lists.
     */
    public MetadataContent {
        fields = fields == null ? List.of() : List.copyOf(fields);
        displays = displays == null ? List.of() : List.copyOf(displays);
        services = services == null ? List.of() : List.copyOf(services);
    }

    /**
     * Boundary guard: rebuild from foreign data, replaying the invariants so a
     * malformed payload never enters the domain.
     *
     * @param objectName    object name
     * @param objectType    object type code
     * @param description   description
     * @param businessField business field code
     * @param packageName   package name
     * @param routerPath    route path
     * @param fields        fields
     * @param displays      displays
     * @param services      services
     * @return validated content
     */
    public static MetadataContent reconstitute(String objectName, String objectType, String description,
            String businessField, String packageName, String routerPath, List<JulyMetadataField> fields,
            List<JulyMetadataDisplay> displays, List<JulyMetadataService> services) {
        if (objectName == null || !OBJECT_NAME.matcher(objectName).matches()) {
            throw new IllegalArgumentException("objectName invalid (^[a-z][a-z0-9_]{0,49}$): " + objectName);
        }

        rejectDuplicateCodes(fields == null ? List.of() : fields.stream().map(JulyMetadataField::fieldCode).toList(),
                "field");
        rejectDuplicateCodes(displays == null ? List.of() : displays.stream().map(JulyMetadataDisplay::displayCode)
                .toList(), "display");
        rejectDuplicateCodes(services == null ? List.of() : services.stream().map(JulyMetadataService::serviceCode)
                .toList(), "service");

        return new MetadataContent(objectName, objectType, description, businessField, packageName, routerPath, fields,
                displays, services);
    }

    /**
     * Reject case-insensitive duplicate codes (the persistence keys are
     * case-insensitive).
     *
     * @param codes codes
     * @param kind  child kind for the message
     */
    private static void rejectDuplicateCodes(List<String> codes, String kind) {
        Set<String> seen = new HashSet<>();

        for (String code : codes) {
            String key = code == null ? "" : code.toLowerCase(Locale.ROOT);
            if (!seen.add(key)) {
                throw new IllegalArgumentException("duplicate " + kind + " code (case-insensitive): " + code);
            }
        }
    }
}
