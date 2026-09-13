-- ============================================================
-- july_menu — 系统管理-菜单（树形：目录 / 菜单 / 按钮，权限标识挂菜单）
-- 列顺序规范：id → 业务字段 → status → 审计四列 → dr
-- 设计：docs/requirement011/011.topic-july-menu.md
-- ============================================================

CREATE TABLE IF NOT EXISTS july_menu (
    id                 VARCHAR(33)  NOT NULL                COMMENT '主键',
    parent_id          VARCHAR(33)  NOT NULL DEFAULT ''     COMMENT '上级菜单（空串为根）',
    sort_order         INT          NOT NULL DEFAULT 9999   COMMENT '排序（同级内，越小越靠前）',
    menu_code          VARCHAR(30)  NOT NULL                COMMENT '菜单编码',
    menu_name          VARCHAR(60)  NOT NULL                COMMENT '菜单名称',
    menu_type          VARCHAR(3)   NOT NULL DEFAULT '1'    COMMENT '菜单类型（1 目录 / 2 菜单 / 3 按钮）',
    menu_icon          VARCHAR(60)  NULL                    COMMENT '菜单图标',
    menu_route         VARCHAR(200) NULL                    COMMENT '菜单路由',
    permission_code    VARCHAR(100) NULL                    COMMENT '权限标识（模块:对象:动作）',
    component          VARCHAR(200) NULL                    COMMENT '前端组件',
    status             VARCHAR(3)   NOT NULL DEFAULT '1'    COMMENT '菜单状态（0 停用 / 1 启用）',
    create_by          VARCHAR(33)  NULL                    COMMENT '创建人',
    update_by          VARCHAR(33)  NULL                    COMMENT '最后修改人',
    create_time        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    update_time        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
    dr                 VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT '删除标记（0 正常 / 1 已删除）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_menu_code (menu_code),
    KEY idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统管理-菜单';
