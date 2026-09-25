package com.klsjnh.infrastructure.system011.scheduler.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.klsjnh.infrastructure.persistence.entity.BasePo;
import com.klsjnh.infrastructure.persistence.entity.MasterLinked;

import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

/*                JulySchedulerAuditPo class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  july scheduler audit po class
 *
 */


/**
 * Execution-audit PO mapped to july_scheduler_audit (append-only child of
 * july_scheduler). Records run outcomes only — not start / stop. Master link
 * column is the framework-fixed {@code pk_mt} ({@link MasterLinked}).
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("july_scheduler_audit")
public class JulySchedulerAuditPo extends BasePo implements MasterLinked {

    /** Master link: july_scheduler.id (pk_mt). */
    private String pkMt;

    /** Task code snapshot at fire time. */
    private String schedulerCode;

    /** Run start time. */
    private LocalDateTime startTime;

    /** Run end time. */
    private LocalDateTime endTime;

    /** Execution result: SUCCESS / FAIL. */
    private String execStatus;

    /** Truncated failure message, nullable. */
    private String errorMessage;
}
