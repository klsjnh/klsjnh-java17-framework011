-- ============================================================
-- july_sync_rule / july_sync_rule_column — 数据源中心 · 同步规则（主子表）
-- 列顺序规范：id → 业务字段 → sort_order → status → 审计四列 → dr
-- 设计：docs/infrastructure011/017.datasource-center/016.topic-type-contract.md §017
-- 说明：主表 = 从哪到哪 + 判重键；子表 = 源列/源类型 → 目标列/目标类型
-- ============================================================

CREATE TABLE IF NOT EXISTS july_sync_rule (
    id             VARCHAR(33)   NOT NULL                COMMENT '主键',
    sync_code      VARCHAR(60)   NOT NULL                COMMENT '同步编码（全局唯一，不可变）',
    sync_name      VARCHAR(100)  NOT NULL                COMMENT '同步名称',
    source_ds_code VARCHAR(60)   NOT NULL                COMMENT '源头数据源编码（内部对象表 = 主库）',
    source_kind    VARCHAR(20)   NOT NULL DEFAULT 'sql'  COMMENT '源形态（sql / table / object）',
    source_data    VARCHAR(2000) NOT NULL                COMMENT '源数据（SQL 文本 / 表名 / 对象名）',
    target_ds_code VARCHAR(60)   NOT NULL                COMMENT '目标数据源编码',
    target_kind    VARCHAR(20)   NOT NULL DEFAULT 'table' COMMENT '目标形态（table / object）',
    target_data    VARCHAR(200)  NOT NULL                COMMENT '目标数据（表名 / 对象名）',
    mode           VARCHAR(20)   NOT NULL DEFAULT 'full' COMMENT '模式（full 全量 / incr 增量）',
    sync_key       VARCHAR(500)  NOT NULL                COMMENT '业务键（判重键）：目标侧列名 CSV',
    conflict       VARCHAR(20)   NOT NULL DEFAULT 'upsert' COMMENT '冲突策略（upsert / append）',
    options        VARCHAR(2000) NULL                    COMMENT 'JSON 扩展位（预留：group/parent/fk/rowTransform）',
    page_size      INT           NOT NULL DEFAULT 500    COMMENT '分页读取批大小',
    sort_order     INT           NOT NULL DEFAULT 9999   COMMENT '排序（越小越靠前）',
    status         VARCHAR(3)    NOT NULL DEFAULT '1'    COMMENT '状态（0 停用 / 1 启用）',
    remark         VARCHAR(300)  NULL                    COMMENT '备注',
    create_by      VARCHAR(33)   NULL                    COMMENT '创建人',
    update_by      VARCHAR(33)   NULL                    COMMENT '最后修改人',
    create_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    update_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
    dr             VARCHAR(3)    NOT NULL DEFAULT '0'    COMMENT '删除标记（0 正常 / 1 已删除）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sync_code (sync_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据源中心 - 同步规则（主表）';

CREATE TABLE IF NOT EXISTS july_sync_rule_column (
    id            VARCHAR(33)  NOT NULL               COMMENT '主键',
    pk_mt         VARCHAR(33)  NOT NULL               COMMENT '主表链接（july_sync_rule.id）',
    source_column VARCHAR(128) NOT NULL               COMMENT '原表列',
    source_type   VARCHAR(20)  NOT NULL DEFAULT 'string' COMMENT '原表列类型（中性类型 code）',
    target_column VARCHAR(128) NOT NULL               COMMENT '目标列',
    target_type   VARCHAR(20)  NOT NULL DEFAULT 'string' COMMENT '目标列类型（中性类型 code）',
    transform     VARCHAR(300) NULL                   COMMENT '可选值转换（trim / 日期格式 / 表达式）',
    sort_order    INT          NOT NULL DEFAULT 9999  COMMENT '排序（越小越靠前）',
    status        VARCHAR(3)   NOT NULL DEFAULT '1'   COMMENT '状态（0 停用 / 1 启用）',
    remark        VARCHAR(300) NULL                   COMMENT '备注',
    create_by     VARCHAR(33)  NULL                   COMMENT '创建人',
    update_by     VARCHAR(33)  NULL                   COMMENT '最后修改人',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
    dr            VARCHAR(3)   NOT NULL DEFAULT '0'   COMMENT '删除标记（0 正常 / 1 已删除）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_pk_mt_source_column (pk_mt, source_column),
    KEY idx_pk_mt_sort (pk_mt, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据源中心 - 同步列对照（明细表）';
