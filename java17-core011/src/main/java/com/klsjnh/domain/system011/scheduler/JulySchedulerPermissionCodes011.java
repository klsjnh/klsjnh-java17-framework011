package com.klsjnh.domain.system011.scheduler;

/*                JulySchedulerPermissionCodes011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.24
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.24  july scheduler permission codes (P3 blueprint)
 *
 */

/**
 * Permission codes for the julyScheduler blueprint — must match catalog seed.
 */

public final class JulySchedulerPermissionCodes011 {

    /**
     * View (getById / selectListByPage).
     */
    public static final String SELECT = "system011:julyScheduler:select";

    /**
     * Insert.
     */
    public static final String INSERT = "system011:julyScheduler:insert";

    /**
     * Update.
     */
    public static final String UPDATE = "system011:julyScheduler:update";

    /**
     * Logic delete.
     */
    public static final String LOGIC_DELETE = "system011:julyScheduler:logicDelete";

    /**
     * Start task.
     */
    public static final String START = "system011:julyScheduler:start";

    /**
     * Stop task.
     */
    public static final String STOP = "system011:julyScheduler:stop";

    /**
     * Run once.
     */
    public static final String EXECUTE_ONCE = "system011:julyScheduler:executeOnce";

    private JulySchedulerPermissionCodes011() {
    }
}
