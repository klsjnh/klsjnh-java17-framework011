package com.klsjnh.web.system011.vo.julyscheduler;

/*                JulySchedulerVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july scheduler vo 011 class
 *      2026.09.15  moved into the julyscheduler sub-package
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Scheduled task response VO (detail and page rows).
 */

@Data
public class JulySchedulerVo011 {

    /** Primary key. */
    @Schema(description = "主键")
    private String id;

    /** Scheduler code, unique. */
    @Schema(description = "任务编码（唯一）")
    private String schedulerCode;

    /** Scheduler name. */
    @Schema(description = "任务名称")
    private String schedulerName;

    /** Handler content: Spring bean name implementing Runnable. */
    @Schema(description = "处理器内容（Spring Bean 名）")
    private String schedulerHandler;

    /** Cron expression. */
    @Schema(description = "cron 表达式")
    private String schedulerCron;

    /** Execute times, incremented on every trigger. */
    @Schema(description = "执行次数（触发即计）")
    private Integer executeTimes;

    /** Runtime status: 0 stopped / 1 running. */
    @Schema(description = "运行态（0 停止 / 1 运行）")
    private String status;

    /** Remark, max 300. */
    @Schema(description = "备注（最长 300）")
    private String remark;

    /** Creator. */
    @Schema(description = "创建人")
    private String createBy;

    /** Last modifier. */
    @Schema(description = "最后修改人")
    private String updateBy;

    /** Create time. */
    @Schema(description = "创建日期")
    private LocalDateTime createTime;

    /** Update time. */
    @Schema(description = "最后修改日期")
    private LocalDateTime updateTime;
}
