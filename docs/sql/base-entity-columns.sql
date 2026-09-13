-- ============================================================
-- BaseEntity 公共列：所有业务表建表时包含以下字段（表名前缀 july_ 按需调整）
-- 注意：本文件是公共列"模板"；真实业务表的列顺序规范 = id → 业务字段 → status → 审计四列 → dr
--       （业务列紧跟主键可读性最好，技术列沉底；参见各主题 july_*.sql）
-- ============================================================

-- 建表模板示例：
-- CREATE TABLE july_xxx (
    id          VARCHAR(33)  NOT NULL                COMMENT '主键',
    status      VARCHAR(3)   NOT NULL DEFAULT '1'    COMMENT '状态',
    create_by   VARCHAR(33)  NULL                    COMMENT '创建人',
    update_by   VARCHAR(33)  NULL                    COMMENT '最后修改人',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
    dr          VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT '删除标记（0 正常 / 1 已删除）',
    PRIMARY KEY (id)
-- ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务表';

-- 说明：
-- 1. id 由应用层生成 32 位无连字符 UUID，列宽 33 预留 1 位。
-- 2. 时间列用 DATETIME 而非 TIMESTAMP：TIMESTAMP 受 2038 年上限约束。
-- 3. update_time 加 ON UPDATE CURRENT_TIMESTAMP，即使绕过应用层直接改库也能刷新。

-- ============================================================
-- BaseEntity011（继承 BaseEntity）：需要业务排序的表（字典/分类等）额外追加一列
-- ============================================================
    sort_order INT          NOT NULL DEFAULT 9999   COMMENT '排序（越小越靠前）',

-- ============================================================
-- TreeEntity（继承 BaseEntity）：树形结构表（部门/菜单/地区等）额外追加一列
-- ============================================================
    parent_id  VARCHAR(33)  NOT NULL DEFAULT ''     COMMENT '父节点 id（根节点为空串）',
    KEY idx_parent_id (parent_id)

-- ============================================================
-- TreeEntity011（继承 TreeEntity）：同级需排序的树形表（菜单/部门等），在 TreeEntity 基础上追加
-- ============================================================
    sort_order INT          NOT NULL DEFAULT 9999   COMMENT '排序（越小越靠前）'



