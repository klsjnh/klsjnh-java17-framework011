package com.klsjnh.web.storagecenter.controller;

/*                JulyObjectController class
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
 *      2026.09.17  module renamed to julyObject, actions prefixed
 *
 */

import com.klsjnh.common.constant.AuditObjectCodes011;
import com.klsjnh.common.enums.AuditType011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.response.Response011;

import com.klsjnh.application.storagecenter.object.StorageObjectUseCase;
import com.klsjnh.application.storagecenter.object.StorageTextContent;
import com.klsjnh.domain.storagecenter.object.ObjectStat;

import com.klsjnh.web.global.audit.AuditLog;
import com.klsjnh.web.storagecenter.vo.object.StorageObjectBatchRemoveVo011;
import com.klsjnh.web.storagecenter.vo.object.StorageObjectQueryVo011;
import com.klsjnh.web.storagecenter.vo.object.StorageObjectRefVo011;
import com.klsjnh.web.storagecenter.vo.object.StorageObjectSaveTextVo011;

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
 * JulyObject HTTP adapter — the {@code julyObject} module: object listing, stat,
 * upload / download, removal, online text editing and the presigned URL.
 */

@Tag(name = "存储中心011 - 对象")
@RestController
@RequestMapping("/klsjnh/storagecenter/julyObject/v1")
public class JulyObjectController {

    /**
     * Storage object use case.
     */
    private final StorageObjectUseCase storageObjectUseCase;

    /**
     * Create the controller.
     *
     * @param storageObjectUseCase storage object use case
     */
    public JulyObjectController(StorageObjectUseCase storageObjectUseCase) {
        this.storageObjectUseCase = storageObjectUseCase;
    }

    /**
     * List object keys.
     *
     * @param vo query request
     * @return object keys
     */
    @PostMapping("/selectObjectList")
    @Operation(summary = "对象列表（前缀过滤）")
    public Response011<List<String>> selectObjectList(@RequestBody StorageObjectQueryVo011 vo) {
        String funcName = "select object list";

        return Response011.success(funcName, storageObjectUseCase.selectList(vo.getStorageCode(), vo.getBucketName(),
                vo.getPrefix()));
    }

    /**
     * Page object keys.
     *
     * @param vo query request
     * @return page result
     */
    @PostMapping("/selectObjectListByPage")
    @Operation(summary = "对象分页（每行带 key/size/lastModified/contentType）")
    public Response011<PageResult011<ObjectStat>> selectObjectListByPage(@RequestBody StorageObjectQueryVo011 vo) {
        String funcName = "select object list by page";

        return Response011.success(funcName, storageObjectUseCase.selectListByPage(vo.getStorageCode(), vo.getBucketName(),
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
    @GetMapping("/statObject")
    @Operation(summary = "对象元数据（size/lastModified/contentType，走 query）")
    public Response011<ObjectStat> statObject(@RequestParam(value = "storageCode", required = false) String storageCode,
            @RequestParam(value = "bucketName", required = false) String bucketName,
            @RequestParam("objectName") String objectName) {
        String funcName = "stat object";

        return Response011.success(funcName, storageObjectUseCase.stat(storageCode, bucketName, objectName));
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
    @AuditLog(type = AuditType011.INSERT, objectCode = AuditObjectCodes011.JULY_STORAGE_OBJECT)
    @PostMapping("/uploadObject")
    @Operation(summary = "上传对象（multipart）")
    public Response011<String> uploadObject(@RequestParam(value = "storageCode", required = false) String storageCode,
            @RequestParam(value = "bucketName", required = false) String bucketName,
            @RequestParam(value = "objectName", required = false) String objectName,
            @RequestParam("file") MultipartFile file) throws Exception {
        String funcName = "upload object";

        String key = storageObjectUseCase.upload(storageCode, bucketName, objectName, file.getBytes(), file.getContentType());

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
    @GetMapping("/downloadObject")
    @Operation(summary = "下载对象（二进制流，不走信封）")
    public void downloadObject(@RequestParam(value = "storageCode", required = false) String storageCode,
            @RequestParam(value = "bucketName", required = false) String bucketName,
            @RequestParam("objectName") String objectName, HttpServletResponse response) throws Exception {
        byte[] content = storageObjectUseCase.download(storageCode, bucketName, objectName);
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
    @AuditLog(type = AuditType011.DELETE, objectCode = AuditObjectCodes011.JULY_STORAGE_OBJECT)
    @PostMapping("/removeObject")
    @Operation(summary = "删除对象")
    public Response011<String> removeObject(@RequestBody StorageObjectRefVo011 vo) {
        String funcName = "remove object";

        storageObjectUseCase.remove(vo.getStorageCode(), vo.getBucketName(), vo.getObjectName());

        return Response011.success(funcName, vo.getObjectName());
    }

    /**
     * Delete objects in batch.
     *
     * @param vo batch remove request
     * @return success envelope
     */
    @AuditLog(type = AuditType011.DELETE, objectCode = AuditObjectCodes011.JULY_STORAGE_OBJECT)
    @PostMapping("/batchRemoveObject")
    @Operation(summary = "批量删除对象")
    public Response011<String> batchRemoveObject(@RequestBody StorageObjectBatchRemoveVo011 vo) {
        String funcName = "batch remove object";

        storageObjectUseCase.batchRemove(vo.getStorageCode(), vo.getBucketName(), vo.getObjectNames());

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
    @GetMapping("/readObjectText")
    @Operation(summary = "在线编辑·读取（≤1MB，走 query）")
    public Response011<StorageTextContent> readObjectText(
            @RequestParam(value = "storageCode", required = false) String storageCode,
            @RequestParam(value = "bucketName", required = false) String bucketName,
            @RequestParam("objectName") String objectName) {
        String funcName = "read object text";

        return Response011.success(funcName, storageObjectUseCase.readText(storageCode, bucketName, objectName));
    }

    /**
     * Write an object as editable text (≤1MB).
     *
     * @param vo save text request
     * @return the stored key
     */
    @AuditLog(type = AuditType011.UPDATE, objectCode = AuditObjectCodes011.JULY_STORAGE_OBJECT)
    @PostMapping("/saveObjectText")
    @Operation(summary = "在线编辑·保存（≤1MB）")
    public Response011<String> saveObjectText(@RequestBody StorageObjectSaveTextVo011 vo) {
        String funcName = "save object text";

        return Response011.success(funcName, storageObjectUseCase.saveText(vo.getStorageCode(), vo.getBucketName(),
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
    @GetMapping("/presignObjectUrl")
    @Operation(summary = "预签名 URL（local→file: URI；S3/MinIO→真实预签名，走 query）")
    public Response011<String> presignObjectUrl(
            @RequestParam(value = "storageCode", required = false) String storageCode,
            @RequestParam(value = "bucketName", required = false) String bucketName,
            @RequestParam("objectName") String objectName) {
        String funcName = "presign object url";

        return Response011.success(funcName, storageObjectUseCase.presignedUrl(storageCode, bucketName, objectName));
    }
}
