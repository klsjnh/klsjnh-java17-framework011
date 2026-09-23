-- ============================================================
-- july_user — 系统管理-用户（主题文件：主表 + 附属表 user_role / user_audit）
-- 列顺序规范：id → 业务字段 → sort_order（有排序需求时）→ status → 审计四列 → dr
-- 附属表设计：docs/requirement011/015.topic-july-user.md §017
-- ============================================================

-- 主表：用户
CREATE TABLE IF NOT EXISTS july_user (
    id                 VARCHAR(33)  NOT NULL                COMMENT '主键',
    user_account       VARCHAR(30)  NOT NULL                COMMENT '登录账号',
    user_name          VARCHAR(60)  NOT NULL                COMMENT '用户姓名',
    password           VARCHAR(100) NOT NULL                COMMENT '登录密码（bcrypt）',
    mobile             VARCHAR(20)  NULL                    COMMENT '手机号',
    email              VARCHAR(100) NULL                    COMMENT '邮箱',
    avatar             VARCHAR(200) NULL                    COMMENT '头像',
    pk_org             VARCHAR(33)  NULL                    COMMENT '组织链接（july_organization.id，可空）',
    last_login_time    DATETIME     NULL                    COMMENT '最后登录时间',
    status             VARCHAR(3)   NOT NULL DEFAULT '1'    COMMENT '账号状态（0 禁用 / 1 启用）',
    create_by          VARCHAR(33)  NULL                    COMMENT '创建人',
    update_by          VARCHAR(33)  NULL                    COMMENT '最后修改人',
    create_time        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    update_time        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
    dr                 VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT '删除标记（0 正常 / 1 已删除）',
    alive_user_account VARCHAR(30)  GENERATED ALWAYS AS (IF(dr = '0', user_account, NULL)) STORED COMMENT '存活唯一键（dr=0 时=user_account）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_account (alive_user_account)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统管理 - 用户管理';

-- 附属表：用户-角色关联（pk_mt → july_user.id，pk_role → july_role.id；toggle 替换）
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统管理 - 用户_角色';

-- 附属表：用户审计（append-only：只插不改不删，不参与任何级联）
CREATE TABLE IF NOT EXISTS july_user_audit (
    id                 VARCHAR(33)  NOT NULL                COMMENT '主键',
    pk_mt              VARCHAR(33)  NULL                    COMMENT '主表链接（操作者 july_user.id，登录失败可空）',
    user_account       VARCHAR(30)  NULL                    COMMENT '操作者账号（冗余，删号后仍可追溯）',
    audit_type         VARCHAR(30)  NOT NULL                COMMENT '事件类型（LOGIN/LOGIN_FAILED/LOGOUT/INSERT/UPDATE/DELETE/EXPORT/CHANGE_PASSWORD）',
    object_code        VARCHAR(60)  NULL                    COMMENT '对象编码',
    audit_content      VARCHAR(500) NULL                    COMMENT '事件描述',
    audit_ip           VARCHAR(50)  NULL                    COMMENT '客户端 IP',
    status             VARCHAR(3)   NOT NULL DEFAULT '1'    COMMENT '状态',
    create_by          VARCHAR(33)  NULL                    COMMENT '创建人',
    update_by          VARCHAR(33)  NULL                    COMMENT '最后修改人',
    create_time        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期（即事件时间）',
    update_time        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
    dr                 VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT '删除标记（审计不删除，列仅为公共结构）',
    PRIMARY KEY (id),
    KEY idx_user_account (user_account),
    KEY idx_audit_type (audit_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统管理 - 用户审计';
