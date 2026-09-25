package com.klsjnh.domain.platform011.importdata;

/*                ImportResult class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.22  import result class
 *
 */

/**
 * Outcome of a successful import apply (failures throw before this is built).
 *
 * @param objectCode object code
 * @param mastersInserted master rows inserted
 * @param mastersUpdated  master rows updated
 * @param childrenWritten child rows written after replace
 */

public record ImportResult(String objectCode, int mastersInserted, int mastersUpdated, int childrenWritten) {
}
