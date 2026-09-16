package com.klsjnh.domain.dataservice011;

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
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.lowcode011.enums.FieldType011;
import com.klsjnh.domain.lowcode011.enums.ObjectType011;
import com.klsjnh.domain.lowcode011.model.FieldInfo011;
import com.klsjnh.domain.lowcode011.model.MetaData011;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * JulyBusinessModeling aggregate root (data service context): one take-out SQL
 * bound to one runtime datasource, plus the low-code description derived from
 * it. This is the definition source feeding the low-code context — the module
 * hands the description over and never builds a physical table itself.
 * <p>
 * Boundary and invariants:
 * </p>
 * <ul>
 *   <li>{@code modelCode} is immutable after create, as is
 *       {@code metaData.objectName()} — identity must not be renamed under a
 *       handed-over description.</li>
 *   <li>{@code dataSourceCode} is mandatory: an unbound SQL cannot be probed
 *       or executed. The reference implementation allowed a blank datasource,
 *       which is a defect this module does not repeat.</li>
 *   <li>The description itself lives in the owned {@link MetaData011}, which
 *       already owns the {@link FieldInfo011} list. This root deliberately
 *       keeps no second field list — one owner, one source of truth.</li>
 *   <li>{@code sqlContent} is optional (a draft may carry no SQL yet), but when
 *       present it must pass {@link ModelingSqlGuard}: read-only, single
 *       statement.</li>
 *   <li>Every field type must resolve through {@link FieldType011}, the single
 *       validation authority. The attribute on {@code FieldInfo011} stays a
 *       String so external metadata is never blocked from rehydrating.</li>
 * </ul>
 * <p>
 * There are no setters: mutation is only possible through the intent methods,
 * which is what keeps the invariants true at all times.
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
     * Low-code description owned by this root: the object metadata plus the
     * field definitions it holds.
     */
    private final MetaData011 metaData;

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
     * Full constructor, also the rehydration path from persistence. Replays the
     * shared validation so a corrupted row cannot enter the domain silently.
     *
     * @param id             primary key
     * @param modelCode      modeling code, unique
     * @param modelName      modeling name
     * @param metaData       low-code description, required
     * @param dataSourceCode datasource code, required
     * @param sqlContent        take-out sql, optional
     * @param remark         remark, optional
     * @param status         row status
     * @param audit          audit info
     */
    public JulyBusinessModeling(EntityId id, String modelCode, String modelName, MetaData011 metaData,
            String dataSourceCode, String sqlContent, String remark, String status, AuditInfo audit) {
        validateBasics(modelCode, modelName, dataSourceCode, remark);
        validateMetaData(metaData);
        validateFieldTypes(metaData.fields());
        validateSql(sqlContent);

        this.id = id;
        this.modelCode = modelCode;
        this.modelName = modelName;
        this.metaData = metaData;
        this.dataSourceCode = dataSourceCode;
        this.sqlContent = blankToNull(sqlContent);
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
     * @param metaData       low-code description, required
     * @param dataSourceCode datasource code, required, max 60
     * @param sqlContent        take-out sql, optional, read-only when present
     * @param remark         remark, optional, max 300
     * @param audit          audit info
     * @return new aggregate
     */
    public static JulyBusinessModeling create(EntityId id, String modelCode, String modelName, MetaData011 metaData,
            String dataSourceCode, String sqlContent, String remark, AuditInfo audit) {
        return new JulyBusinessModeling(id, modelCode, modelName, metaData, dataSourceCode, sqlContent, remark,
                Status011.ENABLED.getCode(), audit);
    }

    /**
     * Update the mutable descriptive attributes; the modeling code and the
     * object name are immutable after create.
     *
     * @param modelName      modeling name, required, max 100
     * @param dataSourceCode datasource code, required, max 60
     * @param sqlContent        take-out sql, optional, read-only when present
     * @param remark         remark, optional, max 300
     */
    public void updateBasics(String modelName, String dataSourceCode, String sqlContent, String remark) {
        validateBasics(this.modelCode, modelName, dataSourceCode, remark);
        validateSql(sqlContent);

        this.modelName = modelName;
        this.dataSourceCode = dataSourceCode;
        this.sqlContent = blankToNull(sqlContent);
        this.remark = remark;
    }

    /**
     * Update the mutable attributes of the owned description; the object name
     * stays immutable.
     *
     * @param objectType        object type, defaults to type011 when null
     * @param objectDescription object description, max 300
     * @param businessField     business field mapping, max 300
     * @param packageName       target package name, max 300
     * @param routerPath        route path, max 300
     */
    public void updateMetaBasics(ObjectType011 objectType, String objectDescription, String businessField,
            String packageName, String routerPath) {
        metaData.updateBasics(objectType, objectDescription, businessField, packageName, routerPath);
    }

    /**
     * Add a field definition to the owned description. The field code must be
     * unique inside the description and the field type must be a known
     * {@link FieldType011} code.
     *
     * @param field field definition to add, required
     */
    public void addField(FieldInfo011 field) {
        validateFieldType(field == null ? null : field.fieldType());
        metaData.addField(field);
    }

    /**
     * Update an existing field definition; the field code is immutable.
     *
     * @param fieldCode     field code to update, required
     * @param fieldName     new field name, required, max 60
     * @param fieldType     new field type, required, must be a known code
     * @param fieldLength   new maximum length, negative falls back to 0
     * @param requiredField whether the field is required
     * @param defaultValue  new default value, nullable
     */
    public void updateField(String fieldCode, String fieldName, String fieldType, int fieldLength,
            boolean requiredField, String defaultValue) {
        validateFieldType(fieldType);
        metaData.updateField(fieldCode, fieldName, fieldType, fieldLength, requiredField, defaultValue);
    }

    /**
     * Remove a field definition by code.
     *
     * @param fieldCode field code to remove, required
     */
    public void removeField(String fieldCode) {
        metaData.removeField(fieldCode);
    }

    /**
     * Replace every field definition at once — the re-probe overwrite path:
     * after the SQL is re-inferred the description carries the fresh columns
     * and the stale ones are dropped.
     * <p>
     * The replacement is validated in full BEFORE anything is removed: a
     * rejected batch would otherwise leave the aggregate with its old fields
     * already gone and the new ones half added.
     * </p>
     *
     * @param fields new field definitions, nullable for an empty description
     */
    public void replaceFields(List<FieldInfo011> fields) {
        List<FieldInfo011> incoming = nullToEmpty(fields);

        validateFieldTypes(incoming);
        validateUniqueCodes(incoming);

        for (FieldInfo011 existing : metaData.fields()) {
            metaData.removeField(existing.fieldCode());
        }

        for (FieldInfo011 field : incoming) {
            metaData.addField(field);
        }
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
        if (StringUtil011.isMissing(modelCode, CODE_MAX)) {
            throw new IllegalArgumentException("modeling code is required (max " + CODE_MAX + ")");
        }

        if (StringUtil011.isMissing(modelName, NAME_MAX)) {
            throw new IllegalArgumentException("modeling name is required (max " + NAME_MAX + ")");
        }

        if (StringUtil011.isMissing(dataSourceCode, DS_CODE_MAX)) {
            throw new IllegalArgumentException("datasource code is required (max " + DS_CODE_MAX + ")");
        }

        if (StringUtil011.isOver(remark, REMARK_MAX)) {
            throw new IllegalArgumentException("remark is over " + REMARK_MAX);
        }
    }

    /**
     * Validate the owned description.
     *
     * @param metaData description to check
     */
    private static void validateMetaData(MetaData011 metaData) {
        if (metaData == null) {
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
    private static void validateFieldTypes(List<FieldInfo011> fields) {
        for (FieldInfo011 field : nullToEmpty(fields)) {
            validateFieldType(field == null ? null : field.fieldType());
        }
    }

    /**
     * Validate that a batch carries no duplicate code, before any mutation is
     * attempted on the owned description.
     *
     * @param fields fields to check
     */
    private static void validateUniqueCodes(List<FieldInfo011> fields) {
        Set<String> codes = new HashSet<>();

        for (FieldInfo011 field : fields) {
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
     * Treat a blank text as absent, so "no sql yet" has exactly one
     * representation.
     *
     * @param value raw value
     * @return trimmed value, or null when blank
     */
    private static String blankToNull(String value) {
        return StringUtil011.isBlank(value) ? null : value.trim();
    }

    /**
     * Treat a nullable collection as empty.
     *
     * @param source source collection
     * @return the source when non null, otherwise an empty list
     */
    private static List<FieldInfo011> nullToEmpty(List<FieldInfo011> source) {
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
     * Get the owned low-code description.
     *
     * @return description, never null
     */
    public MetaData011 metaData() {
        return metaData;
    }

    /**
     * Get the object name, the identity the description hands over.
     *
     * @return object name
     */
    public String objectName() {
        return metaData.objectName();
    }

    /**
     * Get a read-only view of the field definitions carried by the description.
     *
     * @return immutable field list
     */
    public List<FieldInfo011> fields() {
        return metaData.fields();
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
