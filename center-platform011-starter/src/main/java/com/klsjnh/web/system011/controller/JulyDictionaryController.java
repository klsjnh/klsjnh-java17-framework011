package com.klsjnh.web.system011.controller;

/*                JulyDictionaryController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july dictionary controller class
 *      2026.09.22  xlsx export + import endpoints
 *      2026.09.26  pass operator into use case for permission checks
 *
 */

import com.klsjnh.common.constant.AuditObjectCodes011;
import com.klsjnh.common.enums.AuditType011;
import com.klsjnh.common.enums.ExportFormat011;
import com.klsjnh.common.identity.Operator011;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.response.Response011;
import com.klsjnh.common.vo.IdVo011;

import com.klsjnh.application.system011.dictionary.JulyDictionaryUseCase;
import com.klsjnh.domain.platform011.importdata.ImportResult;
import com.klsjnh.domain.system011.dictionary.JulyDictionary;
import com.klsjnh.domain.system011.dictionary.JulyDictionaryItem;
import com.klsjnh.domain.system011.dictionary.JulyDictionaryQuerySpec;

import com.klsjnh.web.system011.converter.JulyDictionaryConverter;

import com.klsjnh.web.global.audit.AuditLog;
import com.klsjnh.web.system011.vo.julydictionary.JulyDictionaryInsertVo011;
import com.klsjnh.web.system011.vo.julydictionary.JulyDictionaryItemInsertVo011;
import com.klsjnh.web.system011.vo.julydictionary.JulyDictionaryItemQueryVo011;
import com.klsjnh.web.system011.vo.julydictionary.JulyDictionaryItemUpdateVo011;
import com.klsjnh.web.system011.vo.julydictionary.JulyDictionaryItemVo011;
import com.klsjnh.web.system011.vo.julydictionary.JulyDictionaryQueryVo011;
import com.klsjnh.web.system011.vo.julydictionary.JulyDictionaryUpdateVo011;
import com.klsjnh.web.system011.vo.julydictionary.JulyDictionaryVo011;
import com.klsjnh.web.util.Operator011Resolver;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.List;

/**
 * JulyDictionary HTTP adapter: dictionary and item management. A
 * program-facing read entry (getByType / getLabel / isValidItem) lives in the
 * use case and has no HTTP endpoint. Permission checks live in the use case
 * (operator resolved here).
 */

@Tag(name = "系统管理 - 数据字典")
@RestController
@RequestMapping("/klsjnh/system011/julyDictionary/v1")
public class JulyDictionaryController {


    /**
     * JulyDictionary use case.
     */
    private final JulyDictionaryUseCase julyDictionaryUseCase;

    /**
     * Response julyDictionaryConverter.
     */
    private final JulyDictionaryConverter julyDictionaryConverter;

    /**
     * Create the controller.
     *
     * @param julyDictionaryUseCase   july dictionary use case
     * @param julyDictionaryConverter response converter
     */
    public JulyDictionaryController(JulyDictionaryUseCase julyDictionaryUseCase,
            JulyDictionaryConverter julyDictionaryConverter) {
        this.julyDictionaryUseCase = julyDictionaryUseCase;
        this.julyDictionaryConverter = julyDictionaryConverter;
    }

    /**
     * Insert a new dictionary type.
     *
     * @param vo      insert request
     * @param request http request
     * @return envelope with the new dictionary id
     */
    @AuditLog(type = AuditType011.INSERT, objectCode = AuditObjectCodes011.JULY_DICTIONARY)
    @PostMapping("/insert")
    @Operation(summary = "新增字典（dictionaryCode 查重）")
    public Response011<IdVo011> insert(@Valid @RequestBody JulyDictionaryInsertVo011 vo, HttpServletRequest request) {
        String funcName = "insert";
        Operator011 operator = Operator011Resolver.resolve(request);

        return Response011.successId(funcName, julyDictionaryUseCase.insert(operator.id(), vo.getDictionaryCode(),
                vo.getSortOrder(), vo.getDictionaryName(), vo.getStatus(), vo.getRemark()));
    }

    /**
     * Update a dictionary type.
     *
     * @param vo      update request
     * @param request http request
     * @return envelope with the dictionary id
     */
    @AuditLog(type = AuditType011.UPDATE, objectCode = AuditObjectCodes011.JULY_DICTIONARY)
    @PostMapping("/update")
    @Operation(summary = "修改字典（dictionaryCode 不可变）")
    public Response011<IdVo011> update(@Valid @RequestBody JulyDictionaryUpdateVo011 vo, HttpServletRequest request) {
        String funcName = "update";
        Operator011 operator = Operator011Resolver.resolve(request);

        return Response011.successId(funcName, julyDictionaryUseCase.update(operator.id(), vo.getId(),
                vo.getDictionaryName(), vo.getSortOrder(), vo.getStatus(), vo.getRemark()));
    }

    /**
     * Logic delete a dictionary (refused while it has items).
     *
     * @param idVo    request with the dictionary id
     * @param request http request
     * @return envelope with the deleted dictionary id
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = AuditObjectCodes011.JULY_DICTIONARY)
    @PostMapping("/logicDelete")
    @Operation(summary = "逻辑删除字典（仍有字典项则拒绝）")
    public Response011<IdVo011> logicDelete(@Valid @RequestBody IdVo011 idVo, HttpServletRequest request) {
        String funcName = "logic delete";
        Operator011 operator = Operator011Resolver.resolve(request);

        return Response011.successId(funcName, julyDictionaryUseCase.logicDelete(operator.id(), idVo.getId()));
    }

    /**
     * Find a dictionary by primary key together with its items (GET).
     *
     * @param id      dictionary id, passed as a query parameter
     * @param request http request
     * @return dictionary detail with items
     */
    @GetMapping("/getById")
    @Operation(summary = "主键查询（id 走 query，含字典项）")
    public Response011<JulyDictionaryVo011> getById(@RequestParam("id") String id, HttpServletRequest request) {
        String funcName = "get by id";
        Operator011 operator = Operator011Resolver.resolve(request);

        return Response011.success(funcName,
                julyDictionaryConverter.toVo(julyDictionaryUseCase.getByIdWithItems(operator.id(), id)));
    }

    /**
     * Find a dictionary by code together with its items (GET).
     *
     * @param code dictionary code, passed as a query parameter
     * @return dictionary detail with items
     */
    @GetMapping("/getByCode")
    @Operation(summary = "按 dictionaryCode 点查（code 走 query，含字典项）")
    public Response011<JulyDictionaryVo011> getByCode(@RequestParam("code") String code) {
        String funcName = "get by code";

        return Response011.success(funcName, julyDictionaryConverter.toVo(julyDictionaryUseCase.getByCodeWithItems(code)));
    }

    /**
     * Page query with optional keyword / status filters.
     *
     * @param vo      page query request
     * @param request http request
     * @return page result of dictionary types
     */
    @PostMapping("/selectListByPage")
    @Operation(summary = "分页查询（编码/名称 模糊 + 状态过滤）")
    public Response011<PageResult011<JulyDictionaryVo011>> selectListByPage(
            @Valid @RequestBody JulyDictionaryQueryVo011 vo, HttpServletRequest request) {
        String funcName = "select list by page";
        Operator011 operator = Operator011Resolver.resolve(request);

        PageQuery011 pageQuery = new PageQuery011(vo.getPageIndex(), vo.getPageSize());
        PageResult011<JulyDictionary> page = julyDictionaryUseCase.selectListByPage(operator.id(), pageQuery,
                new JulyDictionaryQuerySpec(vo.getKeyword(), vo.getStatus()));

        return Response011.success(funcName, page.withRows(julyDictionaryConverter.toVoList(page.rows())));
    }

    /**
     * Insert a new item under a dictionary.
     *
     * @param vo      insert request
     * @param request http request
     * @return envelope with the new item id
     */
    @AuditLog(type = AuditType011.INSERT, objectCode = AuditObjectCodes011.JULY_DICTIONARY)
    @PostMapping("/insertItem")
    @Operation(summary = "新增字典项（同字典内 itemCode 查重）")
    public Response011<IdVo011> insertItem(@Valid @RequestBody JulyDictionaryItemInsertVo011 vo, HttpServletRequest request) {
        String funcName = "insert item";
        Operator011 operator = Operator011Resolver.resolve(request);

        return Response011.successId(funcName, julyDictionaryUseCase.insertItem(operator.id(), vo.getDictionaryCode(),
                vo.getSortOrder(), vo.getItemCode(), vo.getItemLabel(), vo.getStatus(), vo.getRemark()));
    }

    /**
     * Update an item.
     *
     * @param vo      update request
     * @param request http request
     * @return envelope with the item id
     */
    @AuditLog(type = AuditType011.UPDATE, objectCode = AuditObjectCodes011.JULY_DICTIONARY)
    @PostMapping("/updateItem")
    @Operation(summary = "修改字典项（itemCode 不可变）")
    public Response011<IdVo011> updateItem(@Valid @RequestBody JulyDictionaryItemUpdateVo011 vo, HttpServletRequest request) {
        String funcName = "update item";
        Operator011 operator = Operator011Resolver.resolve(request);

        return Response011.successId(funcName, julyDictionaryUseCase.updateItem(operator.id(), vo.getId(),
                vo.getItemLabel(), vo.getSortOrder(), vo.getStatus(), vo.getRemark()));
    }

    /**
     * Logic delete an item.
     *
     * @param idVo    request with the item id
     * @param request http request
     * @return envelope with the deleted item id
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = AuditObjectCodes011.JULY_DICTIONARY)
    @PostMapping("/logicDeleteItem")
    @Operation(summary = "逻辑删除字典项（单个）")
    public Response011<IdVo011> logicDeleteItem(@Valid @RequestBody IdVo011 idVo, HttpServletRequest request) {
        String funcName = "logic delete item";
        Operator011 operator = Operator011Resolver.resolve(request);

        return Response011.successId(funcName, julyDictionaryUseCase.logicDeleteItem(operator.id(), idVo.getId()));
    }

    /**
     * List the items of a dictionary.
     *
     * @param vo      request with dictionary code and optional status
     * @param request http request
     * @return ordered item list
     */
    @PostMapping("/selectItemListByType")
    @Operation(summary = "按字典取字典项列表（有序）")
    public Response011<List<JulyDictionaryItemVo011>> selectItemListByType(
            @Valid @RequestBody JulyDictionaryItemQueryVo011 vo, HttpServletRequest request) {
        String funcName = "select item list by type";
        Operator011 operator = Operator011Resolver.resolve(request);

        List<JulyDictionaryItem> items = julyDictionaryUseCase.selectItemListByType(operator.id(),
                vo.getDictionaryCode(), vo.getStatus());

        return Response011.success(funcName, julyDictionaryConverter.toItemVoList(items));
    }

    /**
     * Export dictionaries. Default returns a JSON envelope (master rows). Pass
     * {@code format=xlsx} for a binary workbook (master + children sheets).
     *
     * @param format  optional format (json default / xlsx)
     * @param request http request
     * @return envelope or xlsx bytes
     */
    @PostMapping("/export")
    @Operation(summary = "导出字典（默认 JSON 信封；format=xlsx 为两 sheet 二进制）")
    public Object export(@RequestParam(value = "format", required = false) String format,
            HttpServletRequest request) {
        String funcName = "export";
        Operator011 operator = Operator011Resolver.resolve(request);
        ExportFormat011 exportFormat = ExportFormat011.of(format);

        if (exportFormat == ExportFormat011.XLSX) {
            byte[] bytes = julyDictionaryUseCase.exportXlsx(operator);
            String filename = "julyDictionary.xlsx";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(bytes);
        }

        return Response011.success(funcName, julyDictionaryUseCase.export(operator));
    }

    /**
     * Import an xlsx workbook (master + children). Upserts by dictionaryCode;
     * replaces items for codes present on the master sheet.
     *
     * @param file    multipart xlsx file
     * @param request http request
     * @return import result envelope
     */
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "导入字典 xlsx（upsert 主表；文件中出现的 code Replace 明细）")
    public Response011<ImportResult> importXlsx(@RequestParam("file") MultipartFile file, HttpServletRequest request)
            throws Exception {
        String funcName = "import";
        Operator011 operator = Operator011Resolver.resolve(request);
        byte[] bytes = file == null ? null : file.getBytes();

        return Response011.success(funcName, julyDictionaryUseCase.importXlsx(bytes, operator));
    }
}
