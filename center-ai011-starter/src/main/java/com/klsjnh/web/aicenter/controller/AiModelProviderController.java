package com.klsjnh.web.aicenter.controller;

/*                AiModelProviderController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  ai model provider controller class
 *      2026.09.26  pass operator into use case for permission checks
 *
 */

import com.klsjnh.common.constant.AuditObjectCodes011;
import com.klsjnh.common.enums.AuditType011;
import com.klsjnh.common.identity.Operator011;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.response.Response011;
import com.klsjnh.common.vo.IdVo011;

import com.klsjnh.application.aicenter.modelprovider.AiModelProviderApiCommand;
import com.klsjnh.application.aicenter.modelprovider.AiModelProviderUseCase;
import com.klsjnh.domain.aicenter.modelprovider.AiModelProvider;
import com.klsjnh.domain.aicenter.modelprovider.AiModelProviderApi;
import com.klsjnh.domain.aicenter.modelprovider.AiModelProviderQuerySpec;
import com.klsjnh.domain.platform011.export.ExportResult;

import com.klsjnh.web.aicenter.converter.AiModelProviderConverter;

import com.klsjnh.web.global.audit.AuditLog;
import com.klsjnh.web.aicenter.vo.aimodelprovider.AiModelProviderApiInsertVo011;
import com.klsjnh.web.aicenter.vo.aimodelprovider.AiModelProviderApiQueryVo011;
import com.klsjnh.web.aicenter.vo.aimodelprovider.AiModelProviderApiUpdateVo011;
import com.klsjnh.web.aicenter.vo.aimodelprovider.AiModelProviderApiVo011;
import com.klsjnh.web.aicenter.vo.aimodelprovider.AiModelProviderInsertVo011;
import com.klsjnh.web.aicenter.vo.aimodelprovider.AiModelProviderQueryVo011;
import com.klsjnh.web.aicenter.vo.aimodelprovider.AiModelProviderSaveWholeVo011;
import com.klsjnh.web.aicenter.vo.aimodelprovider.AiModelProviderTestResultVo011;
import com.klsjnh.web.aicenter.vo.aimodelprovider.AiModelProviderTestVo011;
import com.klsjnh.web.aicenter.vo.aimodelprovider.AiModelProviderUpdateVo011;
import com.klsjnh.web.aicenter.vo.aimodelprovider.AiModelProviderVo011;
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AiModelProvider HTTP adapter: provider and api key management, plus the
 * provider and api level connectivity probes. The api key is never echoed back.
 */

@Tag(name = "AI中心011 - 提供商管理")
@RestController
@RequestMapping("/klsjnh/aicenter/julyAiModelProvider/v1")
public class AiModelProviderController {


    /**
     * AiModelProvider use case.
     */
    private final AiModelProviderUseCase aiModelProviderUseCase;

    /**
     * Response aiModelProviderConverter.
     */
    private final AiModelProviderConverter aiModelProviderConverter;

    /**
     * Create the controller.
     *
     * @param aiModelProviderUseCase   ai model provider use case
     * @param aiModelProviderConverter response aiModelProviderConverter
     */
    public AiModelProviderController(AiModelProviderUseCase aiModelProviderUseCase,
            AiModelProviderConverter aiModelProviderConverter) {
        this.aiModelProviderUseCase = aiModelProviderUseCase;
        this.aiModelProviderConverter = aiModelProviderConverter;
    }

    /**
     * Insert a new provider.
     *
     * @param vo insert request
     * @return envelope with the new provider id
     */
    @AuditLog(type = AuditType011.INSERT, objectCode = AuditObjectCodes011.JULY_AI_MODEL_PROVIDER)
    @PostMapping("/insert")
    @Operation(summary = "新增提供商（providerCode 查重）")
    public Response011<IdVo011> insert(@RequestBody AiModelProviderInsertVo011 vo, HttpServletRequest request) {
        String funcName = "insert";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.successId(funcName, aiModelProviderUseCase.insert(operator.id(), vo.getProviderCode(), vo.getSortOrder(),
                vo.getProviderName(), vo.getBaseUrl(), vo.getModels(), vo.getRemark()));
    }

    /**
     * Update a provider.
     *
     * @param vo update request
     * @return envelope with the provider id
     */
    @AuditLog(type = AuditType011.UPDATE, objectCode = AuditObjectCodes011.JULY_AI_MODEL_PROVIDER)
    @PostMapping("/update")
    @Operation(summary = "修改提供商（providerCode 不可变）")
    public Response011<IdVo011> update(@RequestBody AiModelProviderUpdateVo011 vo, HttpServletRequest request) {
        String funcName = "update";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.successId(funcName, aiModelProviderUseCase.update(operator.id(), vo.getId(), vo.getProviderName(), vo.getSortOrder(),
                vo.getBaseUrl(), vo.getModels(), vo.getStatus(), vo.getRemark()));
    }

    /**
     * Logic delete a provider (refused while it has api keys).
     *
     * @param idVo request with the provider id
     * @return envelope with the deleted provider id
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = AuditObjectCodes011.JULY_AI_MODEL_PROVIDER)
    @PostMapping("/logicDelete")
    @Operation(summary = "逻辑删除提供商（仍有密钥则拒绝）")
    public Response011<IdVo011> logicDelete(@RequestBody IdVo011 idVo, HttpServletRequest request) {
        String funcName = "logic delete";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.successId(funcName, aiModelProviderUseCase.logicDelete(operator.id(), idVo.getId()));
    }

    /**
     * Find a provider by primary key (safe + idempotent, hence GET).
     *
     * @param id provider id, passed as a query parameter
     * @return provider detail
     */
    @GetMapping("/getById")
    @Operation(summary = "主键查询（id 走 query）")
    public Response011<AiModelProviderVo011> getById(@RequestParam("id") String id, HttpServletRequest request) {
        String funcName = "get by id";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.success(funcName, aiModelProviderConverter.toVo(aiModelProviderUseCase.getById(operator.id(), id)));
    }

    /**
     * Page query with optional keyword / status filters.
     *
     * @param vo page query request
     * @return page result of providers
     */
    @PostMapping("/selectListByPage")
    @Operation(summary = "分页查询（编码/名称/URL 模糊 + 状态过滤）")
    public Response011<PageResult011<AiModelProviderVo011>> selectListByPage(
            @RequestBody AiModelProviderQueryVo011 vo, HttpServletRequest request) {
        String funcName = "select list by page";
        Operator011 operator = Operator011Resolver.resolve(request);


        PageQuery011 pageQuery = new PageQuery011(vo.getPageIndex(), vo.getPageSize());
        PageResult011<AiModelProvider> page = aiModelProviderUseCase.selectListByPage(operator.id(), pageQuery,
                new AiModelProviderQuerySpec(vo.getKeyword(), vo.getStatus()));

        return Response011.success(funcName, page.withRows(aiModelProviderConverter.toVoList(page.rows())));
    }

    /**
     * Insert a new api key under a provider.
     *
     * @param vo insert request
     * @return envelope with the new api key id
     */
    @AuditLog(type = AuditType011.INSERT, objectCode = AuditObjectCodes011.JULY_AI_MODEL_PROVIDER)
    @PostMapping("/insertApi")
    @Operation(summary = "新增密钥（同提供商内 apiCode 查重）")
    public Response011<IdVo011> insertApi(@RequestBody AiModelProviderApiInsertVo011 vo, HttpServletRequest request) {
        String funcName = "insert api";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.successId(funcName, aiModelProviderUseCase.insertApi(operator.id(), vo.getProviderCode(), vo.getSortOrder(),
                vo.getApiCode(), vo.getApiName(), vo.getApiKey(), vo.getRemark()));
    }

    /**
     * Update an api key (a blank apiKey keeps the stored one).
     *
     * @param vo update request
     * @return envelope with the api key id
     */
    @AuditLog(type = AuditType011.UPDATE, objectCode = AuditObjectCodes011.JULY_AI_MODEL_PROVIDER)
    @PostMapping("/updateApi")
    @Operation(summary = "修改密钥（apiCode 不可变；apiKey 留空保持原值）")
    public Response011<IdVo011> updateApi(@RequestBody AiModelProviderApiUpdateVo011 vo, HttpServletRequest request) {
        String funcName = "update api";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.successId(funcName, aiModelProviderUseCase.updateApi(operator.id(), vo.getId(), vo.getApiName(), vo.getSortOrder(),
                vo.getApiKey(), vo.getStatus(), vo.getRemark()));
    }

    /**
     * Logic delete an api key.
     *
     * @param idVo request with the api key id
     * @return envelope with the deleted api key id
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = AuditObjectCodes011.JULY_AI_MODEL_PROVIDER)
    @PostMapping("/logicDeleteApi")
    @Operation(summary = "逻辑删除密钥（单个）")
    public Response011<IdVo011> logicDeleteApi(@RequestBody IdVo011 idVo, HttpServletRequest request) {
        String funcName = "logic delete api";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.successId(funcName, aiModelProviderUseCase.logicDeleteApi(operator.id(), idVo.getId()));
    }

    /**
     * List the api keys of a provider (no key content).
     *
     * @param vo request with provider code and optional status
     * @return ordered api key list
     */
    @PostMapping("/selectApiListByProvider")
    @Operation(summary = "按提供商取密钥列表（有序，出参不含密钥）")
    public Response011<List<AiModelProviderApiVo011>> selectApiListByProvider(
            @RequestBody AiModelProviderApiQueryVo011 vo, HttpServletRequest request) {
        String funcName = "select api list by provider";
        Operator011 operator = Operator011Resolver.resolve(request);


        List<AiModelProviderApi> apis = aiModelProviderUseCase.selectApiListByProvider(operator.id(), vo.getProviderCode(), vo.getStatus());

        return Response011.success(funcName, aiModelProviderConverter.toApiVoList(apis));
    }

    /**
     * Probe a provider with its default enabled api key.
     *
     * @param vo request with provider code
     * @return probe result
     */
    @PostMapping("/testConnection")
    @Operation(summary = "测试连接（提供商级，用默认密钥 GET baseUrl/models）")
    public Response011<AiModelProviderTestResultVo011> testConnection(@RequestBody AiModelProviderTestVo011 vo, HttpServletRequest request) {
        String funcName = "test connection";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.success(funcName, aiModelProviderConverter.toTestResultVo(aiModelProviderUseCase.testConnection(operator.id(), vo.getProviderCode())));
    }

    /**
     * Probe a specific api key (sub-table level).
     *
     * @param idVo request with the api key id
     * @return probe result
     */
    @PostMapping("/testConnectionApi")
    @Operation(summary = "测试连接（密钥级，指定 apiKey 记录）")
    public Response011<AiModelProviderTestResultVo011> testConnectionApi(@RequestBody IdVo011 idVo, HttpServletRequest request) {
        String funcName = "test connection api";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.success(funcName,
                aiModelProviderConverter.toTestResultVo(aiModelProviderUseCase.testConnectionApi(operator.id(), idVo.getId())));
    }

    /**
     * Whole save: provider + api keys in one transaction (children replaced).
     *
     * @param vo request
     * @return envelope with the provider id
     */
    @PostMapping("/saveWhole")
    @Operation(summary = "整存提供商 + 密钥（主+子，一个事务；子表替换）")
    public Response011<IdVo011> saveWhole(@RequestBody AiModelProviderSaveWholeVo011 vo, HttpServletRequest request) {
        String funcName = "save whole";
        Operator011 operator = Operator011Resolver.resolve(request);


        List<AiModelProviderApiCommand> apis = new ArrayList<>();

        for (AiModelProviderApiInsertVo011 row : vo.getApis()) {
            apis.add(new AiModelProviderApiCommand(row.getApiCode(), row.getApiName(), row.getApiKey(),
                    row.getSortOrder(), row.getRemark()));
        }

        return Response011.successId(funcName, aiModelProviderUseCase.saveWhole(operator.id(), vo.getId(), vo.getProviderCode(),
                vo.getSortOrder(), vo.getProviderName(), vo.getBaseUrl(), vo.getModels(), vo.getStatus(),
                vo.getRemark(), apis));
    }

    /**
     * Read a provider together with its api keys (master + children).
     *
     * @param id provider id
     * @return envelope with the provider and its api list
     */
    @GetMapping("/getWithChildren")
    @Operation(summary = "主+子联查（提供商 + 密钥列表）")
    public Response011<Map<String, Object>> getWithChildren(@RequestParam("id") String id, HttpServletRequest request) {
        String funcName = "get with children";
        Operator011 operator = Operator011Resolver.resolve(request);


        AiModelProvider provider = aiModelProviderUseCase.getById(operator.id(), id);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("master", aiModelProviderConverter.toVo(provider));
        result.put("apis", aiModelProviderConverter.toApiVoList(
                aiModelProviderUseCase.selectApiListByProvider(operator.id(), provider.providerCode(), null)));

        return Response011.success(funcName, result);
    }

    /**
     * Export every provider row in batches and return the whole result in the
     * JSON envelope (api keys are never exported).
     *
     * @param request http request (operator from the auth filter)
     * @return envelope with the export result
     */
    @PostMapping("/export")
    @Operation(summary = "导出全部提供商（不含密钥，分批取数，写 EXPORT 审计）")
    public Response011<ExportResult> export(HttpServletRequest request) {
        String funcName = "export";

        Operator011 operator = Operator011Resolver.resolve(request);

        return Response011.success(funcName, aiModelProviderUseCase.export(operator));
    }
}
