package com.klsjnh.web.iam.controller;

/*                JulyUserAuditController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july user audit controller class
 *      2026.09.15  time parsing moved to date util 011
 *      2026.09.26  pass operator into use case for permission checks
 *
 */

import com.klsjnh.common.identity.Operator011;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.response.Response011;
import com.klsjnh.common.util.DateUtil011;

import com.klsjnh.application.iam.user.JulyUserAuditUseCase;
import com.klsjnh.domain.iam.user.JulyUserAuditRow;

import com.klsjnh.web.iam.converter.JulyUserAuditConverter;

import com.klsjnh.web.iam.vo.julyuseraudit.JulyUserAuditQueryVo011;
import com.klsjnh.web.iam.vo.julyuseraudit.JulyUserAuditVo011;
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
 * JulyUserAudit HTTP adapter: read-only query over the append-only audit
 * trail. Permission checks live in the use case (operator resolved here).
 */

@Tag(name = "IAM - 用户审计")
@RestController
@RequestMapping("/klsjnh/iam/julyUserAudit/v1")
public class JulyUserAuditController {

    /**
     * Audit use case.
     */
    private final JulyUserAuditUseCase julyUserAuditUseCase;

    /**
     * Response julyUserAuditConverter.
     */
    private final JulyUserAuditConverter julyUserAuditConverter;

    /**
     * Create the controller.
     *
     * @param julyUserAuditUseCase   audit use case
     * @param julyUserAuditConverter response julyUserAuditConverter
     */
    public JulyUserAuditController(JulyUserAuditUseCase julyUserAuditUseCase,
            JulyUserAuditConverter julyUserAuditConverter) {
        this.julyUserAuditUseCase = julyUserAuditUseCase;
        this.julyUserAuditConverter = julyUserAuditConverter;
    }

    /**
     * Page query with optional filters.
     *
     * @param query   page query request
     * @param request http request
     * @return page result of audit rows
     */
    @PostMapping("/selectListByPage")
    @Operation(summary = "分页查询（账号模糊/类型精确/时间段过滤）")
    public Response011<PageResult011<JulyUserAuditVo011>> selectListByPage(
            @Valid @RequestBody JulyUserAuditQueryVo011 query, HttpServletRequest request) {
        String funcName = "select list by page";
        Operator011 operator = Operator011Resolver.resolve(request);

        PageQuery011 pageQuery = new PageQuery011(query.getPageIndex(), query.getPageSize());
        PageResult011<JulyUserAuditRow> page = julyUserAuditUseCase.selectListByPage(operator.id(), pageQuery,
                query.getUserAccount(), query.getAuditType(), DateUtil011.parseIso(query.getBeginTime()),
                DateUtil011.parseIso(query.getEndTime()));

        return Response011.success(funcName, page.withRows(julyUserAuditConverter.toVoList(page.rows())));
    }
}
