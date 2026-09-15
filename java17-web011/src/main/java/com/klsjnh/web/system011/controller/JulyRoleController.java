package com.klsjnh.web.system011.controller;

/*                JulyRoleController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate 2026.09.15
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july role controller class
 *      2026.09.15  update forwards status
 *      2026.09.15  get endpoints func name aligned
 *
 */

import com.klsjnh.common.identity.Operator011;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.response.Response011;
import com.klsjnh.common.vo.IdVo011;

import com.klsjnh.domain.iam.JulyRole;
import com.klsjnh.domain.platform011.export.ExportResult;
import com.klsjnh.application.iam.JulyRoleUseCase;
import com.klsjnh.application.platform011.backup.BackupUseCase;
import com.klsjnh.application.platform011.export.ExportUseCase;

import com.klsjnh.web.system011.converter.JulyMenuConverter;
import com.klsjnh.web.system011.converter.JulyRoleConverter;
import com.klsjnh.web.system011.converter.JulyUserConverter;

import com.klsjnh.web.system011.vo.julyrole.JulyRoleAssignMenusVo011;
import com.klsjnh.web.system011.vo.julyrole.JulyRoleInsertVo011;
import com.klsjnh.web.system011.vo.julyrole.JulyRoleQueryVo011;
import com.klsjnh.web.system011.vo.julyrole.JulyRoleUpdateVo011;
import com.klsjnh.web.system011.vo.julyrole.JulyRoleVo011;
import com.klsjnh.web.system011.vo.julymenu.JulyMenuVo011;
import com.klsjnh.web.system011.vo.julyuser.JulyUserVo011;
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
 * JulyRole HTTP adapter: role management endpoints (built-in roles protected).
 */

@Tag(name = "系统管理 - 角色管理")
@RestController
@RequestMapping("/klsjnh/system011/julyRole/v1")
public class JulyRoleController {

    /**
     * Object code this controller exports and backs up under.
     */
    private static final String OBJECT_CODE = "julyRole";

    /**
     * JulyRole use case.
     */
    private final JulyRoleUseCase useCase;

    /**
     * Response converter.
     */
    private final JulyRoleConverter converter;

    /**
     * Menu response converter.
     */
    private final JulyMenuConverter menuConverter;

    /**
     * User response converter.
     */
    private final JulyUserConverter userConverter;

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
     * @param useCase       july role use case
     * @param converter     response converter
     * @param menuConverter menu response converter
     * @param userConverter user response converter
     * @param exportUseCase export use case
     * @param backupUseCase backup use case
     */
    public JulyRoleController(JulyRoleUseCase useCase, JulyRoleConverter converter, JulyMenuConverter menuConverter,
            JulyUserConverter userConverter, ExportUseCase exportUseCase, BackupUseCase backupUseCase) {
        this.useCase = useCase;
        this.converter = converter;
        this.menuConverter = menuConverter;
        this.userConverter = userConverter;
        this.exportUseCase = exportUseCase;
        this.backupUseCase = backupUseCase;
    }

    /**
     * Insert a new custom role.
     *
     * @param vo insert request
     * @return envelope with the new role id
     */
    @PostMapping("/insert")
    @Operation(summary = "新增角色")
    public Response011<IdVo011> insert(@RequestBody JulyRoleInsertVo011 vo) {
        String funcName = "insert";

        return Response011.successId(funcName,
                useCase.insert(vo.getRoleCode(), vo.getRoleName(), vo.getRemark()));
    }

    /**
     * Update a role.
     *
     * @param vo update request
     * @return envelope with the role id
     */
    @PostMapping("/update")
    @Operation(summary = "修改角色（编码不可改，状态可改）")
    public Response011<IdVo011> update(@RequestBody JulyRoleUpdateVo011 vo) {
        String funcName = "update";

        useCase.update(vo.getId(), vo.getRoleName(), vo.getRemark(), vo.getStatus());

        return Response011.successId(funcName, vo.getId());
    }

    /**
     * Logic delete a role (built-in roles protected).
     *
     * @param idVo request with the role id
     * @return envelope with the role id
     */
    @PostMapping("/logicDelete")
    @Operation(summary = "逻辑删除（内置角色拒绝）")
    public Response011<IdVo011> logicDelete(@RequestBody IdVo011 idVo) {
        String funcName = "logic delete";

        useCase.logicDelete(idVo.getId());

        return Response011.successId(funcName, idVo.getId());
    }

    /**
     * Assign menus to a role (toggle semantics, replace strategy).
     *
     * @param vo assign request
     * @return envelope with the role id
     */
    @PostMapping("/assignMenus")
    @Operation(summary = "角色授权菜单（整存替换）")
    public Response011<IdVo011> assignMenus(@RequestBody JulyRoleAssignMenusVo011 vo) {
        String funcName = "assign menus";

        useCase.assignMenus(vo.getId(), vo.getPkMenus());

        return Response011.successId(funcName, vo.getId());
    }

    /**
     * Find a role by primary key (safe + idempotent, hence GET).
     *
     * @param id role id, passed as a query parameter
     * @return role detail
     */
    @GetMapping("/getById")
    @Operation(summary = "主键查询（id 走 query）")
    public Response011<JulyRoleVo011> getById(@RequestParam("id") String id) {
        String funcName = "get by id";

        return Response011.success(funcName, converter.toVo(useCase.getById(id)));
    }

    /**
     * Menus granted to a role (flat list, no children assembly — the caller
     * diffs it against the menu tree by id). Safe + idempotent, hence GET.
     *
     * @param id role id, passed as a query parameter
     * @return granted menu list
     */
    @GetMapping("/getMenusByRole")
    @Operation(summary = "角色已授权菜单（平铺列表，id 走 query）")
    public Response011<List<JulyMenuVo011>> getMenusByRole(@RequestParam("id") String id) {
        String funcName = "get menus by role";

        return Response011.success(funcName, menuConverter.toVoList(useCase.getMenusByRole(id)));
    }

    /**
     * Users holding a role (full aggregates, so the caller can re-submit the
     * complete role set to assignRoles). Safe + idempotent, hence GET.
     *
     * @param id role id, passed as a query parameter
     * @return holding user list
     */
    @GetMapping("/getUsersByRole")
    @Operation(summary = "角色关联用户（全量用户列表，id 走 query）")
    public Response011<List<JulyUserVo011>> getUsersByRole(@RequestParam("id") String id) {
        String funcName = "get users by role";

        return Response011.success(funcName, userConverter.toVoList(useCase.getUsersByRole(id)));
    }

    /**
     * Page query with an optional keyword filter.
     *
     * @param query page query request
     * @return page result of roles
     */
    @PostMapping("/selectListByPage")
    @Operation(summary = "分页查询（编码/名称模糊过滤）")
    public Response011<PageResult011<JulyRoleVo011>> selectListByPage(@RequestBody JulyRoleQueryVo011 query) {
        String funcName = "select list by page";

        PageQuery011 pageQuery = new PageQuery011(query.getPageIndex(), query.getPageSize());
        PageResult011<JulyRole> page = useCase.selectListByPage(pageQuery, query.getKeyword());

        return Response011.success(funcName, page.withRows(converter.toVoList(page.rows())));
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

        ExportResult result = exportUseCase.export(OBJECT_CODE, operator);

        return Response011.success(funcName, result);
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

        String key = backupUseCase.backup(OBJECT_CODE, operator);

        return Response011.success(funcName, key);
    }
}
