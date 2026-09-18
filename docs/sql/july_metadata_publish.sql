-- ============================================================
-- 039 低代码设计器与运行时 —— 数据模型增量（一期一次到位）
-- 设计：docs/requirement011/039.topic-lowcode-designer-runtime.md
-- 方案：docs/requirement013/039.topic-lowcode-designer-runtime.md
-- 边界：july_metadata 加发布态列（DRAFT 编辑态）；新增发布快照 + 开放 API 配置
-- ============================================================

-- 1) july_metadata 加发布态列（编辑态 = DRAFT；发布指针 + 探测来源）
ALTER TABLE july_metadata
    ADD COLUMN publish_status    VARCHAR(20)  NOT NULL DEFAULT 'DRAFT' COMMENT '发布状态（DRAFT / PUBLISHED）',
    ADD COLUMN version           VARCHAR(20)  NULL                    COMMENT '当前已发布版本（如 0.0.1）',
    ADD COLUMN physical_table    VARCHAR(60)  NULL                    COMMENT '物理表名（= 对象名）',
    ADD COLUMN probe_sql         TEXT          NULL                    COMMENT '探测 SQL（来源）',
    ADD COLUMN sql_code          TEXT          NULL                    COMMENT '模型 SQL 编码',
    ADD COLUMN data_source_code  VARCHAR(60)  NULL                    COMMENT '探测数据源代码',
    ADD COLUMN data_initialized  VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT '数据是否已初始化（0 否 / 1 是）',
    ADD COLUMN last_sync_at      DATETIME     NULL                    COMMENT '最近一次同步时间';

-- 2) 发布快照：物理表结构由发布那一刻的元数据驱动（不可变）
CREATE TABLE IF NOT EXISTS july_metadata_version (
    id              VARCHAR(33)  NOT NULL                COMMENT '主键',
    object_name     VARCHAR(60)  NOT NULL                COMMENT '低代码对象名',
    version         VARCHAR(20)  NOT NULL                COMMENT '版本（0.0.1 递增）',
    payload_json    MEDIUMTEXT   NOT NULL                COMMENT 'MetaDTO 快照（metaData+fieldData+displayData+serviceData）',
    physical_table  VARCHAR(60)  NOT NULL                COMMENT '物理表名（= 对象名）',
    ddl_text        MEDIUMTEXT   NULL                    COMMENT '本次发布执行的 DDL（逐条，分号分隔）',
    publish_status  VARCHAR(20)  NOT NULL DEFAULT 'PUBLISHED' COMMENT '快照状态（仅 PUBLISHED；实现为无状态幂等，无 PENDING 流程）',
    published_by    VARCHAR(33)  NULL                    COMMENT '发布人',
    published_at    DATETIME     NULL                    COMMENT '发布时间',
    sort_order      INT          NOT NULL DEFAULT 9999   COMMENT '排序',
    status          VARCHAR(3)   NOT NULL DEFAULT '1'    COMMENT '状态（0 停用 / 1 启用）',
    create_by       VARCHAR(33)  NULL                    COMMENT '创建人',
    update_by       VARCHAR(33)  NULL                    COMMENT '最后修改人',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
    dr              VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT '删除标记（0 正常 / 1 已删除）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_object_version (object_name, version),
    KEY idx_object_name (object_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='低代码核心 - 发布快照';

-- 3) 开放 API 配置（apiKey 打码出参；轮换首次回明文一次）
CREATE TABLE IF NOT EXISTS july_metadata_open_api (
    id                VARCHAR(33)  NOT NULL                COMMENT '主键',
    object_name       VARCHAR(60)  NOT NULL                COMMENT '低代码对象名',
    enabled           VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT '是否启用（0 否 / 1 是）',
    auth_mode         VARCHAR(20)  NOT NULL DEFAULT 'closed' COMMENT '鉴权（closed / none / apiKey）',
    allowed_ops       VARCHAR(300) NULL                    COMMENT '允许操作（query/insert/update/delete 逗号分隔）',
    api_key           VARCHAR(300) NULL                    COMMENT 'API Key（出参打码 ******）',
    api_key_updated_at DATETIME    NULL                    COMMENT 'API Key 最近轮换时间',
    sort_order        INT          NOT NULL DEFAULT 9999   COMMENT '排序',
    status            VARCHAR(3)   NOT NULL DEFAULT '1'    COMMENT '状态（0 停用 / 1 启用）',
    create_by         VARCHAR(33)  NULL                    COMMENT '创建人',
    update_by         VARCHAR(33)  NULL                    COMMENT '最后修改人',
    create_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    update_time       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
    dr                VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT '删除标记（0 正常 / 1 已删除）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_object_name (object_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='低代码核心 - 开放 API 配置';
