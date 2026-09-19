package com.klsjnh.web.messagecenter.vo.message;

/*                JulyMessageVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july message vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Send record response VO (detail and page rows).
 */

@Data
public class JulyMessageVo011 {

    /** Primary key. */
    @Schema(description = "主键")
    private String id;

    /** Channel code. */
    @Schema(description = "渠道编码")
    private String channelCode;

    /** Provider type actually used. */
    @Schema(description = "提供商类型")
    private String providerType;

    /** Channel-defined message shape. */
    @Schema(description = "报文形态（渠道自定义）")
    private String messageType;

    /** Channel-defined payload JSON. */
    @Schema(description = "形态载荷 JSON（渠道自定义）")
    private String payload;

    /** Receiver. */
    @Schema(description = "接收方")
    private String msgTo;

    /** Template code. */
    @Schema(description = "模板编码")
    private String templateCode;

    /** Rendered title. */
    @Schema(description = "标题")
    private String title;

    /** Rendered content. */
    @Schema(description = "内容")
    private String content;

    /** Send status: PENDING / SUCCESS / FAILED. */
    @Schema(description = "发送状态（PENDING / SUCCESS / FAILED）")
    private String sendStatus;

    /** Retry count. */
    @Schema(description = "重试次数")
    private Integer retryCount;

    /** Failure reason. */
    @Schema(description = "失败原因")
    private String error;

    /** Remark. */
    @Schema(description = "备注")
    private String remark;

    /** Creator. */
    @Schema(description = "创建人")
    private String createBy;

    /** Last modifier. */
    @Schema(description = "最后修改人")
    private String updateBy;

    /** Create time. */
    @Schema(description = "创建日期")
    private LocalDateTime createTime;

    /** Update time. */
    @Schema(description = "最后修改日期")
    private LocalDateTime updateTime;
}
