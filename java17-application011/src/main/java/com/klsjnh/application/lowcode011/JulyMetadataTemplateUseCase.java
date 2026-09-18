package com.klsjnh.application.lowcode011;

/*                JulyMetadataTemplateUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  metadata template use case class
 *
 */

import com.klsjnh.common.exception.BusinessException;

import com.klsjnh.domain.lowcode011.JulyMetadata;
import com.klsjnh.domain.lowcode011.JulyMetadataDisplay;
import com.klsjnh.domain.lowcode011.JulyMetadataField;
import com.klsjnh.domain.lowcode011.JulyMetadataRepository;
import com.klsjnh.domain.lowcode011.JulyMetadataService;
import com.klsjnh.domain.lowcode011.JulyMetadataSource;
import com.klsjnh.domain.lowcode011.MetadataDdlExecutorPort;
import com.klsjnh.domain.lowcode011.MetadataDdlGeneratorPort;
import com.klsjnh.domain.lowcode011.enums.FieldType011;
import com.klsjnh.domain.lowcode011.enums.ObjectType011;
import com.klsjnh.domain.lowcode011.records.BaseColumn011;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Metadata template use case: default skeleton, export of an object, review of
 * an uploaded template (validate + preview, no persistence) and deployment
 * (validate + save + publish). The template is a self-contained JSON value
 * (MetaDTO + source + templateVersion).
 */

@Service
public class JulyMetadataTemplateUseCase {

    /**
     * Supported template version.
     */
    private static final String TEMPLATE_VERSION = "1.0";

    /**
     * Physical table prefix.
     */
    private static final String TABLE_PREFIX = "lc_";

    /**
     * Object name shape.
     */
    private static final Pattern OBJECT_NAME = Pattern.compile("^[a-z][a-z0-9_]{0,49}$");

    /**
     * Platform base columns: business fields may never reuse these codes.
     */
    private static final Set<String> BASE_COLUMNS = BaseColumn011.ALL;

    /**
     * Metadata CRUD use case.
     */
    private final JulyMetadataUseCase metadataUseCase;

    /**
     * Metadata repository (existence / source).
     */
    private final JulyMetadataRepository repository;

    /**
     * Designer use case (export / load).
     */
    private final JulyMetadataDesignerUseCase designerUseCase;

    /**
     * Publish use case.
     */
    private final JulyMetadataPublishUseCase publishUseCase;

    /**
     * DDL generator (preview of an unsaved template).
     */
    private final MetadataDdlGeneratorPort ddlGenerator;

    /**
     * DDL executor (table existence for the plan).
     */
    private final MetadataDdlExecutorPort ddlExecutor;

    /**
     * Create the use case.
     *
     * @param metadataUseCase metadata CRUD use case
     * @param repository      metadata repository
     * @param designerUseCase designer use case
     * @param publishUseCase  publish use case
     * @param ddlGenerator    ddl generator
     * @param ddlExecutor     ddl executor
     */
    public JulyMetadataTemplateUseCase(JulyMetadataUseCase metadataUseCase, JulyMetadataRepository repository,
            JulyMetadataDesignerUseCase designerUseCase, JulyMetadataPublishUseCase publishUseCase,
            MetadataDdlGeneratorPort ddlGenerator, MetadataDdlExecutorPort ddlExecutor) {
        this.metadataUseCase = metadataUseCase;
        this.repository = repository;
        this.designerUseCase = designerUseCase;
        this.publishUseCase = publishUseCase;
        this.ddlGenerator = ddlGenerator;
        this.ddlExecutor = ddlExecutor;
    }

    /**
     * Default annotated skeleton (valid JSON: guidance lives in {@code _guide}).
     *
     * @return skeleton template
     */
    public Map<String, Object> blankTemplate() {
        Map<String, Object> guide = new LinkedHashMap<>();
        guide.put("objectName", "lowercase [a-z][a-z0-9_]{0,49}; physical table = lc_<objectName>");
        guide.put("businessField", "must exist in fieldData; forced string(33); becomes the sync unique key");
        guide.put("baseColumns", "platform base columns (allowed once, type is normalized): " + BASE_COLUMNS
                + "; do not add another field with the same code (case-insensitive)");
        guide.put("pkColumns", "codes starting with pk_ are forced string(33)");
        guide.put("fieldType", "one of FieldType011: " + codes(FieldType011.values()));
        guide.put("objectType", "one of ObjectType011: " + codes(ObjectType011.values()));
        guide.put("flow", "uploadTemplate011 (review) then deployObject (save + publish)");

        Map<String, Object> metaData = new LinkedHashMap<>();
        metaData.put("objectName", "demo_object");
        metaData.put("objectType", "type011");
        metaData.put("description", "demo description");
        metaData.put("businessField", "sid");
        metaData.put("routerPath", "/runtime/demo_object");
        metaData.put("source", sourceMap(null, null));

        Map<String, Object> field = new LinkedHashMap<>();
        field.put("code", "sid");
        field.put("name", "business id");
        field.put("fieldType", "string");
        field.put("length", 33);
        field.put("notNull", true);
        field.put("sort", 1);

        Map<String, Object> display = new LinkedHashMap<>();
        display.put("code", "sid");
        display.put("name", "business id");
        display.put("componentType", "input");
        display.put("displayType", "all");
        display.put("sort", 1);

        Map<String, Object> service = new LinkedHashMap<>();
        service.put("code", "query");
        service.put("name", "query");
        service.put("objectType", "type011");
        service.put("paramType", "query");
        service.put("enabled", true);
        service.put("sort", 1);

        Map<String, Object> template = new LinkedHashMap<>();
        template.put("_guide", guide);
        template.put("templateVersion", TEMPLATE_VERSION);
        template.put("metaData", metaData);
        template.put("fieldData", List.of(field));
        template.put("displayData", List.of(display));
        template.put("serviceData", List.of(service));

        return template;
    }

    /**
     * Export an existing object as a template.
     *
     * @param objectName object name
     * @return template
     */
    public Map<String, Object> downloadTemplate(String objectName) {
        Map<String, Object> template = new LinkedHashMap<>(designerUseCase.load(objectName));
        JulyMetadataSource source = repository.findSource(objectName);

        if (source != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> metaData = (Map<String, Object>) template.get("metaData");
            metaData.put("source", sourceMap(source.dataSourceCode(), source.probeSql()));
        }

        template.put("templateVersion", TEMPLATE_VERSION);

        return template;
    }

    /**
     * Review an uploaded template: validate + preview, nothing persisted.
     *
     * @param template template value
     * @return review report
     */
    public Map<String, Object> reviewTemplate(Map<String, Object> template) {
        List<String> errors = validate(template);
        List<String> warnings = new ArrayList<>();
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("valid", errors.isEmpty());
        report.put("errors", errors);
        report.put("warnings", warnings);

        if (errors.isEmpty()) {
            Map<String, Object> metaData = asMap(template.get("metaData"));
            String objectName = text(metaData.get("objectName"));
            List<JulyMetadataField> fields = toFields(asList(template.get("fieldData")));
            String table = TABLE_PREFIX + objectName;
            boolean exists = ddlExecutor.tableExists(table);
            report.put("plan", exists ? "alter" : "create");
            try {
                report.put("previewDdl", ddlGenerator.generateCreate(table, text(metaData.get("description")), fields,
                        text(metaData.get("businessField"))));
            } catch (IllegalArgumentException ex) {
                report.put("valid", false);
                errors.add(ex.getMessage());
            }
        }

        return report;
    }

    /**
     * Deploy a template: validate → save (insert / update) → publish.
     *
     * @param template  template value, nullable when only objectName is given
     * @param objectName object name, used when no template is supplied
     * @param overwrite whether an existing object may be replaced
     * @return deploy result
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> deployObject(Map<String, Object> template, String objectName, boolean overwrite) {
        if (template == null || template.isEmpty()) {
            if (objectName == null || objectName.isBlank()) {
                throw BusinessException.badRequest("template or objectName required");
            }
            Map<String, Object> published = publishUseCase.publish(objectName, false, false);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("objectName", objectName);
            result.put("saved", false);
            result.put("published", true);
            result.put("version", published.get("version"));
            result.put("physicalTable", published.get("physicalTable"));
            return result;
        }

        List<String> errors = validate(template);

        if (!errors.isEmpty()) {
            throw BusinessException.badRequest("template invalid: " + String.join("; ", errors));
        }

        Map<String, Object> metaData = asMap(template.get("metaData"));
        String name = text(metaData.get("objectName"));
        JulyMetadata existing = repository.findByObjectName(name);

        if (existing != null && !overwrite) {
            throw BusinessException.badRequest("object exists: " + name + " (set overwrite=true to replace)");
        }

        List<JulyMetadataField> fields = toFields(asList(template.get("fieldData")));
        List<JulyMetadataDisplay> displays = toDisplays(asList(template.get("displayData")));
        List<JulyMetadataService> services = toServices(asList(template.get("serviceData")));

        String objectType = text(metaData.get("objectType"));
        String description = text(metaData.get("description"));
        String businessField = text(metaData.get("businessField"));
        String packageName = text(metaData.get("packageName"));
        String routerPath = text(metaData.get("routerPath"));
        String remark = text(metaData.get("remark"));
        Integer sortOrder = integer(metaData.get("sortOrder"));

        String id = existing == null
                ? metadataUseCase.insert(name, sortOrder, objectType, description, businessField, packageName,
                        routerPath, remark, fields, displays, services)
                : metadataUseCase.update(existing.id().value(), objectType, description, businessField, packageName,
                        routerPath, sortOrder, null, remark, fields, displays, services);

        Map<String, Object> source = asMap(metaData.get("source"));

        if (source != null) {
            repository.updateSource(id, text(source.get("dataSourceCode")), text(source.get("probeSql")));
        }

        Map<String, Object> published = publishUseCase.publish(name, false, false);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("objectName", name);
        result.put("saved", true);
        result.put("published", true);
        result.put("version", published.get("version"));
        result.put("physicalTable", published.get("physicalTable"));
        result.put("ddl", published.get("ddl"));

        return result;
    }

    /**
     * Validate a template and normalize the business / pk columns in place.
     *
     * @param template template value
     * @return errors, empty when valid
     */
    private List<String> validate(Map<String, Object> template) {
        List<String> errors = new ArrayList<>();

        if (template == null) {
            errors.add("template is required");
            return errors;
        }

        String version = text(template.get("templateVersion"));

        if (version != null && !TEMPLATE_VERSION.equals(version)) {
            errors.add("unsupported templateVersion: " + version);
        }

        Map<String, Object> metaData = asMap(template.get("metaData"));

        if (metaData == null) {
            errors.add("metaData is required");
            return errors;
        }

        String objectName = text(metaData.get("objectName"));

        if (objectName == null || !OBJECT_NAME.matcher(objectName).matches()) {
            errors.add("objectName invalid (^[a-z][a-z0-9_]{0,49}$): " + objectName);
        }

        String objectType = text(metaData.get("objectType"));

        if (objectType != null && ObjectType011.fromString(objectType) == null) {
            errors.add("unknown objectType: " + objectType);
        }

        List<Map<String, Object>> fieldData = asList(template.get("fieldData"));

        if (fieldData.isEmpty()) {
            errors.add("fieldData is empty");
        }

        Set<String> codes = new LinkedHashSet<>();
        String businessField = text(metaData.get("businessField"));

        for (Map<String, Object> field : fieldData) {
            String code = text(field.get("code"));
            String lower = code == null ? "" : code.toLowerCase(Locale.ROOT);

            if (code == null || code.isBlank()) {
                errors.add("field code is required");
                continue;
            }

            if (BASE_COLUMNS.contains(lower)) {
                field.put("fieldType", baseFieldType(lower));
                field.put("length", baseLength(lower));
            }

            if (!codes.add(lower)) {
                errors.add("duplicate field code (case-insensitive): " + code);
            }

            if (FieldType011.fromString(text(field.get("fieldType"))) == null) {
                errors.add("unknown fieldType: " + field.get("fieldType") + " (" + code + ")");
            }

            if (lower.startsWith("pk_") || (businessField != null && lower.equals(businessField.toLowerCase(Locale.ROOT)))) {
                field.put("fieldType", "string");
                field.put("length", 33);
            }
        }

        if (businessField != null && !codes.contains(businessField.toLowerCase(Locale.ROOT))) {
            errors.add("businessField not present in fieldData: " + businessField);
        }

        if (businessField != null && BASE_COLUMNS.contains(businessField.toLowerCase(Locale.ROOT))) {
            errors.add("businessField cannot be a base column: " + businessField);
        }

        for (Map<String, Object> display : asList(template.get("displayData"))) {
            String code = text(display.get("code"));

            if (code == null || !codes.contains(code.toLowerCase(Locale.ROOT))) {
                errors.add("display code not bound to a field: " + code);
            }
        }

        return errors;
    }

    /**
     * Canonical field type of a platform base column.
     *
     * @param base base column (lower case)
     * @return field type code
     */
    private String baseFieldType(String base) {
        switch (base) {
            case "id":
                return "id";
            case "create_by":
                return "create_by";
            case "update_by":
                return "update_by";
            case "create_time":
                return "create_time";
            case "update_time":
                return "update_time";
            default:
                return "status";
        }
    }

    /**
     * Canonical length of a platform base column.
     *
     * @param base base column (lower case)
     * @return length
     */
    private int baseLength(String base) {
        switch (base) {
            case "id":
            case "create_by":
            case "update_by":
                return 33;
            case "status":
            case "dr":
                return 3;
            default:
                return 0;
        }
    }

    /**
     * Map template field rows to field records.
     *
     * @param rows rows
     * @return fields
     */
    private List<JulyMetadataField> toFields(List<Map<String, Object>> rows) {
        List<JulyMetadataField> fields = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            fields.add(new JulyMetadataField(text(row.get("code")), text(row.get("name")), text(row.get("fieldType")),
                    number(row.get("length")), bool(row.get("notNull")), text(row.get("defaultValue")),
                    integer(row.get("sort"))));
        }

        return fields;
    }

    /**
     * Map template display rows to display records.
     *
     * @param rows rows
     * @return displays
     */
    private List<JulyMetadataDisplay> toDisplays(List<Map<String, Object>> rows) {
        List<JulyMetadataDisplay> displays = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            displays.add(new JulyMetadataDisplay(text(row.get("code")), text(row.get("name")),
                    defaultIfBlank(text(row.get("align")), "left"), number(row.get("width")),
                    defaultIfBlank(text(row.get("componentType")), "input"),
                    defaultIfBlank(text(row.get("displayType")), "all"), text(row.get("param011")),
                    integer(row.get("sort"))));
        }

        return displays;
    }

    /**
     * Map template service rows to service records.
     *
     * @param rows rows
     * @return services
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
     * Build the source map.
     *
     * @param dataSourceCode datasource code
     * @param probeSql       probe sql
     * @return source map
     */
    private Map<String, Object> sourceMap(String dataSourceCode, String probeSql) {
        Map<String, Object> source = new LinkedHashMap<>();
        source.put("dataSourceCode", dataSourceCode);
        source.put("probeSql", probeSql);

        return source;
    }

    /**
     * Join enum codes for the guide.
     *
     * @param values enum values
     * @return comma list
     */
    private String codes(Enum<?>[] values) {
        return String.join(",", Arrays.stream(values).map(v -> {
            try {
                return (String) v.getClass().getMethod("getCode").invoke(v);
            } catch (Exception ex) {
                return v.name();
            }
        }).toList());
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
     * @param value    raw value
     * @param fallback default
     * @return value or default
     */
    private String defaultIfBlank(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
