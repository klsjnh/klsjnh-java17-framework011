package com.klsjnh.domain.lowcode011;

/*                JulyMetadataService class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july metadata service class
 *
 */

import com.klsjnh.common.util.StringUtil011;

/**
 * Low-code core service definition (child of {@link JulyMetadata}).
 *
 * @param serviceCode        service code, unique within the object
 * @param serviceName        service display name
 * @param serviceDescription service description, nullable
 * @param objectType         service object type code (ServiceObjectType011)
 * @param paramType          parameter type code (ServiceParamType011)
 * @param serviceContent     SQL / script content, nullable
 * @param enabled            whether the service is enabled
 * @param sortOrder          manual sort order, null falls back to the default
 */

public record JulyMetadataService(String serviceCode, String serviceName, String serviceDescription, String objectType,
        String paramType, String serviceContent, boolean enabled, Integer sortOrder) {

    /**
     * Normalize and validate.
     */
    public JulyMetadataService {
        StringUtil011.requirePresent(serviceCode, "service code", 60);

        StringUtil011.requirePresent(serviceName, "service name", 60);

        StringUtil011.requireMax(serviceDescription, "service description", 300);

        if (StringUtil011.isBlank(objectType)) {
            objectType = "global_method";
        }

        if (StringUtil011.isBlank(paramType)) {
            paramType = "none";
        }

        StringUtil011.requireMax(serviceContent, "service content", 20000);

        if (sortOrder == null) {
            sortOrder = 9999;
        }
    }
}
