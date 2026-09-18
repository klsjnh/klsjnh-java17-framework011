package com.klsjnh.domain.lowcode011.records;

/*                MetaDtoKey011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  meta dto json key constants
 *
 */

/**
 * The MetaDTO JSON keys, centralised so the wire schema never drifts between
 * the codec, the designer, the template and the modeling hand-off.
 */

public final class MetaDtoKey011 {

    /** Section: metaData. */
    public static final String META_DATA = "metaData";

    /** Section: fieldData. */
    public static final String FIELD_DATA = "fieldData";

    /** Section: displayData. */
    public static final String DISPLAY_DATA = "displayData";

    /** Section: serviceData. */
    public static final String SERVICE_DATA = "serviceData";

    /** metaData: objectName. */
    public static final String OBJECT_NAME = "objectName";

    /** metaData / service: objectType. */
    public static final String OBJECT_TYPE = "objectType";

    /** metaData / service: description. */
    public static final String DESCRIPTION = "description";

    /** metaData: businessField. */
    public static final String BUSINESS_FIELD = "businessField";

    /** metaData: packageName. */
    public static final String PACKAGE_NAME = "packageName";

    /** metaData: routerPath. */
    public static final String ROUTER_PATH = "routerPath";

    /** metaData: remark. */
    public static final String REMARK = "remark";

    /** metaData: sortOrder. */
    public static final String SORT_ORDER = "sortOrder";

    /** metaData: publishStatus. */
    public static final String PUBLISH_STATUS = "publishStatus";

    /** metaData: version. */
    public static final String VERSION = "version";

    /** Child: code. */
    public static final String CODE = "code";

    /** Child: name. */
    public static final String NAME = "name";

    /** Field: fieldType. */
    public static final String FIELD_TYPE = "fieldType";

    /** Field: length. */
    public static final String LENGTH = "length";

    /** Field: notNull. */
    public static final String NOT_NULL = "notNull";

    /** Field: defaultValue. */
    public static final String DEFAULT_VALUE = "defaultValue";

    /** Child: sort. */
    public static final String SORT = "sort";

    /** Display: align. */
    public static final String ALIGN = "align";

    /** Display: width. */
    public static final String WIDTH = "width";

    /** Display: componentType. */
    public static final String COMPONENT_TYPE = "componentType";

    /** Display: displayType. */
    public static final String DISPLAY_TYPE = "displayType";

    /** Display: param011. */
    public static final String PARAM011 = "param011";

    /** Service: paramType. */
    public static final String PARAM_TYPE = "paramType";

    /** Service: serviceContent. */
    public static final String SERVICE_CONTENT = "serviceContent";

    /** Service: enabled. */
    public static final String ENABLED = "enabled";

    /** Template: _guide. */
    public static final String GUIDE = "_guide";

    /** Template: templateVersion. */
    public static final String TEMPLATE_VERSION = "templateVersion";

    /** metaData.source: section. */
    public static final String SOURCE = "source";

    /** metaData.source: kind. */
    public static final String KIND = "kind";

    /** metaData.source: dataSourceCode. */
    public static final String DATA_SOURCE_CODE = "dataSourceCode";

    /** metaData.source: probeSql. */
    public static final String PROBE_SQL = "probeSql";

    /**
     * Utility: no instances.
     */
    private MetaDtoKey011() {
    }
}
