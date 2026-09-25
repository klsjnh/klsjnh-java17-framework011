package com.klsjnh.domain.datasource.kernel;


import com.klsjnh.domain.datasource.kernel.ConnectionInfo;
/*                DataSourceProbePort interface
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  data source probe port interface
 *
 */

/**
 * Connectivity probe port: opens exactly one connection to a candidate target
 * through a throwaway pool, then closes it. A probe NEVER declares a datasource
 * and NEVER caches a pool — testing connectivity must not change runtime state.
 */

public interface DataSourceProbePort {

    /**
     * Probe a candidate connection config.
     *
     * @param info connection info
     * @return probe result, never null
     */
    ProbeResult probe(ConnectionInfo info);

    /**
     * Connectivity probe result.
     * <p>
     * {@code success=false} is a normal outcome: the probe ACTION succeeded,
     * the target was simply unreachable — the web layer maps it to HTTP 200
     * with this flag false, not to an error status code.
     * </p>
     *
     * @param success         whether a connection could be opened
     * @param message         client-safe hint (never carries the password)
     * @param databaseProduct database product name, null on failure
     * @param databaseVersion database product version, null on failure
     */
    record ProbeResult(boolean success, String message, String databaseProduct, String databaseVersion) {

        /**
         * Build a success result.
         *
         * @param product database product name
         * @param version database product version
         * @return success result
         */
        public static ProbeResult ok(String product, String version) {
            return new ProbeResult(true, "connection success", product, version);
        }

        /**
         * Build a failure result.
         *
         * @param message client-safe failure hint
         * @return failure result
         */
        public static ProbeResult fail(String message) {
            return new ProbeResult(false, message, null, null);
        }
    }
}
