package com.klsjnh.domain.system011.scheduler;

/*                SchedulerExecStatus011 enum
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  scheduler exec status 011 enum
 *
 */

/**
 * Execution outcome bound to {@code july_scheduler_audit.exec_status}: one
 * vocabulary for run-result rows (cron fire or runOnce), so callers do not
 * hand-write SUCCESS / FAIL.
 */

public enum SchedulerExecStatus011 {

    /** Handler finished without throwing. */
    SUCCESS("SUCCESS"),

    /** Handler missing or threw; {@code error_message} holds a truncated hint. */
    FAIL("FAIL");

    /**
     * Persistence code.
     */
    private final String code;

    /**
     * Create the constant.
     *
     * @param code persistence code
     */
    SchedulerExecStatus011(String code) {
        this.code = code;
    }

    /**
     * Get the persistence code.
     *
     * @return persistence code
     */
    public String getCode() {
        return code;
    }

    /**
     * Resolve from a raw code; null when blank or unknown.
     *
     * @param value raw code
     * @return resolved constant, or null
     */
    public static SchedulerExecStatus011 of(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        for (SchedulerExecStatus011 status : values()) {
            if (status.code.equals(value)) {
                return status;
            }
        }

        return null;
    }
}
