-- ============================================================
-- july_datasource — 数据服务-数据源管理（运行时可变业务库连接注册）
-- 列顺序规范：id → 业务字段 → status → 审计四列 → dr
-- 设计：docs/requirement011/031.topic-dataservice011.md
-- 方案：docs/requirement013/031.topic-dataservice011.md
-- 边界：krt.ci011（yaml）为引导数据源；同一 dsCode 只允许一个家
-- ============================================================

CREATE TABLE IF NOT EXISTS july_datasource (
    id            VARCHAR(33)  NOT NULL                COMMENT '主键',
    ds_code       VARCHAR(60)  NOT NULL                COMMENT '数据源编码（全局唯一，不可变，连接池名）',
    ds_name       VARCHAR(100) NOT NULL                COMMENT '数据源名称',
    db_type       VARCHAR(20)  NOT NULL DEFAULT 'mysql' COMMENT '数据库类型（mysql/oracle/sqlserver/postgresql）',
    jdbc_url      VARCHAR(500) NOT NULL                COMMENT 'JDBC URL（须以 jdbc: 开头）',
    schema_name   VARCHAR(60)  NULL                    COMMENT '库名或 Schema',
    username      VARCHAR(100) NULL                    COMMENT '用户名',
    password      VARCHAR(300) NULL                    COMMENT '密码（出参不回显，由 Converter 收口剔除）',
    driver_class  VARCHAR(200) NULL                    COMMENT '驱动类名（为空时按 db_type 取 DatabaseType011 默认值）',
    pool_config   VARCHAR(500) NULL                    COMMENT '连接池 JSON（本期预留，不解析，走代码默认值）',
    remark        VARCHAR(300) NULL                    COMMENT '备注',
    status        VARCHAR(3)   NOT NULL DEFAULT '1'    COMMENT '状态（0 停用 / 1 启用，停用即从运行时注册表摘除）',
    create_by     VARCHAR(33)  NULL                    COMMENT '创建人',
    update_by     VARCHAR(33)  NULL                    COMMENT '最后修改人',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
    dr            VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT '删除标记（0 正常 / 1 已删除）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_ds_code (ds_code),
    KEY idx_ds_code_status (ds_code, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据服务011 - 数据源管理';
