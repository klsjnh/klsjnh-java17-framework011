package com.klsjnh.web.iam.controller;

/*                JulyRoleController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july role controller class
 *      2026.09.15  update forwards status
 *      2026.09.15  get endpoints func name aligned
 *      2026.09.26  pass operator into use case for permission checks
 *
 */

import com.klsjnh.common.constant.AuditObjectCodes011;
import com.klsjnh.common.enums.AuditType011;
import com.klsjnh.common.identity.Operator011;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.response.Response011;
import com.klsjnh.common.vo.IdVo011;

import com.klsjnh.domain.iam.role.JulyRole;
import com.klsjnh.domain.platform011.export.ExportResult;
import com.klsjnh.application.iam.role.JulyRoleUseCase;

import com.klsjnh.web.iam.converter.JulyMenuConverter;
import com.klsjnh.web.iam.converter.JulyRoleConverter;
import com.klsjnh.web.iam.converter.JulyUserConverter;

import com.klsjnh.web.global.audit.AuditLog;
import com.klsjnh.web.iam.vo.julyrole.JulyRoleAssignMenusVo011;
import com.klsjnh.web.iam.vo.julyrole.JulyRoleAssignObjectActionsVo011;
import com.klsjnh.web.iam.vo.julyrole.JulyRoleInsertVo011;
import com.klsjnh.web.iam.vo.julyrole.JulyRoleQueryVo011;
import com.klsjnh.web.iam.vo.julyrole.JulyRoleUpdateVo011;
import com.klsjnh.web.iam.vo.julyrole.JulyRoleVo011;
import com.klsjnh.web.iam.vo.julymenu.JulyMenuVo011;
import com.klsjnh.web.iam.vo.julyuser.JulyUserVo011;
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
import jakarta.validation.Valid;

import java.util.List;

/**
 * JulyRole HTTP adapter: role management endpoints (built-in roles protected).
 * Permission checks live in the use case (operator resolved here).
 */

@Tag(name = "IAM - 角色管理")
@RestController
@RequestMapping("/klsjnh/iam/julyRole/v1")
public class JulyRoleController {


    /**
     * JulyRole use case.
     */
    private final JulyRoleUseCase julyRoleUseCase;

    /**
     * Response julyRoleConverter.
     */
    private final JulyRoleConverter julyRoleConverter;

    /**
     * Menu response julyRoleConverter.
     */
    private final JulyMenuConverter menuConverter;

    /**
     * User response julyRoleConverter.
     */
    private final JulyUserConverter userConverter;

    /**
     * Create the controller.
     *
     * @param julyRoleUseCase   july role use case
     * @param julyRoleConverter response julyRoleConverter
     * @param menuConverter     menu response julyRoleConverter
     * @param userConverter     user response julyRoleConverter
     */
    public JulyRoleController(JulyRoleUseCase julyRoleUseCase, JulyRoleConverter julyRoleConverter,
            JulyMenuConverter menuConverter, JulyUserConverter userConverter) {
        this.julyRoleUseCase = julyRoleUseCase;
        this.julyRoleConverter = julyRoleConverter;
        this.menuConverter = menuConverter;
        this.userConverter = userConverter;
    }

    /**
     * Insert a new custom role.
     *
     * @param vo      insert request
     * @param request http request
     * @return envelope with the new role id
     */
    @AuditLog(type = AuditType011.INSERT, objectCode = AuditObjectCodes011.JULY_ROLE)
    @PostMapping("/insert")
    @Operation(summary = "新增角色")
    public Response011<IdVo011> insert(@Valid @RequestBody JulyRoleInsertVo011 vo, HttpServletRequest request) {
        String funcName = "insert";
        Operator011 operator = Operator011Resolver.resolve(request);

        return Response011.successId(funcName,
                julyRoleUseCase.insert(operator.id(), vo.getRoleCode(), vo.getRoleName(), vo.getRemark()));
    }

    /**
     * Update a role.
     *
     * @param vo      update request
     * @param request http request
     * @return envelope with the role id
     */
    @AuditLog(type = AuditType011.UPDATE, objectCode = AuditObjectCodes011.JULY_ROLE)
    @PostMapping("/update")
    @Operation(summary = "修改角色（编码不可改，状态可改）")
    public Response011<IdVo011> update(@Valid @RequestBody JulyRoleUpdateVo011 vo, HttpServletRequest request) {
        String funcName = "update";
        Operator011 operator = Operator011Resolver.resolve(request);

        julyRoleUseCase.update(operator.id(), vo.getId(), vo.getRoleName(), vo.getRemark(), vo.getStatus());

        return Response011.successId(funcName, vo.getId());
    }

    /**
     * Logic delete a role (built-in roles protected).
     *
     * @param idVo    request with the role id
     * @param request http request
     * @return envelope with the role id
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = AuditObjectCodes011.JULY_ROLE)
    @PostMapping("/logicDelete")
    @Operation(summary = "逻辑删除（内置角色拒绝）")
    public Response011<IdVo011> logicDelete(@Valid @RequestBody IdVo011 idVo, HttpServletRequest request) {
        String funcName = "logic delete";
        Operator011 operator = Operator011Resolver.resolve(request);

        julyRoleUseCase.logicDelete(operator.id(), idVo.getId());

        return Response011.successId(funcName, idVo.getId());
    }

    /**
     * Assign menus to a role (toggle semantics, replace strategy).
     *
     * @param vo      assign request
     * @param request http request
     * @return envelope with the role id
     */
    @AuditLog(type = AuditType011.UPDATE, objectCode = AuditObjectCodes011.JULY_ROLE)
    @PostMapping("/assignMenus")
    @Operation(summary = "角色授权菜单（整存替换）")
    public Response011<IdVo011> assignMenus(@Valid @RequestBody JulyRoleAssignMenusVo011 vo, HttpServletRequest request) {
        String funcName = "assign menus";
        Operator011 operator = Operator011Resolver.resolve(request);

        julyRoleUseCase.assignMenus(operator.id(), vo.getId(), vo.getPkMenus());

        return Response011.successId(funcName, vo.getId());
    }

    /**
     * Assign catalog object actions to a role (replace within that object).
     *
     * @param vo      assign request
     * @param request http request
     * @return envelope with the role id
     */
    @AuditLog(type = AuditType011.UPDATE, objectCode = AuditObjectCodes011.JULY_ROLE)
    @PostMapping("/assignObjectActions")
    @Operation(summary = "角色按对象授权动作（对象内整存替换，直授码）")
    public Response011<IdVo011> assignObjectActions(@Valid @RequestBody JulyRoleAssignObjectActionsVo011 vo,
            HttpServletRequest request) {
        String funcName = "assign object actions";
        Operator011 operator = Operator011Resolver.resolve(request);

        julyRoleUseCase.assignObjectActions(operator.id(), vo.getId(), vo.getObjectCode(), vo.getActionCodes());

        return Response011.successId(funcName, vo.getId());
    }

    /**
     * Permission codes currently held by a role (safe + idempotent, hence GET).
     *
     * @param id      role id, passed as a query parameter
     * @param request http request
     * @return permission codes
     */
    @GetMapping("/getPermissionCodes")
    @Operation(summary = "角色已授权限码列表")
    public Response011<List<String>> getPermissionCodes(@RequestParam("id") String id, HttpServletRequest request) {
        String funcName = "get permission codes";
        Operator011 operator = Operator011Resolver.resolve(request);

        return Response011.success(funcName, julyRoleUseCase.listPermissionCodes(operator.id(), id));
    }

    /**
     * Find a role by primary key (safe + idempotent, hence GET).
     *
     * @param id      role id, passed as a query parameter
     * @param request http request
     * @return role detail
     */
    @GetMapping("/getById")
    @Operation(summary = "主键查询（id 走 query）")
    public Response011<JulyRoleVo011> getById(@RequestParam("id") String id, HttpServletRequest request) {
        String funcName = "get by id";
        Operator011 operator = Operator011Resolver.resolve(request);

        return Response011.success(funcName, julyRoleConverter.toVo(julyRoleUseCase.getById(operator.id(), id)));
    }

    /**
     * Menus granted to a role (flat list, no children assembly — the caller
     * diffs it against the menu tree by id). Safe + idempotent, hence GET.
     *
     * @param id      role id, passed as a query parameter
     * @param request http request
     * @return granted menu list
     */
    @GetMapping("/getMenusByRole")
    @Operation(summary = "角色已授权菜单（平铺列表，id 走 query）")
    public Response011<List<JulyMenuVo011>> getMenusByRole(@RequestParam("id") String id, HttpServletRequest request) {
        String funcName = "get menus by role";
        Operator011 operator = Operator011Resolver.resolve(request);

        return Response011.success(funcName,
                menuConverter.toVoList(julyRoleUseCase.getMenusByRole(operator.id(), id)));
    }

    /**
     * Users holding a role (full aggregates, so the caller can re-submit the
     * complete role set to assignRoles). Safe + idempotent, hence GET.
     *
     * @param id      role id, passed as a query parameter
     * @param request http request
     * @return holding user list
     */
    @GetMapping("/getUsersByRole")
    @Operation(summary = "角色关联用户（全量用户列表，id 走 query）")
    public Response011<List<JulyUserVo011>> getUsersByRole(@RequestParam("id") String id, HttpServletRequest request) {
        String funcName = "get users by role";
        Operator011 operator = Operator011Resolver.resolve(request);

        return Response011.success(funcName,
                userConverter.toVoList(julyRoleUseCase.getUsersByRole(operator.id(), id)));
    }

    /**
     * Page query with an optional keyword filter.
     *
     * @param query   page query request
     * @param request http request
     * @return page result of roles
     */
    @PostMapping("/selectListByPage")
    @Operation(summary = "分页查询（编码/名称模糊过滤）")
    public Response011<PageResult011<JulyRoleVo011>> selectListByPage(@Valid @RequestBody JulyRoleQueryVo011 query,
            HttpServletRequest request) {
        String funcName = "select list by page";
        Operator011 operator = Operator011Resolver.resolve(request);

        PageQuery011 pageQuery = new PageQuery011(query.getPageIndex(), query.getPageSize());
        PageResult011<JulyRole> page = julyRoleUseCase.selectListByPage(operator.id(), pageQuery, query.getKeyword());

        return Response011.success(funcName, page.withRows(julyRoleConverter.toVoList(page.rows())));
    }

    /**
     * Export every role row in batches and return the whole result in the
     * JSON envelope (batching bounds the database load, not the payload).
     *
     * @param request http request (operator from the auth filter)
     * @return envelope with the export result
     */
    @PostMapping("/export")
    @Operation(summary = "导出全部角色（分批取数，写 EXPORT 审计）")
    public Response011<ExportResult> export(HttpServletRequest request) {
        String funcName = "export";
        Operator011 operator = Operator011Resolver.resolve(request);

        return Response011.success(funcName, julyRoleUseCase.export(operator));
    }

    /**
     * Back every role row up into the storage center, keyed by timestamp.
     *
     * @param request http request (operator from the auth filter)
     * @return envelope with the stored object key
     */
    @PostMapping("/backup011")
    @Operation(summary = "备份全部角色到存储中心（写 BACKUP 审计）")
    public Response011<String> backup011(HttpServletRequest request) {
        String funcName = "backup";
        Operator011 operator = Operator011Resolver.resolve(request);

        return Response011.success(funcName, julyRoleUseCase.backup(operator));
    }
}
