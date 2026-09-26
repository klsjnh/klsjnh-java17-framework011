package com.klsjnh.web.messagecenter.outbound.controller;

/*                JulyOutboundMessageController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july message controller class
 *      2026.09.26  pass operator into use case for permission checks
 *
 */

import com.klsjnh.common.constant.AuditObjectCodes011;
import com.klsjnh.common.enums.AuditType011;
import com.klsjnh.common.identity.Operator011;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.response.Response011;
import com.klsjnh.common.vo.BatchDeleteResultVo011;
import com.klsjnh.common.vo.IdVo011;
import com.klsjnh.common.vo.IdsVo011;

import com.klsjnh.application.messagecenter.outbound.message.MessageOutboundUseCase;
import com.klsjnh.application.messagecenter.outbound.message.MessageSendCommand;
import com.klsjnh.domain.messagecenter.outbound.message.JulyOutboundMessage;
import com.klsjnh.domain.messagecenter.outbound.message.JulyOutboundMessageQuerySpec;

import com.klsjnh.web.messagecenter.outbound.converter.JulyOutboundMessageConverter;

import com.klsjnh.web.global.audit.AuditLog;
import com.klsjnh.web.messagecenter.outbound.vo.message.JulyOutboundMessageQueryVo011;
import com.klsjnh.web.messagecenter.outbound.vo.message.JulyOutboundMessageSendResultVo011;
import com.klsjnh.web.messagecenter.outbound.vo.message.JulyOutboundMessageSendVo011;
import com.klsjnh.web.messagecenter.outbound.vo.message.JulyOutboundMessageVo011;
import com.klsjnh.web.util.Operator011Resolver;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * JulyOutboundMessage HTTP adapter: the unified send, the send record page query, the
 * resend and the record logic delete.
 */

@Tag(name = "消息中心011 - 消息发送与记录")
@RestController
@RequestMapping("/klsjnh/messagecenter/julyOutboundMessage/v1")
public class JulyOutboundMessageController {

    /**
     * Message center use case.
     */
    private final MessageOutboundUseCase messageOutboundUseCase;

    /**
     * Response converter.
     */
    private final JulyOutboundMessageConverter julyOutboundMessageConverter;

    /**
     * Create the controller.
     *
     * @param messageOutboundUseCase   message center use case
     * @param julyOutboundMessageConverter   response converter
     */
    public JulyOutboundMessageController(MessageOutboundUseCase messageOutboundUseCase, JulyOutboundMessageConverter julyOutboundMessageConverter) {
        this.messageOutboundUseCase = messageOutboundUseCase;
        this.julyOutboundMessageConverter = julyOutboundMessageConverter;
    }

    /**
     * Send one message through the resolved channel.
     *
     * @param vo send request
     * @return envelope with the send result
     */
    @PostMapping("/send")
    @Operation(summary = "发送消息（解析渠道 → 渲染模板 → 经渠道注册表发送 → 落发送记录）")
    public Response011<JulyOutboundMessageSendResultVo011> send(@Valid @RequestBody JulyOutboundMessageSendVo011 vo, HttpServletRequest request) {
        String funcName = "send";
        Operator011 operator = Operator011Resolver.resolve(request);


        MessageSendCommand command = new MessageSendCommand(vo.getChannelCode(), vo.getTo(), vo.getMessageType(),
                vo.getPayload(), vo.getTemplateCode(), vo.getParams(), vo.getTitle(), vo.getContent(), vo.getRemark());

        return Response011.success(funcName, julyOutboundMessageConverter.toSendResultVo(messageOutboundUseCase.send(operator.id(), command)));
    }

    /**
     * Page query the send records.
     *
     * @param vo page query request
     * @return page result of send records
     */
    @PostMapping("/selectListByPage")
    @Operation(summary = "分页查询发送记录（渠道/状态过滤 + 关键字）")
    public Response011<PageResult011<JulyOutboundMessageVo011>> selectListByPage(@Valid @RequestBody JulyOutboundMessageQueryVo011 vo, HttpServletRequest request) {
        String funcName = "select list by page";
        Operator011 operator = Operator011Resolver.resolve(request);


        PageQuery011 pageQuery = new PageQuery011(vo.getPageIndex(), vo.getPageSize());
        PageResult011<JulyOutboundMessage> page = messageOutboundUseCase.selectListByPage(operator.id(), pageQuery,
                new JulyOutboundMessageQuerySpec(vo.getChannelCode(), vo.getStatus(), vo.getKeyword()));

        return Response011.success(funcName, page.withRows(julyOutboundMessageConverter.toVoList(page.rows())));
    }

    /**
     * Resend a stored send record.
     *
     * @param idVo request with the send record id
     * @return envelope with the send result
     */
    @PostMapping("/resend")
    @Operation(summary = "重发（失败重试，重试次数 +1）")
    public Response011<JulyOutboundMessageSendResultVo011> resend(@Valid @RequestBody IdVo011 idVo, HttpServletRequest request) {
        String funcName = "resend";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.success(funcName, julyOutboundMessageConverter.toSendResultVo(messageOutboundUseCase.resend(operator.id(), idVo.getId())));
    }

    /**
     * Logic delete a send record.
     *
     * @param idVo request with the send record id
     * @return envelope with the deleted record id
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = AuditObjectCodes011.JULY_MESSAGE_OUTBOUND)
    @PostMapping("/logicDelete")
    @Operation(summary = "逻辑删除发送记录（单个）")
    public Response011<IdVo011> logicDelete(@Valid @RequestBody IdVo011 idVo, HttpServletRequest request) {
        String funcName = "logic delete";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.successId(funcName, messageOutboundUseCase.logicDelete(operator.id(), idVo.getId()));
    }

    /**
     * Batch logic delete send records (all-or-nothing).
     *
     * @param idsVo request with the send record ids
     * @return envelope with the batch delete summary
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = AuditObjectCodes011.JULY_MESSAGE_OUTBOUND)
    @PostMapping("/logicDeleteBatch")
    @Operation(summary = "逻辑删除发送记录（批量，全有或全无）")
    public Response011<BatchDeleteResultVo011> logicDeleteBatch(@Valid @RequestBody IdsVo011 idsVo, HttpServletRequest request) {
        String funcName = "batch logic delete";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.success(funcName, messageOutboundUseCase.logicDeleteBatch(operator.id(), idsVo.getIds()));
    }
}
