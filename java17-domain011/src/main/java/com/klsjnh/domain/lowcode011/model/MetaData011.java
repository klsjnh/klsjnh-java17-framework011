package com.klsjnh.domain.lowcode011.model;

/*                MetaData011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.11
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.11  meta data 011 class
 *      2026.09.15  migrated to domain lowcode011, rebuilt as an aggregate root
 *
 */

import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.lowcode011.enums.DisplayType011;
import com.klsjnh.domain.lowcode011.enums.ObjectType011;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Low-code object metadata aggregate root (low-code context): the single
 * consistency boundary describing one low-code business object.
 * <p>
 * Boundary and invariants:
 * </p>
 * <ul>
 *   <li>{@code objectName} is the identity of the aggregate and is immutable
 *       after create.</li>
 *   <li>The owned collections ({@link FieldInfo011} / {@link DisplayInfo011} /
 *       {@link ServiceInfo011}) have no independent lifecycle — they are
 *       reachable only as read-only views and every change goes through the
 *       intent methods on this root.</li>
 *   <li>Codes are unique per collection; a display column must bind to a field
 *       code that exists in this aggregate, so removing a field still
 *       referenced by a column is rejected.</li>
 * </ul>
 * <p>
 * There are no setters: mutation is only possible through the intent methods,
 * which is what keeps the invariants true at all times.
 * </p>
 */

public class MetaData011 {

    /**
     * Max length of the object name.
     */
    private static final int NAME_MAX = 60;

    /**
     * Max length of the optional descriptive attributes.
     */
    private static final int TEXT_MAX = 300;

    /**
     * Object name, identity of the aggregate, immutable after create.
     */
    private final String objectName;

    /**
     * Object type.
     */
    private ObjectType011 objectType;

    /**
     * Object description.
     */
    private String objectDescription;

    /**
     * Business field mapping.
     */
    private String businessField;

    /**
     * Target package name for code generation.
     */
    private String packageName;

    /**
     * Route path for this object.
     */
    private String routerPath;

    /**
     * Field definitions owned by this aggregate.
     */
    private final List<FieldInfo011> fields = new ArrayList<>();

    /**
     * Display column definitions owned by this aggregate.
     */
    private final List<DisplayInfo011> displays = new ArrayList<>();

    /**
     * Service definitions owned by this aggregate.
     */
    private final List<ServiceInfo011> services = new ArrayList<>();

    /**
     * Private constructor: instances are built by {@link #create(String,
     * ObjectType011, String, String, String, String)} for new objects or by
     * {@link #reconstitute(String, ObjectType011, String, String, String,
     * String, List, List, List)} when rehydrating from persistence.
     *
     * @param objectName        object name, identity of the aggregate
     * @param objectType        object type
     * @param objectDescription object description
     * @param businessField     business field mapping
     * @param packageName       target package name for code generation
     * @param routerPath        route path for this object
     */
    private MetaData011(String objectName, ObjectType011 objectType, String objectDescription, String businessField,
            String packageName, String routerPath) {
        this.objectName = objectName;
        this.objectType = objectType == null ? ObjectType011.TYPE011 : objectType;
        this.objectDescription = objectDescription;
        this.businessField = businessField;
        this.packageName = packageName;
        this.routerPath = routerPath;
    }

    /**
     * Factory for a new metadata definition with empty owned collections.
     *
     * @param objectName        object name, required, max 60
     * @param objectType        object type, defaults to type011 when null
     * @param objectDescription object description, max 300
     * @param businessField     business field mapping, max 300
     * @param packageName       target package name, max 300
     * @param routerPath        route path, max 300
     * @return new aggregate root
     */
    public static MetaData011 create(String objectName, ObjectType011 objectType, String objectDescription,
            String businessField, String packageName, String routerPath) {
        validateBasics(objectName, objectDescription, businessField, packageName, routerPath);

        return new MetaData011(objectName, objectType, objectDescription, businessField, packageName, routerPath);
    }

    /**
     * Rehydration path from persistence: rebuilds the aggregate with its owned
     * collections and re-applies the invariants so a corrupted row can never
     * enter the domain silently.
     *
     * @param objectName        object name, required, max 60
     * @param objectType        object type, defaults to type011 when null
     * @param objectDescription object description, max 300
     * @param businessField     business field mapping, max 300
     * @param packageName       target package name, max 300
     * @param routerPath        route path, max 300
     * @param fields            field definitions, nullable
     * @param displays          display column definitions, nullable
     * @param services          service definitions, nullable
     * @return rehydrated aggregate root
     */
    public static MetaData011 reconstitute(String objectName, ObjectType011 objectType, String objectDescription,
            String businessField, String packageName, String routerPath, List<FieldInfo011> fields,
            List<DisplayInfo011> displays, List<ServiceInfo011> services) {
        MetaData011 metaData = create(objectName, objectType, objectDescription, businessField, packageName,
                routerPath);

        for (FieldInfo011 field : nullToEmpty(fields)) {
            metaData.addField(field);
        }

        for (DisplayInfo011 display : nullToEmpty(displays)) {
            metaData.addDisplay(display);
        }

        for (ServiceInfo011 service : nullToEmpty(services)) {
            metaData.addService(service);
        }

        return metaData;
    }

    /**
     * Update the mutable descriptive attributes; the object name is immutable.
     *
     * @param objectType        object type, defaults to type011 when null
     * @param objectDescription object description, max 300
     * @param businessField     business field mapping, max 300
     * @param packageName       target package name, max 300
     * @param routerPath        route path, max 300
     */
    public void updateBasics(ObjectType011 objectType, String objectDescription, String businessField,
            String packageName, String routerPath) {
        validateBasics(this.objectName, objectDescription, businessField, packageName, routerPath);
        this.objectType = objectType == null ? ObjectType011.TYPE011 : objectType;
        this.objectDescription = objectDescription;
        this.businessField = businessField;
        this.packageName = packageName;
        this.routerPath = routerPath;
    }

    /**
     * Add a field definition; the code must be unique within this aggregate.
     *
     * @param field field definition to add, required
     */
    public void addField(FieldInfo011 field) {
        if (field == null) {
            throw new IllegalArgumentException("field must not be null");
        }

        if (findField(field.fieldCode()).isPresent()) {
            throw new IllegalArgumentException("duplicate field code: " + field.fieldCode());
        }

        this.fields.add(field);
    }

    /**
     * Remove a field definition by code; rejected while a display column still
     * binds to it, because that would leave a dangling reference.
     *
     * @param fieldCode field code to remove, required
     */
    public void removeField(String fieldCode) {
        if (StringUtil011.isBlank(fieldCode)) {
            throw new IllegalArgumentException("field code is required");
        }

        FieldInfo011 field = findField(fieldCode)
                .orElseThrow(() -> new IllegalArgumentException("field not found: " + fieldCode));

        if (findDisplay(fieldCode).isPresent()) {
            throw new IllegalArgumentException("field is still bound by a display column: " + fieldCode);
        }

        this.fields.remove(field);
    }

    /**
     * Update an existing field definition; the code is immutable.
     *
     * @param fieldCode     field code to update, required
     * @param fieldName     new field name, required, max 60
     * @param fieldType     new field type, required, max 30
     * @param fieldLength   new maximum length, negative falls back to 0
     * @param requiredField whether the field is required
     * @param defaultValue  new default value, nullable
     */
    public void updateField(String fieldCode, String fieldName, String fieldType, int fieldLength,
            boolean requiredField, String defaultValue) {
        FieldInfo011 field = findField(fieldCode)
                .orElseThrow(() -> new IllegalArgumentException("field not found: " + fieldCode));

        field.updateBasics(fieldName, fieldType, fieldLength, requiredField, defaultValue);
    }

    /**
     * Add a display column; the code must be unique and must bind to a field
     * that exists in this aggregate.
     *
     * @param display display column to add, required
     */
    public void addDisplay(DisplayInfo011 display) {
        if (display == null) {
            throw new IllegalArgumentException("display must not be null");
        }

        if (findDisplay(display.displayCode()).isPresent()) {
            throw new IllegalArgumentException("duplicate display code: " + display.displayCode());
        }

        if (findField(display.displayCode()).isEmpty()) {
            throw new IllegalArgumentException("display binds to an unknown field: " + display.displayCode());
        }

        this.displays.add(display);
    }

    /**
     * Remove a display column by code.
     *
     * @param displayCode column code to remove, required
     */
    public void removeDisplay(String displayCode) {
        if (StringUtil011.isBlank(displayCode)) {
            throw new IllegalArgumentException("display code is required");
        }

        DisplayInfo011 display = findDisplay(displayCode)
                .orElseThrow(() -> new IllegalArgumentException("display not found: " + displayCode));

        this.displays.remove(display);
    }

    /**
     * Update an existing display column; the code is immutable.
     *
     * @param displayCode   column code to update, required
     * @param displayName   new column name, required, max 60
     * @param align         new alignment, falls back to left when unsupported
     * @param width         new column width, negative falls back to 0
     * @param componentType new component type, required, max 30
     * @param displayType   new display type, defaults to all when null
     * @param param011      new extra parameter, nullable
     */
    public void updateDisplay(String displayCode, String displayName, String align, int width, String componentType,
            DisplayType011 displayType, String param011) {
        DisplayInfo011 display = findDisplay(displayCode)
                .orElseThrow(() -> new IllegalArgumentException("display not found: " + displayCode));

        display.updateBasics(displayName, align, width, componentType, displayType, param011);
    }

    /**
     * Add a service definition; the code must be unique within this aggregate.
     *
     * @param service service definition to add, required
     */
    public void addService(ServiceInfo011 service) {
        if (service == null) {
            throw new IllegalArgumentException("service must not be null");
        }

        if (findService(service.serviceCode()).isPresent()) {
            throw new IllegalArgumentException("duplicate service code: " + service.serviceCode());
        }

        this.services.add(service);
    }

    /**
     * Remove a service definition by code.
     *
     * @param serviceCode service code to remove, required
     */
    public void removeService(String serviceCode) {
        if (StringUtil011.isBlank(serviceCode)) {
            throw new IllegalArgumentException("service code is required");
        }

        ServiceInfo011 service = findService(serviceCode)
                .orElseThrow(() -> new IllegalArgumentException("service not found: " + serviceCode));

        this.services.remove(service);
    }

    /**
     * Find a field definition by code.
     *
     * @param fieldCode field code
     * @return matching field, empty when absent
     */
    public Optional<FieldInfo011> findField(String fieldCode) {
        if (StringUtil011.isBlank(fieldCode)) {
            return Optional.empty();
        }

        return fields.stream().filter(field -> field.fieldCode().equals(fieldCode)).findFirst();
    }

    /**
     * Find a display column by code.
     *
     * @param displayCode column code
     * @return matching column, empty when absent
     */
    public Optional<DisplayInfo011> findDisplay(String displayCode) {
        if (StringUtil011.isBlank(displayCode)) {
            return Optional.empty();
        }

        return displays.stream().filter(display -> display.displayCode().equals(displayCode)).findFirst();
    }

    /**
     * Find a service definition by code.
     *
     * @param serviceCode service code
     * @return matching service, empty when absent
     */
    public Optional<ServiceInfo011> findService(String serviceCode) {
        if (StringUtil011.isBlank(serviceCode)) {
            return Optional.empty();
        }

        return services.stream().filter(service -> service.serviceCode().equals(serviceCode)).findFirst();
    }

    /**
     * Validate the mandatory attributes.
     *
     * @param name        object name
     * @param description object description
     * @param business    business field mapping
     * @param pkg         target package name
     * @param router      route path
     */
    private static void validateBasics(String name, String description, String business, String pkg, String router) {
        if (StringUtil011.isMissing(name, NAME_MAX)) {
            throw new IllegalArgumentException("object name is required (max " + NAME_MAX + ")");
        }

        if (StringUtil011.isOver(description, TEXT_MAX)) {
            throw new IllegalArgumentException("object description is too long (max " + TEXT_MAX + ")");
        }

        if (StringUtil011.isOver(business, TEXT_MAX)) {
            throw new IllegalArgumentException("business field is too long (max " + TEXT_MAX + ")");
        }

        if (StringUtil011.isOver(pkg, TEXT_MAX)) {
            throw new IllegalArgumentException("package name is too long (max " + TEXT_MAX + ")");
        }

        if (StringUtil011.isOver(router, TEXT_MAX)) {
            throw new IllegalArgumentException("router path is too long (max " + TEXT_MAX + ")");
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
     * Get the object name.
     *
     * @return object name, the aggregate identity
     */
    public String objectName() {
        return objectName;
    }

    /**
     * Get the object type.
     *
     * @return object type
     */
    public ObjectType011 objectType() {
        return objectType;
    }

    /**
     * Get the object description.
     *
     * @return object description
     */
    public String objectDescription() {
        return objectDescription;
    }

    /**
     * Get the business field mapping.
     *
     * @return business field mapping
     */
    public String businessField() {
        return businessField;
    }

    /**
     * Get the target package name for code generation.
     *
     * @return target package name
     */
    public String packageName() {
        return packageName;
    }

    /**
     * Get the route path.
     *
     * @return route path
     */
    public String routerPath() {
        return routerPath;
    }

    /**
     * Get a read-only view of the owned field definitions.
     *
     * @return immutable field list
     */
    public List<FieldInfo011> fields() {
        return List.copyOf(fields);
    }

    /**
     * Get a read-only view of the owned display columns.
     *
     * @return immutable display list
     */
    public List<DisplayInfo011> displays() {
        return List.copyOf(displays);
    }

    /**
     * Get a read-only view of the owned service definitions.
     *
     * @return immutable service list
     */
    public List<ServiceInfo011> services() {
        return List.copyOf(services);
    }
}
