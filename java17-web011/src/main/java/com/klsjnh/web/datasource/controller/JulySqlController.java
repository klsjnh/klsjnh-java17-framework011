package com.klsjnh.web.datasource.controller;

/*                JulySqlController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  read-only sql query controller
 *
 */

import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.response.Response011;

import com.klsjnh.application.datasource.kernel.SqlQueryUseCase;

import com.klsjnh.web.datasource.vo.julysql.SqlQueryPageVo011;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Data source center — read HTTP adapter ({@code /klsjnh/datasource/julySql/v1}):
 * run a read-only, parameterized, paged query against a dynamic datasource.
 * Non-SELECT statements move to a separate, separately authorized endpoint.
 */

@Tag(name = "数据源011 - SQL 查询")
@RestController
@RequestMapping("/klsjnh/datasource/julySql/v1")
public class JulySqlController {

    /**
     * Read-only query use case.
     */
    private final SqlQueryUseCase sqlQueryUseCase;

    /**
     * Create the controller.
     *
     * @param sqlQueryUseCase read-only query use case
     */
    public JulySqlController(SqlQueryUseCase sqlQueryUseCase) {
        this.sqlQueryUseCase = sqlQueryUseCase;
    }

    /**
     * Run a read-only paged query.
     *
     * @param vo request
     * @return page result
     */
    @PostMapping("/selectByPage")
    @Operation(summary = "执行 SQL 分页查询（只读 SELECT；参数化；pageSize 夹取 [10,500]）")
    public Response011<PageResult011<Map<String, Object>>> selectByPage(@RequestBody SqlQueryPageVo011 vo) {
        String funcName = "sql select by page";

        PageResult011<Map<String, Object>> page = sqlQueryUseCase.selectByPage(vo.getDsCode(), vo.getSql(),
                vo.getParams(), vo.getPageIndex(), vo.getPageSize());

        return Response011.success(funcName, page);
    }
}
