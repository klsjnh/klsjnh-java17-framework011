package com.klsjnh.domain.datasource.kernel;

/*                ReloadResult record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  dynamic datasource reload result record
 *
 */

/**
 * Reconciliation summary of one registry reload, reported as-is in the
 * reloadRegistry log line.
 *
 * @param enabled    declared configs in the incoming snapshot
 * @param registered pools kept or newly declared (config count after reload)
 * @param reused     pools kept because their physical target was unchanged
 * @param closed     pools closed because their config vanished / changed
 * @param failed     configs the reconciler could not process
 *
 * @see DynamicDataSourceRegistryPort#reloadAll(java.util.List)
 */

public record ReloadResult(int enabled, int registered, int reused, int closed, int failed) {

    /**
     * Build an empty result (nothing declared, nothing to reconcile).
     *
     * @return zero filled result
     */
    public static ReloadResult empty() {
        return new ReloadResult(0, 0, 0, 0, 0);
    }
}
