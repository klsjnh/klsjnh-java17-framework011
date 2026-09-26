-- july_core011.sql july_user + july_user_audit
-- Split 2026-09-26

CREATE TABLE IF NOT EXISTS july_user (
  id varchar(33) NOT NULL COMMENT '主键',
  user_account varchar(30) NOT NULL COMMENT '登录账号',
  user_name varchar(60) NOT NULL COMMENT '用户姓名',
  password varchar(100) NOT NULL COMMENT '登录密码（bcrypt）',
  mobile varchar(20) DEFAULT NULL COMMENT '手机号',
  email varchar(100) DEFAULT NULL COMMENT '邮箱',
  avatar varchar(200) DEFAULT NULL COMMENT '头像',
  pk_org varchar(33) DEFAULT NULL COMMENT '组织链接（july_organization.id，可空）',
  last_login_time datetime DEFAULT NULL COMMENT '最后登录时间',
  token_version int NOT NULL DEFAULT 0 COMMENT 'JWT 凭证版本（改密/改状态/logout 递增；与 token claim tv 比对）',
  status varchar(3) NOT NULL DEFAULT '1' COMMENT '账号状态（0 禁用 / 1 启用）',
  create_by varchar(33) DEFAULT NULL COMMENT '创建人',
  update_by varchar(33) DEFAULT NULL COMMENT '最后修改人',
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
  dr varchar(3) NOT NULL DEFAULT '0' COMMENT '删除标记（0 正常 / 1 已删除）',
  alive_user_account varchar(60) GENERATED ALWAYS AS (if((dr = '0'),user_account,NULL)) STORED,
  PRIMARY KEY (id),
  UNIQUE KEY uk_user_account (alive_user_account)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统管理 - 用户管理';
CREATE TABLE IF NOT EXISTS july_user_audit (
  id varchar(33) NOT NULL COMMENT '主键',
  pk_mt varchar(33) DEFAULT NULL COMMENT '主表链接（操作者 july_user.id，登录失败可空）',
  user_account varchar(30) DEFAULT NULL COMMENT '操作者账号（冗余，删号后仍可追溯）',
  audit_type varchar(30) NOT NULL COMMENT '事件类型（LOGIN/LOGIN_FAILED/LOGOUT/INSERT/UPDATE/DELETE/EXPORT/CHANGE_PASSWORD）',
  object_code varchar(60) DEFAULT NULL COMMENT '对象编码',
  audit_content varchar(500) DEFAULT NULL COMMENT '事件描述',
  audit_ip varchar(50) DEFAULT NULL COMMENT '客户端 IP',
  status varchar(3) NOT NULL DEFAULT '1' COMMENT '状态',
  create_by varchar(33) DEFAULT NULL COMMENT '创建人',
  update_by varchar(33) DEFAULT NULL COMMENT '最后修改人',
  create_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期（即事件时间）',
  update_time datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
  dr varchar(3) NOT NULL DEFAULT '0' COMMENT '删除标记（审计不删除，列仅为公共结构）',
  PRIMARY KEY (id),
  KEY idx_user_account (user_account),
  KEY idx_audit_type (audit_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统管理 - 用户审计';
