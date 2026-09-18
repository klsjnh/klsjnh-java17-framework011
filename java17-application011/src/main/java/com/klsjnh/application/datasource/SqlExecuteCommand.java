package com.klsjnh.application.datasource;

/*                SqlExecuteCommand class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.16
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.16  business modeling execute command class
 *
 */

/**
 * Execute-SQL input with polymorphic selectors.
 * <p>
 * Datasource: at most one of {@code dataSourceId} / {@code dataSourceCode}; when
 * neither is given the datasource is derived from the modeling.
 * </p>
 * <p>
 * SQL source: exactly one of {@code modelId} / {@code modelCode} / {@code sqlContent}
 * — a model contributes its stored {@code sql_content}, otherwise the raw
 * {@code sqlContent} is used.
 * </p>
 *
 * @param dataSourceId   datasource id selector, optional
 * @param dataSourceCode datasource code selector, optional
 * @param modelId        modeling id SQL selector, optional
 * @param modelCode      modeling code SQL selector, optional
 * @param sqlContent     raw read-only SQL, optional
 */

public record SqlExecuteCommand(String dataSourceId, String dataSourceCode, String modelId,
        String modelCode, String sqlContent) {
}
