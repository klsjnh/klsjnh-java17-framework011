package com.klsjnh.web.aicenter.controller;

/*                AiDomainPromptController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.21
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.21  ai domain prompt controller
 *
 */

import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.response.Response011;
import com.klsjnh.common.vo.IdVo011;

import com.klsjnh.application.aicenter.prompt.JulyAiDomainPromptUseCase;
import com.klsjnh.domain.aicenter.prompt.JulyAiDomainPrompt;
import com.klsjnh.domain.aicenter.prompt.JulyAiDomainPromptQuerySpec;

import com.klsjnh.web.aicenter.vo.aidomainprompt.JulyAiDomainPromptInsertVo011;
import com.klsjnh.web.aicenter.vo.aidomainprompt.JulyAiDomainPromptQueryVo011;
import com.klsjnh.web.aicenter.vo.aidomainprompt.JulyAiDomainPromptRenderVo011;
import com.klsjnh.web.aicenter.vo.aidomainprompt.JulyAiDomainPromptUpdateVo011;
import com.klsjnh.web.aicenter.vo.aidomainprompt.JulyAiDomainPromptVo011;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AI prompt HTTP adapter
 * ({@code /klsjnh/aicenter/julyAiDomainPrompt/v1}): prompt CRUD under a
 * business domain, storage-mode body read and rendering a prompt for AI
 * development.
 */

@Tag(name = "AI中心011 - 提示词")
@RestController
@RequestMapping("/klsjnh/aicenter/julyAiDomainPrompt/v1")
public class AiDomainPromptController {

    /**
     * Prompt use case.
     */
    private final JulyAiDomainPromptUseCase promptUseCase;

    /**
     * Create the controller.
     *
     * @param promptUseCase prompt use case
     */
    public AiDomainPromptController(JulyAiDomainPromptUseCase promptUseCase) {
        this.promptUseCase = promptUseCase;
    }

    /**
     * Insert a prompt.
     *
     * @param vo request
     * @return new prompt id
     */
    @PostMapping("/insert")
    @Operation(summary = "新增提示词（归属业务域）")
    public Response011<IdVo011> insert(@RequestBody JulyAiDomainPromptInsertVo011 vo) {
        String funcName = "ai domain prompt insert";

        String id = promptUseCase.insert(vo.getPkMt(), vo.getPromptCode(), vo.getPromptName(), vo.getScene(),
                vo.getContentMode(), vo.getContent(), vo.getStorageCode(), vo.getBucket(), vo.getVariables(),
                vo.getSortOrder(), vo.getRemark(), vo.getStatus());

        return Response011.successId(funcName, id);
    }

    /**
     * Update a prompt.
     *
     * @param vo request
     * @return the prompt id
     */
    @PostMapping("/update")
    @Operation(summary = "修改提示词（promptCode 不可变）")
    public Response011<IdVo011> update(@RequestBody JulyAiDomainPromptUpdateVo011 vo) {
        String funcName = "ai domain prompt update";

        String id = promptUseCase.update(vo.getId(), vo.getPromptName(), vo.getScene(), vo.getContentMode(),
                vo.getContent(), vo.getStorageCode(), vo.getBucket(), vo.getVariables(), vo.getSortOrder(),
                vo.getRemark(), vo.getStatus());

        return Response011.successId(funcName, id);
    }

    /**
     * Logic delete a prompt.
     *
     * @param idVo id
     * @return deleted id
     */
    @PostMapping("/logicDelete")
    @Operation(summary = "逻辑删除提示词")
    public Response011<IdVo011> logicDelete(@RequestBody IdVo011 idVo) {
        String funcName = "ai domain prompt logic delete";

        return Response011.successId(funcName, promptUseCase.logicDelete(idVo.getId()));
    }

    /**
     * Find a prompt by id.
     *
     * @param id prompt id
     * @return prompt
     */
    @GetMapping("/getById")
    @Operation(summary = "按主键点查提示词")
    public Response011<JulyAiDomainPromptVo011> getById(@RequestParam("id") String id) {
        String funcName = "ai domain prompt get by id";

        return Response011.success(funcName, toVo(promptUseCase.getById(id)));
    }

    /**
     * Find a prompt by code.
     *
     * @param promptCode prompt code
     * @return prompt
     */
    @GetMapping("/getByCode")
    @Operation(summary = "按提示词编码点查")
    public Response011<JulyAiDomainPromptVo011> getByCode(@RequestParam("promptCode") String promptCode) {
        String funcName = "ai domain prompt get by code";

        return Response011.success(funcName, toVo(promptUseCase.getByCode(promptCode)));
    }

    /**
     * Page query prompts.
     *
     * @param vo request
     * @return page result
     */
    @PostMapping("/selectListByPage")
    @Operation(summary = "提示词分页（keyword/pkMt/scene/status）")
    public Response011<PageResult011<JulyAiDomainPromptVo011>> selectListByPage(
            @RequestBody JulyAiDomainPromptQueryVo011 vo) {
        String funcName = "ai domain prompt select list by page";

        PageQuery011 query = new PageQuery011(vo.getPageIndex(), vo.getPageSize());
        JulyAiDomainPromptQuerySpec spec = new JulyAiDomainPromptQuerySpec(vo.getKeyword(), vo.getPkMt(), vo.getScene(),
                vo.getStatus());
        PageResult011<JulyAiDomainPrompt> page = promptUseCase.selectListByPage(query, spec);

        List<JulyAiDomainPromptVo011> rows = page.rows().stream().map(this::toVo).toList();

        return Response011.success(funcName, PageResult011.of(query, page.total(), rows));
    }

    /**
     * Read a prompt body.
     *
     * @param id prompt id
     * @return content text
     */
    @GetMapping("/getContent")
    @Operation(summary = "读取提示词正文（inline / 对象存储）")
    public Response011<String> getContent(@RequestParam("id") String id) {
        String funcName = "ai domain prompt get content";

        return Response011.success(funcName, promptUseCase.getContent(id));
    }

    /**
     * Render a prompt.
     *
     * @param vo request
     * @return rendered text
     */
    @PostMapping("/render")
    @Operation(summary = "渲染提示词（${var} 替换；公共件给 AI 开发）")
    public Response011<String> render(@RequestBody JulyAiDomainPromptRenderVo011 vo) {
        String funcName = "ai domain prompt render";

        return Response011.success(funcName, promptUseCase.render(vo.getPromptCode(), vo.getParams()));
    }

    /**
     * Map the aggregate to the response VO.
     *
     * @param prompt aggregate
     * @return response VO
     */
    private JulyAiDomainPromptVo011 toVo(JulyAiDomainPrompt prompt) {
        JulyAiDomainPromptVo011 vo = new JulyAiDomainPromptVo011();
        vo.setId(prompt.id().value());
        vo.setPkMt(prompt.pkMt());
        vo.setPromptCode(prompt.promptCode());
        vo.setPromptName(prompt.promptName());
        vo.setScene(prompt.scene());
        vo.setContentMode(prompt.contentMode());
        vo.setStorageCode(prompt.storageCode());
        vo.setBucket(prompt.bucket());
        vo.setObjectKey(prompt.objectKey());
        vo.setContentHash(prompt.contentHash());
        vo.setContentSize(prompt.contentSize());
        vo.setVariables(prompt.variables());
        vo.setSortOrder(prompt.sortOrder());
        vo.setStatus(prompt.status());
        vo.setRemark(prompt.remark());
        vo.setCreateBy(prompt.audit().createBy());
        vo.setUpdateBy(prompt.audit().updateBy());
        vo.setCreateTime(prompt.audit().createTime());
        vo.setUpdateTime(prompt.audit().updateTime());

        return vo;
    }
}
