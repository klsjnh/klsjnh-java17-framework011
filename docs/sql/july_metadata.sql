-- ============================================================
-- july_metadata / _field / _display / _service — 低代码核心（一主三子）
-- 列顺序规范：id → 业务字段 → status → 审计四列 → dr
-- 设计：docs/requirement011/038.topic-lowcode-core.md
-- 方案：docs/requirement013/038.topic-lowcode-core.md
-- 边界：只做 CRUD；不生成物理表 / 代码 / 页面；四表均带 sort_order
-- ============================================================

CREATE TABLE IF NOT EXISTS july_metadata (
    id             VARCHAR(33)  NOT NULL                COMMENT '主键',
    sort_order     INT          NOT NULL DEFAULT 9999   COMMENT '排序（越小越靠前）',
    object_name    VARCHAR(60)  NOT NULL                COMMENT '低代码对象名（全局唯一，不可变）',
    object_type    VARCHAR(20)  NOT NULL DEFAULT 'type011' COMMENT '对象类型（ObjectType011）',
    description    VARCHAR(300) NULL                    COMMENT '对象描述',
    business_field VARCHAR(300) NULL                    COMMENT '参与导入导出的业务字段清单',
    package_name   VARCHAR(300) NULL                    COMMENT '目标包名',
    router_path    VARCHAR(300) NULL                    COMMENT '前端路由',
    remark         VARCHAR(300) NULL                    COMMENT '备注',
    status         VARCHAR(3)   NOT NULL DEFAULT '1'    COMMENT '状态（0 停用 / 1 启用）',
    create_by      VARCHAR(33)  NULL                    COMMENT '创建人',
    update_by      VARCHAR(33)  NULL                    COMMENT '最后修改人',
    create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
    dr             VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT '删除标记（0 正常 / 1 已删除）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_object_name (object_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='低代码核心 - 对象元数据';

CREATE TABLE IF NOT EXISTS july_metadata_field (
    id             VARCHAR(33)  NOT NULL                COMMENT '主键',
    pk_mt          VARCHAR(33)  NOT NULL                COMMENT '主表链接（july_metadata.id）',
    sort_order     INT          NOT NULL DEFAULT 9999   COMMENT '排序（越小越靠前）',
    field_code     VARCHAR(60)  NOT NULL                COMMENT '字段编码（同对象内唯一）',
    field_name     VARCHAR(60)  NOT NULL                COMMENT '字段名称',
    field_type     VARCHAR(30)  NOT NULL                COMMENT '字段类型（FieldType011）',
    field_length   INT          NOT NULL DEFAULT 0      COMMENT '长度',
    required_field VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT '是否必填（0 否 / 1 是）',
    default_value  VARCHAR(300) NULL                    COMMENT '默认值',
    status         VARCHAR(3)   NOT NULL DEFAULT '1'    COMMENT '状态（0 停用 / 1 启用）',
    create_by      VARCHAR(33)  NULL                    COMMENT '创建人',
    update_by      VARCHAR(33)  NULL                    COMMENT '最后修改人',
    create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
    dr             VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT '删除标记（0 正常 / 1 已删除）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_pk_mt_field_code (pk_mt, field_code),
    KEY idx_pk_mt_sort (pk_mt, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='低代码核心 - 字段';

CREATE TABLE IF NOT EXISTS july_metadata_display (
    id             VARCHAR(33)  NOT NULL                COMMENT '主键',
    pk_mt          VARCHAR(33)  NOT NULL                COMMENT '主表链接（july_metadata.id）',
    sort_order     INT          NOT NULL DEFAULT 9999   COMMENT '排序（越小越靠前）',
    display_code   VARCHAR(60)  NOT NULL                COMMENT '列编码（绑定字段，同对象内唯一）',
    display_name   VARCHAR(60)  NOT NULL                COMMENT '列名称',
    align          VARCHAR(10)  NOT NULL DEFAULT 'left' COMMENT '对齐（left/center/right）',
    width          INT          NOT NULL DEFAULT 0      COMMENT '列宽（px）',
    component_type VARCHAR(30)  NOT NULL                COMMENT '组件类型',
    display_type   VARCHAR(20)  NOT NULL DEFAULT 'all'  COMMENT '显示类型（DisplayType011）',
    param011       VARCHAR(300) NULL                    COMMENT '扩展参数',
    status         VARCHAR(3)   NOT NULL DEFAULT '1'    COMMENT '状态（0 停用 / 1 启用）',
    create_by      VARCHAR(33)  NULL                    COMMENT '创建人',
    update_by      VARCHAR(33)  NULL                    COMMENT '最后修改人',
    create_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    update_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
    dr             VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT '删除标记（0 正常 / 1 已删除）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_pk_mt_display_code (pk_mt, display_code),
    KEY idx_pk_mt_sort (pk_mt, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='低代码核心 - 显示列';

CREATE TABLE IF NOT EXISTS july_metadata_service (
    id                  VARCHAR(33)   NOT NULL                COMMENT '主键',
    pk_mt               VARCHAR(33)   NOT NULL                COMMENT '主表链接（july_metadata.id）',
    sort_order          INT           NOT NULL DEFAULT 9999   COMMENT '排序（越小越靠前）',
    service_code        VARCHAR(60)   NOT NULL                COMMENT '服务编码（同对象内唯一）',
    service_name        VARCHAR(60)   NOT NULL                COMMENT '服务名称',
    service_description VARCHAR(300)  NULL                    COMMENT '服务描述',
    object_type         VARCHAR(30)   NOT NULL DEFAULT 'global_method' COMMENT '服务对象类型（ServiceObjectType011）',
    param_type          VARCHAR(30)   NOT NULL DEFAULT 'none' COMMENT '参数类型（ServiceParamType011）',
    service_content     TEXT           NULL                   COMMENT 'SQL / 脚本内容',
    enabled             VARCHAR(3)    NOT NULL DEFAULT '0'    COMMENT '是否启用（0 否 / 1 是）',
    status              VARCHAR(3)    NOT NULL DEFAULT '1'    COMMENT '状态（0 停用 / 1 启用）',
    create_by           VARCHAR(33)   NULL                    COMMENT '创建人',
    update_by           VARCHAR(33)   NULL                    COMMENT '最后修改人',
    create_time         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    update_time         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
    dr                  VARCHAR(3)    NOT NULL DEFAULT '0'    COMMENT '删除标记（0 正常 / 1 已删除）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_pk_mt_service_code (pk_mt, service_code),
    KEY idx_pk_mt_sort (pk_mt, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='低代码核心 - 服务';
