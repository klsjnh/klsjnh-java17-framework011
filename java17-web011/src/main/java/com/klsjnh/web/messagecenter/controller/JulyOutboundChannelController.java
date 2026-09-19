package com.klsjnh.web.messagecenter.controller;

/*                JulyOutboundChannelController class
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

import com.klsjnh.application.messagecenter.outbound.channel.JulyOutboundChannelUseCase;
import com.klsjnh.domain.messagecenter.outbound.channel.JulyOutboundChannel;
import com.klsjnh.domain.messagecenter.outbound.channel.JulyOutboundChannelQuerySpec;

import com.klsjnh.web.messagecenter.converter.JulyOutboundChannelConverter;

import com.klsjnh.web.global.audit.AuditLog;
import com.klsjnh.web.messagecenter.vo.channel.JulyOutboundChannelInsertVo011;
import com.klsjnh.web.messagecenter.vo.channel.JulyOutboundChannelQueryVo011;
import com.klsjnh.web.messagecenter.vo.channel.JulyOutboundChannelUpdateVo011;
import com.klsjnh.web.messagecenter.vo.channel.JulyOutboundChannelVo011;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * JulyOutboundChannel HTTP adapter: channel configuration CRUD.
 */

@Tag(name = "消息中心011 - 渠道配置")
@RestController
@RequestMapping("/klsjnh/messagecenter/julyOutboundChannel/v1")
public class JulyOutboundChannelController {

    /**
     * JulyOutboundChannel use case.
     */
    private final JulyOutboundChannelUseCase julyOutboundChannelUseCase;

    /**
     * Response converter.
     */
    private final JulyOutboundChannelConverter julyOutboundChannelConverter;

    /**
     * Create the controller.
     *
     * @param julyOutboundChannelUseCase    channel use case
     * @param julyOutboundChannelConverter  response converter
     */
    public JulyOutboundChannelController(JulyOutboundChannelUseCase julyOutboundChannelUseCase,
            JulyOutboundChannelConverter julyOutboundChannelConverter) {
        this.julyOutboundChannelUseCase = julyOutboundChannelUseCase;
        this.julyOutboundChannelConverter = julyOutboundChannelConverter;
    }

    /**
     * Insert a new channel.
     *
     * @param vo insert request
     * @return envelope with the new channel id
     */
    @AuditLog(type = AuditType011.INSERT, objectCode = AuditObjectCodes011.JULY_MESSAGE_OUTBOUND_CHANNEL)
    @PostMapping("/insert")
    @Operation(summary = "新增渠道（channelCode 查重）")
    public Response011<IdVo011> insert(@RequestBody JulyOutboundChannelInsertVo011 vo) {
        String funcName = "insert";

        return Response011.successId(funcName, julyOutboundChannelUseCase.insert(vo.getChannelCode(), vo.getSortOrder(),
                vo.getChannelName(), vo.getProviderType(), vo.getConfig(), vo.getRemark()));
    }

    /**
     * Update a channel.
     *
     * @param vo update request
     * @return envelope with the channel id
     */
    @AuditLog(type = AuditType011.UPDATE, objectCode = AuditObjectCodes011.JULY_MESSAGE_OUTBOUND_CHANNEL)
    @PostMapping("/update")
    @Operation(summary = "修改渠道（channelCode 不可变）")
    public Response011<IdVo011> update(@RequestBody JulyOutboundChannelUpdateVo011 vo) {
        String funcName = "update";

        return Response011.successId(funcName, julyOutboundChannelUseCase.update(vo.getId(), vo.getChannelName(),
                vo.getProviderType(), vo.getConfig(), vo.getSortOrder(), vo.getStatus(), vo.getRemark()));
    }

    /**
     * Logic delete a channel.
     *
     * @param idVo request with the channel id
     * @return envelope with the deleted channel id
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = AuditObjectCodes011.JULY_MESSAGE_OUTBOUND_CHANNEL)
    @PostMapping("/logicDelete")
    @Operation(summary = "逻辑删除渠道（单个）")
    public Response011<IdVo011> logicDelete(@RequestBody IdVo011 idVo) {
        String funcName = "logic delete";

        return Response011.successId(funcName, julyOutboundChannelUseCase.logicDelete(idVo.getId()));
    }

    /**
     * Batch logic delete channels (all-or-nothing).
     *
     * @param idsVo request with the channel ids
     * @return envelope with the batch delete summary
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = AuditObjectCodes011.JULY_MESSAGE_OUTBOUND_CHANNEL)
    @PostMapping("/logicDeleteBatch")
    @Operation(summary = "逻辑删除渠道（批量，全有或全无）")
    public Response011<BatchDeleteResultVo011> logicDeleteBatch(@RequestBody IdsVo011 idsVo) {
        String funcName = "batch logic delete";

        return Response011.success(funcName, julyOutboundChannelUseCase.logicDeleteBatch(idsVo.getIds()));
    }

    /**
     * Find a channel by primary key.
     *
     * @param id channel id, passed as a query parameter
     * @return channel detail
     */
    @GetMapping("/getById")
    @Operation(summary = "主键查询（id 走 query）")
    public Response011<JulyOutboundChannelVo011> getById(@RequestParam("id") String id) {
        String funcName = "get by id";

        return Response011.success(funcName, julyOutboundChannelConverter.toVo(julyOutboundChannelUseCase.getById(id)));
    }

    /**
     * Page query with optional keyword / status filters.
     *
     * @param vo page query request
     * @return page result of channels
     */
    @PostMapping("/selectListByPage")
    @Operation(summary = "分页查询（编码/名称/提供商模糊 + 状态过滤）")
    public Response011<PageResult011<JulyOutboundChannelVo011>> selectListByPage(
            @RequestBody JulyOutboundChannelQueryVo011 vo) {
        String funcName = "select list by page";

        PageQuery011 pageQuery = new PageQuery011(vo.getPageIndex(), vo.getPageSize());
        PageResult011<JulyOutboundChannel> page = julyOutboundChannelUseCase.selectListByPage(pageQuery,
                new JulyOutboundChannelQuerySpec(vo.getKeyword(), vo.getStatus()));

        return Response011.success(funcName, page.withRows(julyOutboundChannelConverter.toVoList(page.rows())));
    }
}
