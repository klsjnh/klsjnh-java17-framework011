package com.klsjnh.infrastructure.system011.scheduler.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.klsjnh.infrastructure.persistence.entity.BasePo;

import com.baomidou.mybatisplus.annotation.TableName;

/*                JulySchedulerPo class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july scheduler po class
 *
 */


/**
 * Scheduled task persistence PO mapped to july_scheduler (system management -
 * scheduled task table).
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("july_scheduler")
public class JulySchedulerPo extends BasePo {

    /** Scheduler code, unique. */
    private String schedulerCode;

    /** Scheduler name. */
    private String schedulerName;

    /** Handler content: Spring bean name implementing Runnable. */
    private String schedulerHandler;

    /** Cron expression. */
    private String schedulerCron;

    /** Execute times, incremented on every trigger. */
    private Integer executeTimes;
}
