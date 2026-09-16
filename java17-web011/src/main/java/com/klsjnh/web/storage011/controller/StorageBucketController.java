package com.klsjnh.web.storage011.controller;

/*                StorageBucketController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  storage bucket controller class
 *
 */

import com.klsjnh.common.enums.AuditType011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.response.Response011;

import com.klsjnh.application.storage011.StorageBucketUseCase;
import com.klsjnh.application.storage011.StorageProbeResult;

import com.klsjnh.web.global.audit.AuditLog;
import com.klsjnh.web.storage011.vo.StorageBucketInsertVo011;
import com.klsjnh.web.storage011.vo.StorageBucketQueryVo011;
import com.klsjnh.web.storage011.vo.StorageBucketRemoveVo011;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Storage bucket HTTP adapter: list / get / create / remove / probe.
 */

@Tag(name = "存储中心 - 桶管理")
@RestController
@RequestMapping("/klsjnh/storage011/bucket")
public class StorageBucketController {

    /**
     * Storage bucket use case.
     */
    private final StorageBucketUseCase useCase;

    /**
     * Create the controller.
     *
     * @param useCase storage bucket use case
     */
    public StorageBucketController(StorageBucketUseCase useCase) {
        this.useCase = useCase;
    }

    /**
     * List bucket names.
     *
     * @param vo query request
     * @return bucket names
     */
    @PostMapping("/selectList")
    @Operation(summary = "桶列表")
    public Response011<List<String>> selectList(@RequestBody StorageBucketQueryVo011 vo) {
        String funcName = "select list";

        return Response011.success(funcName, useCase.selectList(vo.getStorageCode()));
    }

    /**
     * Page bucket names.
     *
     * @param vo query request
     * @return page result
     */
    @PostMapping("/selectListByPage")
    @Operation(summary = "桶分页")
    public Response011<PageResult011<String>> selectListByPage(@RequestBody StorageBucketQueryVo011 vo) {
        String funcName = "select list by page";

        return Response011.success(funcName, useCase.selectListByPage(vo.getStorageCode(), vo.getPageIndex(),
                vo.getPageSize(), vo.getKeyword()));
    }

    /**
     * Whether a bucket exists (GET).
     *
     * @param storageCode storage code, optional
     * @param bucketName  bucket name
     * @return true when present
     */
    @GetMapping("/getByName")
    @Operation(summary = "桶是否存在（storageCode/bucketName 走 query）")
    public Response011<Boolean> getByName(@RequestParam(value = "storageCode", required = false) String storageCode,
            @RequestParam("bucketName") String bucketName) {
        String funcName = "get by name";

        return Response011.success(funcName, useCase.exists(storageCode, bucketName));
    }

    /**
     * Create a bucket.
     *
     * @param vo insert request
     * @return success envelope
     */
    @AuditLog(type = AuditType011.INSERT, objectCode = "julyStorageBucket")
    @PostMapping("/insert")
    @Operation(summary = "新建桶（已存在为幂等）")
    public Response011<String> insert(@RequestBody StorageBucketInsertVo011 vo) {
        String funcName = "insert";

        useCase.insert(vo.getStorageCode(), vo.getBucketName(), vo.getRegion());

        return Response011.success(funcName, vo.getBucketName());
    }

    /**
     * Remove an EMPTY bucket.
     *
     * @param vo remove request
     * @return success envelope
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = "julyStorageBucket")
    @PostMapping("/remove")
    @Operation(summary = "删除空桶（非空大声失败）")
    public Response011<String> remove(@RequestBody StorageBucketRemoveVo011 vo) {
        String funcName = "remove";

        useCase.remove(vo.getStorageCode(), vo.getBucketName());

        return Response011.success(funcName, vo.getBucketName());
    }

    /**
     * Probe the resolved instance.
     *
     * @param vo query request (only storageCode is used)
     * @return probe result
     */
    @PostMapping("/testConnection")
    @Operation(summary = "测试连接")
    public Response011<StorageProbeResult> testConnection(@RequestBody StorageBucketQueryVo011 vo) {
        String funcName = "test connection";

        return Response011.success(funcName, useCase.testConnection(vo.getStorageCode()));
    }
}
