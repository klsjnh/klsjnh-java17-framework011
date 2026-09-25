-- ============================================================
-- 平台中心 · 系统配置 + 数据字典（共 3 表）
-- 合并自 july_config.sql / july_dictionary.sql
-- （2026-09-26，按中心归并；文件名 july_center_platform011）
-- 列顺序规范：id → 业务字段 → sort_order（有排序需求时）→ status → 审计四列 → dr
-- 设计：docs/infrastructure011/014.platform-center/
--       docs/requirement011/023.topic-july-config.md
--       docs/requirement011/027.topic-dictionary.md
-- 方案：docs/requirement013/027.topic-dictionary.md
-- 边界：krt.* 框架配置走 application.yml（KrtConfig011），同一配置只允许一个家；
--       字典程序读入口 getByType（无 HTTP 端点）；本期不加缓存，每次查库
-- 组成：系统配置（键值）+ 字典主表 + 字典明细
-- 表顺序：无库级 FK；配置独立；字典主 → 字典子。
-- 备份/导出/导入：无独立物理表（platform011 内核写对象存储），本文件不含 backup DDL。
-- ============================================================

-- ------------------------------------------------------------
-- 系统配置（运行时可变键值参数，改库即生效）
-- ------------------------------------------------------------

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台中心 - 配置管理';

-- ------------------------------------------------------------
-- 数据字典（主）/ 字典明细（子）
-- ------------------------------------------------------------

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
    alive_dictionary_code VARCHAR(60) GENERATED ALWAYS AS (IF(dr = '0', dictionary_code, NULL)) STORED COMMENT '存活唯一键（dr=0 时=dictionary_code）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_dictionary_code (alive_dictionary_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台中心 - 数据字典';

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
    alive_key    VARCHAR(200) GENERATED ALWAYS AS (IF(dr = '0', CONCAT_WS('#', pk_mt, item_code), NULL)) STORED COMMENT '存活唯一键（dr=0 时=pk_mt#item_code）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_pk_mt_item_code (alive_key),
    KEY idx_pk_mt_sort (pk_mt, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台中心 - 数据字典明细';