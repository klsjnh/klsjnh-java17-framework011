-- ============================================================
-- july_message_channel / july_message_template / july_message — 消息中心（渠道 / 模板 / 发送记录）
-- 列顺序规范：id → 业务字段 → sort_order（有排序需求时）→ status → 审计四列 → dr
-- 设计：docs/infrastructure011/021.topic-message-center.md
-- 边界：渠道渠道可插拔（实现型 SPI：MessageChannelPort + 注册表）；config 密钥出参打码，加密二期
-- ============================================================

CREATE TABLE IF NOT EXISTS july_message_channel (
    id            VARCHAR(33)  NOT NULL                COMMENT '主键',
    channel_code  VARCHAR(60)  NOT NULL                COMMENT '渠道编码（全局唯一，不可变）',
    channel_name  VARCHAR(100) NOT NULL                COMMENT '渠道名称',
    provider_type VARCHAR(60)  NOT NULL                COMMENT '提供商类型（绑定 SPI channelCode，如 inapp/webhook/sms）',
    config        TEXT         NULL                    COMMENT '渠道配置 JSON（url / 密钥等，出参打码）',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息中心011 - 渠道配置';

CREATE TABLE IF NOT EXISTS july_message_template (
    id            VARCHAR(33)  NOT NULL                COMMENT '主键',
    template_code VARCHAR(60)  NOT NULL                COMMENT '模板编码（全局唯一，不可变）',
    template_name VARCHAR(100) NOT NULL                COMMENT '模板名称',
    channel_code  VARCHAR(60)  NOT NULL                COMMENT '渠道编码（july_message_channel.channel_code）',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息中心011 - 消息模板';

CREATE TABLE IF NOT EXISTS july_message (
    id            VARCHAR(33)   NOT NULL                COMMENT '主键',
    channel_code  VARCHAR(60)   NOT NULL                COMMENT '渠道编码',
    provider_type VARCHAR(60)   NOT NULL                COMMENT '提供商类型（实际发送的 SPI channelCode）',
    msg_to        VARCHAR(300)  NULL                    COMMENT '接收方（站内信为用户 id / 手机号 / webhook 标识）',
    template_code VARCHAR(60)   NULL                    COMMENT '模板编码（可空，纯文本直发）',
    title         VARCHAR(300)  NULL                    COMMENT '标题',
    content       TEXT          NULL                    COMMENT '渲染后的内容',
    send_status   VARCHAR(20)   NOT NULL DEFAULT 'PENDING' COMMENT '发送状态（PENDING 待发 / SUCCESS 成功 / FAILED 失败）',
    retry_count   INT           NOT NULL DEFAULT 0      COMMENT '重试次数',
    error         VARCHAR(1000) NULL                    COMMENT '失败原因',
    remark        VARCHAR(300)  NULL                    COMMENT '备注',
    sort_order    INT           NOT NULL DEFAULT 9999   COMMENT '排序（越小越靠前）',
    status        VARCHAR(3)    NOT NULL DEFAULT '1'    COMMENT '状态（0 停用 / 1 启用）',
    create_by     VARCHAR(33)   NULL                    COMMENT '创建人',
    update_by     VARCHAR(33)   NULL                    COMMENT '最后修改人',
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建日期',
    update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改日期',
    dr            VARCHAR(3)    NOT NULL DEFAULT '0'    COMMENT '删除标记（0 正常 / 1 已删除）',
    PRIMARY KEY (id),
    KEY idx_channel_code (channel_code),
    KEY idx_send_status (send_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息中心011 - 发送记录';
