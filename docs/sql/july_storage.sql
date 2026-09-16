-- ============================================================
-- july_storage — 存储中心 - 对象存储实例（表驱动多实例）
-- 列顺序规范：id → 业务字段 → status → 审计四列 → dr
-- 设计：docs/requirement011/037.topic-storage-migration.md
-- 方案：docs/requirement013/037.topic-storage-migration.md
-- 边界：yaml krt.storage-center.* 仅作播种；表为运行时唯一真源；secret_key 出参不回显
-- ============================================================

CREATE TABLE IF NOT EXISTS july_storage (
    id                     VARCHAR(33)  NOT NULL                COMMENT '主键',
    sort_order             INT          NOT NULL DEFAULT 9999   COMMENT '排序（越小越靠前）',
    storage_code           VARCHAR(60)  NOT NULL                COMMENT '存储实例编码（全局唯一，不可变）',
    storage_name           VARCHAR(100) NOT NULL                COMMENT '存储实例名称',
    provider               VARCHAR(20)  NOT NULL DEFAULT 'local011' COMMENT '存储类型（StorageType011：local011/minio011/cos011/tos011/oss011/s3011）',
    base_path              VARCHAR(500) NULL                    COMMENT '本地根目录（local011 必填）',
    endpoint               VARCHAR(300) NULL                    COMMENT 'Endpoint（S3 系必填）',
    access_key             VARCHAR(100) NULL                    COMMENT 'Access Key',
    secret_key             VARCHAR(300) NULL                    COMMENT 'Secret Key（出参不回显）',
    secure                 VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT 'HTTPS（0 否 / 1 是）',
    default_bucket         VARCHAR(100) NULL                    COMMENT '默认桶',
    presign_expiry_seconds INT          NOT NULL DEFAULT 3600   COMMENT '预签名有效期（秒）',
    remark                 VARCHAR(300) NULL                    COMMENT '备注',
    status                 VARCHAR(3)   NOT NULL DEFAULT '1'    COMMENT '状态（0 停用 / 1 启用）',
    create_by              VARCHAR(33)  NULL                    COMMENT '创建人',
    update_by              VARCHAR(33)  NULL                    COMMENT '最后修改人',
    create_time            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    update_time            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
    dr                     VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT '删除标记（0 正常 / 1 已删除）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_storage_code (storage_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='存储中心 - 对象存储实例';
