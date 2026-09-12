package com.klsjnh.web.system011.controller;

/*                JulyUserController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july user controller class
 *
 */

import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.response.Response011;
import com.klsjnh.common.vo.BatchDeleteResultVo011;
import com.klsjnh.common.vo.IdVo011;

import com.klsjnh.application.iam.JulyUserUseCase;
import com.klsjnh.application.iam.LoginResult;
import com.klsjnh.domain.iam.JulyUser;
import com.klsjnh.web.system011.converter.JulyUserConverter;
import com.klsjnh.web.system011.vo.julyuser.JulyUserAssignRolesVo011;
import com.klsjnh.web.system011.vo.julyuser.JulyUserInsertVo011;
import com.klsjnh.web.system011.vo.julyuser.JulyUserLoginVo011;
import com.klsjnh.web.system011.vo.julyuser.JulyUserQueryVo011;
import com.klsjnh.web.system011.vo.julyuser.JulyUserResetPasswordVo011;
import com.klsjnh.web.system011.vo.julyuser.JulyUserSessionVo011;
import com.klsjnh.web.system011.vo.julyuser.JulyUserUpdateVo011;
import com.klsjnh.web.system011.vo.julyuser.JulyUserVo011;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * JulyUser HTTP adapter: user CRUD, role assignment, password reset and the
 * two login kinds (password / passwordless).
 */

@Tag(name = "用户管理")
@RestController
@RequestMapping("/klsjnh/system011/julyUser/v1")
public class JulyUserController {

    /**
     * JulyUser use case.
     */
    private final JulyUserUseCase useCase;

    /**
     * Response converter.
     */
    private final JulyUserConverter converter;

    /**
     * Create the controller.
     *
     * @param useCase   july user use case
     * @param converter response converter
     */
    public JulyUserController(JulyUserUseCase useCase, JulyUserConverter converter) {
        this.useCase = useCase;
        this.converter = converter;
    }

    /**
     * Insert a new user.
     *
     * @param vo insert request
     * @return envelope with the new user id
     */
    @PostMapping("/insert")
    @Operation(summary = "新增用户")
    public Response011<IdVo011> insert(@RequestBody JulyUserInsertVo011 vo) {
        String funcName = "insert";

        return Response011.successId(funcName,
                useCase.insert(vo.getUserAccount(), vo.getUserName(), vo.getPassword(), vo.getMobile(),
                        vo.getEmail()));
    }

    /**
     * Update the user profile.
     *
     * @param vo update request
     * @return envelope with the user id
     */
    @PostMapping("/update")
    @Operation(summary = "修改用户资料（不含账号与密码）")
    public Response011<IdVo011> update(@RequestBody JulyUserUpdateVo011 vo) {
        String funcName = "update";

        useCase.update(vo.getId(), vo.getUserName(), vo.getMobile(), vo.getEmail());

        return Response011.successId(funcName, vo.getId());
    }

    /**
     * Logic delete users (batch, cascades to the user_role children).
     *
     * @param ids user id list
     * @return per-id success/failure summary
     */
    @PostMapping("/logicDelete")
    @Operation(summary = "逻辑删除（批量，级联停用用户角色关联）")
    public Response011<BatchDeleteResultVo011> logicDelete(@RequestBody List<String> ids) {
        String funcName = "logic delete";

        return Response011.success(funcName, useCase.logicDelete(ids));
    }

    /**
     * Find a user by primary key.
     *
     * @param idVo request with the user id
     * @return user detail
     */
    @PostMapping("/getById")
    @Operation(summary = "主键查询")
    public Response011<JulyUserVo011> getById(@RequestBody IdVo011 idVo) {
        String funcName = "get by id";

        return Response011.success(funcName, converter.toVo(useCase.getById(idVo.getId())));
    }

    /**
     * Page query with optional keyword filters.
     *
     * @param query page query request
     * @return page result of users
     */
    @PostMapping("/selectListByPage")
    @Operation(summary = "分页查询（账号/姓名模糊过滤）")
    public Response011<PageResult011<JulyUserVo011>> selectListByPage(@RequestBody JulyUserQueryVo011 query) {
        String funcName = "select list by page";

        PageQuery011 pageQuery = new PageQuery011(query.getPageIndex(), query.getPageSize());
        PageResult011<JulyUser> page = useCase.selectListByPage(pageQuery, query.getUserAccount(),
                query.getUserName());

        return Response011.success(funcName, page.withRows(converter.toVoList(page.rows())));
    }

    /**
     * Assign roles to a user (toggle semantics, replace strategy).
     *
     * @param vo assign request
     * @return envelope with the user id
     */
    @PostMapping("/assignRoles")
    @Operation(summary = "分配角色（整存替换）")
    public Response011<IdVo011> assignRoles(@RequestBody JulyUserAssignRolesVo011 vo) {
        String funcName = "assign roles";

        useCase.assignRoles(vo.getId(), vo.getPkRoles());

        return Response011.successId(funcName, vo.getId());
    }

    /**
     * Reset a user password (admin action).
     *
     * @param vo reset request
     * @return envelope with the user id
     */
    @PostMapping("/resetPassword")
    @Operation(summary = "重置密码（管理员动作）")
    public Response011<IdVo011> resetPassword(@RequestBody JulyUserResetPasswordVo011 vo) {
        String funcName = "reset password";

        useCase.resetPassword(vo.getId(), vo.getPassword());

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

        LoginResult result = useCase.login(vo.getUserAccount(), vo.getPassword(), request.getRemoteAddr());

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

        LoginResult result = useCase.loginByUserName(vo.getUserAccount(), request.getRemoteAddr());

        return Response011.success(funcName, toSession(result));
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

        return vo;
    }
}
