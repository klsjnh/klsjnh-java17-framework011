package com.klsjnh.domain.lowcode011.records;

/*                MetadataContentMapper class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  metadata content mapper class
 *
 */

import com.klsjnh.domain.lowcode011.JulyMetadata;
import com.klsjnh.domain.lowcode011.JulyMetadataDisplay;
import com.klsjnh.domain.lowcode011.JulyMetadataField;
import com.klsjnh.domain.lowcode011.JulyMetadataService;
import com.klsjnh.domain.lowcode011.model.DisplayInfo011;
import com.klsjnh.domain.lowcode011.model.FieldInfo011;
import com.klsjnh.domain.lowcode011.model.MetaData011;
import com.klsjnh.domain.lowcode011.model.ServiceInfo011;

import java.util.ArrayList;
import java.util.List;

/**
 * The <b>single</b> mapping between {@link MetadataContent} and its two current
 * representations: the engine aggregate {@code JulyMetadata} and the modeling
 * value object {@code MetaData011}. Keeping it in one place is what stops the
 * two models from drifting during the transition.
 */

public final class MetadataContentMapper {

    /**
     * Utility: no instances.
     */
    private MetadataContentMapper() {
    }

    /**
     * Project the engine aggregate onto the content contract (sort orders kept).
     *
     * @param metadata engine aggregate
     * @return content contract
     */
    public static MetadataContent from(JulyMetadata metadata) {
        return new MetadataContent(metadata.objectName(), metadata.objectType(), metadata.description(),
                metadata.businessField(), metadata.packageName(), metadata.routerPath(), metadata.fields(),
                metadata.displays(), metadata.services());
    }

    /**
     * Project the modeling value object onto the content contract. Sort orders
     * are not carried by the modeling value object (null).
     *
     * @param metadata modeling value object
     * @return content contract
     */
    public static MetadataContent from(MetaData011 metadata) {
        List<JulyMetadataField> fields = new ArrayList<>();

        for (FieldInfo011 field : metadata.fields()) {
            fields.add(new JulyMetadataField(field.fieldCode(), field.fieldName(), field.fieldType(),
                    field.fieldLength(), field.requiredField(), field.defaultValue(), null));
        }

        List<JulyMetadataDisplay> displays = new ArrayList<>();

        for (DisplayInfo011 display : metadata.displays()) {
            displays.add(new JulyMetadataDisplay(display.displayCode(), display.displayName(), display.align(),
                    display.width(), display.componentType(), code(display.displayType()), display.param011(), null));
        }

        List<JulyMetadataService> services = new ArrayList<>();

        for (ServiceInfo011 service : metadata.services()) {
            services.add(new JulyMetadataService(service.serviceCode(), service.serviceName(),
                    service.serviceDescription(), code(service.objectType()), code(service.paramType()),
                    service.serviceContent(), service.enabled(), null));
        }

        return new MetadataContent(metadata.objectName(), code(metadata.objectType()), metadata.objectDescription(),
                metadata.businessField(), metadata.packageName(), metadata.routerPath(), fields, displays, services);
    }

    /**
     * Enum code of a low-code enum value, null when absent.
     *
     * @param value enum value
     * @return code or null
     */
    private static String code(Enum<?> value) {
        if (value == null) {
            return null;
        }

        try {
            return (String) value.getClass().getMethod("getCode").invoke(value);
        } catch (Exception ex) {
            return value.name();
        }
    }
}
