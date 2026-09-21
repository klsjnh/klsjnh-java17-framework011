package com.klsjnh.web.aicenter.controller;

/*                AiDomainController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.21
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.21  ai domain controller
 *
 */

import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.response.Response011;
import com.klsjnh.common.vo.IdVo011;

import com.klsjnh.application.aicenter.prompt.JulyAiDomainUseCase;
import com.klsjnh.domain.aicenter.prompt.JulyAiDomain;
import com.klsjnh.domain.aicenter.prompt.JulyAiDomainQuerySpec;

import com.klsjnh.web.aicenter.vo.aidomain.JulyAiDomainInsertVo011;
import com.klsjnh.web.aicenter.vo.aidomain.JulyAiDomainQueryVo011;
import com.klsjnh.web.aicenter.vo.aidomain.JulyAiDomainUpdateVo011;
import com.klsjnh.web.aicenter.vo.aidomain.JulyAiDomainVo011;

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
 * AI prompt business domain HTTP adapter
 * ({@code /klsjnh/aicenter/julyAiDomain/v1}): domain tree CRUD, page query and
 * tree assembly.
 */

@Tag(name = "AI中心011 - 提示词业务域")
@RestController
@RequestMapping("/klsjnh/aicenter/julyAiDomain/v1")
public class AiDomainController {

    /**
     * Domain use case.
     */
    private final JulyAiDomainUseCase domainUseCase;

    /**
     * Create the controller.
     *
     * @param domainUseCase domain use case
     */
    public AiDomainController(JulyAiDomainUseCase domainUseCase) {
        this.domainUseCase = domainUseCase;
    }

    /**
     * Insert a domain.
     *
     * @param vo request
     * @return new domain id
     */
    @PostMapping("/insert")
    @Operation(summary = "新增业务域（domainCode 不可变）")
    public Response011<IdVo011> insert(@RequestBody JulyAiDomainInsertVo011 vo) {
        String funcName = "ai domain insert";

        return Response011.successId(funcName, domainUseCase.insert(vo.getDomainCode(), vo.getDomainName(),
                vo.getParentId(), vo.getSortOrder(), vo.getRemark()));
    }

    /**
     * Update a domain.
     *
     * @param vo request
     * @return the domain id
     */
    @PostMapping("/update")
    @Operation(summary = "修改业务域（domainCode 不可变）")
    public Response011<IdVo011> update(@RequestBody JulyAiDomainUpdateVo011 vo) {
        String funcName = "ai domain update";

        return Response011.successId(funcName, domainUseCase.update(vo.getId(), vo.getDomainName(), vo.getParentId(),
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
    public Response011<IdVo011> logicDelete(@RequestBody IdVo011 idVo) {
        String funcName = "ai domain logic delete";

        return Response011.successId(funcName, domainUseCase.logicDelete(idVo.getId()));
    }

    /**
     * Find a domain by id.
     *
     * @param id domain id
     * @return domain
     */
    @GetMapping("/getById")
    @Operation(summary = "按主键点查业务域")
    public Response011<JulyAiDomainVo011> getById(@RequestParam("id") String id) {
        String funcName = "ai domain get by id";

        return Response011.success(funcName, toVo(domainUseCase.getById(id)));
    }

    /**
     * Find a domain by code.
     *
     * @param domainCode domain code
     * @return domain
     */
    @GetMapping("/getByCode")
    @Operation(summary = "按域编码点查")
    public Response011<JulyAiDomainVo011> getByCode(@RequestParam("domainCode") String domainCode) {
        String funcName = "ai domain get by code";

        return Response011.success(funcName, toVo(domainUseCase.getByCode(domainCode)));
    }

    /**
     * Page query domains.
     *
     * @param vo request
     * @return page result
     */
    @PostMapping("/selectListByPage")
    @Operation(summary = "业务域分页（keyword/parentId/status）")
    public Response011<PageResult011<JulyAiDomainVo011>> selectListByPage(@RequestBody JulyAiDomainQueryVo011 vo) {
        String funcName = "ai domain select list by page";

        PageQuery011 query = new PageQuery011(vo.getPageIndex(), vo.getPageSize());
        JulyAiDomainQuerySpec spec = new JulyAiDomainQuerySpec(vo.getKeyword(), vo.getParentId(), vo.getStatus());
        PageResult011<JulyAiDomain> page = domainUseCase.selectListByPage(query, spec);

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
    public Response011<List<JulyAiDomainVo011>> selectTree() {
        String funcName = "ai domain select tree";

        List<JulyAiDomainVo011> rows = new ArrayList<>();

        for (JulyAiDomain domain : domainUseCase.selectTree()) {
            rows.add(toVo(domain));
        }

        return Response011.success(funcName, rows);
    }

    /**
     * Map the aggregate to the response VO (children mapped recursively).
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
}
