package com.klsjnh.domain.lowcode011;

/*                JulyMetadata class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july metadata class
 *
 */

import com.klsjnh.common.enums.Status011;
import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.lowcode011.enums.ObjectType011;
import com.klsjnh.domain.lowcode011.records.MetadataContent;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * JulyMetadata aggregate root (low-code core context): one business object's
 * lifecycle plus the {@link MetadataContent} contract it composes. The three
 * child collections (fields / display columns / services) live inside that
 * contract — the single schema source — and are rebuilt on every children
 * change; the repository persists the whole aggregate (replace strategy).
 */

public class JulyMetadata {

    /**
     * Default sort order.
     */
    private static final int DEFAULT_SORT_ORDER = 9999;

    /**
     * Max length of the object name.
     */
    private static final int NAME_MAX = 60;

    /**
     * Max length of optional text attributes.
     */
    private static final int TEXT_MAX = 300;

    /**
     * Primary key.
     */
    private final EntityId id;

    /**
     * Object name, unique and immutable.
     */
    private final String objectName;

    /**
     * Manual sort order.
     */
    private Integer sortOrder;

    /**
     * Object type code (ObjectType011).
     */
    private String objectType;

    /**
     * Object description.
     */
    private String description;

    /**
     * Business field mapping.
     */
    private String businessField;

    /**
     * Target package name.
     */
    private String packageName;

    /**
     * Route path.
     */
    private String routerPath;

    /**
     * Remark, optional.
     */
    private String remark;

    /**
     * Row status.
     */
    private String status;

    /**
     * Audit info.
     */
    private AuditInfo audit;

    /**
     * The content contract owns the three child collections (single schema
     * source); it is rebuilt on every children change.
     */
    private MetadataContent content;

    /**
     * Full constructor (also the rehydration path).
     *
     * @param id            primary key
     * @param objectName    object name, unique
     * @param sortOrder     manual sort order
     * @param objectType    object type code
     * @param description   object description
     * @param businessField business field mapping
     * @param packageName   target package name
     * @param routerPath    route path
     * @param remark        remark
     * @param status        row status
     * @param audit         audit info
     */
    public JulyMetadata(EntityId id, String objectName, Integer sortOrder, String objectType, String description,
            String businessField, String packageName, String routerPath, String remark, String status,
            AuditInfo audit) {
        validate(objectName, objectType, description, businessField, packageName, routerPath, remark);

        this.id = id;
        this.objectName = objectName;
        this.sortOrder = sortOrder == null ? DEFAULT_SORT_ORDER : sortOrder;
        this.objectType = objectType;
        this.description = description;
        this.businessField = businessField;
        this.packageName = packageName;
        this.routerPath = routerPath;
        this.remark = remark;
        this.status = status == null ? Status011.ENABLED.getCode() : status;
        this.audit = audit == null ? AuditInfo.empty() : audit;
        this.content = new MetadataContent(objectName, objectType, description, businessField, packageName,
                routerPath, List.of(), List.of(), List.of());
    }

    /**
     * Factory for a new object.
     *
     * @param id            primary key
     * @param objectName    object name, required, max 60
     * @param sortOrder     manual sort order
     * @param objectType    object type code
     * @param description   object description
     * @param businessField business field mapping
     * @param packageName   target package name
     * @param routerPath    route path
     * @param remark        remark
     * @param audit         audit info
     * @return new aggregate
     */
    public static JulyMetadata create(EntityId id, String objectName, Integer sortOrder, String objectType,
            String description, String businessField, String packageName, String routerPath, String remark,
            AuditInfo audit) {
        return new JulyMetadata(id, objectName, sortOrder, objectType, description, businessField, packageName,
                routerPath, remark, Status011.ENABLED.getCode(), audit);
    }

    /**
     * Update the mutable basics (objectName immutable).
     *
     * @param objectType    object type code
     * @param description   object description
     * @param businessField business field mapping
     * @param packageName   target package name
     * @param routerPath    route path
     * @param sortOrder     manual sort order, null keeps the stored one
     * @param status        row status, null keeps the stored one
     * @param remark        remark
     */
    public void update(String objectType, String description, String businessField, String packageName,
            String routerPath, Integer sortOrder, String status, String remark) {
        validate(this.objectName, objectType, description, businessField, packageName, routerPath, remark);
        this.objectType = objectType;
        this.description = description;
        this.businessField = businessField;
        this.packageName = packageName;
        this.routerPath = routerPath;
        this.remark = remark;

        if (sortOrder != null) {
            this.sortOrder = sortOrder;
        }

        if (!StringUtil011.isBlank(status)) {
            this.status = status;
        }

        this.content = new MetadataContent(this.objectName, this.objectType, this.description, this.businessField,
                this.packageName, this.routerPath, content.fields(), content.displays(), content.services());
    }

    /**
     * Replace the content's three child collections (validate-then-rebuild, so a
     * rejected batch never leaves a half-updated aggregate).
     *
     * @param newFields   fields, nullable for none
     * @param newDisplays displays, nullable for none
     * @param newServices services, nullable for none
     */
    public void replaceChildren(List<JulyMetadataField> newFields, List<JulyMetadataDisplay> newDisplays,
            List<JulyMetadataService> newServices) {
        validateChildren(newFields, newDisplays, newServices);

        this.content = new MetadataContent(this.objectName, this.objectType, this.description, this.businessField,
                this.packageName, this.routerPath, nullToEmpty(newFields), nullToEmpty(newDisplays),
                nullToEmpty(newServices));
    }

    /**
     * The content contract (single schema source).
     *
     * @return content
     */
    public MetadataContent content() {
        return content;
    }

    /**
     * Find a field by code.
     *
     * @param fieldCode field code
     * @return matching field, empty when absent
     */
    public Optional<JulyMetadataField> findField(String fieldCode) {
        if (StringUtil011.isBlank(fieldCode)) {
            return Optional.empty();
        }

        return content.fields().stream().filter(field -> field.fieldCode().equals(fieldCode)).findFirst();
    }

    /**
     * Validate the child collections: unique codes inside each, and every display
     * must bind an existing field.
     *
     * @param newFields   fields
     * @param newDisplays displays
     * @param newServices services
     */
    private static void validateChildren(List<JulyMetadataField> newFields, List<JulyMetadataDisplay> newDisplays,
            List<JulyMetadataService> newServices) {
        Set<String> fieldCodes = new HashSet<>();

        for (JulyMetadataField field : nullToEmpty(newFields)) {
            if (!fieldCodes.add(field.fieldCode())) {
                throw new IllegalArgumentException("duplicate field code: " + field.fieldCode());
            }
        }

        Set<String> displayCodes = new HashSet<>();

        for (JulyMetadataDisplay display : nullToEmpty(newDisplays)) {
            if (!displayCodes.add(display.displayCode())) {
                throw new IllegalArgumentException("duplicate display code: " + display.displayCode());
            }

            if (!fieldCodes.contains(display.displayCode())) {
                throw new IllegalArgumentException("display binds an unknown field: " + display.displayCode());
            }
        }

        Set<String> serviceCodes = new HashSet<>();

        for (JulyMetadataService service : nullToEmpty(newServices)) {
            if (!serviceCodes.add(service.serviceCode())) {
                throw new IllegalArgumentException("duplicate service code: " + service.serviceCode());
            }
        }
    }

    /**
     * Validate the shared basics.
     *
     * @param objectName    object name
     * @param objectType    object type code
     * @param description   object description
     * @param businessField business field mapping
     * @param packageName   target package name
     * @param routerPath    route path
     * @param remark        remark
     */
    private static void validate(String objectName, String objectType, String description, String businessField,
            String packageName, String routerPath, String remark) {
        if (StringUtil011.isMissing(objectName, NAME_MAX)) {
            throw new IllegalArgumentException("object name is required (max 60)");
        }

        if (!StringUtil011.isBlank(objectType) && ObjectType011.fromString(objectType) == null) {
            throw new IllegalArgumentException("unknown object type: " + objectType);
        }

        if (StringUtil011.isOver(description, TEXT_MAX) || StringUtil011.isOver(businessField, TEXT_MAX)
                || StringUtil011.isOver(packageName, TEXT_MAX) || StringUtil011.isOver(routerPath, TEXT_MAX)
                || StringUtil011.isOver(remark, TEXT_MAX)) {
            throw new IllegalArgumentException("a metadata field exceeds " + TEXT_MAX);
        }
    }

    /**
     * Treat a nullable collection as empty.
     *
     * @param source source collection
     * @param <T>    element type
     * @return the source when non null, otherwise an empty list
     */
    private static <T> List<T> nullToEmpty(List<T> source) {
        return source == null ? List.of() : source;
    }

    /**
     * Get the primary key.
     *
     * @return id
     */
    public EntityId id() {
        return id;
    }

    /**
     * Get the object name.
     *
     * @return object name
     */
    public String objectName() {
        return objectName;
    }

    /**
     * Get the manual sort order.
     *
     * @return sort order
     */
    public Integer sortOrder() {
        return sortOrder;
    }

    /**
     * Get the object type code.
     *
     * @return object type
     */
    public String objectType() {
        return objectType;
    }

    /**
     * Get the object description.
     *
     * @return description
     */
    public String description() {
        return description;
    }

    /**
     * Get the business field mapping.
     *
     * @return business field
     */
    public String businessField() {
        return businessField;
    }

    /**
     * Get the target package name.
     *
     * @return package name
     */
    public String packageName() {
        return packageName;
    }

    /**
     * Get the route path.
     *
     * @return router path
     */
    public String routerPath() {
        return routerPath;
    }

    /**
     * Get the remark.
     *
     * @return remark
     */
    public String remark() {
        return remark;
    }

    /**
     * Get the row status.
     *
     * @return status
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

    /**
     * Get a read-only view of the fields.
     *
     * @return fields
     */
    public List<JulyMetadataField> fields() {
        return content.fields();
    }

    /**
     * Get a read-only view of the displays.
     *
     * @return displays
     */
    public List<JulyMetadataDisplay> displays() {
        return content.displays();
    }

    /**
     * Get a read-only view of the services.
     *
     * @return services
     */
    public List<JulyMetadataService> services() {
        return content.services();
    }
}
