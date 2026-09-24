package com.klsjnh.web.iam.controller;

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
 *      2026.09.15  tree endpoint renamed to get
 *
 */

import com.klsjnh.common.constant.AuditObjectCodes011;
import com.klsjnh.common.enums.AuditType011;
import com.klsjnh.common.identity.Operator011;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.response.Response011;
import com.klsjnh.common.vo.BatchDeleteResultVo011;
import com.klsjnh.common.vo.IdVo011;

import com.klsjnh.domain.platform011.export.ExportResult;
import com.klsjnh.domain.iam.organization.JulyOrganization;
import com.klsjnh.application.platform011.backup.BackupUseCase;
import com.klsjnh.application.platform011.export.ExportUseCase;
import com.klsjnh.application.iam.organization.JulyOrganizationUseCase;

import com.klsjnh.web.iam.converter.JulyOrganizationConverter;

import com.klsjnh.web.global.audit.AuditLog;
import com.klsjnh.web.iam.vo.julyorganization.JulyOrganizationInsertVo011;
import com.klsjnh.web.iam.vo.julyorganization.JulyOrganizationQueryVo011;
import com.klsjnh.web.iam.vo.julyorganization.JulyOrganizationUpdateVo011;
import com.klsjnh.web.iam.vo.julyorganization.JulyOrganizationVo011;
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
 * JulyOrganization HTTP adapter: org tree CRUD with member-count badges and
 * move / delete constraints.
 */

@Tag(name = "IAM - 组织机构")
@RestController
@RequestMapping("/klsjnh/iam/julyOrganization/v1")
public class JulyOrganizationController {


    /**
     * JulyOrganization use case.
     */
    private final JulyOrganizationUseCase julyOrganizationUseCase;

    /**
     * Response julyOrganizationConverter.
     */
    private final JulyOrganizationConverter julyOrganizationConverter;

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
     * @param julyOrganizationUseCase       july organization use case
     * @param julyOrganizationConverter     response julyOrganizationConverter
     * @param exportUseCase export use case
     * @param backupUseCase backup use case
     */
    public JulyOrganizationController(JulyOrganizationUseCase julyOrganizationUseCase, JulyOrganizationConverter julyOrganizationConverter,
            ExportUseCase exportUseCase, BackupUseCase backupUseCase) {
        this.julyOrganizationUseCase = julyOrganizationUseCase;
        this.julyOrganizationConverter = julyOrganizationConverter;
        this.exportUseCase = exportUseCase;
        this.backupUseCase = backupUseCase;
    }

    /**
     * Insert a new organization node.
     *
     * @param vo insert request
     * @return envelope with the new org id
     */
    @AuditLog(type = AuditType011.INSERT, objectCode = AuditObjectCodes011.JULY_ORGANIZATION)
    @PostMapping("/insert")
    @Operation(summary = "新增组织（层级由上级推导）")
    public Response011<IdVo011> insert(@RequestBody JulyOrganizationInsertVo011 vo) {
        String funcName = "insert";

        return Response011.successId(funcName,
                julyOrganizationUseCase.insert(vo.getOrgCode(), vo.getOrgName(), vo.getPkUser(), vo.getParentId(), vo.getSortOrder()));
    }

    /**
     * Update an organization node (parent move allowed, subtree re-leveled).
     *
     * @param vo update request
     * @return envelope with the org id
     */
    @AuditLog(type = AuditType011.UPDATE, objectCode = AuditObjectCodes011.JULY_ORGANIZATION)
    @PostMapping("/update")
    @Operation(summary = "修改组织（编码不可改，可移动上级并重排层级）")
    public Response011<IdVo011> update(@RequestBody JulyOrganizationUpdateVo011 vo) {
        String funcName = "update";

        julyOrganizationUseCase.update(vo.getId(), vo.getOrgName(), vo.getPkUser(), vo.getParentId(), vo.getSortOrder());

        return Response011.successId(funcName, vo.getId());
    }

    /**
     * Logic delete an organization (children / mounted users reject).
     *
     * @param idVo request with the org id
     * @return envelope with the org id
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = AuditObjectCodes011.JULY_ORGANIZATION)
    @PostMapping("/logicDelete")
    @Operation(summary = "逻辑删除（有子组织或挂有用户拒绝）")
    public Response011<IdVo011> logicDelete(@RequestBody IdVo011 idVo) {
        String funcName = "logic delete";

        julyOrganizationUseCase.logicDelete(idVo.getId());

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

        return Response011.success(funcName, julyOrganizationConverter.toVo(julyOrganizationUseCase.getById(id)));
    }

    /**
     * Load the full alive organization tree with member-count badges
     * (read-only, hence GET).
     *
     * @return root nodes with nested children
     */
    @GetMapping("/getTree")
    @Operation(summary = "组织树（含人数角标，GET）")
    public Response011<List<JulyOrganizationVo011>> getTree() {
        String funcName = "get tree";

        JulyOrganizationUseCase.TreeWithCounts tree = julyOrganizationUseCase.getTree();

        return Response011.success(funcName, julyOrganizationConverter.toVoList(tree.tree(), tree.counts()));
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
        PageResult011<JulyOrganization> page = julyOrganizationUseCase.selectListByPage(pageQuery, query.getKeyword());

        return Response011.success(funcName, page.withRows(julyOrganizationConverter.toVoList(page.rows(), null)));
    }

    /**
     * Export every organization row in batches and return the whole result in
     * the JSON envelope (batching bounds the database load, not the payload).
     *
     * @param request http request (operator from the auth filter)
     * @return envelope with the export result
     */
    @PostMapping("/export")
    @Operation(summary = "导出全部组织（分批取数，写 EXPORT 审计）")
    public Response011<ExportResult> export(HttpServletRequest request) {
        String funcName = "export";

        Operator011 operator = Operator011Resolver.resolve(request);

        ExportResult result = exportUseCase.export(AuditObjectCodes011.JULY_ORGANIZATION, operator);

        return Response011.success(funcName, result);
    }

    /**
     * Back every organization row up into the storage center, keyed by
     * timestamp.
     *
     * @param request http request (operator from the auth filter)
     * @return envelope with the stored object key
     */
    @PostMapping("/backup011")
    @Operation(summary = "备份全部组织到存储中心（写 BACKUP 审计）")
    public Response011<String> backup011(HttpServletRequest request) {
        String funcName = "backup";

        Operator011 operator = Operator011Resolver.resolve(request);

        String key = backupUseCase.backup(AuditObjectCodes011.JULY_ORGANIZATION, operator);

        return Response011.success(funcName, key);
    }
}
