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
 *      2026.09.15  tree endpoints renamed to get
 *
 */

import com.klsjnh.common.constant.AuditObjectCodes011;
import com.klsjnh.common.enums.AuditType011;
import com.klsjnh.common.constant.FrameConst011;
import com.klsjnh.common.exception.BusinessException;
import com.klsjnh.common.identity.Operator011;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.response.Response011;
import com.klsjnh.common.vo.BatchDeleteResultVo011;
import com.klsjnh.common.vo.IdVo011;
import com.klsjnh.common.vo.IdsVo011;

import com.klsjnh.application.system011.menu.JulyMenuUseCase;
import com.klsjnh.domain.system011.menu.JulyMenu;
import com.klsjnh.domain.platform011.export.ExportResult;
import com.klsjnh.application.platform011.backup.BackupUseCase;
import com.klsjnh.application.platform011.export.ExportUseCase;

import com.klsjnh.web.system011.converter.JulyMenuConverter;

import com.klsjnh.web.global.audit.AuditLog;
import com.klsjnh.web.system011.vo.julymenu.JulyMenuInsertVo011;
import com.klsjnh.web.system011.vo.julymenu.JulyMenuQueryVo011;
import com.klsjnh.web.system011.vo.julymenu.JulyMenuUpdateVo011;
import com.klsjnh.web.system011.vo.julymenu.JulyMenuVo011;
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

import java.util.List;

/**
 * JulyMenu HTTP adapter: menu CRUD / tree plus the login-linked
 * getUserMenuTree.
 */

@Tag(name = "系统管理 - 菜单管理")
@RestController
@RequestMapping("/klsjnh/system011/julyMenu/v1")
public class JulyMenuController {


    /**
     * JulyMenu use case.
     */
    private final JulyMenuUseCase julyMenuUseCase;

    /**
     * Response julyMenuConverter.
     */
    private final JulyMenuConverter julyMenuConverter;

    /**
     * Export use case (platform capability).
     */
    private final ExportUseCase exportUseCase;

    /**
     * Backup use case (platform capability).
     */
    private final BackupUseCase backupUseCase;

    /**
     * Create the controller.
     *
     * @param julyMenuUseCase       july menu use case
     * @param julyMenuConverter     response julyMenuConverter
     * @param exportUseCase export use case
     * @param backupUseCase backup use case
     */
    public JulyMenuController(JulyMenuUseCase julyMenuUseCase, JulyMenuConverter julyMenuConverter,
            ExportUseCase exportUseCase, BackupUseCase backupUseCase) {
        this.julyMenuUseCase = julyMenuUseCase;
        this.julyMenuConverter = julyMenuConverter;
        this.exportUseCase = exportUseCase;
        this.backupUseCase = backupUseCase;
    }

    /**
     * Insert a new menu node.
     *
     * @param vo insert request
     * @return envelope with the new menu id
     */
    @AuditLog(type = AuditType011.INSERT, objectCode = AuditObjectCodes011.JULY_MENU)
    @PostMapping("/insert")
    @Operation(summary = "新增菜单")
    public Response011<IdVo011> insert(@RequestBody JulyMenuInsertVo011 vo) {
        String funcName = "insert";

        return Response011.successId(funcName, julyMenuUseCase.insert(vo.getMenuCode(), vo.getMenuName(), vo.getMenuType(),
                vo.getMenuIcon(), vo.getMenuRoute(), vo.getPermissionCode(), vo.getComponent(), vo.getParentId(),
                vo.getSortOrder()));
    }

    /**
     * Update a menu node.
     *
     * @param vo update request
     * @return envelope with the menu id
     */
    @AuditLog(type = AuditType011.UPDATE, objectCode = AuditObjectCodes011.JULY_MENU)
    @PostMapping("/update")
    @Operation(summary = "修改菜单（编码不可改，可移动上级）")
    public Response011<IdVo011> update(@RequestBody JulyMenuUpdateVo011 vo) {
        String funcName = "update";

        julyMenuUseCase.update(vo.getId(), vo.getMenuName(), vo.getMenuType(), vo.getMenuIcon(), vo.getMenuRoute(),
                vo.getPermissionCode(), vo.getComponent(), vo.getParentId(), vo.getSortOrder());

        return Response011.successId(funcName, vo.getId());
    }

    /**
     * Logic delete a single menu; menus with alive children are rejected.
     *
     * @param idVo request with the menu id
     * @return envelope with the deleted menu id
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = AuditObjectCodes011.JULY_MENU)
    @PostMapping("/logicDelete")
    @Operation(summary = "逻辑删除（单个，有子菜单拒绝）")
    public Response011<IdVo011> logicDelete(@RequestBody IdVo011 idVo) {
        String funcName = "logic delete";

        return Response011.successId(funcName, julyMenuUseCase.logicDelete(idVo.getId()));
    }

    /**
     * Logic delete menus in batch; per-id failure is reported, not thrown.
     *
     * @param idsVo request with the menu id list
     * @return per-id success/failure summary
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = AuditObjectCodes011.JULY_MENU)
    @PostMapping("/logicDeleteBatch")
    @Operation(summary = "逻辑删除（批量，有子菜单拒绝的逐条回报）")
    public Response011<BatchDeleteResultVo011> logicDeleteBatch(@RequestBody IdsVo011 idsVo) {
        String funcName = "logic delete batch";

        return Response011.success(funcName, julyMenuUseCase.logicDeleteBatch(idsVo.getIds()));
    }

    /**
     * Find a menu by primary key (safe + idempotent, hence GET).
     *
     * @param id menu id, passed as a query parameter
     * @return menu detail
     */
    @GetMapping("/getById")
    @Operation(summary = "主键查询（id 走 query）")
    public Response011<JulyMenuVo011> getById(@RequestParam("id") String id) {
        String funcName = "get by id";

        return Response011.success(funcName, julyMenuConverter.toVo(julyMenuUseCase.getById(id)));
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
        PageResult011<JulyMenu> page = julyMenuUseCase.selectListByPage(pageQuery, query.getKeyword());

        return Response011.success(funcName, page.withRows(julyMenuConverter.toVoList(page.rows())));
    }

    /**
     * Load the full alive menu tree (read-only, hence GET).
     *
     * @return root nodes with nested children
     */
    @GetMapping("/getTree")
    @Operation(summary = "全量菜单树（GET）")
    public Response011<List<JulyMenuVo011>> getTree() {
        String funcName = "get tree";

        return Response011.success(funcName, julyMenuConverter.toVoList(julyMenuUseCase.getTree()));
    }

    /**
     * Navigation menu tree of the current operator (login linked, read-only
     * hence GET; the operator comes from the auth filter, not the body).
     *
     * @param request http request (operator id from the auth filter)
     * @return root nodes with nested children
     */
    @GetMapping("/getUserMenuTree")
    @Operation(summary = "当前登录人的菜单树（内置角色全量旁路，GET）")
    public Response011<List<JulyMenuVo011>> getUserMenuTree(HttpServletRequest request) {
        String funcName = "get user menu tree";

        String operatorId = (String) request.getAttribute(FrameConst011.OPERATOR_ID);

        if (operatorId == null || operatorId.isBlank()) {
            throw BusinessException.unauthorized(funcName + ": not authenticated");
        }

        return Response011.success(funcName, julyMenuConverter.toVoList(julyMenuUseCase.getUserMenuTree(operatorId)));
    }

    /**
     * Export every menu row in batches and return the whole result in the
     * JSON envelope (batching bounds the database load, not the payload).
     *
     * @param request http request (operator from the auth filter)
     * @return envelope with the export result
     */
    @PostMapping("/export")
    @Operation(summary = "导出全部菜单（分批取数，写 EXPORT 审计）")
    public Response011<ExportResult> export(HttpServletRequest request) {
        String funcName = "export";

        Operator011 operator = Operator011Resolver.resolve(request);

        ExportResult result = exportUseCase.export(AuditObjectCodes011.JULY_MENU, operator);

        return Response011.success(funcName, result);
    }

    /**
     * Back every menu row up into the storage center, keyed by timestamp.
     *
     * @param request http request (operator from the auth filter)
     * @return envelope with the stored object key
     */
    @PostMapping("/backup011")
    @Operation(summary = "备份全部菜单到存储中心（写 BACKUP 审计）")
    public Response011<String> backup011(HttpServletRequest request) {
        String funcName = "backup";

        Operator011 operator = Operator011Resolver.resolve(request);

        String key = backupUseCase.backup(AuditObjectCodes011.JULY_MENU, operator);

        return Response011.success(funcName, key);
    }
}
