package com.klsjnh.web.iam.controller;

/*                JulyUserController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july user controller class
 *      2026.09.26  pass operator into use case for permission checks
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
import com.klsjnh.common.vo.IdsVo011;

import com.klsjnh.domain.iam.user.JulyUser;
import com.klsjnh.domain.platform011.export.ExportResult;
import com.klsjnh.application.iam.user.JulyUserUseCase;
import com.klsjnh.application.iam.user.LoginResult;

import com.klsjnh.web.iam.converter.JulyUserConverter;

import com.klsjnh.web.global.WebPaths011;
import com.klsjnh.web.global.audit.AuditLog;
import com.klsjnh.web.iam.vo.julyuser.JulyUserChangePasswordVo011;
import com.klsjnh.web.iam.vo.julyuser.JulyUserInsertVo011;
import com.klsjnh.web.iam.vo.julyuser.JulyUserLoginVo011;
import com.klsjnh.web.iam.vo.julyuser.JulyUserQueryVo011;
import com.klsjnh.web.iam.vo.julyuser.JulyUserResetPasswordVo011;
import com.klsjnh.web.iam.vo.julyuser.JulyUserSessionVo011;
import com.klsjnh.web.iam.vo.julyuser.JulyUserUpdateVo011;
import com.klsjnh.web.iam.vo.julyuser.JulyUserVo011;
import com.klsjnh.web.util.ClientIp011Resolver;
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

/**
 * JulyUser HTTP adapter: user CRUD, role assignment, password reset and the
 * two login kinds (password / passwordless). Permission checks live in the
 * use case (operator resolved here).
 */

@Tag(name = "IAM - 用户管理")
@RestController
@RequestMapping(WebPaths011.IAM_USER)
public class JulyUserController {


    /**
     * JulyUser use case.
     */
    private final JulyUserUseCase julyUserUseCase;

    /**
     * Response julyUserConverter.
     */
    private final JulyUserConverter julyUserConverter;

    /**
     * Create the controller.
     *
     * @param julyUserUseCase   july user use case
     * @param julyUserConverter response julyUserConverter
     */
    public JulyUserController(JulyUserUseCase julyUserUseCase, JulyUserConverter julyUserConverter) {
        this.julyUserUseCase = julyUserUseCase;
        this.julyUserConverter = julyUserConverter;
    }

    /**
     * Insert a new user.
     *
     * @param vo      insert request
     * @param request http request
     * @return envelope with the new user id
     */
    @AuditLog(type = AuditType011.INSERT, objectCode = AuditObjectCodes011.JULY_USER)
    @PostMapping("/insert")
    @Operation(summary = "新增用户")
    public Response011<IdVo011> insert(@RequestBody JulyUserInsertVo011 vo, HttpServletRequest request) {
        String funcName = "insert";
        Operator011 operator = Operator011Resolver.resolve(request);

        return Response011.successId(funcName,
                julyUserUseCase.insert(operator.id(), vo.getUserAccount(), vo.getUserName(), vo.getPassword(),
                        vo.getMobile(), vo.getEmail(), vo.getAvatar(), vo.getPkOrg()));
    }

    /**
     * Update the user profile.
     *
     * @param vo      update request
     * @param request http request
     * @return envelope with the user id
     */
    @AuditLog(type = AuditType011.UPDATE, objectCode = AuditObjectCodes011.JULY_USER)
    @PostMapping("/update")
    @Operation(summary = "修改用户资料（不含账号与密码）")
    public Response011<IdVo011> update(@RequestBody JulyUserUpdateVo011 vo, HttpServletRequest request) {
        String funcName = "update";
        Operator011 operator = Operator011Resolver.resolve(request);

        julyUserUseCase.update(operator.id(), vo.getId(), vo.getUserName(), vo.getMobile(), vo.getEmail(),
                vo.getAvatar(), vo.getPkOrg());

        return Response011.successId(funcName, vo.getId());
    }

    /**
     * Logic delete a single user.
     *
     * @param idVo    request with the user id
     * @param request http request
     * @return envelope with the deleted user id
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = AuditObjectCodes011.JULY_USER)
    @PostMapping("/logicDelete")
    @Operation(summary = "逻辑删除（单个）")
    public Response011<IdVo011> logicDelete(@RequestBody IdVo011 idVo, HttpServletRequest request) {
        String funcName = "logic delete";
        Operator011 operator = Operator011Resolver.resolve(request);

        return Response011.successId(funcName, julyUserUseCase.logicDelete(operator.id(), idVo.getId()));
    }

    /**
     * Logic delete users in batch.
     *
     * @param idsVo   request with the user id list
     * @param request http request
     * @return per-id success/failure summary
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = AuditObjectCodes011.JULY_USER)
    @PostMapping("/logicDeleteBatch")
    @Operation(summary = "逻辑删除（批量）")
    public Response011<BatchDeleteResultVo011> logicDeleteBatch(@RequestBody IdsVo011 idsVo,
            HttpServletRequest request) {
        String funcName = "logic delete batch";
        Operator011 operator = Operator011Resolver.resolve(request);

        return Response011.success(funcName, julyUserUseCase.logicDeleteBatch(operator.id(), idsVo.getIds()));
    }

    /**
     * Find a user by primary key (safe + idempotent, hence GET).
     *
     * @param id      user id, passed as a query parameter
     * @param request http request
     * @return user detail
     */
    @GetMapping("/getById")
    @Operation(summary = "主键查询（id 走 query）")
    public Response011<JulyUserVo011> getById(@RequestParam("id") String id, HttpServletRequest request) {
        String funcName = "get by id";
        Operator011 operator = Operator011Resolver.resolve(request);

        return Response011.success(funcName, julyUserConverter.toVo(julyUserUseCase.getById(operator.id(), id)));
    }

    /**
     * Page query with optional keyword filters.
     *
     * @param query   page query request
     * @param request http request
     * @return page result of users
     */
    @PostMapping("/selectListByPage")
    @Operation(summary = "分页查询（账号/姓名模糊过滤）")
    public Response011<PageResult011<JulyUserVo011>> selectListByPage(@RequestBody JulyUserQueryVo011 query,
            HttpServletRequest request) {
        String funcName = "select list by page";
        Operator011 operator = Operator011Resolver.resolve(request);

        PageQuery011 pageQuery = new PageQuery011(query.getPageIndex(), query.getPageSize());
        PageResult011<JulyUser> page = julyUserUseCase.selectListByPage(operator.id(), pageQuery,
                query.getUserAccount(), query.getUserName());

        return Response011.success(funcName, page.withRows(julyUserConverter.toVoList(page.rows())));
    }

    /**
     * Reset a user password (admin action).
     *
     * @param vo      reset request
     * @param request http request
     * @return envelope with the user id
     */
    @AuditLog(type = AuditType011.UPDATE, objectCode = AuditObjectCodes011.JULY_USER)
    @PostMapping("/resetPassword")
    @Operation(summary = "重置密码（管理员动作）")
    public Response011<IdVo011> resetPassword(@RequestBody JulyUserResetPasswordVo011 vo,
            HttpServletRequest request) {
        String funcName = "reset password";
        Operator011 operator = Operator011Resolver.resolve(request);

        julyUserUseCase.resetPassword(operator.id(), vo.getId(), vo.getPassword());

        return Response011.successId(funcName, vo.getId());
    }

    /**
     * Login with account and password (any environment).
     *
     * @param vo      login request
     * @param request http request (client IP for the audit)
     * @return login session with the signed token
     */
    @PostMapping("/login")
    @Operation(summary = "账号密码登录（任何环境）")
    public Response011<JulyUserSessionVo011> login(@RequestBody JulyUserLoginVo011 vo, HttpServletRequest request) {
        String funcName = "login";

        LoginResult result = julyUserUseCase.login(vo.getUserAccount(), vo.getPassword(),
                ClientIp011Resolver.resolve(request));

        return Response011.success(funcName, toSession(result));
    }

    /**
     * Passwordless login by account only (debug / development runtime modes).
     *
     * @param vo      login request (account only)
     * @param request http request (client IP for the audit)
     * @return login session with the signed token
     */
    @PostMapping("/loginByUserName")
    @Operation(summary = "免密登录（仅 debug / development 运行态）")
    public Response011<JulyUserSessionVo011> loginByUserName(@RequestBody JulyUserLoginVo011 vo,
            HttpServletRequest request) {
        String funcName = "login by user name";

        LoginResult result = julyUserUseCase.loginByUserName(vo.getUserAccount(), ClientIp011Resolver.resolve(request));

        return Response011.success(funcName, toSession(result));
    }

    /**
     * Change the password of the current operator (old password verified).
     *
     * @param vo      change request
     * @param request http request (operator id from the auth filter)
     * @return envelope with the operator id
     */
    @PostMapping("/changePassword")
    @Operation(summary = "本人修改密码（验旧密）")
    public Response011<IdVo011> changePassword(@RequestBody JulyUserChangePasswordVo011 vo,
            HttpServletRequest request) {
        String funcName = "change password";
        Operator011 operator = Operator011Resolver.resolve(request);

        julyUserUseCase.changePassword(operator.id(), vo.getOldPassword(), vo.getNewPassword());

        return Response011.successId(funcName, operator.id());
    }

    /**
     * Logout: records the LOGOUT audit row (stateless JWT — the client clears
     * the token).
     *
     * @param request http request (operator from the auth filter)
     * @return empty envelope
     */
    @PostMapping("/logout")
    @Operation(summary = "登出（记录 LOGOUT 审计，客户端清除 token）")
    public Response011<Void> logout(HttpServletRequest request) {
        String funcName = "logout";

        Operator011 operator = Operator011Resolver.resolve(request);
        julyUserUseCase.logout(operator.id(), operator.userAccount(), operator.ip());

        return Response011.success(funcName, null);
    }

    /**
     * Export every user row in batches and return the whole result in the
     * JSON envelope (batching bounds the database load, not the payload).
     *
     * @param request http request (operator from the auth filter)
     * @return envelope with the export result
     */
    @PostMapping("/export")
    @Operation(summary = "导出全部用户（分批取数，写 EXPORT 审计）")
    public Response011<ExportResult> export(HttpServletRequest request) {
        String funcName = "export";
        Operator011 operator = Operator011Resolver.resolve(request);

        return Response011.success(funcName, julyUserUseCase.export(operator));
    }

    /**
     * Back every user row up into the storage center, keyed by timestamp.
     *
     * @param request http request (operator from the auth filter)
     * @return envelope with the stored object key
     */
    @PostMapping("/backup011")
    @Operation(summary = "备份全部用户到存储中心（写 BACKUP 审计）")
    public Response011<String> backup011(HttpServletRequest request) {
        String funcName = "backup";
        Operator011 operator = Operator011Resolver.resolve(request);

        return Response011.success(funcName, julyUserUseCase.backup(operator));
    }

    /**
     * Map the login result to the session VO.
     *
     * @param result login result
     * @return session VO
     */
    private JulyUserSessionVo011 toSession(LoginResult result) {
        JulyUserSessionVo011 vo = new JulyUserSessionVo011();
        vo.setToken(result.token());
        vo.setUserAccount(result.userAccount());
        vo.setUserName(result.userName());
        vo.setRoles(result.roles());

        return vo;
    }
}
