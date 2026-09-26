package com.klsjnh.web.messagecenter.outbound.controller;

/*                JulyOutboundTemplateController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july message template controller class
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

import com.klsjnh.application.messagecenter.outbound.template.JulyOutboundTemplateUseCase;
import com.klsjnh.domain.messagecenter.outbound.template.JulyOutboundTemplate;
import com.klsjnh.domain.messagecenter.outbound.template.JulyOutboundTemplateQuerySpec;

import com.klsjnh.web.messagecenter.outbound.converter.JulyOutboundTemplateConverter;

import com.klsjnh.web.global.audit.AuditLog;
import com.klsjnh.web.messagecenter.outbound.vo.template.JulyOutboundTemplateInsertVo011;
import com.klsjnh.web.messagecenter.outbound.vo.template.JulyOutboundTemplateQueryVo011;
import com.klsjnh.web.messagecenter.outbound.vo.template.JulyOutboundTemplateUpdateVo011;
import com.klsjnh.web.messagecenter.outbound.vo.template.JulyOutboundTemplateVo011;
import com.klsjnh.web.util.Operator011Resolver;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * JulyOutboundTemplate HTTP adapter: message template CRUD.
 */

@Tag(name = "消息中心011 - 消息模板")
@RestController
@RequestMapping("/klsjnh/messagecenter/julyOutboundTemplate/v1")
public class JulyOutboundTemplateController {

    /**
     * JulyOutboundTemplate use case.
     */
    private final JulyOutboundTemplateUseCase julyOutboundTemplateUseCase;

    /**
     * Response converter.
     */
    private final JulyOutboundTemplateConverter julyOutboundTemplateConverter;

    /**
     * Create the controller.
     *
     * @param julyOutboundTemplateUseCase    template use case
     * @param julyOutboundTemplateConverter  response converter
     */
    public JulyOutboundTemplateController(JulyOutboundTemplateUseCase julyOutboundTemplateUseCase,
            JulyOutboundTemplateConverter julyOutboundTemplateConverter) {
        this.julyOutboundTemplateUseCase = julyOutboundTemplateUseCase;
        this.julyOutboundTemplateConverter = julyOutboundTemplateConverter;
    }

    /**
     * Insert a new template.
     *
     * @param vo insert request
     * @return envelope with the new template id
     */
    @AuditLog(type = AuditType011.INSERT, objectCode = AuditObjectCodes011.JULY_MESSAGE_OUTBOUND_TEMPLATE)
    @PostMapping("/insert")
    @Operation(summary = "新增模板（templateCode 查重）")
    public Response011<IdVo011> insert(@Valid @RequestBody JulyOutboundTemplateInsertVo011 vo, HttpServletRequest request) {
        String funcName = "insert";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.successId(funcName, julyOutboundTemplateUseCase.insert(operator.id(), vo.getTemplateCode(),
                vo.getSortOrder(), vo.getTemplateName(), vo.getChannelCode(), vo.getTitle(), vo.getContent(),
                vo.getRemark()));
    }

    /**
     * Update a template.
     *
     * @param vo update request
     * @return envelope with the template id
     */
    @AuditLog(type = AuditType011.UPDATE, objectCode = AuditObjectCodes011.JULY_MESSAGE_OUTBOUND_TEMPLATE)
    @PostMapping("/update")
    @Operation(summary = "修改模板（templateCode 不可变）")
    public Response011<IdVo011> update(@Valid @RequestBody JulyOutboundTemplateUpdateVo011 vo, HttpServletRequest request) {
        String funcName = "update";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.successId(funcName, julyOutboundTemplateUseCase.update(operator.id(), vo.getId(), vo.getTemplateName(),
                vo.getChannelCode(), vo.getTitle(), vo.getContent(), vo.getSortOrder(), vo.getStatus(), vo.getRemark()));
    }

    /**
     * Logic delete a template.
     *
     * @param idVo request with the template id
     * @return envelope with the deleted template id
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = AuditObjectCodes011.JULY_MESSAGE_OUTBOUND_TEMPLATE)
    @PostMapping("/logicDelete")
    @Operation(summary = "逻辑删除模板（单个）")
    public Response011<IdVo011> logicDelete(@Valid @RequestBody IdVo011 idVo, HttpServletRequest request) {
        String funcName = "logic delete";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.successId(funcName, julyOutboundTemplateUseCase.logicDelete(operator.id(), idVo.getId()));
    }

    /**
     * Batch logic delete templates (all-or-nothing).
     *
     * @param idsVo request with the template ids
     * @return envelope with the batch delete summary
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = AuditObjectCodes011.JULY_MESSAGE_OUTBOUND_TEMPLATE)
    @PostMapping("/logicDeleteBatch")
    @Operation(summary = "逻辑删除模板（批量，全有或全无）")
    public Response011<BatchDeleteResultVo011> logicDeleteBatch(@Valid @RequestBody IdsVo011 idsVo, HttpServletRequest request) {
        String funcName = "batch logic delete";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.success(funcName, julyOutboundTemplateUseCase.logicDeleteBatch(operator.id(), idsVo.getIds()));
    }

    /**
     * Find a template by primary key.
     *
     * @param id template id, passed as a query parameter
     * @return template detail
     */
    @GetMapping("/getById")
    @Operation(summary = "主键查询（id 走 query）")
    public Response011<JulyOutboundTemplateVo011> getById(@RequestParam("id") String id, HttpServletRequest request) {
        String funcName = "get by id";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.success(funcName, julyOutboundTemplateConverter.toVo(julyOutboundTemplateUseCase.getById(operator.id(), id)));
    }

    /**
     * Page query with optional keyword / channel / status filters.
     *
     * @param vo page query request
     * @return page result of templates
     */
    @PostMapping("/selectListByPage")
    @Operation(summary = "分页查询（编码/名称模糊 + 渠道/状态过滤）")
    public Response011<PageResult011<JulyOutboundTemplateVo011>> selectListByPage(
            @Valid @RequestBody JulyOutboundTemplateQueryVo011 vo, HttpServletRequest request) {
        String funcName = "select list by page";
        Operator011 operator = Operator011Resolver.resolve(request);


        PageQuery011 pageQuery = new PageQuery011(vo.getPageIndex(), vo.getPageSize());
        PageResult011<JulyOutboundTemplate> page = julyOutboundTemplateUseCase.selectListByPage(operator.id(), pageQuery,
                new JulyOutboundTemplateQuerySpec(vo.getKeyword(), vo.getChannelCode(), vo.getStatus()));

        return Response011.success(funcName, page.withRows(julyOutboundTemplateConverter.toVoList(page.rows())));
    }
}
