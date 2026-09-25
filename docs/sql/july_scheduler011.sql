-- ============================================================
-- july_scheduler / july_scheduler_audit — 调度技术栈 DDL（非「调度中心」）
-- 归属：java17-scheduler-quartz011-starter（Quartz 内存模式事实源，重启按 status 重注册）
-- 2026-09-26：文件更名为 july_scheduler011.sql（对齐 quartz011 starter；勿命名为 july_center_*）
-- 2026-09-26：主子表 — 定义 july_scheduler + 执行审计 july_scheduler_audit（append-only）
-- 列顺序规范：id → 业务字段 → sort_order（有排序需求时）→ status → 审计四列 → dr
-- 设计：docs/infrastructure011/022.topic-july-scheduler.md
--       docs/requirement011/022.topic-july-scheduler.md · requirement013/022
-- ============================================================

CREATE TABLE IF NOT EXISTS july_scheduler (
    id                 VARCHAR(33)  NOT NULL                COMMENT '主键',
    scheduler_code     VARCHAR(30)  NOT NULL                COMMENT '任务编码',
    scheduler_name     VARCHAR(60)  NOT NULL                COMMENT '任务名称',
    scheduler_handler  VARCHAR(300) NOT NULL                COMMENT '处理器内容（JobHandler Bean 名）',
    scheduler_cron     VARCHAR(30)  NOT NULL                COMMENT 'cron 表达式',
    execute_times      INT          NOT NULL DEFAULT 0      COMMENT '执行次数（触发即计）',
    status             VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT '运行态（0 停止 / 1 运行）——本表业务约定默认 0',
    remark             VARCHAR(300) NULL                    COMMENT '备注',
    create_by          VARCHAR(33)  NULL                    COMMENT '创建人',
    update_by          VARCHAR(33)  NULL                    COMMENT '最后修改人',
    create_time        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    update_time        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
    dr                 VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT '删除标记（0 正常 / 1 已删除）',
    alive_scheduler_code VARCHAR(30) GENERATED ALWAYS AS (IF(dr = '0', scheduler_code, NULL)) STORED COMMENT '存活唯一键（dr=0 时=scheduler_code）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_scheduler_code (alive_scheduler_code),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='调度技术栈 - 定时任务（quartz011）';

-- 执行审计子表（append-only）：仅记录实际跑完的 job 结果（cron 触发或「执行一次」）
-- 启停属操作者管理动作 → july_user_audit（@AuditLog），不入本表、不入平台审计
CREATE TABLE IF NOT EXISTS july_scheduler_audit (
    id                 VARCHAR(33)  NOT NULL                COMMENT '主键',
    pk_mt              VARCHAR(33)  NOT NULL                COMMENT '主表链接（july_scheduler.id）',
    scheduler_code     VARCHAR(30)  NULL                    COMMENT '任务编码快照',
    start_time         DATETIME     NOT NULL                COMMENT '开始时间',
    end_time           DATETIME     NULL                    COMMENT '结束时间',
    exec_status        VARCHAR(10)  NOT NULL                COMMENT '执行结果（SUCCESS/FAIL）',
    error_message      VARCHAR(500) NULL                    COMMENT '失败信息（截断）',
    status             VARCHAR(3)   NOT NULL DEFAULT '1'    COMMENT '状态',
    create_by          VARCHAR(33)  NULL                    COMMENT '创建人',
    update_by          VARCHAR(33)  NULL                    COMMENT '最后修改人',
    create_time        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期（≈事件时间）',
    update_time        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
    dr                 VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT '删除标记（设计不删，仅结构一致）',
    PRIMARY KEY (id),
    KEY idx_pk_mt_time (pk_mt, start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='调度技术栈 - 执行审计（append-only 子表）';