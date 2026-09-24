-- ============================================================
-- 数据源中心 · 数据源管理 + 同步规则（共 3 表）
-- 合并自 july_datasource.sql / july_sync_rule.sql
-- （2026-09-24，按中心归并；文件名 july_center_datasource）
-- 列顺序规范：id → 业务字段 → sort_order（有排序需求时）→ status → 审计四列 → dr
-- 设计：docs/infrastructure011/017.datasource-center/
--       docs/requirement011/026.topic-datasource.md
-- 方案：docs/requirement013/026.topic-datasource.md
--       docs/requirement013/031.topic-datasource-center.md
-- 边界：krt.ci011（yaml）为引导数据源；同一 dsCode 只允许一个家；
--       july_datasource 表为运行时唯一真源
-- 组成：数据源（连接注册）+ 同步规则（主）+ 同步列对照（子）
-- 表顺序：无库级 FK；按逻辑依赖排列（数据源 → 同步主表 → 同步子表）。
-- ============================================================

-- ------------------------------------------------------------
-- 数据源管理（运行时可变业务库连接注册）
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS july_datasource (
    id            VARCHAR(33)  NOT NULL                COMMENT '主键',
    ds_code       VARCHAR(60)  NOT NULL                COMMENT '数据源编码（全局唯一，不可变，连接池名）',
    ds_name       VARCHAR(100) NOT NULL                COMMENT '数据源名称',
    db_type       VARCHAR(20)  NOT NULL DEFAULT 'mysql' COMMENT '数据库类型（mysql/oracle/sqlserver/postgresql）',
    jdbc_url      VARCHAR(500) NOT NULL                COMMENT 'JDBC URL（须以 jdbc: 开头）',
    schema_name   VARCHAR(60)  NULL                    COMMENT '库名或 Schema',
    username      VARCHAR(100) NULL                    COMMENT '用户名',
    password      VARCHAR(300) NULL                    COMMENT '密码（出参不回显，由 Converter 收口剔除）',
    driver_class  VARCHAR(200) NULL                    COMMENT '驱动类名（为空时按 db_type 取 DatabaseTypes011 默认值）',
    pool_config   VARCHAR(500) NULL                    COMMENT '连接池 JSON（本期预留，不解析，走代码默认值）',
    remark        VARCHAR(300) NULL                    COMMENT '备注',
    sort_order    INT          NOT NULL DEFAULT 9999   COMMENT '排序（越小越靠前）',
    status        VARCHAR(3)   NOT NULL DEFAULT '1'    COMMENT '状态（0 停用 / 1 启用，停用即从运行时注册表摘除）',
    create_by     VARCHAR(33)  NULL                    COMMENT '创建人',
    update_by     VARCHAR(33)  NULL                    COMMENT '最后修改人',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
    dr            VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT '删除标记（0 正常 / 1 已删除）',
    alive_ds_code VARCHAR(60)  GENERATED ALWAYS AS (IF(dr = '0', ds_code, NULL)) STORED COMMENT '存活唯一键（dr=0 时=ds_code）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_ds_code (alive_ds_code),
    KEY idx_ds_code_status (ds_code, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据源中心 - 数据源管理';

-- ------------------------------------------------------------
-- 同步规则（主表：从哪到哪 + 判重键）
-- ------------------------------------------------------------

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
    alive_sync_code VARCHAR(60)  GENERATED ALWAYS AS (IF(dr = '0', sync_code, NULL)) STORED COMMENT '存活唯一键（dr=0 时=sync_code）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_sync_code (alive_sync_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据源中心 - 同步规则（主表）';

-- ------------------------------------------------------------
-- 同步列对照（子表：源列/源类型 → 目标列/目标类型）
-- ------------------------------------------------------------

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
    alive_key     VARCHAR(200) GENERATED ALWAYS AS (IF(dr = '0', CONCAT_WS('#', pk_mt, source_column), NULL)) STORED COMMENT '存活唯一键（dr=0 时=pk_mt#source_column）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_pk_mt_source_column (alive_key),
    KEY idx_pk_mt_sort (pk_mt, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据源中心 - 同步列对照（明细表）';
