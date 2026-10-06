-- july-scheduler-task-param.sql — migration: add the task_param payload column
-- (2026.10.05, 同步调度接入：july_scheduler 透传 payload 给 JobHandler，如 {"syncCode":"s1"}）
-- 对已有库执行一次；新库直接用 july_scheduler011.sql。

ALTER TABLE july_scheduler
    ADD COLUMN task_param VARCHAR(500) NULL COMMENT '任务参数（JSON 文本，透传给 JobHandler 的 payload，如 {"syncCode":"s1"}）' AFTER scheduler_cron;
