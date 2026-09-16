-- ============================================================
-- july_business_modeling — 数据服务011 - 业务建模
-- 列顺序规范：id → 业务字段 → status → 审计四列 → dr
-- 设计：docs/requirement011/033.topic-business-modeling.md
-- 方案：docs/requirement013/033.topic-business-modeling.md
-- 边界：合并老项目 july_sql + july_model；产物 = MetaData011 + FieldInfo011 的 JSON；
--       本表不做物理建表，产物交 lowcode011 域
-- 两个裸列的理由：object_name 需唯一索引查重；sql_content 是探针/执行/分页执行的共同输入
-- ============================================================

CREATE TABLE IF NOT EXISTS july_business_modeling (
    id               VARCHAR(33)   NOT NULL                COMMENT '主键',
    model_code       VARCHAR(60)   NOT NULL                COMMENT '建模编码（全局唯一，不可变）',
    model_name       VARCHAR(100)  NOT NULL                COMMENT '建模名称',
    object_name      VARCHAR(60)   NOT NULL                COMMENT '低代码对象名（产物 MetaData011.objectName，全局唯一）',
    data_source_code VARCHAR(60)   NOT NULL                COMMENT '数据源编码（挂 july_datasource.ds_code）',
    sql_content      TEXT          NULL                    COMMENT '取数 SQL（探针推断 / 执行 / 分页执行共用，须以 SELECT 开头）',
    model_data       TEXT          NULL                    COMMENT '建模产物 JSON（metaData + fieldData 两个键）',
    remark           VARCHAR(300)  NULL                    COMMENT '备注',
    status           VARCHAR(3)    NOT NULL DEFAULT '1'    COMMENT '状态（0 停用 / 1 启用）',
    create_by        VARCHAR(33)   NULL                    COMMENT '创建人',
    update_by        VARCHAR(33)   NULL                    COMMENT '最后修改人',
    create_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    update_time      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
    dr               VARCHAR(3)    NOT NULL DEFAULT '0'    COMMENT '删除标记（0 正常 / 1 已删除）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_model_code (model_code),
    UNIQUE KEY uk_object_name (object_name),
    KEY idx_ds_code (data_source_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据服务011 - 业务建模';
