-- ============================================================
-- july_config — 系统管理-配置管理（运行时可变键值参数，改库即生效）
-- 列顺序规范：id → 业务字段 → sort_order（有排序需求时）→ status → 审计四列 → dr
-- 设计：docs/requirement011/023.topic-july-config.md
-- 边界：krt.* 框架配置走 application.yml（KrtConfig011），同一配置只允许一个家
-- ============================================================

CREATE TABLE IF NOT EXISTS july_config (
    id                 VARCHAR(33)  NOT NULL                COMMENT '主键',
    code               VARCHAR(60)  NOT NULL                COMMENT '配置项（全局唯一，程序按此读取）',
    data               VARCHAR(300) NOT NULL                COMMENT '配置值（全 String，消费方自行解析）',
    status             VARCHAR(3)   NOT NULL DEFAULT '1'    COMMENT '状态（0 停用 / 1 启用，停用即不生效）',
    remark             VARCHAR(300) NULL                    COMMENT '备注',
    create_by          VARCHAR(33)  NULL                    COMMENT '创建人',
    update_by          VARCHAR(33)  NULL                    COMMENT '最后修改人',
    create_time        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    update_time        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
    dr                 VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT '删除标记（0 正常 / 1 已删除）',
    alive_code         VARCHAR(60)  GENERATED ALWAYS AS (IF(dr = '0', code, NULL)) STORED COMMENT '存活唯一键（dr=0 时=code）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (alive_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统管理 - 配置管理';
