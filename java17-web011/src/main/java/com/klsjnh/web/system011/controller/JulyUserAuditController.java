package com.klsjnh.web.system011.controller;

/*                JulyUserAuditController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july user audit controller class
 *      2026.09.15  time parsing moved to date util 011
 *
 */

import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.response.Response011;
import com.klsjnh.common.util.DateUtil011;

import com.klsjnh.application.iam.JulyUserAuditUseCase;
import com.klsjnh.domain.iam.JulyUserAuditRow;

import com.klsjnh.web.system011.converter.JulyUserAuditConverter;

import com.klsjnh.web.system011.vo.julyuseraudit.JulyUserAuditQueryVo011;
import com.klsjnh.web.system011.vo.julyuseraudit.JulyUserAuditVo011;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * JulyUserAudit HTTP adapter: read-only query over the append-only audit
 * trail.
 */

@Tag(name = "系统管理 - 用户审计")
@RestController
@RequestMapping("/klsjnh/system011/julyUserAudit/v1")
public class JulyUserAuditController {

    /**
     * Audit use case.
     */
    private final JulyUserAuditUseCase useCase;

    /**
     * Response converter.
     */
    private final JulyUserAuditConverter converter;

    /**
     * Create the controller.
     *
     * @param useCase   audit use case
     * @param converter response converter
     */
    public JulyUserAuditController(JulyUserAuditUseCase useCase, JulyUserAuditConverter converter) {
        this.useCase = useCase;
        this.converter = converter;
    }

    /**
     * Page query with optional filters.
     *
     * @param query page query request
     * @return page result of audit rows
     */
    @PostMapping("/selectListByPage")
    @Operation(summary = "分页查询（账号模糊/类型精确/时间段过滤）")
    public Response011<PageResult011<JulyUserAuditVo011>> selectListByPage(
            @RequestBody JulyUserAuditQueryVo011 query) {
        String funcName = "select list by page";

        PageQuery011 pageQuery = new PageQuery011(query.getPageIndex(), query.getPageSize());
        PageResult011<JulyUserAuditRow> page = useCase.selectListByPage(pageQuery, query.getUserAccount(),
                query.getAuditType(), DateUtil011.parseIso(query.getBeginTime()),
                DateUtil011.parseIso(query.getEndTime()));

        return Response011.success(funcName, page.withRows(converter.toVoList(page.rows())));
    }
}
