package com.klsjnh.web.messagecenter.inbound.controller;

/*                JulyInboundMessageController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july inbound message controller class
 *
 */

import com.klsjnh.common.constant.AuditObjectCodes011;
import com.klsjnh.common.enums.AuditType011;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.response.Response011;
import com.klsjnh.common.vo.BatchDeleteResultVo011;
import com.klsjnh.common.vo.IdVo011;
import com.klsjnh.common.vo.IdsVo011;

import com.klsjnh.application.messagecenter.inbound.message.MessageInboundUseCase;
import com.klsjnh.domain.messagecenter.inbound.message.JulyInboundMessage;
import com.klsjnh.domain.messagecenter.inbound.message.JulyInboundMessageQuerySpec;

import com.klsjnh.web.messagecenter.inbound.converter.JulyInboundMessageConverter;

import com.klsjnh.web.global.audit.AuditLog;
import com.klsjnh.web.messagecenter.inbound.vo.message.JulyInboundMessageQueryVo011;
import com.klsjnh.web.messagecenter.inbound.vo.message.JulyInboundMessageVo011;
import com.klsjnh.web.messagecenter.inbound.vo.message.JulyInboundReceiveResultVo011;
import com.klsjnh.web.messagecenter.inbound.vo.message.JulyInboundReceiveVo011;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * JulyInboundMessage HTTP adapter: the unified receive, the received record
 * page query and the record logic delete.
 */

@Tag(name = "消息中心011 - 消息接收与记录")
@RestController
@RequestMapping("/klsjnh/messagecenter/julyInboundMessage/v1")
public class JulyInboundMessageController {

    /**
     * Message inbound use case.
     */
    private final MessageInboundUseCase messageInboundUseCase;

    /**
     * Response converter.
     */
    private final JulyInboundMessageConverter julyInboundMessageConverter;

    /**
     * Create the controller.
     *
     * @param messageInboundUseCase       message inbound use case
     * @param julyInboundMessageConverter response converter
     */
    public JulyInboundMessageController(MessageInboundUseCase messageInboundUseCase,
            JulyInboundMessageConverter julyInboundMessageConverter) {
        this.messageInboundUseCase = messageInboundUseCase;
        this.julyInboundMessageConverter = julyInboundMessageConverter;
    }

    /**
     * Receive one inbound message (channel code travels as a query parameter).
     *
     * @param channelCode channel code
     * @param vo          receive request with the raw body, may be absent
     * @return envelope with the receive result
     */
    @PostMapping("/receive")
    @Operation(summary = "接收消息（解析渠道 → 入站端口解析 → 去重落库 → 监听器处理）")
    public Response011<JulyInboundReceiveResultVo011> receive(@RequestParam("channelCode") String channelCode,
            @RequestBody(required = false) JulyInboundReceiveVo011 vo) {
        String funcName = "receive";

        String rawBody = vo == null ? null : vo.getRawBody();

        return Response011.success(funcName,
                julyInboundMessageConverter.toReceiveResultVo(messageInboundUseCase.receive(channelCode, rawBody)));
    }

    /**
     * Page query the received records.
     *
     * @param vo page query request
     * @return page result of received records
     */
    @PostMapping("/selectListByPage")
    @Operation(summary = "分页查询接收记录（渠道/状态过滤 + 关键字）")
    public Response011<PageResult011<JulyInboundMessageVo011>> selectListByPage(
            @RequestBody JulyInboundMessageQueryVo011 vo) {
        String funcName = "select list by page";

        PageQuery011 pageQuery = new PageQuery011(vo.getPageIndex(), vo.getPageSize());
        PageResult011<JulyInboundMessage> page = messageInboundUseCase.selectListByPage(pageQuery,
                new JulyInboundMessageQuerySpec(vo.getChannelCode(), vo.getStatus(), vo.getKeyword()));

        return Response011.success(funcName, page.withRows(julyInboundMessageConverter.toVoList(page.rows())));
    }

    /**
     * Logic delete a received record.
     *
     * @param idVo request with the record id
     * @return envelope with the deleted record id
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = AuditObjectCodes011.JULY_MESSAGE_INBOUND)
    @PostMapping("/logicDelete")
    @Operation(summary = "逻辑删除接收记录（单个）")
    public Response011<IdVo011> logicDelete(@RequestBody IdVo011 idVo) {
        String funcName = "logic delete";

        return Response011.successId(funcName, messageInboundUseCase.logicDelete(idVo.getId()));
    }

    /**
     * Batch logic delete received records (all-or-nothing).
     *
     * @param idsVo request with the record ids
     * @return envelope with the batch delete summary
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = AuditObjectCodes011.JULY_MESSAGE_INBOUND)
    @PostMapping("/logicDeleteBatch")
    @Operation(summary = "逻辑删除接收记录（批量，全有或全无）")
    public Response011<BatchDeleteResultVo011> logicDeleteBatch(@RequestBody IdsVo011 idsVo) {
        String funcName = "batch logic delete";

        return Response011.success(funcName, messageInboundUseCase.logicDeleteBatch(idsVo.getIds()));
    }
}
