package com.klsjnh.domain.lowcode011.modeling;

/*                JulyBusinessModeling class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july business modeling class
 *      2026.09.17  own the shared MetadataContent contract (was MetaData011)
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.lowcode011.JulyMetadataField;
import com.klsjnh.domain.lowcode011.enums.FieldType011;
import com.klsjnh.domain.lowcode011.enums.ObjectType011;
import com.klsjnh.domain.lowcode011.records.MetadataContent;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * JulyBusinessModeling aggregate root (data service context): one take-out SQL
 * bound to one runtime datasource, plus the low-code description derived from
 * it. This is the definition source feeding the low-code context — the module
 * hands the description over and never builds a physical table itself.
 * <p>
 * The description is the shared contract {@link MetadataContent} (owned here as
 * the conforming downstream of lowcode011); mutations rebuild the immutable
 * content (copy-on-write).
 * </p>
 */

public class JulyBusinessModeling {

    /**
     * Max length of the modeling code.
     */
    private static final int CODE_MAX = 60;

    /**
     * Max length of the modeling name.
     */
    private static final int NAME_MAX = 100;

    /**
     * Max length of the datasource code.
     */
    private static final int DS_CODE_MAX = 60;

    /**
     * Max length of the remark.
     */
    private static final int REMARK_MAX = 300;

    /**
     * Primary key.
     */
    private final EntityId id;

    /**
     * Modeling code, unique and immutable after create.
     */
    private final String modelCode;

    /**
     * Modeling name, display only.
     */
    private String modelName;

    /**
     * Low-code description owned by this root (shared contract).
     */
    private MetadataContent content;

    /**
     * Datasource code the SQL runs against, mandatory.
     */
    private String dataSourceCode;

    /**
     * Take-out SQL, optional; read-only and single statement when present.
     */
    private String sqlContent;

    /**
     * Remark, optional.
     */
    private String remark;

    /**
     * Row status: '1' enabled / '0' disabled.
     */
    private String status;

    /**
     * Audit info.
     */
    private AuditInfo audit;

    /**
     * Full constructor, also the rehydration path from persistence.
     *
     * @param id             primary key
     * @param modelCode      modeling code, unique
     * @param modelName      modeling name
     * @param content        low-code description, required
     * @param dataSourceCode datasource code, required
     * @param sqlContent     take-out sql, optional
     * @param remark         remark, optional
     * @param status         row status
     * @param audit          audit info
     */
    public JulyBusinessModeling(EntityId id, String modelCode, String modelName, MetadataContent content,
            String dataSourceCode, String sqlContent, String remark, String status, AuditInfo audit) {
        validateBasics(modelCode, modelName, dataSourceCode, remark);
        validateContent(content);
        validateFieldTypes(content.fields());
        validateSql(sqlContent);

        this.id = id;
        this.modelCode = modelCode;
        this.modelName = modelName;
        this.content = content;
        this.dataSourceCode = dataSourceCode;
        this.sqlContent = StringUtil011.blankToNull(sqlContent);
        this.remark = remark;
        this.status = status == null ? Status011.ENABLED.getCode() : status;
        this.audit = audit == null ? AuditInfo.empty() : audit;
    }

    /**
     * Factory for a new modeling entry.
     *
     * @param id             primary key
     * @param modelCode      modeling code, required, max 60
     * @param modelName      modeling name, required, max 100
     * @param content        low-code description, required
     * @param dataSourceCode datasource code, required, max 60
     * @param sqlContent     take-out sql, optional, read-only when present
     * @param remark         remark, optional, max 300
     * @param audit          audit info
     * @return new aggregate
     */
    public static JulyBusinessModeling create(EntityId id, String modelCode, String modelName,
            MetadataContent content, String dataSourceCode, String sqlContent, String remark, AuditInfo audit) {
        return new JulyBusinessModeling(id, modelCode, modelName, content, dataSourceCode, sqlContent, remark,
                Status011.ENABLED.getCode(), audit);
    }

    /**
     * Update the mutable descriptive attributes; the modeling code and the
     * object name stay immutable.
     *
     * @param modelName      modeling name, required, max 100
     * @param dataSourceCode datasource code, required, max 60
     * @param sqlContent     take-out sql, optional, read-only when present
     * @param remark         remark, optional, max 300
     */
    public void updateBasics(String modelName, String dataSourceCode, String sqlContent, String remark) {
        validateBasics(this.modelCode, modelName, dataSourceCode, remark);
        validateSql(sqlContent);

        this.modelName = modelName;
        this.dataSourceCode = dataSourceCode;
        this.sqlContent = StringUtil011.blankToNull(sqlContent);
        this.remark = remark;
    }

    /**
     * Update the mutable attributes of the owned description; the object name
     * stays immutable.
     *
     * @param objectType        object type, nullable
     * @param objectDescription object description, max 300
     * @param businessField     business field mapping, max 300
     * @param packageName       target package name, max 300
     * @param routerPath        route path, max 300
     */
    public void updateMetaBasics(ObjectType011 objectType, String objectDescription, String businessField,
            String packageName, String routerPath) {
        this.content = rebuild(content.fields(), objectType == null ? null : objectType.getCode(), objectDescription,
                businessField, packageName, routerPath);
    }

    /**
     * Add a field definition (code unique, type known).
     *
     * @param field field definition to add, required
     */
    public void addField(JulyMetadataField field) {
        validateFieldType(field == null ? null : field.fieldType());

        List<JulyMetadataField> next = new ArrayList<>(content.fields());
        next.add(field);
        this.content = rebuild(next, content.objectType(), content.description(), content.businessField(),
                content.packageName(), content.routerPath());
    }

    /**
     * Update an existing field definition; the field code is immutable.
     *
     * @param fieldCode     field code to update
     * @param fieldName     new field name
     * @param fieldType     new field type, must be a known code
     * @param fieldLength   new maximum length, negative falls back to 0
     * @param requiredField whether the field is required
     * @param defaultValue  new default value, nullable
     */
    public void updateField(String fieldCode, String fieldName, String fieldType, int fieldLength,
            boolean requiredField, String defaultValue) {
        validateFieldType(fieldType);

        List<JulyMetadataField> next = new ArrayList<>();

        for (JulyMetadataField field : content.fields()) {
            if (field.fieldCode().equals(fieldCode)) {
                next.add(new JulyMetadataField(fieldCode, fieldName, fieldType, fieldLength, requiredField,
                        defaultValue, field.sortOrder()));
            } else {
                next.add(field);
            }
        }

        this.content = rebuild(next, content.objectType(), content.description(), content.businessField(),
                content.packageName(), content.routerPath());
    }

    /**
     * Remove a field definition by code.
     *
     * @param fieldCode field code to remove, required
     */
    public void removeField(String fieldCode) {
        List<JulyMetadataField> next = content.fields().stream()
                .filter(field -> !field.fieldCode().equals(fieldCode))
                .toList();

        this.content = rebuild(next, content.objectType(), content.description(), content.businessField(),
                content.packageName(), content.routerPath());
    }

    /**
     * Replace every field definition at once (re-probe overwrite), validated
     * before the swap.
     *
     * @param fields new field definitions, nullable for empty
     */
    public void replaceFields(List<JulyMetadataField> fields) {
        List<JulyMetadataField> incoming = nullToEmpty(fields);

        validateFieldTypes(incoming);
        validateUniqueCodes(incoming);

        this.content = rebuild(incoming, content.objectType(), content.description(), content.businessField(),
                content.packageName(), content.routerPath());
    }

    /**
     * Rebuild the immutable content with new fields / scalars.
     *
     * @param fields        new fields
     * @param objectType    object type code
     * @param description   description
     * @param businessField business field
     * @param packageName   package name
     * @param routerPath    route path
     * @return new content
     */
    private MetadataContent rebuild(List<JulyMetadataField> fields, String objectType, String description,
            String businessField, String packageName, String routerPath) {
        return new MetadataContent(content.objectName(), objectType, description, businessField, packageName,
                routerPath, fields, content.displays(), content.services());
    }

    /**
     * Validate the shared create / update basics.
     *
     * @param modelCode      modeling code
     * @param modelName      modeling name
     * @param dataSourceCode datasource code
     * @param remark         remark
     */
    private static void validateBasics(String modelCode, String modelName, String dataSourceCode, String remark) {
        StringUtil011.requirePresent(modelCode, "modeling code", CODE_MAX);
        StringUtil011.requirePresent(modelName, "modeling name", NAME_MAX);
        StringUtil011.requirePresent(dataSourceCode, "datasource code", DS_CODE_MAX);
        StringUtil011.requireMax(remark, "remark", REMARK_MAX);
    }

    /**
     * Validate the owned description.
     *
     * @param content description to check
     */
    private static void validateContent(MetadataContent content) {
        if (content == null) {
            throw new IllegalArgumentException("meta data is required");
        }
    }

    /**
     * Validate one field type against the enum authority.
     *
     * @param fieldType raw field type
     */
    private static void validateFieldType(String fieldType) {
        if (!FieldType011.isKnown(fieldType)) {
            throw new IllegalArgumentException("unknown field type: " + fieldType);
        }
    }

    /**
     * Validate every field type of a collection.
     *
     * @param fields fields to check, nullable
     */
    private static void validateFieldTypes(List<JulyMetadataField> fields) {
        for (JulyMetadataField field : nullToEmpty(fields)) {
            validateFieldType(field == null ? null : field.fieldType());
        }
    }

    /**
     * Validate that a batch carries no duplicate code.
     *
     * @param fields fields to check
     */
    private static void validateUniqueCodes(List<JulyMetadataField> fields) {
        Set<String> codes = new HashSet<>();

        for (JulyMetadataField field : fields) {
            if (field == null || !codes.add(field.fieldCode())) {
                throw new IllegalArgumentException("duplicate field code in batch: "
                        + (field == null ? "null" : field.fieldCode()));
            }
        }
    }

    /**
     * Validate the take-out sql when present.
     *
     * @param sqlContent raw sql, optional
     */
    private static void validateSql(String sqlContent) {
        if (!StringUtil011.isBlank(sqlContent)) {
            ModelingSqlGuard.validate(sqlContent);
        }
    }

    /**
     * Treat a nullable collection as empty.
     *
     * @param source source collection
     * @return the source when non null, otherwise an empty list
     */
    private static List<JulyMetadataField> nullToEmpty(List<JulyMetadataField> source) {
        return source == null ? List.of() : source;
    }

    /**
     * Get the primary key.
     *
     * @return id value object
     */
    public EntityId id() {
        return id;
    }

    /**
     * Get the modeling code.
     *
     * @return modeling code
     */
    public String modelCode() {
        return modelCode;
    }

    /**
     * Get the modeling name.
     *
     * @return modeling name
     */
    public String modelName() {
        return modelName;
    }

    /**
     * Get the owned low-code description contract.
     *
     * @return content, never null
     */
    public MetadataContent content() {
        return content;
    }

    /**
     * Get the object name, the identity the description hands over.
     *
     * @return object name
     */
    public String objectName() {
        return content.objectName();
    }

    /**
     * Get a read-only view of the field definitions carried by the description.
     *
     * @return immutable field list
     */
    public List<JulyMetadataField> fields() {
        return content.fields();
    }

    /**
     * Get the datasource code.
     *
     * @return datasource code
     */
    public String dataSourceCode() {
        return dataSourceCode;
    }

    /**
     * Get the take-out sql.
     *
     * @return sql text or null
     */
    public String sqlContent() {
        return sqlContent;
    }

    /**
     * Get the remark.
     *
     * @return remark or null
     */
    public String remark() {
        return remark;
    }

    /**
     * Get the row status.
     *
     * @return '1' enabled / '0' disabled
     */
    public String status() {
        return status;
    }

    /**
     * Get the audit info.
     *
     * @return audit info
     */
    public AuditInfo audit() {
        return audit;
    }
}
