package com.klsjnh.domain.lowcode011.model;

/*                ServiceInfo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.11
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.11  service info 011 class
 *      2026.09.15  migrated to domain lowcode011, rebuilt as a setter-free entity
 *
 */

import com.klsjnh.common.util.StringUtil011;

import com.klsjnh.domain.lowcode011.enums.ServiceObjectType011;
import com.klsjnh.domain.lowcode011.enums.ServiceParamType011;

/**
 * Low-code service definition: an entity inside the {@link MetaData011}
 * aggregate boundary, describing one invocable operation of a low-code object.
 * <p>
 * The service body is a SQL statement or a script fragment. No setters: every
 * change goes through an intent method owned by the aggregate, and enabling or
 * disabling is an explicit state transition.
 * </p>
 */

public class ServiceInfo011 {

    /**
     * Max length of the service code.
     */
    private static final int CODE_MAX = 60;

    /**
     * Max length of the service body (SQL or script).
     */
    private static final int CONTENT_MAX = 20000;

    /**
     * Service code, unique within the aggregate, immutable after create.
     */
    private final String serviceCode;

    /**
     * Human-readable service name.
     */
    private String serviceName;

    /**
     * Service description.
     */
    private String serviceDescription;

    /**
     * Service object type (global method / table row method).
     */
    private ServiceObjectType011 objectType;

    /**
     * Parameter type (none / string / object / single file / multi file).
     */
    private ServiceParamType011 paramType;

    /**
     * SQL or script content for this service.
     */
    private String serviceContent;

    /**
     * Whether this service is enabled.
     */
    private boolean enabled;

    /**
     * Full constructor, also the rehydration path from persistence.
     *
     * @param serviceCode        service code, unique within the aggregate
     * @param serviceName        human-readable service name
     * @param serviceDescription service description
     * @param objectType         service object type, defaults to global when null
     * @param paramType          parameter type, defaults to none when null
     * @param serviceContent     SQL or script content
     * @param enabled            whether this service is enabled
     */
    public ServiceInfo011(String serviceCode, String serviceName, String serviceDescription,
            ServiceObjectType011 objectType, ServiceParamType011 paramType, String serviceContent, boolean enabled) {
        this.serviceCode = serviceCode;
        this.serviceName = serviceName;
        this.serviceDescription = serviceDescription;
        this.objectType = objectType == null ? ServiceObjectType011.GLOBAL_METHOD : objectType;
        this.paramType = paramType == null ? ServiceParamType011.NONE : paramType;
        this.serviceContent = serviceContent;
        this.enabled = enabled;
    }

    /**
     * Factory for a new service definition, disabled by default.
     *
     * @param serviceCode        service code, required, max 60
     * @param serviceName        human-readable service name, required, max 60
     * @param serviceDescription service description, max 300
     * @param objectType         service object type, defaults to global when null
     * @param paramType          parameter type, defaults to none when null
     * @param serviceContent     SQL or script content, required, max 20000
     * @return new service definition
     */
    public static ServiceInfo011 create(String serviceCode, String serviceName, String serviceDescription,
            ServiceObjectType011 objectType, ServiceParamType011 paramType, String serviceContent) {
        validateBasics(serviceCode, serviceName, serviceDescription, serviceContent);

        return new ServiceInfo011(serviceCode, serviceName, serviceDescription, objectType, paramType,
                serviceContent, false);
    }

    /**
     * Update the mutable descriptive attributes; the code is immutable.
     *
     * @param serviceName        human-readable service name, required, max 60
     * @param serviceDescription service description, max 300
     * @param objectType         service object type, defaults to global when null
     * @param paramType          parameter type, defaults to none when null
     * @param serviceContent     SQL or script content, required, max 20000
     */
    public void updateBasics(String serviceName, String serviceDescription, ServiceObjectType011 objectType,
            ServiceParamType011 paramType, String serviceContent) {
        validateBasics(this.serviceCode, serviceName, serviceDescription, serviceContent);
        this.serviceName = serviceName;
        this.serviceDescription = serviceDescription;
        this.objectType = objectType == null ? ServiceObjectType011.GLOBAL_METHOD : objectType;
        this.paramType = paramType == null ? ServiceParamType011.NONE : paramType;
        this.serviceContent = serviceContent;
    }

    /**
     * Switch to enabled.
     */
    public void enable() {
        this.enabled = true;
    }

    /**
     * Switch to disabled.
     */
    public void disable() {
        this.enabled = false;
    }

    /**
     * Validate the mandatory attributes.
     *
     * @param code        service code
     * @param name        service name
     * @param description service description
     * @param content     service body
     */
    private static void validateBasics(String code, String name, String description, String content) {
        if (StringUtil011.isMissing(code, CODE_MAX)) {
            throw new IllegalArgumentException("service code is required (max " + CODE_MAX + ")");
        }

        if (StringUtil011.isMissing(name, CODE_MAX)) {
            throw new IllegalArgumentException("service name is required (max " + CODE_MAX + ")");
        }

        if (StringUtil011.isOver(description, 300)) {
            throw new IllegalArgumentException("service description is too long (max 300)");
        }

        if (StringUtil011.isMissing(content, CONTENT_MAX)) {
            throw new IllegalArgumentException("service content is required (max " + CONTENT_MAX + ")");
        }
    }

    /**
     * Get the service code.
     *
     * @return service code
     */
    public String serviceCode() {
        return serviceCode;
    }

    /**
     * Get the human-readable service name.
     *
     * @return service name
     */
    public String serviceName() {
        return serviceName;
    }

    /**
     * Get the service description.
     *
     * @return service description
     */
    public String serviceDescription() {
        return serviceDescription;
    }

    /**
     * Get the service object type.
     *
     * @return service object type
     */
    public ServiceObjectType011 objectType() {
        return objectType;
    }

    /**
     * Get the parameter type.
     *
     * @return parameter type
     */
    public ServiceParamType011 paramType() {
        return paramType;
    }

    /**
     * Get the SQL or script content.
     *
     * @return service body
     */
    public String serviceContent() {
        return serviceContent;
    }

    /**
     * Whether this service is enabled.
     *
     * @return true when enabled
     */
    public boolean enabled() {
        return enabled;
    }
}
