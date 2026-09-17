package com.klsjnh.domain.lowcode011;

/*                JulyMetadataSource record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.17
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.17  metadata source read model
 *
 */

/**
 * Source descriptor of a low-code object: the datasource it was probed from
 * and the sql used, so a template can carry where the object comes from.
 *
 * @param dataSourceCode source datasource code, nullable
 * @param probeSql       probe sql, nullable
 */

public record JulyMetadataSource(String dataSourceCode, String probeSql) {
}
