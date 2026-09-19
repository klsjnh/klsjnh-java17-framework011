package com.klsjnh.web.messagecenter.inbound.controller;

/*                JulyInboundTemplateController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.19
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.19  july message template controller class
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

import com.klsjnh.application.messagecenter.inbound.template.JulyInboundTemplateUseCase;
import com.klsjnh.domain.messagecenter.inbound.template.JulyInboundTemplate;
import com.klsjnh.domain.messagecenter.inbound.template.JulyInboundTemplateQuerySpec;

import com.klsjnh.web.messagecenter.inbound.converter.JulyInboundTemplateConverter;

import com.klsjnh.web.global.audit.AuditLog;
import com.klsjnh.web.messagecenter.inbound.vo.template.JulyInboundTemplateInsertVo011;
import com.klsjnh.web.messagecenter.inbound.vo.template.JulyInboundTemplateQueryVo011;
import com.klsjnh.web.messagecenter.inbound.vo.template.JulyInboundTemplateUpdateVo011;
import com.klsjnh.web.messagecenter.inbound.vo.template.JulyInboundTemplateVo011;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * JulyInboundTemplate HTTP adapter: message template CRUD.
 */

@Tag(name = "消息中心011 - 消息模板")
@RestController
@RequestMapping("/klsjnh/messagecenter/julyInboundTemplate/v1")
public class JulyInboundTemplateController {

    /**
     * JulyInboundTemplate use case.
     */
    private final JulyInboundTemplateUseCase julyInboundTemplateUseCase;

    /**
     * Response converter.
     */
    private final JulyInboundTemplateConverter julyInboundTemplateConverter;

    /**
     * Create the controller.
     *
     * @param julyInboundTemplateUseCase    template use case
     * @param julyInboundTemplateConverter  response converter
     */
    public JulyInboundTemplateController(JulyInboundTemplateUseCase julyInboundTemplateUseCase,
            JulyInboundTemplateConverter julyInboundTemplateConverter) {
        this.julyInboundTemplateUseCase = julyInboundTemplateUseCase;
        this.julyInboundTemplateConverter = julyInboundTemplateConverter;
    }

    /**
     * Insert a new template.
     *
     * @param vo insert request
     * @return envelope with the new template id
     */
    @AuditLog(type = AuditType011.INSERT, objectCode = AuditObjectCodes011.JULY_MESSAGE_INBOUND_TEMPLATE)
    @PostMapping("/insert")
    @Operation(summary = "新增模板（templateCode 查重）")
    public Response011<IdVo011> insert(@RequestBody JulyInboundTemplateInsertVo011 vo) {
        String funcName = "insert";

        return Response011.successId(funcName, julyInboundTemplateUseCase.insert(vo.getTemplateCode(),
                vo.getSortOrder(), vo.getTemplateName(), vo.getChannelCode(), vo.getTitle(), vo.getContent(),
                vo.getRemark()));
    }

    /**
     * Update a template.
     *
     * @param vo update request
     * @return envelope with the template id
     */
    @AuditLog(type = AuditType011.UPDATE, objectCode = AuditObjectCodes011.JULY_MESSAGE_INBOUND_TEMPLATE)
    @PostMapping("/update")
    @Operation(summary = "修改模板（templateCode 不可变）")
    public Response011<IdVo011> update(@RequestBody JulyInboundTemplateUpdateVo011 vo) {
        String funcName = "update";

        return Response011.successId(funcName, julyInboundTemplateUseCase.update(vo.getId(), vo.getTemplateName(),
                vo.getChannelCode(), vo.getTitle(), vo.getContent(), vo.getSortOrder(), vo.getStatus(), vo.getRemark()));
    }

    /**
     * Logic delete a template.
     *
     * @param idVo request with the template id
     * @return envelope with the deleted template id
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = AuditObjectCodes011.JULY_MESSAGE_INBOUND_TEMPLATE)
    @PostMapping("/logicDelete")
    @Operation(summary = "逻辑删除模板（单个）")
    public Response011<IdVo011> logicDelete(@RequestBody IdVo011 idVo) {
        String funcName = "logic delete";

        return Response011.successId(funcName, julyInboundTemplateUseCase.logicDelete(idVo.getId()));
    }

    /**
     * Batch logic delete templates (all-or-nothing).
     *
     * @param idsVo request with the template ids
     * @return envelope with the batch delete summary
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = AuditObjectCodes011.JULY_MESSAGE_INBOUND_TEMPLATE)
    @PostMapping("/logicDeleteBatch")
    @Operation(summary = "逻辑删除模板（批量，全有或全无）")
    public Response011<BatchDeleteResultVo011> logicDeleteBatch(@RequestBody IdsVo011 idsVo) {
        String funcName = "batch logic delete";

        return Response011.success(funcName, julyInboundTemplateUseCase.logicDeleteBatch(idsVo.getIds()));
    }

    /**
     * Find a template by primary key.
     *
     * @param id template id, passed as a query parameter
     * @return template detail
     */
    @GetMapping("/getById")
    @Operation(summary = "主键查询（id 走 query）")
    public Response011<JulyInboundTemplateVo011> getById(@RequestParam("id") String id) {
        String funcName = "get by id";

        return Response011.success(funcName, julyInboundTemplateConverter.toVo(julyInboundTemplateUseCase.getById(id)));
    }

    /**
     * Page query with optional keyword / channel / status filters.
     *
     * @param vo page query request
     * @return page result of templates
     */
    @PostMapping("/selectListByPage")
    @Operation(summary = "分页查询（编码/名称模糊 + 渠道/状态过滤）")
    public Response011<PageResult011<JulyInboundTemplateVo011>> selectListByPage(
            @RequestBody JulyInboundTemplateQueryVo011 vo) {
        String funcName = "select list by page";

        PageQuery011 pageQuery = new PageQuery011(vo.getPageIndex(), vo.getPageSize());
        PageResult011<JulyInboundTemplate> page = julyInboundTemplateUseCase.selectListByPage(pageQuery,
                new JulyInboundTemplateQuerySpec(vo.getKeyword(), vo.getChannelCode(), vo.getStatus()));

        return Response011.success(funcName, page.withRows(julyInboundTemplateConverter.toVoList(page.rows())));
    }
}
