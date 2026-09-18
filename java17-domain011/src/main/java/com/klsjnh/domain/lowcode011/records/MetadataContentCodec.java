package com.klsjnh.domain.lowcode011.records;

/*                MetadataContentCodec class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  metadata content json codec class
 *
 */

import com.klsjnh.domain.lowcode011.JulyMetadataDisplay;
import com.klsjnh.domain.lowcode011.JulyMetadataField;
import com.klsjnh.domain.lowcode011.JulyMetadataService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The <b>single</b> JSON codec between {@link MetadataContent} and the MetaDTO
 * wire shape ({@code metaData} / {@code fieldData} / {@code displayData} /
 * {@code serviceData}). Designer, template and modeling all serialize through
 * here so the JSON schema can never drift. Engine-only extras (sortOrder,
 * publishStatus, version, remark) are merged by the caller.
 */

public final class MetadataContentCodec {

    /**
     * Utility: no instances.
     */
    private MetadataContentCodec() {
    }

    /**
     * Serialize the content contract to the MetaDTO map.
     *
     * @param content content contract
     * @return MetaDTO map
     */
    public static Map<String, Object> toMetaDto(MetadataContent content) {
        Map<String, Object> metaData = new LinkedHashMap<>();
        metaData.put(MetaDtoKey011.OBJECT_NAME, content.objectName());
        metaData.put(MetaDtoKey011.OBJECT_TYPE, content.objectType());
        metaData.put(MetaDtoKey011.DESCRIPTION, content.description());
        metaData.put(MetaDtoKey011.BUSINESS_FIELD, content.businessField());
        metaData.put(MetaDtoKey011.PACKAGE_NAME, content.packageName());
        metaData.put(MetaDtoKey011.ROUTER_PATH, content.routerPath());

        List<Map<String, Object>> fieldData = new ArrayList<>();

        for (JulyMetadataField field : content.fields()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put(MetaDtoKey011.CODE, field.fieldCode());
            row.put(MetaDtoKey011.NAME, field.fieldName());
            row.put(MetaDtoKey011.FIELD_TYPE, field.fieldType());
            row.put(MetaDtoKey011.LENGTH, field.fieldLength());
            row.put(MetaDtoKey011.NOT_NULL, field.requiredField());
            row.put(MetaDtoKey011.DEFAULT_VALUE, field.defaultValue());
            row.put(MetaDtoKey011.SORT, field.sortOrder());
            fieldData.add(row);
        }

        List<Map<String, Object>> displayData = new ArrayList<>();

        for (JulyMetadataDisplay display : content.displays()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put(MetaDtoKey011.CODE, display.displayCode());
            row.put(MetaDtoKey011.NAME, display.displayName());
            row.put(MetaDtoKey011.ALIGN, display.align());
            row.put(MetaDtoKey011.WIDTH, display.width());
            row.put(MetaDtoKey011.COMPONENT_TYPE, display.componentType());
            row.put(MetaDtoKey011.DISPLAY_TYPE, display.displayType());
            row.put(MetaDtoKey011.PARAM011, display.param011());
            row.put(MetaDtoKey011.SORT, display.sortOrder());
            displayData.add(row);
        }

        List<Map<String, Object>> serviceData = new ArrayList<>();

        for (JulyMetadataService service : content.services()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put(MetaDtoKey011.CODE, service.serviceCode());
            row.put(MetaDtoKey011.NAME, service.serviceName());
            row.put(MetaDtoKey011.DESCRIPTION, service.serviceDescription());
            row.put(MetaDtoKey011.OBJECT_TYPE, service.objectType());
            row.put(MetaDtoKey011.PARAM_TYPE, service.paramType());
            row.put(MetaDtoKey011.SERVICE_CONTENT, service.serviceContent());
            row.put(MetaDtoKey011.ENABLED, service.enabled());
            row.put(MetaDtoKey011.SORT, service.sortOrder());
            serviceData.add(row);
        }

        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put(MetaDtoKey011.META_DATA, metaData);
        dto.put(MetaDtoKey011.FIELD_DATA, fieldData);
        dto.put(MetaDtoKey011.DISPLAY_DATA, displayData);
        dto.put(MetaDtoKey011.SERVICE_DATA, serviceData);

        return dto;
    }

    /**
     * Parse the MetaDTO map into the content contract (reconstitute validates).
     *
     * @param dto MetaDTO map
     * @return content contract
     */
    @SuppressWarnings("unchecked")
    public static MetadataContent fromMetaDto(Map<String, Object> dto) {
        Map<String, Object> metaData = dto.get(MetaDtoKey011.META_DATA) instanceof Map
                ? (Map<String, Object>) dto.get(MetaDtoKey011.META_DATA)
                : Map.of();

        List<JulyMetadataField> fields = new ArrayList<>();

        for (Map<String, Object> row : asList(dto.get(MetaDtoKey011.FIELD_DATA))) {
            fields.add(new JulyMetadataField(text(row.get(MetaDtoKey011.CODE)), text(row.get(MetaDtoKey011.NAME)),
                    text(row.get(MetaDtoKey011.FIELD_TYPE)), number(row.get(MetaDtoKey011.LENGTH)),
                    bool(row.get(MetaDtoKey011.NOT_NULL)), text(row.get(MetaDtoKey011.DEFAULT_VALUE)),
                    integer(row.get(MetaDtoKey011.SORT))));
        }

        List<JulyMetadataDisplay> displays = new ArrayList<>();

        for (Map<String, Object> row : asList(dto.get(MetaDtoKey011.DISPLAY_DATA))) {
            displays.add(new JulyMetadataDisplay(text(row.get(MetaDtoKey011.CODE)), text(row.get(MetaDtoKey011.NAME)),
                    defaultIfBlank(text(row.get(MetaDtoKey011.ALIGN)), "left"), number(row.get(MetaDtoKey011.WIDTH)),
                    defaultIfBlank(text(row.get(MetaDtoKey011.COMPONENT_TYPE)), "input"),
                    defaultIfBlank(text(row.get(MetaDtoKey011.DISPLAY_TYPE)), "all"),
                    text(row.get(MetaDtoKey011.PARAM011)), integer(row.get(MetaDtoKey011.SORT))));
        }

        List<JulyMetadataService> services = new ArrayList<>();

        for (Map<String, Object> row : asList(dto.get(MetaDtoKey011.SERVICE_DATA))) {
            services.add(new JulyMetadataService(text(row.get(MetaDtoKey011.CODE)), text(row.get(MetaDtoKey011.NAME)),
                    defaultIfBlank(text(row.get(MetaDtoKey011.DESCRIPTION)), ""),
                    text(row.get(MetaDtoKey011.OBJECT_TYPE)), text(row.get(MetaDtoKey011.PARAM_TYPE)),
                    text(row.get(MetaDtoKey011.SERVICE_CONTENT)), bool(row.get(MetaDtoKey011.ENABLED)),
                    integer(row.get(MetaDtoKey011.SORT))));
        }

        return MetadataContent.reconstitute(text(metaData.get(MetaDtoKey011.OBJECT_NAME)),
                text(metaData.get(MetaDtoKey011.OBJECT_TYPE)), text(metaData.get(MetaDtoKey011.DESCRIPTION)),
                text(metaData.get(MetaDtoKey011.BUSINESS_FIELD)), text(metaData.get(MetaDtoKey011.PACKAGE_NAME)),
                text(metaData.get(MetaDtoKey011.ROUTER_PATH)), fields, displays, services);
    }

    /**
     * Cast a raw value to a list of maps.
     *
     * @param value raw value
     * @return list, never null
     */
    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> asList(Object value) {
        return value instanceof List ? (List<Map<String, Object>>) value : List.of();
    }

    /**
     * Read a string value.
     *
     * @param value raw value
     * @return string or null
     */
    private static String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    /**
     * Read an int value.
     *
     * @param value raw value
     * @return int, 0 when absent
     */
    private static int number(Object value) {
        return value instanceof Number ? ((Number) value).intValue() : 0;
    }

    /**
     * Read a nullable integer value.
     *
     * @param value raw value
     * @return integer or null
     */
    private static Integer integer(Object value) {
        return value instanceof Number ? ((Number) value).intValue() : null;
    }

    /**
     * Read a boolean value.
     *
     * @param value raw value
     * @return boolean
     */
    private static boolean bool(Object value) {
        return value instanceof Boolean ? (Boolean) value : "true".equalsIgnoreCase(String.valueOf(value));
    }

    /**
     * Fall back to a default when blank.
     *
     * @param value    raw value
     * @param fallback default
     * @return value or default
     */
    private static String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
