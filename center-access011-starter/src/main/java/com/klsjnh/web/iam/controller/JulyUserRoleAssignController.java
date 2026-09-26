package com.klsjnh.web.iam.controller;

/*                JulyUserRoleAssignController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.26
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.26  assignRoles endpoint in access center
 *      2026.09.26  pass operator into use case for permission checks
 *
 */

import com.klsjnh.common.constant.AuditObjectCodes011;
import com.klsjnh.common.enums.AuditType011;
import com.klsjnh.common.identity.Operator011;
import com.klsjnh.common.response.Response011;
import com.klsjnh.common.vo.IdVo011;

import com.klsjnh.application.iam.user.JulyUserRoleAssignUseCase;

import com.klsjnh.web.global.WebPaths011;
import com.klsjnh.web.global.audit.AuditLog;
import com.klsjnh.web.iam.vo.julyuser.JulyUserAssignRolesVo011;
import com.klsjnh.web.util.Operator011Resolver;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * Access-center endpoint: assign roles to a user. Same URL as before
 * ({@code /klsjnh/iam/julyUser/v1/assignRoles}). Permission checks live in
 * the use case (operator resolved here).
 */

@RestController
@RequestMapping(WebPaths011.IAM_USER)
@Tag(name = "用户角色分配（访问中心）")
public class JulyUserRoleAssignController {

    /**
     * Assign use case.
     */
    private final JulyUserRoleAssignUseCase julyUserRoleAssignUseCase;

    /**
     * Create the controller.
     *
     * @param julyUserRoleAssignUseCase assign use case
     */
    public JulyUserRoleAssignController(JulyUserRoleAssignUseCase julyUserRoleAssignUseCase) {
        this.julyUserRoleAssignUseCase = julyUserRoleAssignUseCase;
    }

    /**
     * Assign roles to a user (toggle semantics, replace strategy).
     *
     * @param vo      assign request
     * @param request http request
     * @return envelope with the user id
     */
    @AuditLog(type = AuditType011.UPDATE, objectCode = AuditObjectCodes011.JULY_USER)
    @PostMapping("/assignRoles")
    @Operation(summary = "分配角色（整存替换）")
    public Response011<IdVo011> assignRoles(@Valid @RequestBody JulyUserAssignRolesVo011 vo, HttpServletRequest request) {
        String funcName = "assign roles";
        Operator011 operator = Operator011Resolver.resolve(request);

        julyUserRoleAssignUseCase.assignRoles(operator.id(), vo.getId(), vo.getPkRoles());

        return Response011.successId(funcName, vo.getId());
    }
}
