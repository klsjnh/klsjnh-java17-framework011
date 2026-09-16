package com.klsjnh.application.storage011;

/*                StorageProbeResult class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  storage probe result class
 *
 */

/**
 * Connectivity probe result for a storage instance (HTTP always 200, result in
 * the payload).
 *
 * @param success whether the storage answered
 * @param message client-safe detail
 */

public record StorageProbeResult(boolean success, String message) {

    /**
     * Build a success result.
     *
     * @param message detail
     * @return success result
     */
    public static StorageProbeResult ok(String message) {
        return new StorageProbeResult(true, message);
    }

    /**
     * Build a failure result.
     *
     * @param message client-safe failure hint
     * @return failure result
     */
    public static StorageProbeResult fail(String message) {
        return new StorageProbeResult(false, message);
    }
}
