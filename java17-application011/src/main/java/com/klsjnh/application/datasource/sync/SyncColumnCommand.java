package com.klsjnh.application.datasource.sync;

/*                SyncColumnCommand record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  sync column command
 *
 */

/**
 * One column mapping command (source column / type → target column / type).
 *
 * @param sourceColumn source column
 * @param sourceType   source neutral type code
 * @param targetColumn target column
 * @param targetType   target neutral type code
 * @param transform    optional value transform
 * @param sortOrder    sort order, nullable
 */

public record SyncColumnCommand(String sourceColumn, String sourceType, String targetColumn, String targetType,
        String transform, Integer sortOrder) {
}
