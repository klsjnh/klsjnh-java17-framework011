package com.klsjnh.domain.platform011.export;

/*                ExportProviderInfo record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.10.06
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.10.06  generic export provider listing item
 *
 */

/**
 * Listing item of one registered export provider (generic export discovery).
 *
 * @param objectCode  unique object code
 * @param moduleCode  permission module (first segment of the per-object
 *                    export code)
 * @param columnCount number of exported columns
 */

public record ExportProviderInfo(String objectCode, String moduleCode, int columnCount) {
}
