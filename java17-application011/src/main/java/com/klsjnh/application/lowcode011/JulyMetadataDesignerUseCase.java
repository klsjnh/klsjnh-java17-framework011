package com.klsjnh.application.lowcode011;

/*                JulyMetadataDesignerUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  metadata designer use case class
 *
 */

import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;

import com.klsjnh.domain.lowcode011.JulyMetadata;
import com.klsjnh.domain.lowcode011.JulyMetadataDisplay;
import com.klsjnh.domain.lowcode011.JulyMetadataField;
import com.klsjnh.domain.lowcode011.JulyMetadataRepository;
import com.klsjnh.domain.lowcode011.JulyMetadataService;
import com.klsjnh.domain.lowcode011.MetadataDdlGeneratorPort;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Low-code designer use case (phase 1, read-only): list models, load a model as
 * the legacy MetaDTO shape, save the full MetaDTO back onto the one-master
 * three-children aggregate, and preview the publish DDL with the same generator
 * publish will use.
 */

@Service
public class JulyMetadataDesignerUseCase {

    /**
     * Physical table prefix for generated objects.
     */
    private static final String TABLE_PREFIX = "lc_";

    /**
     * Metadata CRUD use case (one master + three children).
     */
    private final JulyMetadataUseCase metadataUseCase;

    /**
     * Metadata repository (existence probe / id lookup).
     */
    private final JulyMetadataRepository repository;

    /**
     * DDL generator (shared with publish).
     */
    private final MetadataDdlGeneratorPort ddlGenerator;

    /**
     * Create the use case.
     *
     * @param metadataUseCase metadata CRUD use case
     * @param repository      metadata repository
     * @param ddlGenerator    DDL generator
     */
    public JulyMetadataDesignerUseCase(JulyMetadataUseCase metadataUseCase, JulyMetadataRepository repository,
            MetadataDdlGeneratorPort ddlGenerator) {
        this.metadataUseCase = metadataUseCase;
        this.repository = repository;
        this.ddlGenerator = ddlGenerator;
    }

    /**
     * List all models with a display status.
     *
     * @return model rows
     */
    public List<Map<String, Object>> listModels() {
        PageResult011<JulyMetadata> page = metadataUseCase.selectListByPage(new PageQuery011(1, 1000), null);
        List<Map<String, Object>> rows = new ArrayList<>();

        for (JulyMetadata item : page.rows()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("objectName", item.objectName());
            row.put("description", item.description());
            row.put("objectType", item.objectType());
            row.put("publishStatus", "draft");
            row.put("version", "");
            rows.add(row);
        }

        return rows;
    }

    /**
     * Load a model as the legacy MetaDTO shape.
     *
     * @param objectName object name
     * @return MetaDTO map
     */
    public Map<String, Object> load(String objectName) {
        return toMetaDto(metadataUseCase.getByObjectName(objectName));
    }

    /**
     * Save a model from the legacy MetaDTO shape (insert or update by object
     * name; the object name is immutable).
     *
     * @param body       MetaDTO body
     * @param sourceType source type (designer / probe)
     * @return object id
     */
    public String save(Map<String, Object> body, String sourceType) {
        Map<String, Object> metaData = asMap(body.get("metaData"));

        if (metaData == null) {
            throw BusinessException.badRequest("metaData required");
        }

        String objectName = text(metaData.get("objectName"));

        if (objectName == null || objectName.isBlank()) {
            throw BusinessException.badRequest("objectName required in metaData");
        }

        List<JulyMetadataField> fields = toFields(asList(body.get("fieldData")));
        List<JulyMetadataDisplay> displays = toDisplays(asList(body.get("displayData")));
        List<JulyMetadataService> services = toServices(asList(body.get("serviceData")));

        String objectType = text(metaData.get("objectType"));
        String description = text(metaData.get("description"));
        String businessField = text(metaData.get("businessField"));
        fields = normalizeBusinessField(fields, businessField);
        String packageName = text(metaData.get("packageName"));
        String routerPath = text(metaData.get("routerPath"));
        String remark = text(metaData.get("remark"));
        Integer sortOrder = integer(metaData.get("sortOrder"));

        JulyMetadata existing = repository.findByObjectName(objectName);

        if (existing == null) {
            return metadataUseCase.insert(objectName, sortOrder, objectType, description, businessField, packageName,
                    routerPath, remark, fields, displays, services);
        }

        return metadataUseCase.update(existing.id().value(), objectType, description, businessField, packageName,
                routerPath, sortOrder, null, remark, fields, displays, services);
    }

    /**
     * Preview the publish DDL of a model.
     *
     * @param objectName object name
     * @return CREATE TABLE statement
     */
    public String previewDdl(String objectName) {
        JulyMetadata metadata = metadataUseCase.getByObjectName(objectName);

        try {
            return ddlGenerator.generateCreate(TABLE_PREFIX + metadata.objectName(), metadata.description(),
                    metadata.fields(), metadata.businessField());
        } catch (IllegalArgumentException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }
    }

    /**
     * Build the legacy MetaDTO shape from the aggregate.
     *
     * @param metadata aggregate
     * @return MetaDTO map
     */
    private Map<String, Object> toMetaDto(JulyMetadata metadata) {
        Map<String, Object> metaData = new LinkedHashMap<>();
        metaData.put("objectName", metadata.objectName());
        metaData.put("objectType", metadata.objectType());
        metaData.put("description", metadata.description());
        metaData.put("businessField", metadata.businessField());
        metaData.put("packageName", metadata.packageName());
        metaData.put("routerPath", metadata.routerPath());
        metaData.put("remark", metadata.remark());
        metaData.put("sortOrder", metadata.sortOrder());
        metaData.put("publishStatus", "draft");
        metaData.put("version", "");

        List<Map<String, Object>> fieldData = new ArrayList<>();
        for (JulyMetadataField field : metadata.fields()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("code", field.fieldCode());
            row.put("name", field.fieldName());
            row.put("fieldType", field.fieldType());
            row.put("length", field.fieldLength());
            row.put("notNull", field.requiredField());
            row.put("defaultValue", field.defaultValue());
            row.put("sort", field.sortOrder());
            fieldData.add(row);
        }

        List<Map<String, Object>> displayData = new ArrayList<>();
        for (JulyMetadataDisplay display : metadata.displays()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("code", display.displayCode());
            row.put("name", display.displayName());
            row.put("align", display.align());
            row.put("width", display.width());
            row.put("componentType", display.componentType());
            row.put("displayType", display.displayType());
            row.put("param011", display.param011());
            row.put("sort", display.sortOrder());
            displayData.add(row);
        }

        List<Map<String, Object>> serviceData = new ArrayList<>();
        for (JulyMetadataService service : metadata.services()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("code", service.serviceCode());
            row.put("name", service.serviceName());
            row.put("description", service.serviceDescription());
            row.put("objectType", service.objectType());
            row.put("paramType", service.paramType());
            row.put("serviceContent", service.serviceContent());
            row.put("enabled", service.enabled());
            row.put("sort", service.sortOrder());
            serviceData.add(row);
        }

        Map<String, Object> dto = new LinkedHashMap<>();
        dto.put("metaData", metaData);
        dto.put("fieldData", fieldData);
        dto.put("displayData", displayData);
        dto.put("serviceData", serviceData);

        return dto;
    }

    /**
     * Force the business field to the platform-fixed shape: string, length 33
     * (the business unique key, aligned with the surrogate id column).
     *
     * @param fields        parsed fields
     * @param businessField business field code, nullable
     * @return normalized fields
     */
    private List<JulyMetadataField> normalizeBusinessField(List<JulyMetadataField> fields, String businessField) {
        if (businessField == null || businessField.isBlank()) {
            return fields;
        }

        List<JulyMetadataField> normalized = new ArrayList<>();

        for (JulyMetadataField field : fields) {
            if (field.fieldCode() != null && field.fieldCode().equalsIgnoreCase(businessField)) {
                normalized.add(new JulyMetadataField(field.fieldCode(), field.fieldName(), "string", 33,
                        field.requiredField(), field.defaultValue(), field.sortOrder()));
            } else {
                normalized.add(field);
            }
        }

        return normalized;
    }

    /**
     * Map fieldData rows to field records.
     *
     * @param rows raw rows
     * @return field records
     */
    private List<JulyMetadataField> toFields(List<Map<String, Object>> rows) {
        List<JulyMetadataField> fields = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            fields.add(new JulyMetadataField(text(row.get("code")), text(row.get("name")),
                    text(row.get("fieldType")), number(row.get("length")), bool(row.get("notNull")),
                    text(row.get("defaultValue")), integer(row.get("sort"))));
        }

        return fields;
    }

    /**
     * Map displayData rows to display records.
     *
     * @param rows raw rows
     * @return display records
     */
    private List<JulyMetadataDisplay> toDisplays(List<Map<String, Object>> rows) {
        List<JulyMetadataDisplay> displays = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            String componentType = text(row.get("componentType"));
            displays.add(new JulyMetadataDisplay(text(row.get("code")), text(row.get("name")),
                    defaultIfBlank(text(row.get("align")), "left"), number(row.get("width")),
                    defaultIfBlank(componentType, "input"), defaultIfBlank(text(row.get("displayType")), "all"),
                    text(row.get("param011")), integer(row.get("sort"))));
        }

        return displays;
    }

    /**
     * Map serviceData rows to service records.
     *
     * @param rows raw rows
     * @return service records
     */
    private List<JulyMetadataService> toServices(List<Map<String, Object>> rows) {
        List<JulyMetadataService> services = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            services.add(new JulyMetadataService(text(row.get("code")), text(row.get("name")),
                    defaultIfBlank(text(row.get("description")), ""), text(row.get("objectType")),
                    text(row.get("paramType")), text(row.get("serviceContent")), bool(row.get("enabled")),
                    integer(row.get("sort"))));
        }

        return services;
    }

    /**
     * Cast a raw value to a map.
     *
     * @param value raw value
     * @return map or null
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        return value instanceof Map ? (Map<String, Object>) value : null;
    }

    /**
     * Cast a raw value to a list of maps.
     *
     * @param value raw value
     * @return list, never null
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> asList(Object value) {
        return value instanceof List ? (List<Map<String, Object>>) value : List.of();
    }

    /**
     * Read a string value.
     *
     * @param value raw value
     * @return string or null
     */
    private String text(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    /**
     * Read an int value.
     *
     * @param value raw value
     * @return int, 0 when absent
     */
    private int number(Object value) {
        return value instanceof Number ? ((Number) value).intValue() : 0;
    }

    /**
     * Read a nullable integer value.
     *
     * @param value raw value
     * @return integer or null
     */
    private Integer integer(Object value) {
        return value instanceof Number ? ((Number) value).intValue() : null;
    }

    /**
     * Read a boolean value.
     *
     * @param value raw value
     * @return boolean
     */
    private boolean bool(Object value) {
        return value instanceof Boolean ? (Boolean) value : "true".equalsIgnoreCase(String.valueOf(value));
    }

    /**
     * Fall back to a default when blank.
     *
     * @param value        raw value
     * @param fallback     default
     * @return value or default
     */
    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
