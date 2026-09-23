-- ============================================================
-- july_role — 系统管理-角色（主题文件：主表 + 附属表 role_permissions）
-- 列顺序规范：id → 业务字段 → sort_order（有排序需求时）→ status → 审计四列 → dr
-- 附属表设计：docs/requirement011/016.topic-july-role.md §017.1
-- ============================================================

-- 主表：角色
CREATE TABLE IF NOT EXISTS july_role (
    id                 VARCHAR(33)  NOT NULL                COMMENT '主键',
    role_code          VARCHAR(30)  NOT NULL                COMMENT '角色编码',
    role_name          VARCHAR(60)  NOT NULL                COMMENT '角色名称',
    is_builtin         VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT '内置角色（1 是 / 0 否，禁删禁停）',
    remark             VARCHAR(200) NULL                    COMMENT '备注',
    status             VARCHAR(3)   NOT NULL DEFAULT '1'    COMMENT '角色状态（0 禁用 / 1 启用）',
    create_by          VARCHAR(33)  NULL                    COMMENT '创建人',
    update_by          VARCHAR(33)  NULL                    COMMENT '最后修改人',
    create_time        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    update_time        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
    dr                 VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT '删除标记（0 正常 / 1 已删除）',
    alive_role_code    VARCHAR(30)  GENERATED ALWAYS AS (IF(dr = '0', role_code, NULL)) STORED COMMENT '存活唯一键（dr=0 时=role_code）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (alive_role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统管理 - 角色管理';

-- 附属表：角色-权限（挂菜单；一行 = 角色 × 菜单 × 权限标识快照；toggle 替换）
CREATE TABLE IF NOT EXISTS july_role_permissions (
    id                 VARCHAR(33)  NOT NULL                COMMENT '主键',
    pk_mt              VARCHAR(33)  NOT NULL                COMMENT '主表链接（july_role.id）',
    pk_menu            VARCHAR(33)  NOT NULL                COMMENT '菜单链接（july_menu.id）',
    permission_code    VARCHAR(100) NOT NULL DEFAULT ''     COMMENT '权限标识快照（空串=纯页面可见）',
    status             VARCHAR(3)   NOT NULL DEFAULT '1'    COMMENT '状态',
    create_by          VARCHAR(33)  NULL                    COMMENT '创建人',
    update_by          VARCHAR(33)  NULL                    COMMENT '最后修改人',
    create_time        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    update_time        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
    dr                 VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT '删除标记（0 正常 / 1 已删除）',
    alive_key          VARCHAR(300) GENERATED ALWAYS AS (IF(dr = '0', CONCAT_WS('#', pk_mt, pk_menu, permission_code), NULL)) STORED COMMENT '存活唯一键（dr=0 时=pk_mt#pk_menu#permission_code）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_menu_code (alive_key),
    KEY idx_pk_menu (pk_menu)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统管理 - 角色_权限';
