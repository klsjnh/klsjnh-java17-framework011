-- ============================================================
-- AI 中心 · 模型接入 + 提示词（共 4 表）
-- 合并自 july_ai_model_provider.sql / july_ai_domain.sql（2026-09-21，按中心归并）
-- 列顺序规范：id → 业务字段 → sort_order（有排序需求时）→ status → 审计四列 → dr
-- 设计：docs/infrastructure011/015.ai-center/（011 设计思路 / 013 整体架构 / 015 怎么使用）
-- 边界：api_key 出参不回显（VO 类型层无该字段）；本期明文入库，加密留二期
-- 组成：模型接入（provider 主 + api 子）+ 提示词（domain 主树 + domain_prompt 子）
-- ============================================================

-- ------------------------------------------------------------
-- 模型接入：提供商 / 提供商密钥（主子表）
-- 方案：docs/requirement013/028.topic-aicenter.md · 调用：030.topic-aicenter-invoke.md
-- ------------------------------------------------------------

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
    alive_provider_code VARCHAR(60) GENERATED ALWAYS AS (IF(dr = '0', provider_code, NULL)) STORED COMMENT '存活唯一键（dr=0 时=provider_code）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_provider_code (alive_provider_code)
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
    alive_key   VARCHAR(200) GENERATED ALWAYS AS (IF(dr = '0', CONCAT_WS('#', pk_mt, api_code), NULL)) STORED COMMENT '存活唯一键（dr=0 时=pk_mt#api_code）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_pk_mt_api_code (alive_key),
    KEY idx_pk_mt_sort (pk_mt, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模型接入011 - 提供商密钥';

-- ------------------------------------------------------------
-- 提示词：业务域（主 · 树）/ 提示词（子）
-- 设计：docs/infrastructure011/015.ai-center/016.topic-prompt-templates.md
-- 说明：主 = 业务域（树 + 排序，domain_code 全局唯一）；子 = 提示词（prompt_code 全局唯一）
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS july_ai_domain (
    id          VARCHAR(33)  NOT NULL                COMMENT '主键',
    domain_code VARCHAR(60)  NOT NULL                COMMENT '域编码（全局唯一，不可变）',
    domain_name VARCHAR(100) NOT NULL                COMMENT '域名称',
    parent_id   VARCHAR(33)  NOT NULL DEFAULT ''     COMMENT '上级域 id（空串为根）',
    sort_order  INT          NOT NULL DEFAULT 9999   COMMENT '排序（越小越靠前）',
    status      VARCHAR(3)   NOT NULL DEFAULT '1'    COMMENT '状态（0 停用 / 1 启用）',
    remark      VARCHAR(300) NULL                    COMMENT '备注',
    create_by   VARCHAR(33)  NULL                    COMMENT '创建人',
    update_by   VARCHAR(33)  NULL                    COMMENT '最后修改人',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
    dr          VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT '删除标记（0 正常 / 1 已删除）',
    alive_domain_code VARCHAR(60) GENERATED ALWAYS AS (IF(dr = '0', domain_code, NULL)) STORED COMMENT '存活唯一键（dr=0 时=domain_code）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_domain_code (alive_domain_code),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 中心 - 提示词业务域（主表 · 树）';

CREATE TABLE IF NOT EXISTS july_ai_domain_prompt (
    id            VARCHAR(33)  NOT NULL               COMMENT '主键',
    pk_mt         VARCHAR(33)  NOT NULL               COMMENT '主表链接（july_ai_domain.id）',
    prompt_code   VARCHAR(60)  NOT NULL               COMMENT '提示词编码（全局唯一，不可变）',
    prompt_name   VARCHAR(100) NOT NULL               COMMENT '提示词名称',
    scene         VARCHAR(20)  NOT NULL DEFAULT 'inference' COMMENT '适用能力（inference / image / tts，仅分类）',
    content_mode  VARCHAR(20)  NOT NULL DEFAULT 'inline' COMMENT '内容模式（inline / storage）',
    content       TEXT         NULL                   COMMENT '正文（inline 时用；TEXT≈2.1万汉字）',
    storage_code  VARCHAR(60)  NULL                   COMMENT '存储实例（storage 时选，缺省=默认实例）',
    bucket        VARCHAR(100) NULL                   COMMENT '桶（storage 时用，约定 ai-prompt）',
    object_key    VARCHAR(300) NULL                   COMMENT '对象 key（平台生成）',
    content_hash  VARCHAR(64)  NULL                   COMMENT '内容 hash（变更检测 / 去重）',
    content_size  BIGINT       NULL                   COMMENT '字节数',
    variables     VARCHAR(1000) NULL                  COMMENT '变量声明（可空）',
    sort_order    INT          NOT NULL DEFAULT 9999  COMMENT '排序（越小越靠前）',
    status        VARCHAR(3)   NOT NULL DEFAULT '1'   COMMENT '状态（0 停用 / 1 启用）',
    remark        VARCHAR(300) NULL                   COMMENT '备注',
    create_by     VARCHAR(33)  NULL                   COMMENT '创建人',
    update_by     VARCHAR(33)  NULL                   COMMENT '最后修改人',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
    dr            VARCHAR(3)   NOT NULL DEFAULT '0'   COMMENT '删除标记（0 正常 / 1 已删除）',
    alive_prompt_code VARCHAR(60) GENERATED ALWAYS AS (IF(dr = '0', prompt_code, NULL)) STORED COMMENT '存活唯一键（dr=0 时=prompt_code）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_prompt_code (alive_prompt_code),
    KEY idx_pk_mt_sort (pk_mt, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 中心 - 提示词（子表）';
