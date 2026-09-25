package com.klsjnh.web.aicenter.controller;

/*                AiDomainController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.21
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.21  ai domain controller
 *      2026.09.21  merged master-sub module: master / whole / child saves + render
 *      2026.09.26  pass operator into use case for permission checks
 *
 */

import com.klsjnh.common.identity.Operator011;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.response.Response011;
import com.klsjnh.common.vo.IdVo011;

import com.klsjnh.application.aicenter.prompt.JulyAiDomainUseCase;
import com.klsjnh.application.aicenter.prompt.PromptSaveCommand;
import com.klsjnh.domain.aicenter.prompt.JulyAiDomain;
import com.klsjnh.domain.aicenter.prompt.JulyAiDomainBundle;
import com.klsjnh.domain.aicenter.prompt.JulyAiDomainPrompt;
import com.klsjnh.domain.aicenter.prompt.JulyAiDomainPromptQuerySpec;
import com.klsjnh.domain.aicenter.prompt.JulyAiDomainQuerySpec;

import com.klsjnh.web.aicenter.vo.aidomain.JulyAiDomainBundleVo011;
import com.klsjnh.web.aicenter.vo.aidomain.JulyAiDomainInsertVo011;
import com.klsjnh.web.aicenter.vo.aidomain.JulyAiDomainQueryVo011;
import com.klsjnh.web.aicenter.vo.aidomain.JulyAiDomainSaveWholeVo011;
import com.klsjnh.web.aicenter.vo.aidomain.JulyAiDomainUpdateVo011;
import com.klsjnh.web.aicenter.vo.aidomain.JulyAiDomainVo011;
import com.klsjnh.web.aicenter.vo.aidomainprompt.JulyAiDomainPromptInsertVo011;
import com.klsjnh.web.aicenter.vo.aidomainprompt.JulyAiDomainPromptQueryVo011;
import com.klsjnh.web.aicenter.vo.aidomainprompt.JulyAiDomainPromptRenderVo011;
import com.klsjnh.web.aicenter.vo.aidomainprompt.JulyAiDomainPromptSaveVo011;
import com.klsjnh.web.aicenter.vo.aidomainprompt.JulyAiDomainPromptUpdateVo011;
import com.klsjnh.web.aicenter.vo.aidomainprompt.JulyAiDomainPromptVo011;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Prompt master-sub HTTP adapter ({@code /klsjnh/aicenter/julyAiDomain/v1}):
 * master = business domain tree (july_ai_domain), child = prompt
 * (july_ai_domain_prompt).
 * <p>
 * One module, three save granularities — master only (insert / update), whole
 * (saveWhole) and child only (insertDetail / updateDetail) — plus the read side
 * (tree / page / with-children) and prompt rendering.
 * </p>
 */

@Tag(name = "AI中心011 - 提示词业务域（主子表）")
@RestController
@RequestMapping("/klsjnh/aicenter/julyAiDomain/v1")
public class AiDomainController {

    /**
     * Master-sub use case.
     */
    private final JulyAiDomainUseCase useCase;

    /**
     * Create the controller.
     *
     * @param useCase master-sub use case
     */
    public AiDomainController(JulyAiDomainUseCase useCase) {
        this.useCase = useCase;
    }

    /**
     * Insert a domain (master save).
     *
     * @param vo request
     * @return new domain id
     */
    @PostMapping("/insert")
    @Operation(summary = "新增业务域（主表；domainCode 不可变）")
    public Response011<IdVo011> insert(@RequestBody JulyAiDomainInsertVo011 vo, HttpServletRequest request) {
        String funcName = "ai domain insert";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.successId(funcName, useCase.insert(operator.id(), vo.getDomainCode(), vo.getDomainName(),
                vo.getParentId(), vo.getSortOrder(), vo.getRemark()));
    }

    /**
     * Update a domain (master save).
     *
     * @param vo request
     * @return the domain id
     */
    @PostMapping("/update")
    @Operation(summary = "修改业务域（主表；domainCode 不可变）")
    public Response011<IdVo011> update(@RequestBody JulyAiDomainUpdateVo011 vo, HttpServletRequest request) {
        String funcName = "ai domain update";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.successId(funcName, useCase.update(operator.id(), vo.getId(), vo.getDomainName(), vo.getParentId(),
                vo.getSortOrder(), vo.getRemark(), vo.getStatus()));
    }

    /**
     * Logic delete a domain.
     *
     * @param idVo id
     * @return deleted id
     */
    @PostMapping("/logicDelete")
    @Operation(summary = "逻辑删除业务域（有子域或提示词拒绝）")
    public Response011<IdVo011> logicDelete(@RequestBody IdVo011 idVo, HttpServletRequest request) {
        String funcName = "ai domain logic delete";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.successId(funcName, useCase.logicDelete(operator.id(), idVo.getId()));
    }

    /**
     * Find a domain by id.
     *
     * @param id domain id
     * @return domain
     */
    @GetMapping("/getById")
    @Operation(summary = "按主键点查业务域")
    public Response011<JulyAiDomainVo011> getById(@RequestParam("id") String id, HttpServletRequest request) {
        String funcName = "ai domain get by id";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.success(funcName, toVo(useCase.getById(operator.id(), id)));
    }

    /**
     * Find a domain by code.
     *
     * @param domainCode domain code
     * @return domain
     */
    @GetMapping("/getByCode")
    @Operation(summary = "按域编码点查")
    public Response011<JulyAiDomainVo011> getByCode(@RequestParam("domainCode") String domainCode, HttpServletRequest request) {
        String funcName = "ai domain get by code";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.success(funcName, toVo(useCase.getByCode(operator.id(), domainCode)));
    }

    /**
     * Page query domains.
     *
     * @param vo request
     * @return page result
     */
    @PostMapping("/selectListByPage")
    @Operation(summary = "业务域分页（keyword/parentId/status）")
    public Response011<PageResult011<JulyAiDomainVo011>> selectListByPage(@RequestBody JulyAiDomainQueryVo011 vo, HttpServletRequest request) {
        String funcName = "ai domain select list by page";
        Operator011 operator = Operator011Resolver.resolve(request);


        PageQuery011 query = new PageQuery011(vo.getPageIndex(), vo.getPageSize());
        JulyAiDomainQuerySpec spec = new JulyAiDomainQuerySpec(vo.getKeyword(), vo.getParentId(), vo.getStatus());
        PageResult011<JulyAiDomain> page = useCase.selectListByPage(operator.id(), query, spec);

        List<JulyAiDomainVo011> rows = page.rows().stream().map(this::toVo).toList();

        return Response011.success(funcName, PageResult011.of(query, page.total(), rows));
    }

    /**
     * Load the enabled domain tree.
     *
     * @return root nodes with nested children
     */
    @GetMapping("/selectTree")
    @Operation(summary = "业务域树（启用，按排序）")
    public Response011<List<JulyAiDomainVo011>> selectTree(HttpServletRequest request) {
        String funcName = "ai domain select tree";
        Operator011 operator = Operator011Resolver.resolve(request);

        List<JulyAiDomainVo011> rows = new ArrayList<>();

        for (JulyAiDomain domain : useCase.selectTree(operator.id())) {
            rows.add(toVo(domain));
        }

        return Response011.success(funcName, rows);
    }

    /**
     * Whole save: master + children in one transaction (children replaced).
     *
     * @param vo request
     * @return the domain id
     */
    @PostMapping("/saveWhole")
    @Operation(summary = "整存业务域 + 提示词（主+子，一个事务；子表替换）")
    public Response011<IdVo011> saveWhole(@RequestBody JulyAiDomainSaveWholeVo011 vo, HttpServletRequest request) {
        String funcName = "ai domain save whole";
        Operator011 operator = Operator011Resolver.resolve(request);


        List<PromptSaveCommand> prompts = new ArrayList<>();

        for (JulyAiDomainPromptSaveVo011 row : vo.getPrompts()) {
            prompts.add(new PromptSaveCommand(row.getPromptCode(), row.getPromptName(), row.getScene(),
                    row.getContentMode(), row.getContent(), row.getStorageCode(), row.getBucket(), row.getVariables(),
                    row.getSortOrder(), row.getRemark(), row.getStatus()));
        }

        return Response011.successId(funcName, useCase.saveWhole(operator.id(), vo.getId(), vo.getDomainCode(), vo.getDomainName(),
                vo.getParentId(), vo.getSortOrder(), vo.getRemark(), vo.getStatus(), prompts));
    }

    /**
     * Read a domain together with its prompts (master + children).
     *
     * @param id domain id
     * @return domain with its ordered prompts
     */
    @GetMapping("/getWithChildren")
    @Operation(summary = "主+子联查（业务域 + 提示词列表）")
    public Response011<JulyAiDomainBundleVo011> getWithChildren(@RequestParam("id") String id, HttpServletRequest request) {
        String funcName = "ai domain get with children";
        Operator011 operator = Operator011Resolver.resolve(request);


        JulyAiDomainBundle bundle = useCase.getWithChildren(operator.id(), id);
        JulyAiDomainBundleVo011 vo = new JulyAiDomainBundleVo011();
        vo.setDomain(toVo(bundle.domain()));

        for (JulyAiDomainPrompt prompt : bundle.prompts()) {
            vo.getPrompts().add(toVo(prompt));
        }

        return Response011.success(funcName, vo);
    }

    /**
     * Insert one prompt under a domain (child save).
     *
     * @param vo request
     * @return new prompt id
     */
    @PostMapping("/insertDetail")
    @Operation(summary = "新增提示词（子表；归属业务域）")
    public Response011<IdVo011> insertDetail(@RequestBody JulyAiDomainPromptInsertVo011 vo, HttpServletRequest request) {
        String funcName = "ai domain prompt insert";
        Operator011 operator = Operator011Resolver.resolve(request);


        PromptSaveCommand command = new PromptSaveCommand(vo.getPromptCode(), vo.getPromptName(), vo.getScene(),
                vo.getContentMode(), vo.getContent(), vo.getStorageCode(), vo.getBucket(), vo.getVariables(),
                vo.getSortOrder(), vo.getRemark(), vo.getStatus());

        return Response011.successId(funcName, useCase.insertDetail(operator.id(), vo.getPkMt(), command));
    }

    /**
     * Update one prompt (child save).
     *
     * @param vo request
     * @return the prompt id
     */
    @PostMapping("/updateDetail")
    @Operation(summary = "修改提示词（子表；promptCode 不可变）")
    public Response011<IdVo011> updateDetail(@RequestBody JulyAiDomainPromptUpdateVo011 vo, HttpServletRequest request) {
        String funcName = "ai domain prompt update";
        Operator011 operator = Operator011Resolver.resolve(request);


        PromptSaveCommand command = new PromptSaveCommand(null, vo.getPromptName(), vo.getScene(),
                vo.getContentMode(), vo.getContent(), vo.getStorageCode(), vo.getBucket(), vo.getVariables(),
                vo.getSortOrder(), vo.getRemark(), vo.getStatus());

        return Response011.successId(funcName, useCase.updateDetail(operator.id(), vo.getId(), command));
    }

    /**
     * Logic delete one prompt (child save).
     *
     * @param idVo id
     * @return deleted id
     */
    @PostMapping("/logicDeleteDetail")
    @Operation(summary = "逻辑删除提示词（子表）")
    public Response011<IdVo011> logicDeleteDetail(@RequestBody IdVo011 idVo, HttpServletRequest request) {
        String funcName = "ai domain prompt logic delete";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.successId(funcName, useCase.logicDeleteDetail(operator.id(), idVo.getId()));
    }

    /**
     * Find a prompt by id.
     *
     * @param id prompt id
     * @return prompt
     */
    @GetMapping("/getDetailById")
    @Operation(summary = "按主键点查提示词")
    public Response011<JulyAiDomainPromptVo011> getDetailById(@RequestParam("id") String id, HttpServletRequest request) {
        String funcName = "ai domain prompt get by id";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.success(funcName, toVo(useCase.getDetailById(operator.id(), id)));
    }

    /**
     * Find a prompt by code.
     *
     * @param promptCode prompt code
     * @return prompt
     */
    @GetMapping("/getDetailByCode")
    @Operation(summary = "按提示词编码点查")
    public Response011<JulyAiDomainPromptVo011> getDetailByCode(@RequestParam("promptCode") String promptCode, HttpServletRequest request) {
        String funcName = "ai domain prompt get by code";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.success(funcName, toVo(useCase.getDetailByCode(operator.id(), promptCode)));
    }

    /**
     * Page query prompts.
     *
     * @param vo request
     * @return page result
     */
    @PostMapping("/selectDetailListByPage")
    @Operation(summary = "提示词分页（keyword/pkMt/scene/status）")
    public Response011<PageResult011<JulyAiDomainPromptVo011>> selectDetailListByPage(
            @RequestBody JulyAiDomainPromptQueryVo011 vo, HttpServletRequest request) {
        String funcName = "ai domain prompt select list by page";
        Operator011 operator = Operator011Resolver.resolve(request);


        PageQuery011 query = new PageQuery011(vo.getPageIndex(), vo.getPageSize());
        JulyAiDomainPromptQuerySpec spec = new JulyAiDomainPromptQuerySpec(vo.getKeyword(), vo.getPkMt(), vo.getScene(),
                vo.getStatus());
        PageResult011<JulyAiDomainPrompt> page = useCase.selectDetailListByPage(operator.id(), query, spec);

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
    public Response011<String> getContent(@RequestParam("id") String id, HttpServletRequest request) {
        String funcName = "ai domain prompt get content";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.success(funcName, useCase.getContent(operator.id(), id));
    }

    /**
     * Render a prompt.
     *
     * @param vo request
     * @return rendered text
     */
    @PostMapping("/render")
    @Operation(summary = "渲染提示词（${var} 替换；公共件给 AI 开发）")
    public Response011<String> render(@RequestBody JulyAiDomainPromptRenderVo011 vo, HttpServletRequest request) {
        String funcName = "ai domain prompt render";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.success(funcName, useCase.render(operator.id(), vo.getPromptCode(), vo.getParams()));
    }

    /**
     * Map the domain aggregate to the response VO (children mapped recursively).
     *
     * @param domain aggregate
     * @return response VO
     */
    private JulyAiDomainVo011 toVo(JulyAiDomain domain) {
        JulyAiDomainVo011 vo = new JulyAiDomainVo011();
        vo.setId(domain.id().value());
        vo.setDomainCode(domain.domainCode());
        vo.setDomainName(domain.domainName());
        vo.setParentId(domain.parentId());
        vo.setSortOrder(domain.sortOrder());
        vo.setStatus(domain.status());
        vo.setRemark(domain.remark());
        vo.setCreateBy(domain.audit().createBy());
        vo.setUpdateBy(domain.audit().updateBy());
        vo.setCreateTime(domain.audit().createTime());
        vo.setUpdateTime(domain.audit().updateTime());

        for (JulyAiDomain child : domain.children()) {
            vo.getChildren().add(toVo(child));
        }

        return vo;
    }

    /**
     * Map the prompt aggregate to the response VO.
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
