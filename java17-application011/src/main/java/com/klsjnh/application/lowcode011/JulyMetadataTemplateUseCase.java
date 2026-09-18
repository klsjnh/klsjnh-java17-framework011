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
import com.klsjnh.domain.lowcode011.records.ResultKey011;
import com.klsjnh.domain.lowcode011.JulyMetadataDisplay;
import com.klsjnh.domain.lowcode011.JulyMetadataField;
import com.klsjnh.domain.lowcode011.JulyMetadataRepository;
import com.klsjnh.domain.lowcode011.JulyMetadataService;
import com.klsjnh.domain.lowcode011.JulyMetadataSource;
import com.klsjnh.domain.lowcode011.MetadataDdlExecutorPort;
import com.klsjnh.domain.lowcode011.MetadataDdlGeneratorPort;
import com.klsjnh.domain.lowcode011.TemplateCodec;
import com.klsjnh.domain.lowcode011.enums.FieldType011;
import com.klsjnh.domain.lowcode011.enums.ObjectType011;
import com.klsjnh.domain.lowcode011.enums.TemplateFormat011;
import com.klsjnh.domain.lowcode011.records.BaseColumn011;
import com.klsjnh.domain.lowcode011.records.MetaDtoKey011;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
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
     * Template codecs by format.
     */
    private final Map<TemplateFormat011, TemplateCodec> codecs = new EnumMap<>(TemplateFormat011.class);

    /**
     * Create the use case.
     *
     * @param metadataUseCase metadata CRUD use case
     * @param repository      metadata repository
     * @param designerUseCase designer use case
     * @param publishUseCase  publish use case
     * @param ddlGenerator    ddl generator
     * @param ddlExecutor     ddl executor
     * @param codecs          template codecs (Spring-collected, one per format)
     */
    public JulyMetadataTemplateUseCase(JulyMetadataUseCase metadataUseCase, JulyMetadataRepository repository,
            JulyMetadataDesignerUseCase designerUseCase, JulyMetadataPublishUseCase publishUseCase,
            MetadataDdlGeneratorPort ddlGenerator, MetadataDdlExecutorPort ddlExecutor, List<TemplateCodec> codecs) {
        this.metadataUseCase = metadataUseCase;
        this.repository = repository;
        this.designerUseCase = designerUseCase;
        this.publishUseCase = publishUseCase;
        this.ddlGenerator = ddlGenerator;
        this.ddlExecutor = ddlExecutor;

        for (TemplateCodec codec : codecs) {
            this.codecs.put(codec.format(), codec);
        }
    }

    /**
     * Export a template value to file bytes in the requested format.
     *
     * @param template template value
     * @param format   format code (json / csv / xlsx), blank for json
     * @return file bytes
     */
    public byte[] exportFile(Map<String, Object> template, String format) {
        try {
            return codec(resolveFormat(format)).exportTemplate(template);
        } catch (UnsupportedOperationException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }
    }

    /**
     * Export an existing object as a template file in the requested format.
     *
     * @param objectName object name
     * @param format     format code, blank for json
     * @return file bytes
     */
    public byte[] downloadFile(String objectName, String format) {
        return exportFile(downloadTemplate(objectName), format);
    }

    /**
     * Export the default skeleton as a template file in the requested format.
     *
     * @param format format code, blank for json
     * @return file bytes
     */
    public byte[] blankFile(String format) {
        return exportFile(blankTemplate(), format);
    }

    /**
     * Parse an uploaded template file into the MetaDTO value, detecting the
     * format from the explicit code or the file extension.
     *
     * @param data     file bytes
     * @param filename original filename, nullable
     * @param format   explicit format code, nullable
     * @return template value
     */
    public Map<String, Object> parseFile(byte[] data, String filename, String format) {
        TemplateCodec codec = codec(resolveFormat(filename, format));

        try {
            return codec.importTemplate(data);
        } catch (IllegalArgumentException | UnsupportedOperationException ex) {
            throw BusinessException.badRequest(ex.getMessage());
        }
    }

    /**
     * Resolve a format from an explicit code only.
     *
     * @param format format code, nullable
     * @return format, json by default
     */
    private TemplateFormat011 resolveFormat(String format) {
        TemplateFormat011 resolved = TemplateFormat011.fromString(format);

        return resolved == null ? TemplateFormat011.JSON : resolved;
    }

    /**
     * Resolve a format from an explicit code, else the filename extension, else
     * json.
     *
     * @param filename filename, nullable
     * @param format   format code, nullable
     * @return format
     */
    private TemplateFormat011 resolveFormat(String filename, String format) {
        TemplateFormat011 resolved = TemplateFormat011.fromString(format);

        if (resolved != null) {
            return resolved;
        }

        if (filename != null) {
            int dot = filename.lastIndexOf('.');
            resolved = dot < 0 ? null : TemplateFormat011.fromString(filename.substring(dot + 1));
            if (resolved != null) {
                return resolved;
            }
        }

        return TemplateFormat011.JSON;
    }

    /**
     * Look up the codec of a format.
     *
     * @param format format
     * @return codec
     */
    private TemplateCodec codec(TemplateFormat011 format) {
        TemplateCodec codec = codecs.get(format);

        if (codec == null) {
            throw BusinessException.badRequest("unsupported template format: " + format.getCode());
        }

        return codec;
    }

    /**
     * Default annotated skeleton (valid JSON: guidance lives in {@code _guide}).
     *
     * @return skeleton template
     */
    public Map<String, Object> blankTemplate() {
        Map<String, Object> guide = new LinkedHashMap<>();
        guide.put(MetaDtoKey011.OBJECT_NAME, "lowercase [a-z][a-z0-9_]{0,49}; physical table = <objectName>");
        guide.put(MetaDtoKey011.BUSINESS_FIELD, "required business key; its field name is free (sid is NOT special/required); forced "
                + "string(33); becomes the sync unique key. AI may fall back to \"id\" when it cannot pick one");
        guide.put("baseColumns", "platform base columns (allowed once, type is normalized): " + BASE_COLUMNS
                + "; do not add another field with the same code (case-insensitive)");
        guide.put("pkColumns", "codes starting with pk_ are forced string(33)");
        guide.put(MetaDtoKey011.FIELD_TYPE, "one of FieldType011: " + codes(FieldType011.values()));
        guide.put(MetaDtoKey011.OBJECT_TYPE, "one of ObjectType011: " + codes(ObjectType011.values()));
        guide.put("flow", "uploadTemplate011 (review) then deployObject (save + publish)");

        Map<String, Object> metaData = new LinkedHashMap<>();
        metaData.put(MetaDtoKey011.OBJECT_NAME, "demo_object");
        metaData.put(MetaDtoKey011.OBJECT_TYPE, "type011");
        metaData.put(MetaDtoKey011.DESCRIPTION, "demo description");
        metaData.put(MetaDtoKey011.BUSINESS_FIELD, "order_no");
        metaData.put(MetaDtoKey011.ROUTER_PATH, "/runtime/demo_object");
        metaData.put(MetaDtoKey011.SOURCE, sourceMap(null, null));

        Map<String, Object> field = new LinkedHashMap<>();
        field.put(MetaDtoKey011.CODE, "order_no");
        field.put(MetaDtoKey011.NAME, "business key");
        field.put(MetaDtoKey011.FIELD_TYPE, "string");
        field.put(MetaDtoKey011.LENGTH, 33);
        field.put(MetaDtoKey011.NOT_NULL, true);
        field.put(MetaDtoKey011.SORT, 1);

        Map<String, Object> display = new LinkedHashMap<>();
        display.put(MetaDtoKey011.CODE, "order_no");
        display.put(MetaDtoKey011.NAME, "business key");
        display.put(MetaDtoKey011.COMPONENT_TYPE, "input");
        display.put(MetaDtoKey011.DISPLAY_TYPE, "all");
        display.put(MetaDtoKey011.SORT, 1);

        Map<String, Object> service = new LinkedHashMap<>();
        service.put(MetaDtoKey011.CODE, "query");
        service.put(MetaDtoKey011.NAME, "query");
        service.put(MetaDtoKey011.OBJECT_TYPE, "type011");
        service.put(MetaDtoKey011.PARAM_TYPE, "query");
        service.put(MetaDtoKey011.ENABLED, true);
        service.put(MetaDtoKey011.SORT, 1);

        Map<String, Object> template = new LinkedHashMap<>();
        template.put(MetaDtoKey011.GUIDE, guide);
        template.put(MetaDtoKey011.TEMPLATE_VERSION, TEMPLATE_VERSION);
        template.put(MetaDtoKey011.META_DATA, metaData);
        template.put(MetaDtoKey011.FIELD_DATA, List.of(field));
        template.put(MetaDtoKey011.DISPLAY_DATA, List.of(display));
        template.put(MetaDtoKey011.SERVICE_DATA, List.of(service));

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
            Map<String, Object> metaData = (Map<String, Object>) template.get(MetaDtoKey011.META_DATA);
            metaData.put(MetaDtoKey011.SOURCE, sourceMap(source.dataSourceCode(), source.probeSql()));
        }

        template.put(MetaDtoKey011.TEMPLATE_VERSION, TEMPLATE_VERSION);

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
        report.put(ResultKey011.VALID, errors.isEmpty());
        report.put(ResultKey011.ERRORS, errors);
        report.put(ResultKey011.WARNINGS, warnings);

        if (errors.isEmpty()) {
            Map<String, Object> metaData = asMap(template.get(MetaDtoKey011.META_DATA));
            String objectName = text(metaData.get(MetaDtoKey011.OBJECT_NAME));
            List<JulyMetadataField> fields = toFields(asList(template.get(MetaDtoKey011.FIELD_DATA)));
            String table = objectName;
            boolean exists = ddlExecutor.tableExists(table);
            report.put(ResultKey011.PLAN, exists ? "alter" : "create");
            try {
                report.put(ResultKey011.PREVIEW_DDL, ddlGenerator.generateCreate(table, text(metaData.get(MetaDtoKey011.DESCRIPTION)), fields,
                        text(metaData.get(MetaDtoKey011.BUSINESS_FIELD))));
            } catch (IllegalArgumentException ex) {
                report.put(ResultKey011.VALID, false);
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
            result.put(MetaDtoKey011.OBJECT_NAME, objectName);
            result.put(ResultKey011.SAVED, false);
            result.put(ResultKey011.PUBLISHED, true);
            result.put(ResultKey011.VERSION, published.get(ResultKey011.VERSION));
            result.put(ResultKey011.PHYSICAL_TABLE, published.get(ResultKey011.PHYSICAL_TABLE));
            return result;
        }

        List<String> errors = validate(template);

        if (!errors.isEmpty()) {
            throw BusinessException.badRequest("template invalid: " + String.join("; ", errors));
        }

        Map<String, Object> metaData = asMap(template.get(MetaDtoKey011.META_DATA));
        String name = text(metaData.get(MetaDtoKey011.OBJECT_NAME));
        JulyMetadata existing = repository.findByObjectName(name);

        if (existing != null && !overwrite) {
            throw BusinessException.badRequest("object exists: " + name + " (set overwrite=true to replace)");
        }

        List<JulyMetadataField> fields = toFields(asList(template.get(MetaDtoKey011.FIELD_DATA)));
        List<JulyMetadataDisplay> displays = toDisplays(asList(template.get(MetaDtoKey011.DISPLAY_DATA)));
        List<JulyMetadataService> services = toServices(asList(template.get(MetaDtoKey011.SERVICE_DATA)));

        String objectType = text(metaData.get(MetaDtoKey011.OBJECT_TYPE));
        String description = text(metaData.get(MetaDtoKey011.DESCRIPTION));
        String businessField = text(metaData.get(MetaDtoKey011.BUSINESS_FIELD));
        String packageName = text(metaData.get(MetaDtoKey011.PACKAGE_NAME));
        String routerPath = text(metaData.get(MetaDtoKey011.ROUTER_PATH));
        String remark = text(metaData.get(MetaDtoKey011.REMARK));
        Integer sortOrder = integer(metaData.get(MetaDtoKey011.SORT_ORDER));

        String id = existing == null
                ? metadataUseCase.insert(name, sortOrder, objectType, description, businessField, packageName,
                        routerPath, remark, fields, displays, services)
                : metadataUseCase.update(existing.id().value(), objectType, description, businessField, packageName,
                        routerPath, sortOrder, null, remark, fields, displays, services);

        Map<String, Object> source = asMap(metaData.get(MetaDtoKey011.SOURCE));

        if (source != null) {
            repository.updateSource(id, text(source.get(MetaDtoKey011.DATA_SOURCE_CODE)), text(source.get(MetaDtoKey011.PROBE_SQL)));
        }

        Map<String, Object> published = publishUseCase.publish(name, false, false);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put(MetaDtoKey011.OBJECT_NAME, name);
        result.put(ResultKey011.SAVED, true);
        result.put(ResultKey011.PUBLISHED, true);
        result.put(ResultKey011.VERSION, published.get(ResultKey011.VERSION));
        result.put(ResultKey011.PHYSICAL_TABLE, published.get(ResultKey011.PHYSICAL_TABLE));
        result.put(ResultKey011.DDL, published.get(ResultKey011.DDL));

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

        String version = text(template.get(MetaDtoKey011.TEMPLATE_VERSION));

        if (version != null && !TEMPLATE_VERSION.equals(version)) {
            errors.add("unsupported templateVersion: " + version);
        }

        Map<String, Object> metaData = asMap(template.get(MetaDtoKey011.META_DATA));

        if (metaData == null) {
            errors.add("metaData is required");
            return errors;
        }

        String objectName = text(metaData.get(MetaDtoKey011.OBJECT_NAME));

        if (objectName == null || !OBJECT_NAME.matcher(objectName).matches()) {
            errors.add("objectName invalid (^[a-z][a-z0-9_]{0,49}$): " + objectName);
        }

        String objectType = text(metaData.get(MetaDtoKey011.OBJECT_TYPE));

        if (objectType != null && ObjectType011.fromString(objectType) == null) {
            errors.add("unknown objectType: " + objectType);
        }

        List<Map<String, Object>> fieldData = asList(template.get(MetaDtoKey011.FIELD_DATA));

        if (fieldData.isEmpty()) {
            errors.add("fieldData is empty");
        }

        Set<String> codes = new LinkedHashSet<>();
        String businessField = text(metaData.get(MetaDtoKey011.BUSINESS_FIELD));
        Map<String, Object> source = asMap(metaData.get(MetaDtoKey011.SOURCE));
        String sourceKind = source == null ? null : text(source.get(MetaDtoKey011.KIND));
        boolean businessFieldBlank = businessField == null || businessField.isBlank();
        boolean aiIdFallback = "ai".equals(sourceKind) && !businessFieldBlank && "id".equalsIgnoreCase(businessField);

        if (businessFieldBlank && !"ai".equals(sourceKind)) {
            errors.add("businessField required (business key)");
        }

        for (Map<String, Object> field : fieldData) {
            String code = text(field.get(MetaDtoKey011.CODE));
            String lower = code == null ? "" : code.toLowerCase(Locale.ROOT);

            if (code == null || code.isBlank()) {
                errors.add("field code is required");
                continue;
            }

            if (BASE_COLUMNS.contains(lower)) {
                field.put(MetaDtoKey011.FIELD_TYPE, baseFieldType(lower));
                field.put(MetaDtoKey011.LENGTH, baseLength(lower));
            }

            if (!codes.add(lower)) {
                errors.add("duplicate field code (case-insensitive): " + code);
            }

            if (FieldType011.fromString(text(field.get(MetaDtoKey011.FIELD_TYPE))) == null) {
                errors.add("unknown fieldType: " + field.get(MetaDtoKey011.FIELD_TYPE) + " (" + code + ")");
            }

            if (lower.startsWith("pk_") || (!businessFieldBlank && !BASE_COLUMNS.contains(lower)
                    && lower.equals(businessField.toLowerCase(Locale.ROOT)))) {
                field.put(MetaDtoKey011.FIELD_TYPE, "string");
                field.put(MetaDtoKey011.LENGTH, 33);
            }
        }

        if (!businessFieldBlank && !aiIdFallback && !codes.contains(businessField.toLowerCase(Locale.ROOT))) {
            errors.add("businessField not present in fieldData: " + businessField);
        }

        if (!businessFieldBlank && BASE_COLUMNS.contains(businessField.toLowerCase(Locale.ROOT)) && !aiIdFallback) {
            errors.add("businessField cannot be a base column: " + businessField);
        }

        for (Map<String, Object> display : asList(template.get(MetaDtoKey011.DISPLAY_DATA))) {
            String code = text(display.get(MetaDtoKey011.CODE));

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
            fields.add(new JulyMetadataField(text(row.get(MetaDtoKey011.CODE)), text(row.get(MetaDtoKey011.NAME)), text(row.get(MetaDtoKey011.FIELD_TYPE)),
                    number(row.get(MetaDtoKey011.LENGTH)), bool(row.get(MetaDtoKey011.NOT_NULL)), text(row.get(MetaDtoKey011.DEFAULT_VALUE)),
                    integer(row.get(MetaDtoKey011.SORT))));
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
            displays.add(new JulyMetadataDisplay(text(row.get(MetaDtoKey011.CODE)), text(row.get(MetaDtoKey011.NAME)),
                    defaultIfBlank(text(row.get(MetaDtoKey011.ALIGN)), "left"), number(row.get(MetaDtoKey011.WIDTH)),
                    defaultIfBlank(text(row.get(MetaDtoKey011.COMPONENT_TYPE)), "input"),
                    defaultIfBlank(text(row.get(MetaDtoKey011.DISPLAY_TYPE)), "all"), text(row.get(MetaDtoKey011.PARAM011)),
                    integer(row.get(MetaDtoKey011.SORT))));
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
            services.add(new JulyMetadataService(text(row.get(MetaDtoKey011.CODE)), text(row.get(MetaDtoKey011.NAME)),
                    defaultIfBlank(text(row.get(MetaDtoKey011.DESCRIPTION)), ""), text(row.get(MetaDtoKey011.OBJECT_TYPE)),
                    text(row.get(MetaDtoKey011.PARAM_TYPE)), text(row.get(MetaDtoKey011.SERVICE_CONTENT)), bool(row.get(MetaDtoKey011.ENABLED)),
                    integer(row.get(MetaDtoKey011.SORT))));
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
        source.put(MetaDtoKey011.DATA_SOURCE_CODE, dataSourceCode);
        source.put(MetaDtoKey011.PROBE_SQL, probeSql);

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
