package com.klsjnh.web.messagecenter.controller;

/*                JulyMessageTemplateController class
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

import com.klsjnh.application.messagecenter.template.JulyMessageTemplateUseCase;
import com.klsjnh.domain.messagecenter.template.JulyMessageTemplate;
import com.klsjnh.domain.messagecenter.template.JulyMessageTemplateQuerySpec;

import com.klsjnh.web.messagecenter.converter.JulyMessageTemplateConverter;

import com.klsjnh.web.global.audit.AuditLog;
import com.klsjnh.web.messagecenter.vo.template.JulyMessageTemplateInsertVo011;
import com.klsjnh.web.messagecenter.vo.template.JulyMessageTemplateQueryVo011;
import com.klsjnh.web.messagecenter.vo.template.JulyMessageTemplateUpdateVo011;
import com.klsjnh.web.messagecenter.vo.template.JulyMessageTemplateVo011;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * JulyMessageTemplate HTTP adapter: message template CRUD.
 */

@Tag(name = "消息中心011 - 消息模板")
@RestController
@RequestMapping("/klsjnh/messagecenter/julyMessageTemplate/v1")
public class JulyMessageTemplateController {

    /**
     * JulyMessageTemplate use case.
     */
    private final JulyMessageTemplateUseCase julyMessageTemplateUseCase;

    /**
     * Response converter.
     */
    private final JulyMessageTemplateConverter julyMessageTemplateConverter;

    /**
     * Create the controller.
     *
     * @param julyMessageTemplateUseCase    template use case
     * @param julyMessageTemplateConverter  response converter
     */
    public JulyMessageTemplateController(JulyMessageTemplateUseCase julyMessageTemplateUseCase,
            JulyMessageTemplateConverter julyMessageTemplateConverter) {
        this.julyMessageTemplateUseCase = julyMessageTemplateUseCase;
        this.julyMessageTemplateConverter = julyMessageTemplateConverter;
    }

    /**
     * Insert a new template.
     *
     * @param vo insert request
     * @return envelope with the new template id
     */
    @AuditLog(type = AuditType011.INSERT, objectCode = AuditObjectCodes011.JULY_MESSAGE_TEMPLATE)
    @PostMapping("/insert")
    @Operation(summary = "新增模板（templateCode 查重）")
    public Response011<IdVo011> insert(@RequestBody JulyMessageTemplateInsertVo011 vo) {
        String funcName = "insert";

        return Response011.successId(funcName, julyMessageTemplateUseCase.insert(vo.getTemplateCode(),
                vo.getSortOrder(), vo.getTemplateName(), vo.getChannelCode(), vo.getTitle(), vo.getContent(),
                vo.getRemark()));
    }

    /**
     * Update a template.
     *
     * @param vo update request
     * @return envelope with the template id
     */
    @AuditLog(type = AuditType011.UPDATE, objectCode = AuditObjectCodes011.JULY_MESSAGE_TEMPLATE)
    @PostMapping("/update")
    @Operation(summary = "修改模板（templateCode 不可变）")
    public Response011<IdVo011> update(@RequestBody JulyMessageTemplateUpdateVo011 vo) {
        String funcName = "update";

        return Response011.successId(funcName, julyMessageTemplateUseCase.update(vo.getId(), vo.getTemplateName(),
                vo.getChannelCode(), vo.getTitle(), vo.getContent(), vo.getSortOrder(), vo.getStatus(), vo.getRemark()));
    }

    /**
     * Logic delete a template.
     *
     * @param idVo request with the template id
     * @return envelope with the deleted template id
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = AuditObjectCodes011.JULY_MESSAGE_TEMPLATE)
    @PostMapping("/logicDelete")
    @Operation(summary = "逻辑删除模板（单个）")
    public Response011<IdVo011> logicDelete(@RequestBody IdVo011 idVo) {
        String funcName = "logic delete";

        return Response011.successId(funcName, julyMessageTemplateUseCase.logicDelete(idVo.getId()));
    }

    /**
     * Batch logic delete templates (all-or-nothing).
     *
     * @param idsVo request with the template ids
     * @return envelope with the batch delete summary
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = AuditObjectCodes011.JULY_MESSAGE_TEMPLATE)
    @PostMapping("/logicDeleteBatch")
    @Operation(summary = "逻辑删除模板（批量，全有或全无）")
    public Response011<BatchDeleteResultVo011> logicDeleteBatch(@RequestBody IdsVo011 idsVo) {
        String funcName = "batch logic delete";

        return Response011.success(funcName, julyMessageTemplateUseCase.logicDeleteBatch(idsVo.getIds()));
    }

    /**
     * Find a template by primary key.
     *
     * @param id template id, passed as a query parameter
     * @return template detail
     */
    @GetMapping("/getById")
    @Operation(summary = "主键查询（id 走 query）")
    public Response011<JulyMessageTemplateVo011> getById(@RequestParam("id") String id) {
        String funcName = "get by id";

        return Response011.success(funcName, julyMessageTemplateConverter.toVo(julyMessageTemplateUseCase.getById(id)));
    }

    /**
     * Page query with optional keyword / channel / status filters.
     *
     * @param vo page query request
     * @return page result of templates
     */
    @PostMapping("/selectListByPage")
    @Operation(summary = "分页查询（编码/名称模糊 + 渠道/状态过滤）")
    public Response011<PageResult011<JulyMessageTemplateVo011>> selectListByPage(
            @RequestBody JulyMessageTemplateQueryVo011 vo) {
        String funcName = "select list by page";

        PageQuery011 pageQuery = new PageQuery011(vo.getPageIndex(), vo.getPageSize());
        PageResult011<JulyMessageTemplate> page = julyMessageTemplateUseCase.selectListByPage(pageQuery,
                new JulyMessageTemplateQuerySpec(vo.getKeyword(), vo.getChannelCode(), vo.getStatus()));

        return Response011.success(funcName, page.withRows(julyMessageTemplateConverter.toVoList(page.rows())));
    }
}
