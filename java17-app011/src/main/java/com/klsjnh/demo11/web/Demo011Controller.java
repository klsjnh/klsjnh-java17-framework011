package com.klsjnh.demo11.web;

/*                Demo011Controller class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  demo 011 controller class
 *
 */

import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.response.Response011;
import com.klsjnh.common.vo.IdVo011;

import com.klsjnh.demo11.application.Demo011UseCase;
import com.klsjnh.demo11.domain.Demo011;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Demo011 HTTP adapter: the reference controller for third parties — request
 * in, response out, mapping inline (a single-file vertical slice demo).
 */

@Tag(name = "Demo011 演示")
@RestController
@RequestMapping("/klsjnh/demo11/v1")
public class Demo011Controller {

    /**
     * Demo011 use case.
     */
    private final Demo011UseCase useCase;

    /**
     * Create the controller.
     *
     * @param useCase demo use case
     */
    public Demo011Controller(Demo011UseCase useCase) {
        this.useCase = useCase;
    }

    /**
     * Insert a new demo row.
     *
     * @param vo insert request
     * @return envelope with the new id
     */
    @PostMapping("/insert")
    @Operation(summary = "新增演示数据")
    public Response011<IdVo011> insert(@RequestBody Demo011Vo vo) {
        String funcName = "insert";

        return Response011.successId(funcName, useCase.insert(vo.getCode(), vo.getName()));
    }

    /**
     * Update the name of a demo row.
     *
     * @param vo update request
     * @return envelope with the id
     */
    @PostMapping("/update")
    @Operation(summary = "修改名称（编码不可改）")
    public Response011<IdVo011> update(@RequestBody Demo011Vo vo) {
        String funcName = "update";

        useCase.update(vo.getId(), vo.getName());

        return Response011.successId(funcName, vo.getId());
    }

    /**
     * Logic delete a demo row.
     *
     * @param idVo request with the id
     * @return envelope with the id
     */
    @PostMapping("/logicDelete")
    @Operation(summary = "逻辑删除")
    public Response011<IdVo011> logicDelete(@RequestBody IdVo011 idVo) {
        String funcName = "logic delete";

        useCase.logicDelete(idVo.getId());

        return Response011.successId(funcName, idVo.getId());
    }

    /**
     * Find a demo row by primary key.
     *
     * @param idVo request with the id
     * @return demo detail
     */
    @PostMapping("/getById")
    @Operation(summary = "主键查询")
    public Response011<Demo011Vo> getById(@RequestBody IdVo011 idVo) {
        String funcName = "get by id";

        return Response011.success(funcName, toVo(useCase.getById(idVo.getId())));
    }

    /**
     * Page query with an optional keyword filter.
     *
     * @param vo page query request
     * @return page result of demo rows
     */
    @PostMapping("/selectListByPage")
    @Operation(summary = "分页查询（编码/名称模糊过滤）")
    public Response011<PageResult011<Demo011Vo>> selectListByPage(@RequestBody Demo011Vo vo) {
        String funcName = "select list by page";

        PageQuery011 pageQuery = new PageQuery011(vo.getPageIndex(), vo.getPageSize());
        PageResult011<Demo011> page = useCase.selectListByPage(pageQuery, vo.getKeyword());

        return Response011.success(funcName, page.withRows(page.rows().stream().map(this::toVo).toList()));
    }

    /**
     * Map the aggregate to the response VO.
     *
     * @param demo aggregate
     * @return response VO
     */
    private Demo011Vo toVo(Demo011 demo) {
        Demo011Vo vo = new Demo011Vo();
        vo.setId(demo.id().value());
        vo.setCode(demo.code());
        vo.setName(demo.name());
        vo.setStatus(demo.status());
        vo.setCreateBy(demo.audit().createBy());
        vo.setUpdateBy(demo.audit().updateBy());
        vo.setCreateTime(demo.audit().createTime());
        vo.setUpdateTime(demo.audit().updateTime());

        return vo;
    }

    /**
     * Demo request / response VO (single-file demo keeps VO inline).
     */
    @Data
    public static class Demo011Vo {

        /** Primary key. */
        @Schema(description = "主键")
        private String id;

        /** Demo code, unique. */
        @Schema(description = "编码（唯一）")
        private String code;

        /** Demo name. */
        @Schema(description = "名称")
        private String name;

        /** Row status. */
        @Schema(description = "状态（0 停用 / 1 启用）")
        private String status;

        /** Code / name keyword (fuzzy), request only. */
        @Schema(description = "编码/名称关键字（模糊，仅查询用）")
        private String keyword;

        /** Page index, request only. */
        @Schema(description = "页码（仅查询用）")
        private Integer pageIndex;

        /** Page size, request only. */
        @Schema(description = "每页条数（仅查询用）")
        private Integer pageSize;

        /** Creator. */
        @Schema(description = "创建人")
        private String createBy;

        /** Last modifier. */
        @Schema(description = "最后修改人")
        private String updateBy;

        /** Create time. */
        @Schema(description = "创建日期")
        private LocalDateTime createTime;

        /** Update time. */
        @Schema(description = "最后修改日期")
        private LocalDateTime updateTime;
    }
}
