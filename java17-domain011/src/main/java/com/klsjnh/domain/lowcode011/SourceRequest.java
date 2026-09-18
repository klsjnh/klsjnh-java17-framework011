package com.klsjnh.domain.lowcode011;

/*                SourceRequest record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.18
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.18  modeling intake source request record
 *
 */

import java.util.Map;

/**
 * One modeling intake request: the source kind plus whatever that source needs
 * (datasource + SQL, a model reference, a natural-language prompt, a template,
 * ...). It is the single input shape the intake engine routes on; adding an
 * entry point means adding an adapter, not changing the engine.
 *
 * @param kind          source kind (sql / table / template / ai / modeling / copy)
 * @param objectName    target object name (override / rename), nullable
 * @param objectType    object type code, nullable
 * @param description   description, nullable
 * @param businessField business field code, nullable
 * @param packageName   target package name, nullable
 * @param routerPath    route path, nullable
 * @param dataSourceCode datasource code for sql / table sources, nullable
 * @param table         table name for the table source, nullable
 * @param sql           read-only SQL for the sql source, nullable
 * @param prompt        natural-language requirement for the ai source, nullable
 * @param ref           reference id / code for copy / modeling sources, nullable
 * @param provider      AI provider id or code for the ai source, nullable
 * @param api           AI api id or code for the ai source, nullable
 * @param model         AI model name for the ai source, nullable
 * @param template      template value for the template source, nullable
 */

public record SourceRequest(String kind, String objectName, String objectType, String description,
        String businessField, String packageName, String routerPath, String dataSourceCode, String table, String sql,
        String prompt, String ref, String provider, String api, String model, Map<String, Object> template) {
}
