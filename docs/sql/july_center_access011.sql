-- ============================================================
-- 访问中心 · DDL（共 7 表）
-- 合并自 july_organization.sql / july_menu.sql / july_role.sql /
--       july_perm.sql（2026-09-24，按中心归并；曾用名 july_center_iam.sql）
-- 2026-09-26：july_user / july_user_audit 拆至 july_core011.sql（core 用户纵切）
-- 2026-09-26：文件更名为 july_center_access011.sql（对齐 center-access011-starter）
-- 列顺序规范：id → 业务字段 → sort_order（有排序需求时）→ status → 审计四列 → dr
-- 设计：docs/infrastructure011/013.access-center/（011 设计 / 013 架构 / 015 用法 / 016 动态权限）
--       docs/requirement011/011 菜单 · 013 组织 · 016 角色
-- 组成：组织树 · 菜单树 · 角色(+权限) · 用户-角色关联 · 权限目录(对象/动作)
-- 种子：目录与演示账号由 Java Seed Runner 幂等写入（JulyPermCatalogSeed011 /
--       AuthPermissionDemoSeed011），不在本文件 INSERT。
-- 表顺序：无库级 FK；按领域依赖排列（组织/菜单/角色 → 用户角色结 → 权限目录）。
-- ============================================================

-- ------------------------------------------------------------
-- 组织机构（树；负责人挂 july_user，可空；用户表见 july_core011.sql）
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS july_organization (
    id                 VARCHAR(33)  NOT NULL                COMMENT '主键',
    parent_id          VARCHAR(33)  NOT NULL DEFAULT ''     COMMENT '上级组织（空串为根）',
    org_code           VARCHAR(30)  NOT NULL                COMMENT '组织编码',
    org_name           VARCHAR(60)  NOT NULL                COMMENT '组织名称',
    pk_user            VARCHAR(33)  NULL                    COMMENT '负责人（july_user.id，可空）',
    org_level          INT          NOT NULL DEFAULT 1      COMMENT '组织层级（根为 1）',
    sort_order         INT          NOT NULL DEFAULT 9999   COMMENT '排序（同级内，越小越靠前）',
    status             VARCHAR(3)   NOT NULL DEFAULT '1'    COMMENT '组织状态（0 停用 / 1 启用）',
    create_by          VARCHAR(33)  NULL                    COMMENT '创建人',
    update_by          VARCHAR(33)  NULL                    COMMENT '最后修改人',
    create_time        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    update_time        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
    dr                 VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT '删除标记（0 正常 / 1 已删除）',
    alive_org_code     VARCHAR(30)  GENERATED ALWAYS AS (IF(dr = '0', org_code, NULL)) STORED COMMENT '存活唯一键（dr=0 时=org_code）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_org_code (alive_org_code),
    KEY idx_parent_id (parent_id),
    KEY idx_pk_user (pk_user)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访问中心 - 组织机构';

-- ------------------------------------------------------------
-- 菜单（树：目录 / 菜单 / 按钮；权限标识挂菜单）
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS july_menu (
    id                 VARCHAR(33)  NOT NULL                COMMENT '主键',
    parent_id          VARCHAR(33)  NOT NULL DEFAULT ''     COMMENT '上级菜单（空串为根）',
    menu_code          VARCHAR(30)  NOT NULL                COMMENT '菜单编码',
    menu_name          VARCHAR(60)  NOT NULL                COMMENT '菜单名称',
    menu_type          VARCHAR(3)   NOT NULL DEFAULT '1'    COMMENT '菜单类型（1 目录 / 2 菜单 / 3 按钮）',
    menu_icon          VARCHAR(60)  NULL                    COMMENT '菜单图标',
    menu_route         VARCHAR(200) NULL                    COMMENT '菜单路由',
    permission_code    VARCHAR(100) NULL                    COMMENT '权限标识（模块:对象:动作）',
    component          VARCHAR(200) NULL                    COMMENT '前端组件',
    sort_order         INT          NOT NULL DEFAULT 9999   COMMENT '排序（同级内，越小越靠前）',
    status             VARCHAR(3)   NOT NULL DEFAULT '1'    COMMENT '菜单状态（0 停用 / 1 启用）',
    create_by          VARCHAR(33)  NULL                    COMMENT '创建人',
    update_by          VARCHAR(33)  NULL                    COMMENT '最后修改人',
    create_time        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    update_time        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
    dr                 VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT '删除标记（0 正常 / 1 已删除）',
    alive_menu_code    VARCHAR(30)  GENERATED ALWAYS AS (IF(dr = '0', menu_code, NULL)) STORED COMMENT '存活唯一键（dr=0 时=menu_code）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_menu_code (alive_menu_code),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访问中心 - 菜单管理';

-- ------------------------------------------------------------
-- 角色（主表）
-- ------------------------------------------------------------

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访问中心 - 角色管理';

-- ------------------------------------------------------------
-- 用户-角色关联（pk_mt → july_user.id，pk_role → july_role.id；toggle；用户表见 july_core011.sql）
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS july_user_role (
    id                 VARCHAR(33)  NOT NULL                COMMENT '主键',
    pk_mt              VARCHAR(33)  NOT NULL                COMMENT '主表链接（july_user.id）',
    pk_role            VARCHAR(33)  NOT NULL                COMMENT '角色链接（july_role.id）',
    status             VARCHAR(3)   NOT NULL DEFAULT '1'    COMMENT '状态',
    create_by          VARCHAR(33)  NULL                    COMMENT '创建人',
    update_by          VARCHAR(33)  NULL                    COMMENT '最后修改人',
    create_time        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    update_time        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
    dr                 VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT '删除标记（0 正常 / 1 已删除）',
    alive_key          VARCHAR(200) GENERATED ALWAYS AS (IF(dr = '0', CONCAT_WS('#', pk_mt, pk_role), NULL)) STORED COMMENT '存活唯一键（dr=0 时=pk_mt#pk_role）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_role (alive_key),
    KEY idx_pk_role (pk_role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访问中心 - 用户_角色';

-- ------------------------------------------------------------
-- 角色-权限（菜单通道 pk_menu；直授通道 pk_menu=''；toggle）
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS july_role_permissions (
    id                 VARCHAR(33)  NOT NULL                COMMENT '主键',
    pk_mt              VARCHAR(33)  NOT NULL                COMMENT '主表链接（july_role.id）',
    pk_menu            VARCHAR(33)  NOT NULL                COMMENT '菜单链接（july_menu.id；直授通道为空串）',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访问中心 - 角色_权限';

-- ------------------------------------------------------------
-- 权限目录：对象 / 动作（P3；permission_code = 模块:对象:动作）
-- ------------------------------------------------------------

CREATE TABLE IF NOT EXISTS july_perm_object (
    id               VARCHAR(33)  NOT NULL                COMMENT '主键',
    object_code      VARCHAR(60)  NOT NULL                COMMENT '对象编码（如 julyScheduler）',
    object_name      VARCHAR(100) NOT NULL                COMMENT '对象名称',
    module_code      VARCHAR(60)  NOT NULL DEFAULT ''     COMMENT '模块（如 system011 / iam）',
    sort_order       INT          NOT NULL DEFAULT 9999   COMMENT '排序（升序）',
    status           VARCHAR(3)   NOT NULL DEFAULT '1'    COMMENT '状态（0 停用 / 1 启用）',
    remark           VARCHAR(300) NULL                    COMMENT '备注',
    create_by        VARCHAR(33)  NULL                    COMMENT '创建人',
    update_by        VARCHAR(33)  NULL                    COMMENT '最后修改人',
    create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    update_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
    dr               VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT '删除标记（0 正常 / 1 已删除）',
    alive_object_code VARCHAR(60) GENERATED ALWAYS AS (IF(dr = '0', object_code, NULL)) STORED COMMENT '存活唯一键',
    PRIMARY KEY (id),
    UNIQUE KEY uk_perm_object_code (alive_object_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访问中心 - 权限对象目录';

CREATE TABLE IF NOT EXISTS july_perm_action (
    id               VARCHAR(33)  NOT NULL                COMMENT '主键',
    object_code      VARCHAR(60)  NOT NULL                COMMENT '对象编码（july_perm_object.object_code）',
    action_code      VARCHAR(60)  NOT NULL                COMMENT '动作编码（如 start）',
    action_name      VARCHAR(100) NOT NULL                COMMENT '动作名称',
    permission_code  VARCHAR(100) NOT NULL                COMMENT '完整权限码（模块:对象:动作）',
    sort_order       INT          NOT NULL DEFAULT 9999   COMMENT '排序（升序）',
    status           VARCHAR(3)   NOT NULL DEFAULT '1'    COMMENT '状态（0 停用 / 1 启用）',
    remark           VARCHAR(300) NULL                    COMMENT '备注',
    create_by        VARCHAR(33)  NULL                    COMMENT '创建人',
    update_by        VARCHAR(33)  NULL                    COMMENT '最后修改人',
    create_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    update_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
    dr               VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT '删除标记（0 正常 / 1 已删除）',
    alive_permission_code VARCHAR(100) GENERATED ALWAYS AS (IF(dr = '0', permission_code, NULL)) STORED COMMENT '存活唯一键（permission_code）',
    alive_object_action VARCHAR(130) GENERATED ALWAYS AS (IF(dr = '0', CONCAT_WS('#', object_code, action_code), NULL)) STORED COMMENT '存活唯一键（object#action）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_perm_action_code (alive_permission_code),
    UNIQUE KEY uk_perm_object_action (alive_object_action),
    KEY idx_perm_action_object (object_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='访问中心 - 权限动作目录';
