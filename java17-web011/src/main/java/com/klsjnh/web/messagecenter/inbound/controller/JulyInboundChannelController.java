package com.klsjnh.web.messagecenter.inbound.controller;

/*                JulyInboundChannelController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july message channel controller class
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

import com.klsjnh.application.messagecenter.inbound.channel.JulyInboundChannelUseCase;
import com.klsjnh.domain.messagecenter.inbound.channel.JulyInboundChannel;
import com.klsjnh.domain.messagecenter.inbound.channel.JulyInboundChannelQuerySpec;

import com.klsjnh.web.messagecenter.inbound.converter.JulyInboundChannelConverter;

import com.klsjnh.web.global.audit.AuditLog;
import com.klsjnh.web.messagecenter.inbound.vo.channel.JulyInboundChannelInsertVo011;
import com.klsjnh.web.messagecenter.inbound.vo.channel.JulyInboundChannelQueryVo011;
import com.klsjnh.web.messagecenter.inbound.vo.channel.JulyInboundChannelUpdateVo011;
import com.klsjnh.web.messagecenter.inbound.vo.channel.JulyInboundChannelVo011;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * JulyInboundChannel HTTP adapter: channel configuration CRUD.
 */

@Tag(name = "消息中心011 - 渠道配置")
@RestController
@RequestMapping("/klsjnh/messagecenter/julyInboundChannel/v1")
public class JulyInboundChannelController {

    /**
     * JulyInboundChannel use case.
     */
    private final JulyInboundChannelUseCase julyInboundChannelUseCase;

    /**
     * Response converter.
     */
    private final JulyInboundChannelConverter julyInboundChannelConverter;

    /**
     * Create the controller.
     *
     * @param julyInboundChannelUseCase    channel use case
     * @param julyInboundChannelConverter  response converter
     */
    public JulyInboundChannelController(JulyInboundChannelUseCase julyInboundChannelUseCase,
            JulyInboundChannelConverter julyInboundChannelConverter) {
        this.julyInboundChannelUseCase = julyInboundChannelUseCase;
        this.julyInboundChannelConverter = julyInboundChannelConverter;
    }

    /**
     * Insert a new channel.
     *
     * @param vo insert request
     * @return envelope with the new channel id
     */
    @AuditLog(type = AuditType011.INSERT, objectCode = AuditObjectCodes011.JULY_MESSAGE_INBOUND_CHANNEL)
    @PostMapping("/insert")
    @Operation(summary = "新增渠道（channelCode 查重）")
    public Response011<IdVo011> insert(@RequestBody JulyInboundChannelInsertVo011 vo) {
        String funcName = "insert";

        return Response011.successId(funcName, julyInboundChannelUseCase.insert(vo.getChannelCode(), vo.getSortOrder(),
                vo.getChannelName(), vo.getProviderType(), vo.getConfig(), vo.getRemark()));
    }

    /**
     * Update a channel.
     *
     * @param vo update request
     * @return envelope with the channel id
     */
    @AuditLog(type = AuditType011.UPDATE, objectCode = AuditObjectCodes011.JULY_MESSAGE_INBOUND_CHANNEL)
    @PostMapping("/update")
    @Operation(summary = "修改渠道（channelCode 不可变）")
    public Response011<IdVo011> update(@RequestBody JulyInboundChannelUpdateVo011 vo) {
        String funcName = "update";

        return Response011.successId(funcName, julyInboundChannelUseCase.update(vo.getId(), vo.getChannelName(),
                vo.getProviderType(), vo.getConfig(), vo.getSortOrder(), vo.getStatus(), vo.getRemark()));
    }

    /**
     * Logic delete a channel.
     *
     * @param idVo request with the channel id
     * @return envelope with the deleted channel id
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = AuditObjectCodes011.JULY_MESSAGE_INBOUND_CHANNEL)
    @PostMapping("/logicDelete")
    @Operation(summary = "逻辑删除渠道（单个）")
    public Response011<IdVo011> logicDelete(@RequestBody IdVo011 idVo) {
        String funcName = "logic delete";

        return Response011.successId(funcName, julyInboundChannelUseCase.logicDelete(idVo.getId()));
    }

    /**
     * Batch logic delete channels (all-or-nothing).
     *
     * @param idsVo request with the channel ids
     * @return envelope with the batch delete summary
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = AuditObjectCodes011.JULY_MESSAGE_INBOUND_CHANNEL)
    @PostMapping("/logicDeleteBatch")
    @Operation(summary = "逻辑删除渠道（批量，全有或全无）")
    public Response011<BatchDeleteResultVo011> logicDeleteBatch(@RequestBody IdsVo011 idsVo) {
        String funcName = "batch logic delete";

        return Response011.success(funcName, julyInboundChannelUseCase.logicDeleteBatch(idsVo.getIds()));
    }

    /**
     * Find a channel by primary key.
     *
     * @param id channel id, passed as a query parameter
     * @return channel detail
     */
    @GetMapping("/getById")
    @Operation(summary = "主键查询（id 走 query）")
    public Response011<JulyInboundChannelVo011> getById(@RequestParam("id") String id) {
        String funcName = "get by id";

        return Response011.success(funcName, julyInboundChannelConverter.toVo(julyInboundChannelUseCase.getById(id)));
    }

    /**
     * Page query with optional keyword / status filters.
     *
     * @param vo page query request
     * @return page result of channels
     */
    @PostMapping("/selectListByPage")
    @Operation(summary = "分页查询（编码/名称/提供商模糊 + 状态过滤）")
    public Response011<PageResult011<JulyInboundChannelVo011>> selectListByPage(
            @RequestBody JulyInboundChannelQueryVo011 vo) {
        String funcName = "select list by page";

        PageQuery011 pageQuery = new PageQuery011(vo.getPageIndex(), vo.getPageSize());
        PageResult011<JulyInboundChannel> page = julyInboundChannelUseCase.selectListByPage(pageQuery,
                new JulyInboundChannelQuerySpec(vo.getKeyword(), vo.getStatus()));

        return Response011.success(funcName, page.withRows(julyInboundChannelConverter.toVoList(page.rows())));
    }
}
