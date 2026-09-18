-- ============================================================
-- july_ai_model_provider / july_ai_model_provider_api — AI 模型接入（主子表）
-- 列顺序规范：id → 业务字段 → sort_order（有排序需求时）→ status → 审计四列 → dr
-- 设计：docs/requirement011/036.topic-ai011.md
-- 方案：docs/requirement013/036.topic-ai011.md
-- 边界：api_key 出参不回显（VO 类型层无该字段）；本期明文入库，加密留二期
-- ============================================================

CREATE TABLE IF NOT EXISTS july_ai_model_provider (
    id            VARCHAR(33)  NOT NULL                COMMENT '主键',
    provider_code VARCHAR(60)  NOT NULL                COMMENT '提供商编码（全局唯一，不可变）',
    provider_name VARCHAR(100) NOT NULL                COMMENT '提供商名称',
    base_url      VARCHAR(300) NOT NULL                COMMENT '接口 Base URL（OpenAI 兼容）',
    models        VARCHAR(500) NULL                    COMMENT '模型清单（逗号分隔）',
    sort_order    INT          NOT NULL DEFAULT 9999   COMMENT '排序（越小越靠前）',
    status        VARCHAR(3)   NOT NULL DEFAULT '1'    COMMENT '状态（0 停用 / 1 启用）',
    remark        VARCHAR(300) NULL                    COMMENT '备注',
    create_by     VARCHAR(33)  NULL                    COMMENT '创建人',
    update_by     VARCHAR(33)  NULL                    COMMENT '最后修改人',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
    dr            VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT '删除标记（0 正常 / 1 已删除）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_provider_code (provider_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模型接入011 - 提供商管理';

CREATE TABLE IF NOT EXISTS july_ai_model_provider_api (
    id          VARCHAR(33)  NOT NULL                COMMENT '主键',
    pk_mt       VARCHAR(33)  NOT NULL                COMMENT '主表链接（july_ai_model_provider.id）',
    api_code    VARCHAR(60)  NOT NULL                COMMENT '密钥编码（同提供商内唯一，不可变）',
    api_name    VARCHAR(100) NOT NULL                COMMENT '密钥名称',
    api_key     VARCHAR(300) NOT NULL                COMMENT 'API Key（出参不回显）',
    sort_order  INT          NOT NULL DEFAULT 9999   COMMENT '排序（越小越靠前）',
    status      VARCHAR(3)   NOT NULL DEFAULT '1'    COMMENT '状态（0 停用 / 1 启用）',
    remark      VARCHAR(300) NULL                    COMMENT '备注',
    create_by   VARCHAR(33)  NULL                    COMMENT '创建人',
    update_by   VARCHAR(33)  NULL                    COMMENT '最后修改人',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
    dr          VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT '删除标记（0 正常 / 1 已删除）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_pk_mt_api_code (pk_mt, api_code),
    KEY idx_pk_mt_sort (pk_mt, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模型接入011 - 提供商密钥';
