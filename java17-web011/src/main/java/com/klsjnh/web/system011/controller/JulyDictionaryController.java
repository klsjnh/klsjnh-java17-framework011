package com.klsjnh.web.system011.controller;

/*                JulyDictionaryController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july dictionary controller class
 *
 */

import com.klsjnh.common.constant.AuditObjectCodes011;
import com.klsjnh.common.enums.AuditType011;
import com.klsjnh.common.identity.Operator011;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.response.Response011;
import com.klsjnh.common.vo.IdVo011;

import com.klsjnh.application.platform011.export.ExportUseCase;
import com.klsjnh.application.system011.dictionary.JulyDictionaryUseCase;
import com.klsjnh.domain.platform011.export.ExportResult;
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

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * JulyDictionary HTTP adapter: dictionary and item management. A
 * program-facing read entry (getByType / getLabel / isValidItem) lives in the
 * use case and has no HTTP endpoint.
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
     * Export use case.
     */
    private final ExportUseCase exportUseCase;

    /**
     * Create the controller.
     *
     * @param julyDictionaryUseCase       july dictionary use case
     * @param julyDictionaryConverter     response julyDictionaryConverter
     * @param exportUseCase export use case
     */
    public JulyDictionaryController(JulyDictionaryUseCase julyDictionaryUseCase, JulyDictionaryConverter julyDictionaryConverter,
            ExportUseCase exportUseCase) {
        this.julyDictionaryUseCase = julyDictionaryUseCase;
        this.julyDictionaryConverter = julyDictionaryConverter;
        this.exportUseCase = exportUseCase;
    }

    /**
     * Insert a new dictionary type.
     *
     * @param vo insert request
     * @return envelope with the new dictionary id
     */
    @AuditLog(type = AuditType011.INSERT, objectCode = AuditObjectCodes011.JULY_DICTIONARY)
    @PostMapping("/insert")
    @Operation(summary = "新增字典（dictionaryCode 查重）")
    public Response011<IdVo011> insert(@RequestBody JulyDictionaryInsertVo011 vo) {
        String funcName = "insert";

        return Response011.successId(funcName, julyDictionaryUseCase.insert(vo.getDictionaryCode(), vo.getSortOrder(),
                vo.getDictionaryName(), vo.getStatus(), vo.getRemark()));
    }

    /**
     * Update a dictionary type.
     *
     * @param vo update request
     * @return envelope with the dictionary id
     */
    @AuditLog(type = AuditType011.UPDATE, objectCode = AuditObjectCodes011.JULY_DICTIONARY)
    @PostMapping("/update")
    @Operation(summary = "修改字典（dictionaryCode 不可变）")
    public Response011<IdVo011> update(@RequestBody JulyDictionaryUpdateVo011 vo) {
        String funcName = "update";

        return Response011.successId(funcName, julyDictionaryUseCase.update(vo.getId(), vo.getDictionaryName(), vo.getSortOrder(),
                vo.getStatus(), vo.getRemark()));
    }

    /**
     * Logic delete a dictionary (refused while it has items).
     *
     * @param idVo request with the dictionary id
     * @return envelope with the deleted dictionary id
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = AuditObjectCodes011.JULY_DICTIONARY)
    @PostMapping("/logicDelete")
    @Operation(summary = "逻辑删除字典（仍有字典项则拒绝）")
    public Response011<IdVo011> logicDelete(@RequestBody IdVo011 idVo) {
        String funcName = "logic delete";

        return Response011.successId(funcName, julyDictionaryUseCase.logicDelete(idVo.getId()));
    }

    /**
     * Find a dictionary by primary key together with its items (GET).
     *
     * @param id dictionary id, passed as a query parameter
     * @return dictionary detail with items
     */
    @GetMapping("/getById")
    @Operation(summary = "主键查询（id 走 query，含字典项）")
    public Response011<JulyDictionaryVo011> getById(@RequestParam("id") String id) {
        String funcName = "get by id";

        return Response011.success(funcName, julyDictionaryConverter.toVo(julyDictionaryUseCase.getByIdWithItems(id)));
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
     * @param vo page query request
     * @return page result of dictionary types
     */
    @PostMapping("/selectListByPage")
    @Operation(summary = "分页查询（编码/名称 模糊 + 状态过滤）")
    public Response011<PageResult011<JulyDictionaryVo011>> selectListByPage(
            @RequestBody JulyDictionaryQueryVo011 vo) {
        String funcName = "select list by page";

        PageQuery011 pageQuery = new PageQuery011(vo.getPageIndex(), vo.getPageSize());
        PageResult011<JulyDictionary> page = julyDictionaryUseCase.selectListByPage(pageQuery,
                new JulyDictionaryQuerySpec(vo.getKeyword(), vo.getStatus()));

        return Response011.success(funcName, page.withRows(julyDictionaryConverter.toVoList(page.rows())));
    }

    /**
     * Insert a new item under a dictionary.
     *
     * @param vo insert request
     * @return envelope with the new item id
     */
    @AuditLog(type = AuditType011.INSERT, objectCode = AuditObjectCodes011.JULY_DICTIONARY)
    @PostMapping("/insertItem")
    @Operation(summary = "新增字典项（同字典内 itemCode 查重）")
    public Response011<IdVo011> insertItem(@RequestBody JulyDictionaryItemInsertVo011 vo) {
        String funcName = "insert item";

        return Response011.successId(funcName, julyDictionaryUseCase.insertItem(vo.getDictionaryCode(), vo.getSortOrder(),
                vo.getItemCode(), vo.getItemLabel(), vo.getStatus(), vo.getRemark()));
    }

    /**
     * Update an item.
     *
     * @param vo update request
     * @return envelope with the item id
     */
    @AuditLog(type = AuditType011.UPDATE, objectCode = AuditObjectCodes011.JULY_DICTIONARY)
    @PostMapping("/updateItem")
    @Operation(summary = "修改字典项（itemCode 不可变）")
    public Response011<IdVo011> updateItem(@RequestBody JulyDictionaryItemUpdateVo011 vo) {
        String funcName = "update item";

        return Response011.successId(funcName, julyDictionaryUseCase.updateItem(vo.getId(), vo.getItemLabel(), vo.getSortOrder(),
                vo.getStatus(), vo.getRemark()));
    }

    /**
     * Logic delete an item.
     *
     * @param idVo request with the item id
     * @return envelope with the deleted item id
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = AuditObjectCodes011.JULY_DICTIONARY)
    @PostMapping("/logicDeleteItem")
    @Operation(summary = "逻辑删除字典项（单个）")
    public Response011<IdVo011> logicDeleteItem(@RequestBody IdVo011 idVo) {
        String funcName = "logic delete item";

        return Response011.successId(funcName, julyDictionaryUseCase.logicDeleteItem(idVo.getId()));
    }

    /**
     * List the items of a dictionary.
     *
     * @param vo request with dictionary code and optional status
     * @return ordered item list
     */
    @PostMapping("/selectItemListByType")
    @Operation(summary = "按字典取字典项列表（有序）")
    public Response011<List<JulyDictionaryItemVo011>> selectItemListByType(
            @RequestBody JulyDictionaryItemQueryVo011 vo) {
        String funcName = "select item list by type";

        List<JulyDictionaryItem> items = julyDictionaryUseCase.selectItemListByType(vo.getDictionaryCode(), vo.getStatus());

        return Response011.success(funcName, julyDictionaryConverter.toItemVoList(items));
    }

    /**
     * Export every dictionary row in batches and return the whole result in the
     * JSON envelope.
     *
     * @param request http request (operator from the auth filter)
     * @return envelope with the export result
     */
    @PostMapping("/export")
    @Operation(summary = "导出全部字典（分批取数，写 EXPORT 审计）")
    public Response011<ExportResult> export(HttpServletRequest request) {
        String funcName = "export";

        Operator011 operator = Operator011Resolver.resolve(request);

        return Response011.success(funcName, exportUseCase.export(AuditObjectCodes011.JULY_DICTIONARY, operator));
    }
}
