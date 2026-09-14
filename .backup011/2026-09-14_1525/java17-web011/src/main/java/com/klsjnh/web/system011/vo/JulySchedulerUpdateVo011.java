package com.klsjnh.web.system011.vo;

/*                JulySchedulerUpdateVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july scheduler update vo 011 class
 *
 */

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Update request VO for a scheduled task; the scheduler code is immutable.
 */

@Data
public class JulySchedulerUpdateVo011 {

    /** Primary key. */
    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    /** Scheduler name, max 60. */
    @Schema(description = "任务名称（最长 60）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String schedulerName;

    /** Handler content: Spring bean name implementing Runnable, max 300. */
    @Schema(description = "处理器内容：Spring 容器内实现 Runnable 的 Bean 名（最长 300）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String schedulerHandler;

    /** Cron expression, max 30, validated on save. */
    @Schema(description = "cron 表达式（最长 30，保存时校验合法性）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String schedulerCron;

    /** Runtime status: 0 stopped / 1 running; changes resync the engine. */
    @Schema(description = "运行态（0 停止 / 1 运行）；状态或 cron/handler 变更会触发调度引擎联动")
    private String status;
}
