package com.klsjnh.application.aicenter.prompt;

/*                JulyAiPromptUseCase class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  ai prompt use case (crud + render)
 *
 */

import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;

import com.klsjnh.domain.aicenter.prompt.JulyAiPrompt;
import com.klsjnh.domain.aicenter.prompt.JulyAiPromptDetail;
import com.klsjnh.domain.aicenter.prompt.JulyAiPromptDetailRepository;
import com.klsjnh.domain.aicenter.prompt.JulyAiPromptQuerySpec;
import com.klsjnh.domain.aicenter.prompt.JulyAiPromptRepository;
import com.klsjnh.domain.aicenter.prompt.PromptRenderPort;
import com.klsjnh.domain.shared.AuditInfo;
import com.klsjnh.domain.shared.EntityId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * AI prompt use case: prompt CRUD (with the per-domain content detail) and
 * rendering a prompt for AI development.
 */

@Service
public class JulyAiPromptUseCase {

    /**
     * Prompt repository.
     */
    private final JulyAiPromptRepository promptRepository;

    /**
     * Detail repository.
     */
    private final JulyAiPromptDetailRepository detailRepository;

    /**
     * Prompt render port.
     */
    private final PromptRenderPort promptRender;

    /**
     * Create the use case.
     *
     * @param promptRepository prompt repository
     * @param detailRepository detail repository
     * @param promptRender     prompt render port
     */
    public JulyAiPromptUseCase(JulyAiPromptRepository promptRepository, JulyAiPromptDetailRepository detailRepository,
            PromptRenderPort promptRender) {
        this.promptRepository = promptRepository;
        this.detailRepository = detailRepository;
        this.promptRender = promptRender;
    }

    /**
     * Insert a prompt with its domain details.
     *
     * @param promptCode prompt code
     * @param promptName prompt name
     * @param scene      scene
     * @param sortOrder  sort order
     * @param remark     remark
     * @param details    domain details
     * @return new prompt id
     */
    @Transactional
    public String insert(String promptCode, String promptName, String scene, Integer sortOrder, String remark,
            List<PromptDetailCommand> details) {
        String id = EntityId.generate().value();
        JulyAiPrompt prompt = JulyAiPrompt.create(EntityId.of(id), promptCode, promptName, scene, sortOrder, remark,
                AuditInfo.empty());
        promptRepository.insert(prompt);
        insertDetails(id, details);

        return id;
    }

    /**
     * Find a prompt by code.
     *
     * @param promptCode prompt code
     * @return prompt
     */
    public JulyAiPrompt getByCode(String promptCode) {
        JulyAiPrompt prompt = promptRepository.findByCode(promptCode);

        if (prompt == null) {
            throw BusinessException.recordNotFound("prompt: " + promptCode);
        }

        return prompt;
    }

    /**
     * The domain details of a prompt.
     *
     * @param pkMt prompt id
     * @return detail rows
     */
    public List<JulyAiPromptDetail> details(String pkMt) {
        return detailRepository.findByMaster(pkMt);
    }

    /**
     * Page query.
     *
     * @param query page query
     * @param spec  filter
     * @return page result
     */
    public PageResult011<JulyAiPrompt> selectListByPage(PageQuery011 query, JulyAiPromptQuerySpec spec) {
        long total = promptRepository.count(spec);
        List<JulyAiPrompt> rows = promptRepository.findPage(query.offset(), query.pageSize(), spec);

        return PageResult011.of(query, total, rows);
    }

    /**
     * Logic delete a prompt and its details.
     *
     * @param id prompt id
     * @return the deleted id
     */
    @Transactional
    public String logicDelete(String id) {
        JulyAiPrompt prompt = promptRepository.findById(id);

        if (prompt == null) {
            throw BusinessException.recordNotFound("prompt: " + id);
        }

        detailRepository.logicDeleteByMaster(id);
        promptRepository.logicDeleteById(id);

        return id;
    }

    /**
     * Render a prompt by code.
     *
     * @param promptCode prompt code
     * @param domainCode business domain, blank for the default
     * @param params     variable values, nullable
     * @return rendered text
     */
    public String render(String promptCode, String domainCode, Map<String, String> params) {
        return promptRender.render(promptCode, domainCode, params);
    }

    /**
     * Insert the detail rows.
     *
     * @param pkMt    prompt id
     * @param details detail commands
     */
    private void insertDetails(String pkMt, List<PromptDetailCommand> details) {
        if (details == null || details.isEmpty()) {
            return;
        }

        int order = 1;

        for (PromptDetailCommand detail : details) {
            JulyAiPromptDetail row = JulyAiPromptDetail.create(EntityId.generate(), pkMt, detail.domainCode(),
                    detail.contentMode(), detail.content(), detail.variables(),
                    detail.sortOrder() == null ? order : detail.sortOrder(), null, AuditInfo.empty());
            detailRepository.insert(row);
            order++;
        }
    }
}
