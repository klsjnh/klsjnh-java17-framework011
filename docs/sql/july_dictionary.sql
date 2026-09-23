-- ============================================================
-- july_dictionary / july_dictionary_item — 系统管理-数据字典（主子表）
-- 列顺序规范：id → 业务字段 → sort_order（有排序需求时）→ status → 审计四列 → dr
-- 设计：docs/requirement011/027.topic-dictionary.md
-- 方案：docs/requirement013/027.topic-dictionary.md
-- 边界：程序读入口 getByType（无 HTTP 端点）；本期不加缓存，每次查库
-- 唯一键：业务 UNIQUE 须落在 alive_* 生成列（见 base-entity-columns.sql / 031/020 任务）
-- ============================================================

CREATE TABLE IF NOT EXISTS july_dictionary (
    id               VARCHAR(33)  NOT NULL                COMMENT '主键',
    dictionary_code  VARCHAR(60)  NOT NULL                COMMENT '字典编码（全局唯一，不可变，程序按此读取）',
    dictionary_name  VARCHAR(100) NOT NULL                COMMENT '字典名称',
    sort_order       INT          NOT NULL DEFAULT 0      COMMENT '排序（升序）',
    status           VARCHAR(3)   NOT NULL DEFAULT '1'    COMMENT '状态（0 停用 / 1 启用，停用即对程序不可见）',
    remark           VARCHAR(300) NULL                    COMMENT '备注',
    create_by        VARCHAR(33)  NULL                    COMMENT '创建人',
    update_by        VARCHAR(33)  NULL                    COMMENT '最后修改人',
    create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    update_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
    dr               VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT '删除标记（0 正常 / 1 已删除）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_dictionary_code (dictionary_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统管理 - 数据字典';

CREATE TABLE IF NOT EXISTS july_dictionary_item (
    id           VARCHAR(33)  NOT NULL                COMMENT '主键',
    pk_mt        VARCHAR(33)  NOT NULL                COMMENT '主表链接（july_dictionary.id）',
    item_code    VARCHAR(60)  NOT NULL                COMMENT '字典项编码（同字典内唯一，不可变）',
    item_label   VARCHAR(100) NOT NULL                COMMENT '字典项名称',
    sort_order   INT          NOT NULL DEFAULT 0      COMMENT '排序（升序）',
    status       VARCHAR(3)   NOT NULL DEFAULT '1'    COMMENT '状态（0 停用 / 1 启用）',
    remark       VARCHAR(300) NULL                    COMMENT '备注',
    create_by    VARCHAR(33)  NULL                    COMMENT '创建人',
    update_by    VARCHAR(33)  NULL                    COMMENT '最后修改人',
    create_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    update_time  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
    dr           VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT '删除标记（0 正常 / 1 已删除）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_pk_mt_item_code (pk_mt, item_code),
    KEY idx_pk_mt_sort (pk_mt, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统管理 - 数据字典明细';
