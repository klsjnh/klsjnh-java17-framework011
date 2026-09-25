package com.klsjnh.web.system011.vo.julyscheduler;

/*                JulySchedulerInsertVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july scheduler insert vo 011 class
 *      2026.09.15  moved into the julyscheduler sub-package
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Insert request VO for a scheduled task (new tasks default to stopped).
 */

@Data
public class JulySchedulerInsertVo011 {

    /** Scheduler code, unique, max 30. */
    @Schema(description = "任务编码（唯一，最长 30）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String schedulerCode;

    /** Scheduler name, max 60. */
    @Schema(description = "任务名称（最长 60）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String schedulerName;

    /** Handler content: Spring bean name implementing Runnable, max 300. */
    @Schema(description = "处理器内容：Spring 容器内实现 Runnable 的 Bean 名（最长 300）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String schedulerHandler;

    /** Cron expression, max 30, validated on save. */
    @Schema(description = "cron 表达式（最长 30，保存时校验合法性）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String schedulerCron;

    /** Remark, max 300. */
    @Schema(description = "备注（最长 300）")
    private String remark;
}
