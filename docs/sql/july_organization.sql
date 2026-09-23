-- ============================================================
-- july_organization — 系统管理-组织机构（树形，负责人挂 july_user）
-- 列顺序规范：id → 业务字段 → sort_order（有排序需求时）→ status → 审计四列 → dr
-- 设计：docs/requirement011/013.topic-july-organization.md
-- ============================================================

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统管理 - 组织机构';
