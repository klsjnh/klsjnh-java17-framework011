-- ============================================================
-- 存储中心 · 对象存储实例 + 桶（共 2 表）
-- 迁自 july_storage_provider.sql（2026-09-24，按中心归并；文件名 july_center_storage）
-- 列顺序规范：id → 业务字段 → sort_order（有排序需求时）→ status → 审计四列 → dr
-- 设计：docs/requirement011/029.topic-storage-migration.md
-- 方案：docs/requirement013/029.topic-storage-migration.md
-- 架构：docs/infrastructure011/011.storage-center/
-- 边界：yaml krt.storage-center.* 仅作播种（默认桶除外）；表为运行时唯一真源；AK/SK 出参打码
-- 组成：存储实例（主）+ 桶（子）
-- ============================================================

CREATE TABLE IF NOT EXISTS july_storage_provider (
    id                     VARCHAR(33)  NOT NULL                COMMENT '主键',
    storage_code           VARCHAR(60)  NOT NULL                COMMENT '存储实例编码（全局唯一，不可变）',
    storage_name           VARCHAR(100) NOT NULL                COMMENT '存储实例名称',
    provider               VARCHAR(20)  NOT NULL DEFAULT 'local011' COMMENT '存储类型（开放字符串：内置 local011/minio011/s3011，其余由使用者扩展）',
    base_path              VARCHAR(500) NULL                    COMMENT '本地根目录（local011 必填）',
    endpoint               VARCHAR(300) NULL                    COMMENT 'Endpoint（S3 系必填）',
    access_key             VARCHAR(100) NULL                    COMMENT 'Access Key',
    secret_key             VARCHAR(300) NULL                    COMMENT 'Secret Key（出参打码 ******）',
    secure                 VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT 'HTTPS（0 否 / 1 是）',
    presign_expiry_seconds INT          NOT NULL DEFAULT 3600   COMMENT '预签名有效期（秒）',
    sort_order             INT          NOT NULL DEFAULT 9999   COMMENT '排序（越小越靠前）',
    status                 VARCHAR(3)   NOT NULL DEFAULT '1'    COMMENT '状态（0 停用 / 1 启用）',
    remark                 VARCHAR(300) NULL                    COMMENT '备注',
    create_by              VARCHAR(33)  NULL                    COMMENT '创建人',
    update_by              VARCHAR(33)  NULL                    COMMENT '最后修改人',
    create_time            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    update_time            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
    dr                     VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT '删除标记（0 正常 / 1 已删除）',
    alive_storage_code     VARCHAR(60)  GENERATED ALWAYS AS (IF(dr = '0', storage_code, NULL)) STORED COMMENT '存活唯一键（dr=0 时=storage_code）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_storage_code (alive_storage_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='存储中心 - 对象存储实例（主表）';

CREATE TABLE IF NOT EXISTS july_storage_provider_bucket (
    id          VARCHAR(33)  NOT NULL                COMMENT '主键',
    pk_mt       VARCHAR(33)  NOT NULL                COMMENT '主表链接（july_storage_provider.id）',
    bucket_code VARCHAR(60)  NOT NULL                COMMENT '桶编码（同实例内唯一，不可变）',
    bucket_name VARCHAR(100) NOT NULL                COMMENT '桶名称',
    is_default  VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT '默认桶（0 否 / 1 是，同实例内至多一个，应用层保证）',
    sort_order  INT          NOT NULL DEFAULT 9999   COMMENT '排序（越小越靠前）',
    status      VARCHAR(3)   NOT NULL DEFAULT '1'    COMMENT '状态（0 停用 / 1 启用）',
    remark      VARCHAR(300) NULL                    COMMENT '备注',
    create_by   VARCHAR(33)  NULL                    COMMENT '创建人',
    update_by   VARCHAR(33)  NULL                    COMMENT '最后修改人',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
    dr          VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT '删除标记（0 正常 / 1 已删除）',
    alive_key   VARCHAR(200) GENERATED ALWAYS AS (IF(dr = '0', CONCAT_WS('#', pk_mt, bucket_code), NULL)) STORED COMMENT '存活唯一键（dr=0 时=pk_mt#bucket_code）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_pk_mt_bucket_code (alive_key),
    KEY idx_pk_mt_sort (pk_mt, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='存储中心 - 对象存储桶（明细表）';
