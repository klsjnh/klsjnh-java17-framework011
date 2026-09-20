package com.klsjnh.web.aicenter.controller;

/*                AiPromptController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  ai prompt controller
 *
 */

import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.response.Response011;
import com.klsjnh.common.vo.IdVo011;

import com.klsjnh.application.aicenter.prompt.JulyAiPromptUseCase;
import com.klsjnh.application.aicenter.prompt.PromptDetailCommand;
import com.klsjnh.domain.aicenter.prompt.JulyAiPrompt;
import com.klsjnh.domain.aicenter.prompt.JulyAiPromptQuerySpec;

import com.klsjnh.web.aicenter.vo.aiprompt.JulyAiPromptDetailVo011;
import com.klsjnh.web.aicenter.vo.aiprompt.JulyAiPromptInsertVo011;
import com.klsjnh.web.aicenter.vo.aiprompt.JulyAiPromptQueryVo011;
import com.klsjnh.web.aicenter.vo.aiprompt.JulyAiPromptRenderVo011;
import com.klsjnh.web.aicenter.vo.aiprompt.JulyAiPromptVo011;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * AI prompt HTTP adapter ({@code /klsjnh/aicenter/julyAiPrompt/v1}): prompt CRUD
 * (with per-domain details) and rendering a prompt for AI development.
 */

@Tag(name = "AI中心011 - 提示词")
@RestController
@RequestMapping("/klsjnh/aicenter/julyAiPrompt/v1")
public class AiPromptController {

    /**
     * Prompt use case.
     */
    private final JulyAiPromptUseCase promptUseCase;

    /**
     * Create the controller.
     *
     * @param promptUseCase prompt use case
     */
    public AiPromptController(JulyAiPromptUseCase promptUseCase) {
        this.promptUseCase = promptUseCase;
    }

    /**
     * Insert a prompt.
     *
     * @param vo request
     * @return new prompt id
     */
    @PostMapping("/insert")
    @Operation(summary = "新增提示词（含业务域明细）")
    public Response011<IdVo011> insert(@RequestBody JulyAiPromptInsertVo011 vo) {
        String funcName = "ai prompt insert";

        List<PromptDetailCommand> details = new ArrayList<>();

        if (vo.getDetails() != null) {
            for (JulyAiPromptDetailVo011 detail : vo.getDetails()) {
                details.add(new PromptDetailCommand(detail.getDomainCode(), detail.getContentMode(),
                        detail.getContent(), detail.getStorageCode(), detail.getBucket(), detail.getVariables(),
                        detail.getSortOrder()));
            }
        }

        String id = promptUseCase.insert(vo.getPromptCode(), vo.getPromptName(), vo.getScene(), vo.getSortOrder(),
                vo.getRemark(), details);

        return Response011.successId(funcName, id);
    }

    /**
     * Page query prompts.
     *
     * @param vo request
     * @return page result
     */
    @PostMapping("/selectListByPage")
    @Operation(summary = "提示词分页（keyword/scene/status）")
    public Response011<PageResult011<JulyAiPromptVo011>> selectListByPage(@RequestBody JulyAiPromptQueryVo011 vo) {
        String funcName = "ai prompt select list by page";

        PageQuery011 query = new PageQuery011(vo.getPageIndex(), vo.getPageSize());
        JulyAiPromptQuerySpec spec = new JulyAiPromptQuerySpec(vo.getKeyword(), vo.getScene(), vo.getStatus());
        PageResult011<JulyAiPrompt> page = promptUseCase.selectListByPage(query, spec);

        List<JulyAiPromptVo011> rows = page.rows().stream().map(this::toVo).toList();

        return Response011.success(funcName, PageResult011.of(query, page.total(), rows));
    }

    /**
     * Find a prompt by code.
     *
     * @param promptCode prompt code
     * @return prompt
     */
    @GetMapping("/getByCode")
    @Operation(summary = "按提示词编码点查")
    public Response011<JulyAiPromptVo011> getByCode(@RequestParam("promptCode") String promptCode) {
        String funcName = "ai prompt get by code";

        return Response011.success(funcName, toVo(promptUseCase.getByCode(promptCode)));
    }

    /**
     * Logic delete a prompt.
     *
     * @param idVo id
     * @return deleted id
     */
    @PostMapping("/logicDelete")
    @Operation(summary = "逻辑删除提示词（含业务域明细）")
    public Response011<IdVo011> logicDelete(@RequestBody IdVo011 idVo) {
        String funcName = "ai prompt logic delete";

        return Response011.successId(funcName, promptUseCase.logicDelete(idVo.getId()));
    }

    /**
     * Render a prompt.
     *
     * @param vo request
     * @return rendered text
     */
    @PostMapping("/render")
    @Operation(summary = "渲染提示词（${var} 替换；公共件给 AI 开发）")
    public Response011<String> render(@RequestBody JulyAiPromptRenderVo011 vo) {
        String funcName = "ai prompt render";

        return Response011.success(funcName, promptUseCase.render(vo.getPromptCode(), vo.getDomainCode(),
                vo.getParams()));
    }

    /**
     * Map the aggregate to the response VO.
     *
     * @param prompt aggregate
     * @return response VO
     */
    private JulyAiPromptVo011 toVo(JulyAiPrompt prompt) {
        JulyAiPromptVo011 vo = new JulyAiPromptVo011();
        vo.setId(prompt.id().value());
        vo.setPromptCode(prompt.promptCode());
        vo.setPromptName(prompt.promptName());
        vo.setScene(prompt.scene());
        vo.setStatus(prompt.status());

        return vo;
    }
}
