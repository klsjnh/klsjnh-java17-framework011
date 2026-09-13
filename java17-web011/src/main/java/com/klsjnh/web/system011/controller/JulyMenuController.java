package com.klsjnh.web.system011.controller;

/*                JulyMenuController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july menu controller class
 *
 */

import com.klsjnh.common.constant.FrameConst011;
import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.response.Response011;
import com.klsjnh.common.vo.BatchDeleteResultVo011;
import com.klsjnh.common.vo.IdVo011;

import com.klsjnh.application.menu.JulyMenuUseCase;
import com.klsjnh.domain.menu.JulyMenu;
import com.klsjnh.web.system011.converter.JulyMenuConverter;
import com.klsjnh.web.system011.vo.julymenu.JulyMenuInsertVo011;
import com.klsjnh.web.system011.vo.julymenu.JulyMenuQueryVo011;
import com.klsjnh.web.system011.vo.julymenu.JulyMenuUpdateVo011;
import com.klsjnh.web.system011.vo.julymenu.JulyMenuVo011;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * JulyMenu HTTP adapter: menu CRUD / tree plus the login-linked
 * selectUserMenuTree.
 */

@Tag(name = "菜单管理")
@RestController
@RequestMapping("/klsjnh/system011/julyMenu/v1")
public class JulyMenuController {

    /**
     * JulyMenu use case.
     */
    private final JulyMenuUseCase useCase;

    /**
     * Response converter.
     */
    private final JulyMenuConverter converter;

    /**
     * Create the controller.
     *
     * @param useCase   july menu use case
     * @param converter response converter
     */
    public JulyMenuController(JulyMenuUseCase useCase, JulyMenuConverter converter) {
        this.useCase = useCase;
        this.converter = converter;
    }

    /**
     * Insert a new menu node.
     *
     * @param vo insert request
     * @return envelope with the new menu id
     */
    @PostMapping("/insert")
    @Operation(summary = "新增菜单")
    public Response011<IdVo011> insert(@RequestBody JulyMenuInsertVo011 vo) {
        String funcName = "insert";

        return Response011.successId(funcName, useCase.insert(vo.getMenuCode(), vo.getMenuName(), vo.getMenuType(),
                vo.getMenuIcon(), vo.getMenuRoute(), vo.getPermissionCode(), vo.getComponent(), vo.getParentId(),
                vo.getSortOrder()));
    }

    /**
     * Update a menu node.
     *
     * @param vo update request
     * @return envelope with the menu id
     */
    @PostMapping("/update")
    @Operation(summary = "修改菜单（编码不可改，可移动上级）")
    public Response011<IdVo011> update(@RequestBody JulyMenuUpdateVo011 vo) {
        String funcName = "update";

        useCase.update(vo.getId(), vo.getMenuName(), vo.getMenuType(), vo.getMenuIcon(), vo.getMenuRoute(),
                vo.getPermissionCode(), vo.getComponent(), vo.getParentId(), vo.getSortOrder());

        return Response011.successId(funcName, vo.getId());
    }

    /**
     * Logic delete menus (batch; children reject).
     *
     * @param ids menu id list
     * @return per-id success/failure summary
     */
    @PostMapping("/logicDelete")
    @Operation(summary = "逻辑删除（批量，有子菜单拒绝）")
    public Response011<BatchDeleteResultVo011> logicDelete(@RequestBody List<String> ids) {
        String funcName = "logic delete";

        return Response011.success(funcName, useCase.logicDelete(ids));
    }

    /**
     * Find a menu by primary key.
     *
     * @param idVo request with the menu id
     * @return menu detail
     */
    @PostMapping("/getById")
    @Operation(summary = "主键查询")
    public Response011<JulyMenuVo011> getById(@RequestBody IdVo011 idVo) {
        String funcName = "get by id";

        return Response011.success(funcName, converter.toVo(useCase.getById(idVo.getId())));
    }

    /**
     * Page query with an optional keyword filter.
     *
     * @param query page query request
     * @return page result of menus
     */
    @PostMapping("/selectListByPage")
    @Operation(summary = "分页查询（编码/名称模糊过滤）")
    public Response011<PageResult011<JulyMenuVo011>> selectListByPage(@RequestBody JulyMenuQueryVo011 query) {
        String funcName = "select list by page";

        PageQuery011 pageQuery = new PageQuery011(query.getPageIndex(), query.getPageSize());
        PageResult011<JulyMenu> page = useCase.selectListByPage(pageQuery, query.getKeyword());

        return Response011.success(funcName, page.withRows(converter.toVoList(page.rows())));
    }

    /**
     * Load the full alive menu tree.
     *
     * @return root nodes with nested children
     */
    @PostMapping("/selectTree")
    @Operation(summary = "全量菜单树")
    public Response011<List<JulyMenuVo011>> selectTree() {
        String funcName = "select tree";

        return Response011.success(funcName, converter.toVoList(useCase.selectTree()));
    }

    /**
     * Navigation menu tree of the current operator (login linked).
     *
     * @param request http request (operator id from the auth filter)
     * @return root nodes with nested children
     */
    @PostMapping("/selectUserMenuTree")
    @Operation(summary = "当前登录人的菜单树（内置角色全量旁路）")
    public Response011<List<JulyMenuVo011>> selectUserMenuTree(HttpServletRequest request) {
        String funcName = "select user menu tree";

        String operatorId = (String) request.getAttribute(FrameConst011.OPERATOR_ID);

        if (operatorId == null || operatorId.isBlank()) {
            throw BusinessException.unauthorized(funcName + ": not authenticated");
        }

        return Response011.success(funcName, converter.toVoList(useCase.selectUserMenuTree(operatorId)));
    }
}
