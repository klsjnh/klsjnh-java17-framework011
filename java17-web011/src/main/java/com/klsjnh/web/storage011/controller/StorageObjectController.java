package com.klsjnh.web.storage011.controller;

/*                StorageObjectController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  storage object controller class
 *
 */

import com.klsjnh.common.enums.AuditType011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.response.Response011;

import com.klsjnh.application.storage011.StorageObjectUseCase;
import com.klsjnh.application.storage011.StorageTextContent;
import com.klsjnh.domain.storage.ObjectStat;

import com.klsjnh.web.global.audit.AuditLog;
import com.klsjnh.web.storage011.vo.StorageObjectBatchRemoveVo011;
import com.klsjnh.web.storage011.vo.StorageObjectQueryVo011;
import com.klsjnh.web.storage011.vo.StorageObjectRefVo011;
import com.klsjnh.web.storage011.vo.StorageObjectSaveTextVo011;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Storage object HTTP adapter: list / stat / upload / download / remove plus the
 * online text editing read/write and the presigned URL.
 */

@Tag(name = "存储中心 - 对象管理")
@RestController
@RequestMapping("/klsjnh/storage011/object")
public class StorageObjectController {

    /**
     * Storage object use case.
     */
    private final StorageObjectUseCase useCase;

    /**
     * Create the controller.
     *
     * @param useCase storage object use case
     */
    public StorageObjectController(StorageObjectUseCase useCase) {
        this.useCase = useCase;
    }

    /**
     * List object keys.
     *
     * @param vo query request
     * @return object keys
     */
    @PostMapping("/selectList")
    @Operation(summary = "对象列表（前缀过滤）")
    public Response011<List<String>> selectList(@RequestBody StorageObjectQueryVo011 vo) {
        String funcName = "select list";

        return Response011.success(funcName, useCase.selectList(vo.getStorageCode(), vo.getBucketName(),
                vo.getPrefix()));
    }

    /**
     * Page object keys.
     *
     * @param vo query request
     * @return page result
     */
    @PostMapping("/selectListByPage")
    @Operation(summary = "对象分页")
    public Response011<PageResult011<String>> selectListByPage(@RequestBody StorageObjectQueryVo011 vo) {
        String funcName = "select list by page";

        return Response011.success(funcName, useCase.selectListByPage(vo.getStorageCode(), vo.getBucketName(),
                vo.getPrefix(), vo.getPageIndex(), vo.getPageSize()));
    }

    /**
     * Object metadata (GET).
     *
     * @param storageCode storage code, optional
     * @param bucketName  bucket name, optional
     * @param objectName  object name
     * @return object stat
     */
    @GetMapping("/stat")
    @Operation(summary = "对象元数据（size/lastModified/contentType，走 query）")
    public Response011<ObjectStat> stat(@RequestParam(value = "storageCode", required = false) String storageCode,
            @RequestParam(value = "bucketName", required = false) String bucketName,
            @RequestParam("objectName") String objectName) {
        String funcName = "stat";

        return Response011.success(funcName, useCase.stat(storageCode, bucketName, objectName));
    }

    /**
     * Upload an object (multipart).
     *
     * @param storageCode storage code, optional
     * @param bucketName  bucket name, optional
     * @param objectName  object name, optional
     * @param file        uploaded file
     * @return the stored key
     * @throws Exception on read failure
     */
    @AuditLog(type = AuditType011.INSERT, objectCode = "julyStorageObject")
    @PostMapping("/upload")
    @Operation(summary = "上传对象（multipart）")
    public Response011<String> upload(@RequestParam(value = "storageCode", required = false) String storageCode,
            @RequestParam(value = "bucketName", required = false) String bucketName,
            @RequestParam(value = "objectName", required = false) String objectName,
            @RequestParam("file") MultipartFile file) throws Exception {
        String funcName = "upload";

        String key = useCase.upload(storageCode, bucketName, objectName, file.getBytes(), file.getContentType());

        return Response011.success(funcName, key);
    }

    /**
     * Download an object as a binary stream (no envelope — contract exception).
     *
     * @param storageCode storage code, optional
     * @param bucketName  bucket name, optional
     * @param objectName  object name
     * @param response    http response
     * @throws Exception on write failure
     */
    @GetMapping("/download")
    @Operation(summary = "下载对象（二进制流，不走信封）")
    public void download(@RequestParam(value = "storageCode", required = false) String storageCode,
            @RequestParam(value = "bucketName", required = false) String bucketName,
            @RequestParam("objectName") String objectName, HttpServletResponse response) throws Exception {
        byte[] content = useCase.download(storageCode, bucketName, objectName);
        String fileName = objectName.contains("/") ? objectName.substring(objectName.lastIndexOf('/') + 1)
                : objectName;

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()) + "\"");

        try (InputStream stream = new java.io.ByteArrayInputStream(content)) {
            StreamUtils.copy(stream, response.getOutputStream());
        }

        response.flushBuffer();
    }

    /**
     * Delete an object.
     *
     * @param vo object reference request
     * @return the deleted object name
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = "julyStorageObject")
    @PostMapping("/remove")
    @Operation(summary = "删除对象")
    public Response011<String> remove(@RequestBody StorageObjectRefVo011 vo) {
        String funcName = "remove";

        useCase.remove(vo.getStorageCode(), vo.getBucketName(), vo.getObjectName());

        return Response011.success(funcName, vo.getObjectName());
    }

    /**
     * Delete objects in batch.
     *
     * @param vo batch remove request
     * @return success envelope
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = "julyStorageObject")
    @PostMapping("/batchRemove")
    @Operation(summary = "批量删除对象")
    public Response011<String> batchRemove(@RequestBody StorageObjectBatchRemoveVo011 vo) {
        String funcName = "batch remove";

        useCase.batchRemove(vo.getStorageCode(), vo.getBucketName(), vo.getObjectNames());

        return Response011.success(funcName, "batch remove success");
    }

    /**
     * Read an object as editable text (GET, ≤1MB).
     *
     * @param storageCode storage code, optional
     * @param bucketName  bucket name, optional
     * @param objectName  object name
     * @return text content
     */
    @GetMapping("/readText")
    @Operation(summary = "在线编辑·读取（≤1MB，走 query）")
    public Response011<StorageTextContent> readText(
            @RequestParam(value = "storageCode", required = false) String storageCode,
            @RequestParam(value = "bucketName", required = false) String bucketName,
            @RequestParam("objectName") String objectName) {
        String funcName = "read text";

        return Response011.success(funcName, useCase.readText(storageCode, bucketName, objectName));
    }

    /**
     * Write an object as editable text (≤1MB).
     *
     * @param vo save text request
     * @return the stored key
     */
    @AuditLog(type = AuditType011.UPDATE, objectCode = "julyStorageObject")
    @PostMapping("/saveText")
    @Operation(summary = "在线编辑·保存（≤1MB）")
    public Response011<String> saveText(@RequestBody StorageObjectSaveTextVo011 vo) {
        String funcName = "save text";

        return Response011.success(funcName, useCase.saveText(vo.getStorageCode(), vo.getBucketName(),
                vo.getObjectName(), vo.getContent()));
    }

    /**
     * Presigned GET URL of an object (GET).
     *
     * @param storageCode storage code, optional
     * @param bucketName  bucket name, optional
     * @param objectName  object name
     * @return presigned URL / URI
     */
    @GetMapping("/presignedUrl")
    @Operation(summary = "预签名 URL（local→file: URI；S3/MinIO→真实预签名，走 query）")
    public Response011<String> presignedUrl(
            @RequestParam(value = "storageCode", required = false) String storageCode,
            @RequestParam(value = "bucketName", required = false) String bucketName,
            @RequestParam("objectName") String objectName) {
        String funcName = "presigned url";

        return Response011.success(funcName, useCase.presignedUrl(storageCode, bucketName, objectName));
    }
}
