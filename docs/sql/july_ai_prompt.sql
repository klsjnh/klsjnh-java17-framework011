-- ============================================================
-- july_ai_prompt / july_ai_prompt_detail — AI 中心 · 提示词（主子表）
-- 列顺序规范：id → 业务字段 → sort_order → status → 审计四列 → dr
-- 设计：docs/infrastructure011/015.ai-center/016.topic-prompt-templates.md
-- 说明：主 = 提示词（prompt_code 全局唯一）；子 = 业务域明细（每域一份正文）
-- ============================================================

CREATE TABLE IF NOT EXISTS july_ai_prompt (
    id          VARCHAR(33)  NOT NULL                COMMENT '主键',
    prompt_code VARCHAR(60)  NOT NULL                COMMENT '提示词编码（全局唯一，不可变）',
    prompt_name VARCHAR(100) NOT NULL                COMMENT '提示词名称',
    scene       VARCHAR(20)  NOT NULL DEFAULT 'inference' COMMENT '适用能力（inference / image / tts，仅分类）',
    sort_order  INT          NOT NULL DEFAULT 9999   COMMENT '排序（越小越靠前）',
    status      VARCHAR(3)   NOT NULL DEFAULT '1'    COMMENT '状态（0 停用 / 1 启用）',
    remark      VARCHAR(300) NULL                    COMMENT '备注',
    create_by   VARCHAR(33)  NULL                    COMMENT '创建人',
    update_by   VARCHAR(33)  NULL                    COMMENT '最后修改人',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
    dr          VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT '删除标记（0 正常 / 1 已删除）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_prompt_code (prompt_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 中心 - 提示词（主表）';

CREATE TABLE IF NOT EXISTS july_ai_prompt_detail (
    id            VARCHAR(33)  NOT NULL               COMMENT '主键',
    pk_mt         VARCHAR(33)  NOT NULL               COMMENT '主表链接（july_ai_prompt.id）',
    domain_code   VARCHAR(60)  NOT NULL               COMMENT '业务域（一个提示词可按业务域出多份内容）',
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
    PRIMARY KEY (id),
    UNIQUE KEY uk_pk_mt_domain_code (pk_mt, domain_code),
    KEY idx_pk_mt_sort (pk_mt, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 中心 - 提示词业务域明细（子表）';
