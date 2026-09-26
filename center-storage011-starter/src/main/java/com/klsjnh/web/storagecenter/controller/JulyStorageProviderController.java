package com.klsjnh.web.storagecenter.controller;

/*                JulyStorageProviderController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july storage provider controller class
 *      2026.09.17  merged bucket actions into the julyStorage module
 *      2026.09.26  pass operator into use case for permission checks
 *
 */

import com.klsjnh.common.constant.AuditObjectCodes011;
import com.klsjnh.common.enums.AuditType011;
import com.klsjnh.common.identity.Operator011;
import com.klsjnh.common.page.PageQuery011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.response.Response011;
import com.klsjnh.common.vo.BatchDeleteResultVo011;
import com.klsjnh.common.vo.IdVo011;
import com.klsjnh.common.vo.IdsVo011;

import com.klsjnh.application.storagecenter.storage.JulyStorageProviderUseCase;
import com.klsjnh.application.storagecenter.storage.StorageBucketUseCase;
import com.klsjnh.domain.storagecenter.object.StorageProbe;
import com.klsjnh.domain.storagecenter.storage.JulyStorageProvider;
import com.klsjnh.domain.storagecenter.storage.JulyStorageProviderBucket;
import com.klsjnh.domain.storagecenter.storage.JulyStorageProviderQuerySpec;

import com.klsjnh.web.storagecenter.converter.JulyStorageProviderConverter;

import com.klsjnh.web.global.audit.AuditLog;
import com.klsjnh.web.storagecenter.vo.bucket.StorageBucketInsertVo011;
import com.klsjnh.web.storagecenter.vo.bucket.StorageBucketQueryVo011;
import com.klsjnh.web.storagecenter.vo.bucket.StorageBucketRemoveVo011;
import com.klsjnh.web.storagecenter.vo.bucket.StorageBucketVo011;
import com.klsjnh.web.storagecenter.vo.storage.JulyStorageProviderConnectVo011;
import com.klsjnh.web.storagecenter.vo.storage.JulyStorageProviderQueryVo011;
import com.klsjnh.web.storagecenter.vo.storage.JulyStorageProviderSaveVo011;
import com.klsjnh.web.storagecenter.vo.storage.JulyStorageProviderVo011;
import com.klsjnh.web.storagecenter.vo.storage.StorageProbeVo011;
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
import jakarta.validation.Valid;

import java.util.List;

/**
 * JulyStorageProvider HTTP adapter — the {@code julyStorage} module: storage
 * provider management (table-driven instances) plus the buckets of an instance
 * and the connectivity probe.
 */

@Tag(name = "存储中心011 - 存储实例与桶")
@RestController
@RequestMapping("/klsjnh/storagecenter/julyStorage/v1")
public class JulyStorageProviderController {

    /**
     * JulyStorageProvider use case.
     */
    private final JulyStorageProviderUseCase julyStorageProviderUseCase;

    /**
     * Storage bucket use case.
     */
    private final StorageBucketUseCase bucketUseCase;

    /**
     * Response julyStorageProviderConverter.
     */
    private final JulyStorageProviderConverter julyStorageProviderConverter;

    /**
     * Create the controller.
     *
     * @param julyStorageProviderUseCase       july storage provider use case
     * @param bucketUseCase storage bucket use case
     * @param julyStorageProviderConverter     response julyStorageProviderConverter
     */
    public JulyStorageProviderController(JulyStorageProviderUseCase julyStorageProviderUseCase, StorageBucketUseCase bucketUseCase,
            JulyStorageProviderConverter julyStorageProviderConverter) {
        this.julyStorageProviderUseCase = julyStorageProviderUseCase;
        this.bucketUseCase = bucketUseCase;
        this.julyStorageProviderConverter = julyStorageProviderConverter;
    }

    /**
     * Insert a new storage instance.
     *
     * @param vo save request
     * @return envelope with the new storage id
     */
    @AuditLog(type = AuditType011.INSERT, objectCode = AuditObjectCodes011.JULY_STORAGE_PROVIDER)
    @PostMapping("/insert")
    @Operation(summary = "新增存储实例（storageCode 查重）")
    public Response011<IdVo011> insert(@Valid @RequestBody JulyStorageProviderSaveVo011 vo, HttpServletRequest request) {
        String funcName = "insert";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.successId(funcName, julyStorageProviderUseCase.insert(operator.id(), vo.getStorageCode(), vo.getSortOrder(),
                vo.getStorageName(), vo.getProvider(), vo.getBasePath(), vo.getEndpoint(), vo.getAccessKey(),
                vo.getSecretKey(), Boolean.TRUE.equals(vo.getSecure()), vo.getDefaultBucket(),
                vo.getPresignExpirySeconds(), vo.getRemark()));
    }

    /**
     * Update a storage instance (blank / {@code ******} accessKey and secretKey
     * keep the stored values).
     *
     * @param vo save request
     * @return envelope with the storage id
     */
    @AuditLog(type = AuditType011.UPDATE, objectCode = AuditObjectCodes011.JULY_STORAGE_PROVIDER)
    @PostMapping("/update")
    @Operation(summary = "修改存储实例（storageCode 不可变；accessKey/secretKey 留空或 ****** 保持）")
    public Response011<IdVo011> update(@Valid @RequestBody JulyStorageProviderSaveVo011 vo, HttpServletRequest request) {
        String funcName = "update";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.successId(funcName, julyStorageProviderUseCase.update(operator.id(), vo.getId(), vo.getStorageName(), vo.getProvider(),
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
    @AuditLog(type = AuditType011.DELETE, objectCode = AuditObjectCodes011.JULY_STORAGE_PROVIDER)
    @PostMapping("/logicDelete")
    @Operation(summary = "逻辑删除存储实例（单个）")
    public Response011<IdVo011> logicDelete(@Valid @RequestBody IdVo011 idVo, HttpServletRequest request) {
        String funcName = "logic delete";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.successId(funcName, julyStorageProviderUseCase.logicDelete(operator.id(), idVo.getId()));
    }

    /**
     * Logic delete storage instances in batch.
     *
     * @param idsVo request with the storage ids
     * @return per-id summary
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = AuditObjectCodes011.JULY_STORAGE_PROVIDER)
    @PostMapping("/logicDeleteBatch")
    @Operation(summary = "逻辑删除存储实例（批量，逐条回报）")
    public Response011<BatchDeleteResultVo011> logicDeleteBatch(@Valid @RequestBody IdsVo011 idsVo, HttpServletRequest request) {
        String funcName = "logic delete batch";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.success(funcName, julyStorageProviderUseCase.logicDeleteBatch(operator.id(), idsVo.getIds()));
    }

    /**
     * Find a storage instance by primary key (GET).
     *
     * @param id storage id, passed as a query parameter
     * @return storage detail (no secretKey)
     */
    @GetMapping("/getById")
    @Operation(summary = "主键查询（id 走 query；出参不含 Secret Key）")
    public Response011<JulyStorageProviderVo011> getById(@RequestParam("id") String id, HttpServletRequest request) {
        String funcName = "get by id";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.success(funcName, julyStorageProviderConverter.toVo(julyStorageProviderUseCase.getById(operator.id(), id)));
    }

    /**
     * Find a storage instance by code (GET).
     *
     * @param code storage code, passed as a query parameter
     * @return storage detail (no secretKey)
     */
    @GetMapping("/getByCode")
    @Operation(summary = "按 storageCode 点查（code 走 query）")
    public Response011<JulyStorageProviderVo011> getByCode(@RequestParam("code") String code, HttpServletRequest request) {
        String funcName = "get by code";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.success(funcName, julyStorageProviderConverter.toVo(julyStorageProviderUseCase.getByCode(operator.id(), code)));
    }

    /**
     * Page query of storage instances.
     *
     * @param vo page query request
     * @return page result of storage instances
     */
    @PostMapping("/selectListByPage")
    @Operation(summary = "分页查询（关键字 + 类型 + 状态过滤）")
    public Response011<PageResult011<JulyStorageProviderVo011>> selectListByPage(
            @Valid @RequestBody JulyStorageProviderQueryVo011 vo, HttpServletRequest request) {
        String funcName = "select list by page";
        Operator011 operator = Operator011Resolver.resolve(request);


        PageQuery011 pageQuery = new PageQuery011(vo.getPageIndex(), vo.getPageSize());
        PageResult011<JulyStorageProvider> page = julyStorageProviderUseCase.selectListByPage(operator.id(), pageQuery,
                new JulyStorageProviderQuerySpec(vo.getKeyword(), vo.getProvider(), vo.getStatus()));

        return Response011.success(funcName, page.withRows(julyStorageProviderConverter.toVoList(page.rows())));
    }

    /**
     * Read a storage instance together with its buckets (master + children).
     * <p>
     * Read side only: buckets are Incremental children (physical resources with
     * their own create / remove endpoints), so there is no whole save here.
     * </p>
     *
     * @param id storage id
     * @return envelope with the storage instance and its bucket list
     */
    @GetMapping("/getWithChildren")
    @Operation(summary = "主+子联查（存储实例 + 桶列表）；桶为 Incremental，无整存")
    public Response011<java.util.Map<String, Object>> getWithChildren(@RequestParam("id") String id, HttpServletRequest request) {
        String funcName = "get with children";
        Operator011 operator = Operator011Resolver.resolve(request);


        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("master", julyStorageProviderConverter.toVo(julyStorageProviderUseCase.getById(operator.id(), id)));
        result.put("buckets", julyStorageProviderConverter.toBucketVoList(julyStorageProviderUseCase.buckets(operator.id(), id)));

        return Response011.success(funcName, result);
    }

    /**
     * Probe a saved instance or a draft config.
     *
     * @param vo probe request
     * @return probe result
     */
    @PostMapping("/testConnection")
    @Operation(summary = "测试连接（传 id 测已保存；否则测草稿；带 bucketCount + basePath/endpoint）")
    public Response011<StorageProbeVo011> testConnection(@Valid @RequestBody JulyStorageProviderConnectVo011 vo, HttpServletRequest request) {
        String funcName = "test connection";
        Operator011 operator = Operator011Resolver.resolve(request);


        StorageProbe probe = vo.getId() != null && !vo.getId().isBlank()
                ? julyStorageProviderUseCase.testSaved(operator.id(), vo.getId())
                : julyStorageProviderUseCase.testDraft(operator.id(), vo.getProvider(), vo.getBasePath(), vo.getEndpoint(), vo.getAccessKey(),
                        vo.getSecretKey(), Boolean.TRUE.equals(vo.getSecure()), vo.getDefaultBucket(),
                        vo.getPresignExpirySeconds());

        return Response011.success(funcName, toProbeVo(probe));
    }

    /**
     * List bucket rows of a storage instance.
     *
     * @param vo query request
     * @return bucket rows
     */
    @PostMapping("/selectBucketList")
    @Operation(summary = "桶列表（每行 bucketCode/bucketName/isDefault/sortOrder/status）")
    public Response011<List<StorageBucketVo011>> selectBucketList(@Valid @RequestBody StorageBucketQueryVo011 vo, HttpServletRequest request) {
        String funcName = "select bucket list";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.success(funcName, julyStorageProviderConverter.toBucketVoList(bucketUseCase.selectList(operator.id(), vo.getStorageCode())));
    }

    /**
     * Page bucket rows of a storage instance.
     *
     * @param vo query request
     * @return page result
     */
    @PostMapping("/selectBucketListByPage")
    @Operation(summary = "桶分页（每行 bucketCode/bucketName/isDefault/sortOrder/status）")
    public Response011<PageResult011<StorageBucketVo011>> selectBucketListByPage(
            @Valid @RequestBody StorageBucketQueryVo011 vo, HttpServletRequest request) {
        String funcName = "select bucket list by page";
        Operator011 operator = Operator011Resolver.resolve(request);


        PageResult011<JulyStorageProviderBucket> page = bucketUseCase.selectListByPage(operator.id(), vo.getStorageCode(),
                vo.getPageIndex(), vo.getPageSize(), vo.getKeyword());

        return Response011.success(funcName, page.withRows(julyStorageProviderConverter.toBucketVoList(page.rows())));
    }

    /**
     * Get a bucket row by code (GET).
     *
     * @param storageCode storage code, optional
     * @param bucketCode  bucket code
     * @return bucket detail
     */
    @GetMapping("/getBucket")
    @Operation(summary = "查桶（bucketCode 走 query；不存在 404）")
    public Response011<StorageBucketVo011> getBucket(
            @RequestParam(value = "storageCode", required = false) String storageCode,
            @RequestParam("bucketCode") String bucketCode, HttpServletRequest request) {
        String funcName = "get bucket";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.success(funcName, julyStorageProviderConverter.toBucketVo(bucketUseCase.getBucket(operator.id(), storageCode, bucketCode)));
    }

    /**
     * Create a bucket (persist a bucket row + ensure on the backend).
     *
     * @param vo insert request
     * @return the created bucket code
     */
    @AuditLog(type = AuditType011.INSERT, objectCode = AuditObjectCodes011.JULY_STORAGE_PROVIDER_BUCKET)
    @PostMapping("/insertBucket")
    @Operation(summary = "新建桶（bucketCode 同实例内查重；已存在为幂等）")
    public Response011<String> insertBucket(@Valid @RequestBody StorageBucketInsertVo011 vo, HttpServletRequest request) {
        String funcName = "insert bucket";
        Operator011 operator = Operator011Resolver.resolve(request);


        bucketUseCase.insert(operator.id(), vo.getStorageCode(), vo.getBucketCode(), vo.getBucketName(),
                Boolean.TRUE.equals(vo.getIsDefault()), vo.getRegion());

        return Response011.success(funcName, vo.getBucketCode());
    }

    /**
     * Remove an EMPTY bucket.
     *
     * @param vo remove request
     * @return the removed bucket code
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = AuditObjectCodes011.JULY_STORAGE_PROVIDER_BUCKET)
    @PostMapping("/removeBucket")
    @Operation(summary = "删除空桶（非空大声失败）")
    public Response011<String> removeBucket(@Valid @RequestBody StorageBucketRemoveVo011 vo, HttpServletRequest request) {
        String funcName = "remove bucket";
        Operator011 operator = Operator011Resolver.resolve(request);


        bucketUseCase.remove(operator.id(), vo.getStorageCode(), vo.getBucketCode());

        return Response011.success(funcName, vo.getBucketCode());
    }

    /**
     * Probe the resolved instance.
     *
     * @param vo query request (only storageCode is used)
     * @return probe result
     */
    @PostMapping("/testBucketConnection")
    @Operation(summary = "测试桶连接（带 bucketCount + basePath/endpoint）")
    public Response011<StorageProbeVo011> testBucketConnection(@Valid @RequestBody StorageBucketQueryVo011 vo, HttpServletRequest request) {
        String funcName = "test bucket connection";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.success(funcName, toProbeVo(bucketUseCase.testConnection(operator.id(), vo.getStorageCode())));
    }

    /**
     * Map the domain probe to the response VO (null locations omitted).
     *
     * @param probe domain probe
     * @return response VO
     */
    private StorageProbeVo011 toProbeVo(StorageProbe probe) {
        StorageProbeVo011 vo = new StorageProbeVo011();
        vo.setSuccess(probe.success());
        vo.setBucketCount(probe.bucketCount());
        vo.setBasePath(probe.basePath());
        vo.setEndpoint(probe.endpoint());
        vo.setMessage(probe.message());

        return vo;
    }
}
