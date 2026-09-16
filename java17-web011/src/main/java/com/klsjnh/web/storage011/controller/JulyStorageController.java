package com.klsjnh.web.storage011.controller;

/*                JulyStorageController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july storage controller class
 *
 */

import com.klsjnh.common.enums.AuditType011;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.response.Response011;
import com.klsjnh.common.vo.BatchDeleteResultVo011;
import com.klsjnh.common.vo.IdVo011;
import com.klsjnh.common.vo.IdsVo011;

import com.klsjnh.application.storage011.JulyStorageUseCase;
import com.klsjnh.application.storage011.StorageProbeResult;
import com.klsjnh.domain.storage.JulyStorage;
import com.klsjnh.domain.storage.JulyStorageQuerySpec;

import com.klsjnh.web.storage011.converter.JulyStorageConverter;

import com.klsjnh.web.global.audit.AuditLog;
import com.klsjnh.web.storage011.vo.JulyStorageConnectVo011;
import com.klsjnh.web.storage011.vo.JulyStorageQueryVo011;
import com.klsjnh.web.storage011.vo.JulyStorageSaveVo011;
import com.klsjnh.web.storage011.vo.JulyStorageVo011;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * JulyStorage HTTP adapter: storage instance management (table-driven instances)
 * plus the connectivity probe.
 */

@Tag(name = "存储中心 - 存储实例")
@RestController
@RequestMapping("/klsjnh/storage011/storage")
public class JulyStorageController {

    /**
     * JulyStorage use case.
     */
    private final JulyStorageUseCase useCase;

    /**
     * Response converter.
     */
    private final JulyStorageConverter converter;

    /**
     * Create the controller.
     *
     * @param useCase   july storage use case
     * @param converter response converter
     */
    public JulyStorageController(JulyStorageUseCase useCase, JulyStorageConverter converter) {
        this.useCase = useCase;
        this.converter = converter;
    }

    /**
     * Insert a new storage instance.
     *
     * @param vo save request
     * @return envelope with the new storage id
     */
    @AuditLog(type = AuditType011.INSERT, objectCode = "julyStorage")
    @PostMapping("/insert")
    @Operation(summary = "新增存储实例（storageCode 查重）")
    public Response011<IdVo011> insert(@RequestBody JulyStorageSaveVo011 vo) {
        String funcName = "insert";

        return Response011.successId(funcName, useCase.insert(vo.getStorageCode(), vo.getSortOrder(),
                vo.getStorageName(), vo.getProvider(), vo.getBasePath(), vo.getEndpoint(), vo.getAccessKey(),
                vo.getSecretKey(), Boolean.TRUE.equals(vo.getSecure()), vo.getDefaultBucket(),
                vo.getPresignExpirySeconds(), vo.getRemark()));
    }

    /**
     * Update a storage instance (blank secretKey keeps the stored one).
     *
     * @param vo save request
     * @return envelope with the storage id
     */
    @AuditLog(type = AuditType011.UPDATE, objectCode = "julyStorage")
    @PostMapping("/update")
    @Operation(summary = "修改存储实例（storageCode 不可变；secretKey 留空保持）")
    public Response011<IdVo011> update(@RequestBody JulyStorageSaveVo011 vo) {
        String funcName = "update";

        return Response011.successId(funcName, useCase.update(vo.getId(), vo.getStorageName(), vo.getProvider(),
                vo.getBasePath(), vo.getEndpoint(), vo.getAccessKey(), vo.getSecretKey(),
                Boolean.TRUE.equals(vo.getSecure()), vo.getDefaultBucket(), vo.getPresignExpirySeconds(),
                vo.getRemark(), vo.getStatus()));
    }

    /**
     * Logic delete a storage instance.
     *
     * @param idVo request with the storage id
     * @return envelope with the deleted storage id
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = "julyStorage")
    @PostMapping("/logicDelete")
    @Operation(summary = "逻辑删除存储实例（单个）")
    public Response011<IdVo011> logicDelete(@RequestBody IdVo011 idVo) {
        String funcName = "logic delete";

        return Response011.successId(funcName, useCase.logicDelete(idVo.getId()));
    }

    /**
     * Logic delete storage instances in batch.
     *
     * @param idsVo request with the storage ids
     * @return per-id summary
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = "julyStorage")
    @PostMapping("/logicDeleteBatch")
    @Operation(summary = "逻辑删除存储实例（批量，逐条回报）")
    public Response011<BatchDeleteResultVo011> logicDeleteBatch(@RequestBody IdsVo011 idsVo) {
        String funcName = "logic delete batch";

        return Response011.success(funcName, useCase.logicDeleteBatch(idsVo.getIds()));
    }

    /**
     * Find a storage instance by primary key (GET).
     *
     * @param id storage id, passed as a query parameter
     * @return storage detail (no secretKey)
     */
    @GetMapping("/getById")
    @Operation(summary = "主键查询（id 走 query；出参不含 Secret Key）")
    public Response011<JulyStorageVo011> getById(@RequestParam("id") String id) {
        String funcName = "get by id";

        return Response011.success(funcName, converter.toVo(useCase.getById(id)));
    }

    /**
     * Find a storage instance by code (GET).
     *
     * @param code storage code, passed as a query parameter
     * @return storage detail (no secretKey)
     */
    @GetMapping("/getByCode")
    @Operation(summary = "按 storageCode 点查（code 走 query）")
    public Response011<JulyStorageVo011> getByCode(@RequestParam("code") String code) {
        String funcName = "get by code";

        return Response011.success(funcName, converter.toVo(useCase.getByCode(code)));
    }

    /**
     * Page query of storage instances.
     *
     * @param vo page query request
     * @return page result of storage instances
     */
    @PostMapping("/selectListByPage")
    @Operation(summary = "分页查询（关键字 + 类型 + 状态过滤）")
    public Response011<PageResult011<JulyStorageVo011>> selectListByPage(@RequestBody JulyStorageQueryVo011 vo) {
        String funcName = "select list by page";

        PageQuery011 pageQuery = new PageQuery011(vo.getPageIndex(), vo.getPageSize());
        PageResult011<JulyStorage> page = useCase.selectListByPage(pageQuery,
                new JulyStorageQuerySpec(vo.getKeyword(), vo.getProvider(), vo.getStatus()));

        return Response011.success(funcName, page.withRows(converter.toVoList(page.rows())));
    }

    /**
     * Probe a saved instance or a draft config.
     *
     * @param vo probe request
     * @return probe result
     */
    @PostMapping("/testConnection")
    @Operation(summary = "测试连接（传 id 测已保存；否则测草稿配置）")
    public Response011<StorageProbeResult> testConnection(@RequestBody JulyStorageConnectVo011 vo) {
        String funcName = "test connection";

        StorageProbeResult result = vo.getId() != null && !vo.getId().isBlank()
                ? useCase.testSaved(vo.getId())
                : useCase.testDraft(vo.getProvider(), vo.getBasePath(), vo.getEndpoint(), vo.getAccessKey(),
                        vo.getSecretKey(), Boolean.TRUE.equals(vo.getSecure()), vo.getDefaultBucket(),
                        vo.getPresignExpirySeconds());

        return Response011.success(funcName, result);
    }
}
