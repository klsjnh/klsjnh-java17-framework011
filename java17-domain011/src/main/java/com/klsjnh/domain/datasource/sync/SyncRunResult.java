package com.klsjnh.domain.datasource.sync;

/*                SyncRunResult record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  sync run result record
 *
 */

/**
 * The outcome of one sync run.
 *
 * @param syncCode  sync rule code
 * @param read      rows read from the source
 * @param written   rows written to the target (insert + update)
 * @param pages     pages processed
 * @param message   client-safe summary
 */

public record SyncRunResult(String syncCode, long read, long written, int pages, String message) {
}
