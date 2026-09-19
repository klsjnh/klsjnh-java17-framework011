-- ============================================================
-- 消息中心 · 入站（inbound）三表：渠道 / 模板 / 接收记录
-- 列顺序规范：id → 业务字段 → sort_order（有排序需求时）→ status → 审计四列 → dr
-- 设计：docs/infrastructure011/021.topic-message-center.md
-- 边界：入站渠道可插拔（实现型 SPI：MessageInboundPort + 注册表）；回调/长连由渠道插件提供
-- 命名：出站/入站两套对称，本文件为入站；出站见 july_message_outbound.sql
-- ============================================================

CREATE TABLE IF NOT EXISTS july_message_inbound_channel (
    id            VARCHAR(33)  NOT NULL                COMMENT '主键',
    channel_code  VARCHAR(60)  NOT NULL                COMMENT '渠道编码（全局唯一，不可变）',
    channel_name  VARCHAR(100) NOT NULL                COMMENT '渠道名称',
    provider_type VARCHAR(60)  NOT NULL                COMMENT '提供商类型（绑定 SPI channelCode，如 webhook/sms）',
    config        TEXT         NULL                    COMMENT '渠道配置 JSON（回调密钥 / 验签等，出参打码）',
    remark        VARCHAR(300) NULL                    COMMENT '备注',
    sort_order    INT          NOT NULL DEFAULT 9999   COMMENT '排序（越小越靠前）',
    status        VARCHAR(3)   NOT NULL DEFAULT '1'    COMMENT '状态（0 停用 / 1 启用）',
    create_by     VARCHAR(33)  NULL                    COMMENT '创建人',
    update_by     VARCHAR(33)  NULL                    COMMENT '最后修改人',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
    dr            VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT '删除标记（0 正常 / 1 已删除）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_channel_code (channel_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息中心011 - 入站渠道配置';

CREATE TABLE IF NOT EXISTS july_message_inbound_template (
    id            VARCHAR(33)  NOT NULL                COMMENT '主键',
    template_code VARCHAR(60)  NOT NULL                COMMENT '模板编码（全局唯一，不可变）',
    template_name VARCHAR(100) NOT NULL                COMMENT '模板名称',
    channel_code  VARCHAR(60)  NOT NULL                COMMENT '渠道编码（july_message_inbound_channel.channel_code）',
    title         VARCHAR(300) NULL                    COMMENT '标题（支持 ${var} 占位）',
    content       TEXT         NOT NULL                COMMENT '内容（支持 ${var} 占位）',
    remark        VARCHAR(300) NULL                    COMMENT '备注',
    sort_order    INT          NOT NULL DEFAULT 9999   COMMENT '排序（越小越靠前）',
    status        VARCHAR(3)   NOT NULL DEFAULT '1'    COMMENT '状态（0 停用 / 1 启用）',
    create_by     VARCHAR(33)  NULL                    COMMENT '创建人',
    update_by     VARCHAR(33)  NULL                    COMMENT '最后修改人',
    create_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    update_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
    dr            VARCHAR(3)   NOT NULL DEFAULT '0'    COMMENT '删除标记（0 正常 / 1 已删除）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_template_code (template_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息中心011 - 入站消息模板';

CREATE TABLE IF NOT EXISTS july_message_inbound (
    id             VARCHAR(33)   NOT NULL                COMMENT '主键',
    channel_code   VARCHAR(60)   NOT NULL                COMMENT '渠道编码',
    provider_type  VARCHAR(60)   NOT NULL                COMMENT '提供商类型（实际接收的 SPI channelCode）',
    message_type   VARCHAR(60)   NULL                    COMMENT '报文形态（渠道自定义开放字符串：text/image/... 框架不解释）',
    payload        TEXT          NULL                    COMMENT '形态载荷 JSON（渠道自定义键值）',
    from_id        VARCHAR(300)  NULL                    COMMENT '发送方（用户 id / openId / 手机号等）',
    content        TEXT          NULL                    COMMENT '消息内容',
    raw_message_id VARCHAR(120)  NULL                    COMMENT '渠道侧消息 id（去重键，同渠道内唯一）',
    error          VARCHAR(1000) NULL                    COMMENT '处理失败原因',
    remark         VARCHAR(300)  NULL                    COMMENT '备注',
    status         VARCHAR(3)    NOT NULL DEFAULT '1'    COMMENT '处理状态（业务状态列：1 已接收 / 2 已处理 / 3 处理失败；见 MessageInboundStatus011）',
    create_by      VARCHAR(33)   NULL                    COMMENT '创建人',
    update_by      VARCHAR(33)   NULL                    COMMENT '最后修改人',
    create_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    update_time    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
    dr             VARCHAR(3)    NOT NULL DEFAULT '0'    COMMENT '删除标记（0 正常 / 1 已删除）',
    PRIMARY KEY (id),
    UNIQUE KEY uk_channel_msg (channel_code, raw_message_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息中心011 - 入站接收记录';
