-- ============================================================
-- july_scheduler — 系统管理-定时任务（Quartz 内存模式的事实源，重启按 status 重注册）
-- 列顺序规范：id → 业务字段 → status → 审计四列 → dr
-- 设计：docs/requirement011/021.topic-july-scheduler.md（技术方案同号）
-- ============================================================

CREATE TABLE IF NOT EXISTS july_scheduler (
    id                 VARCHAR(33)  NOT NULL                COMMENT '主键',
    scheduler_code     VARCHAR(30)  NOT NULL                COMMENT '任务编码',
    scheduler_name     VARCHAR(60)  NOT NULL                COMMENT '任务名称',
    scheduler_handler  VARCHAR(300) NOT NULL                COMMENT '处理器内容（JobHandler Bean 名）',
    scheduler_cron     VARCHAR(30)  NOT NULL                COMMENT 'cron 表达式',
    execute_times      INT          NOT NULL DEFAULT 0      COMMENT '执行次数（触发即计）',
    status             VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT '运行态（0 停止 / 1 运行）——本表业务约定默认 0',
    create_by          VARCHAR(33)  NULL                    COMMENT '创建人',
    update_by          VARCHAR(33)  NULL                    COMMENT '最后修改人',
    create_time        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    update_time        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
    dr                 VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT '删除标记（0 正常 / 1 已删除）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_scheduler_code (scheduler_code),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统管理 - 定时任务';
