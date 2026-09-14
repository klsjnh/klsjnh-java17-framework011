package com.klsjnh.web.system011.controller;

/*                JulyOrganizationController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july organization controller class
 *
 */

import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.response.Response011;
import com.klsjnh.common.vo.BatchDeleteResultVo011;
import com.klsjnh.common.vo.IdVo011;

import com.klsjnh.application.organization.JulyOrganizationUseCase;
import com.klsjnh.domain.system011.organization.JulyOrganization;

import com.klsjnh.web.system011.converter.JulyOrganizationConverter;

import com.klsjnh.web.system011.vo.julyorganization.JulyOrganizationInsertVo011;
import com.klsjnh.web.system011.vo.julyorganization.JulyOrganizationQueryVo011;
import com.klsjnh.web.system011.vo.julyorganization.JulyOrganizationUpdateVo011;
import com.klsjnh.web.system011.vo.julyorganization.JulyOrganizationVo011;

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
 * JulyOrganization HTTP adapter: org tree CRUD with member-count badges and
 * move / delete constraints.
 */

@Tag(name = "组织管理")
@RestController
@RequestMapping("/klsjnh/system011/julyOrganization/v1")
public class JulyOrganizationController {

    /**
     * JulyOrganization use case.
     */
    private final JulyOrganizationUseCase useCase;

    /**
     * Response converter.
     */
    private final JulyOrganizationConverter converter;

    /**
     * Create the controller.
     *
     * @param useCase   july organization use case
     * @param converter response converter
     */
    public JulyOrganizationController(JulyOrganizationUseCase useCase, JulyOrganizationConverter converter) {
        this.useCase = useCase;
        this.converter = converter;
    }

    /**
     * Insert a new organization node.
     *
     * @param vo insert request
     * @return envelope with the new org id
     */
    @PostMapping("/insert")
    @Operation(summary = "新增组织（层级由上级推导）")
    public Response011<IdVo011> insert(@RequestBody JulyOrganizationInsertVo011 vo) {
        String funcName = "insert";

        return Response011.successId(funcName,
                useCase.insert(vo.getOrgCode(), vo.getOrgName(), vo.getPkUser(), vo.getParentId(), vo.getSortOrder()));
    }

    /**
     * Update an organization node (parent move allowed, subtree re-leveled).
     *
     * @param vo update request
     * @return envelope with the org id
     */
    @PostMapping("/update")
    @Operation(summary = "修改组织（编码不可改，可移动上级并重排层级）")
    public Response011<IdVo011> update(@RequestBody JulyOrganizationUpdateVo011 vo) {
        String funcName = "update";

        useCase.update(vo.getId(), vo.getOrgName(), vo.getPkUser(), vo.getParentId(), vo.getSortOrder());

        return Response011.successId(funcName, vo.getId());
    }

    /**
     * Logic delete an organization (children / mounted users reject).
     *
     * @param idVo request with the org id
     * @return envelope with the org id
     */
    @PostMapping("/logicDelete")
    @Operation(summary = "逻辑删除（有子组织或挂有用户拒绝）")
    public Response011<IdVo011> logicDelete(@RequestBody IdVo011 idVo) {
        String funcName = "logic delete";

        useCase.logicDelete(idVo.getId());

        return Response011.successId(funcName, idVo.getId());
    }

    /**
     * Find an organization by primary key (safe + idempotent, hence GET).
     *
     * @param id org id, passed as a query parameter
     * @return org detail
     */
    @GetMapping("/getById")
    @Operation(summary = "主键查询（id 走 query）")
    public Response011<JulyOrganizationVo011> getById(@RequestParam("id") String id) {
        String funcName = "get by id";

        return Response011.success(funcName, converter.toVo(useCase.getById(id)));
    }

    /**
     * Load the full alive organization tree with member-count badges
     * (read-only, hence GET).
     *
     * @return root nodes with nested children
     */
    @GetMapping("/selectTree")
    @Operation(summary = "组织树（含人数角标，GET）")
    public Response011<List<JulyOrganizationVo011>> selectTree() {
        String funcName = "select tree";

        JulyOrganizationUseCase.TreeWithCounts tree = useCase.selectTree();

        return Response011.success(funcName, converter.toVoList(tree.tree(), tree.counts()));
    }

    /**
     * Page query with an optional keyword filter.
     *
     * @param query page query request
     * @return page result of organizations
     */
    @PostMapping("/selectListByPage")
    @Operation(summary = "分页查询（编码/名称模糊过滤）")
    public Response011<PageResult011<JulyOrganizationVo011>> selectListByPage(
            @RequestBody JulyOrganizationQueryVo011 query) {
        String funcName = "select list by page";

        PageQuery011 pageQuery = new PageQuery011(query.getPageIndex(), query.getPageSize());
        PageResult011<JulyOrganization> page = useCase.selectListByPage(pageQuery, query.getKeyword());

        return Response011.success(funcName, page.withRows(converter.toVoList(page.rows(), null)));
    }
}
