package com.klsjnh.web.system011.controller;

/*                JulyConfigController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.13
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.13  july config controller class
 *
 */

import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.response.Response011;
import com.klsjnh.common.vo.IdVo011;

import com.klsjnh.application.config.JulyConfigUseCase;
import com.klsjnh.domain.system011.config.JulyConfig;
import com.klsjnh.web.system011.converter.JulyConfigConverter;
import com.klsjnh.web.system011.vo.julyconfig.JulyConfigQueryVo011;
import com.klsjnh.web.system011.vo.julyconfig.JulyConfigUpdateVo011;
import com.klsjnh.web.system011.vo.julyconfig.JulyConfigVo011;

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
 * JulyConfig HTTP adapter: admin CRUD over runtime key-value parameters.
 * There is deliberately NO getByCode HTTP endpoint — config reading is a
 * program behavior (JulyConfigUseCase.getByCode), not a management action.
 */

@Tag(name = "配置管理")
@RestController
@RequestMapping("/klsjnh/system011/julyConfig/v1")
public class JulyConfigController {

    /**
     * JulyConfig use case.
     */
    private final JulyConfigUseCase useCase;

    /**
     * Response converter.
     */
    private final JulyConfigConverter converter;

    /**
     * Create the controller.
     *
     * @param useCase   july config use case
     * @param converter response converter
     */
    public JulyConfigController(JulyConfigUseCase useCase, JulyConfigConverter converter) {
        this.useCase = useCase;
        this.converter = converter;
    }

    /**
     * Insert a new config entry.
     *
     * @param vo insert request
     * @return envelope with the new config id
     */
    @PostMapping("/insert")
    @Operation(summary = "新增配置（code 查重）")
    public Response011<IdVo011> insert(@RequestBody JulyConfigUpsertVo vo) {
        String funcName = "insert";

        return Response011.successId(funcName, useCase.insert(vo.getCode(), vo.getData()));
    }

    /**
     * Update the value of a config entry (code immutable).
     *
     * @param vo update request
     * @return envelope with the config id
     */
    @PostMapping("/update")
    @Operation(summary = "修改配置值（code 不可变）")
    public Response011<IdVo011> update(@RequestBody JulyConfigUpdateVo011 vo) {
        String funcName = "update";

        useCase.update(vo.getId(), vo.getData());

        return Response011.successId(funcName, vo.getId());
    }

    /**
     * Logic delete a config entry.
     *
     * @param idVo request with the config id
     * @return envelope with the config id
     */
    @PostMapping("/logicDelete")
    @Operation(summary = "逻辑删除")
    public Response011<IdVo011> logicDelete(@RequestBody IdVo011 idVo) {
        String funcName = "logic delete";

        useCase.logicDelete(idVo.getId());

        return Response011.successId(funcName, idVo.getId());
    }

    /**
     * Find a config entry by primary key.
     *
     * @param idVo request with the config id
     * @return config detail
     */
    @PostMapping("/getById")
    @Operation(summary = "主键查询")
    public Response011<JulyConfigVo011> getById(@RequestBody IdVo011 idVo) {
        String funcName = "get by id";

        return Response011.success(funcName, converter.toVo(useCase.getById(idVo.getId())));
    }

    /**
     * Page query with an optional keyword filter.
     *
     * @param vo page query request
     * @return page result of config rows
     */
    @PostMapping("/selectListByPage")
    @Operation(summary = "分页查询（code/data 模糊过滤）")
    public Response011<PageResult011<JulyConfigVo011>> selectListByPage(@RequestBody JulyConfigQueryVo011 vo) {
        String funcName = "select list by page";

        PageQuery011 pageQuery = new PageQuery011(vo.getPageIndex(), vo.getPageSize());
        PageResult011<JulyConfig> page = useCase.selectListByPage(pageQuery, vo.getKeyword());

        return Response011.success(funcName, page.withRows(converter.toVoList(page.rows())));
    }

    /**
     * Config upsert request VO (insert shares it; update uses UpdateVo011
     * with the id).
     */
    @Data
    public static class JulyConfigUpsertVo {

        /** Config key, unique, max 60, immutable after create. */
        @Schema(description = "配置项（唯一，最长 60，创建后不可修改）", requiredMode = Schema.RequiredMode.REQUIRED)
        private String code;

        /** Config value, max 300. */
        @Schema(description = "配置值（最长 300）", requiredMode = Schema.RequiredMode.REQUIRED)
        private String data;
    }
}
