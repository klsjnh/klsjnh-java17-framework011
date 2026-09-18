-- ============================================================
-- july_demo011 — demo11 纵切面参考样板的物理表
-- 用途：第三方接入的「参考 PO / 聚合」演示（非平台业务表）
-- 列顺序规范：id → 业务字段 → sort_order（有排序需求时）→ status → 审计四列 → dr
-- 参考：docs/infrastructure011/013.topic-project-structure.md · Agent.md（demo11 纵切面）
-- 备注：唯一键直接建业务列（未用 alive_* 生成列；demo 表沿用最简形态）
-- ============================================================

CREATE TABLE IF NOT EXISTS july_demo011 (
    id          VARCHAR(33)  NOT NULL                COMMENT '主键',
    code        VARCHAR(30)  NOT NULL                COMMENT '编码',
    name        VARCHAR(60)  NOT NULL                COMMENT '名称',
    status      VARCHAR(3)   NOT NULL DEFAULT '1'    COMMENT '状态（0 停用 / 1 启用）',
    create_by   VARCHAR(33)  NULL                    COMMENT '创建人',
    update_by   VARCHAR(33)  NULL                    COMMENT '最后修改人',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
    dr          VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT '删除标记（0 正常 / 1 已删除）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='示例-演示表';
