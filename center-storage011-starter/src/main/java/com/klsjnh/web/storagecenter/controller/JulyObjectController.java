package com.klsjnh.web.storagecenter.controller;

/*                JulyObjectController class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate 2026.09.26
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  storage object controller class
 *      2026.09.17  module renamed to julyObject, actions prefixed
 *      2026.09.26  pass operator into use case for permission checks
 *
 */

import com.klsjnh.common.constant.AuditObjectCodes011;
import com.klsjnh.common.enums.AuditType011;
import com.klsjnh.common.identity.Operator011;
import com.klsjnh.common.page.PageResult011;
import com.klsjnh.common.response.Response011;

import com.klsjnh.application.storagecenter.object.StorageObjectUseCase;
import com.klsjnh.application.storagecenter.object.StorageTextContent;
import com.klsjnh.domain.storagecenter.object.ObjectListing;
import com.klsjnh.domain.storagecenter.object.ObjectStat;

import com.klsjnh.web.global.audit.AuditLog;
import com.klsjnh.web.storagecenter.vo.object.StorageObjectBatchRemoveVo011;
import com.klsjnh.web.storagecenter.vo.object.StorageObjectCopyVo011;
import com.klsjnh.web.storagecenter.vo.object.StorageObjectPageVo011;
import com.klsjnh.web.storagecenter.vo.object.StorageObjectQueryVo011;
import com.klsjnh.web.storagecenter.vo.object.StorageObjectRefVo011;
import com.klsjnh.web.storagecenter.vo.object.StorageObjectRenameVo011;
import com.klsjnh.web.storagecenter.vo.object.StorageObjectSaveTextVo011;
import com.klsjnh.web.util.Operator011Resolver;

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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

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
    public Response011<List<String>> selectObjectList(@Valid @RequestBody StorageObjectQueryVo011 vo, HttpServletRequest request) {
        String funcName = "select object list";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.success(funcName, storageObjectUseCase.selectList(operator.id(), vo.getStorageCode(), vo.getBucketName(),
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
    public Response011<PageResult011<ObjectStat>> selectObjectListByPage(@Valid @RequestBody StorageObjectQueryVo011 vo, HttpServletRequest request) {
        String funcName = "select object list by page";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.success(funcName, storageObjectUseCase.selectListByPage(operator.id(), vo.getStorageCode(), vo.getBucketName(),
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
            @RequestParam("objectName") String objectName, HttpServletRequest request) {
        String funcName = "stat object";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.success(funcName, storageObjectUseCase.stat(operator.id(), storageCode, bucketName, objectName));
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
            @RequestParam("file") MultipartFile file, HttpServletRequest request) throws Exception {
        String funcName = "upload object";
        Operator011 operator = Operator011Resolver.resolve(request);


        String key = storageObjectUseCase.upload(operator.id(), storageCode, bucketName, objectName, file.getBytes(), file.getContentType());

        return Response011.success(funcName, key);
    }

    /**
     * Download an object as a binary stream (no envelope — contract exception).
     *
     * @param storageCode storage code, optional
     * @param bucketName  bucket name, optional
     * @param objectName  object name
     * @param request     http request (operator)
     * @param response    http response
     * @throws Exception on write failure
     */
    @GetMapping("/downloadObject")
    @Operation(summary = "下载对象（二进制流，不走信封）")
    public void downloadObject(@RequestParam(value = "storageCode", required = false) String storageCode,
            @RequestParam(value = "bucketName", required = false) String bucketName,
            @RequestParam("objectName") String objectName, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        Operator011 operator = Operator011Resolver.resolve(request);
        byte[] content = storageObjectUseCase.download(operator.id(), storageCode, bucketName, objectName);
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
     * Download an object as a stream (no full-object buffering; no envelope).
     *
     * @param storageCode storage code, optional
     * @param bucketName  bucket name, optional
     * @param objectName  object name
     * @param request     http request (operator)
     * @param response    http response
     * @throws Exception on write failure
     */
    @GetMapping("/downloadObjectStream")
    @Operation(summary = "流式下载对象（不整对象进堆，不走信封）")
    public void downloadObjectStream(@RequestParam(value = "storageCode", required = false) String storageCode,
            @RequestParam(value = "bucketName", required = false) String bucketName,
            @RequestParam("objectName") String objectName, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        Operator011 operator = Operator011Resolver.resolve(request);
        String fileName = objectName.contains("/") ? objectName.substring(objectName.lastIndexOf('/') + 1)
                : objectName;

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()) + "\"");

        try (InputStream stream = storageObjectUseCase.downloadStream(operator.id(), storageCode, bucketName,
                objectName)) {
            StreamUtils.copy(stream, response.getOutputStream());
        }

        response.flushBuffer();
    }

    /**
     * Copy an object within / across buckets.
     *
     * @param vo copy request
     * @return the target key
     */
    @AuditLog(type = AuditType011.INSERT, objectCode = AuditObjectCodes011.JULY_STORAGE_OBJECT)
    @PostMapping("/copyObject")
    @Operation(summary = "复制对象（同/跨桶）")
    public Response011<String> copyObject(@Valid @RequestBody StorageObjectCopyVo011 vo, HttpServletRequest request) {
        String funcName = "copy object";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.success(funcName, storageObjectUseCase.copy(operator.id(), vo.getStorageCode(), vo.getBucketName(),
                vo.getObjectName(), vo.getTargetBucket(), vo.getTargetName()));
    }

    /**
     * Rename (move) an object within the same bucket.
     *
     * @param vo rename request
     * @return the target key
     */
    @AuditLog(type = AuditType011.UPDATE, objectCode = AuditObjectCodes011.JULY_STORAGE_OBJECT)
    @PostMapping("/renameObject")
    @Operation(summary = "重命名对象（同桶 move）")
    public Response011<String> renameObject(@Valid @RequestBody StorageObjectRenameVo011 vo, HttpServletRequest request) {
        String funcName = "rename object";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.success(funcName, storageObjectUseCase.rename(operator.id(), vo.getStorageCode(), vo.getBucketName(),
                vo.getObjectName(), vo.getTargetName()));
    }

    /**
     * Native paged listing (delimiter + marker) — the folder-like view.
     *
     * @param vo page request
     * @return one page of objects / prefixes
     */
    @PostMapping("/selectObjectPage")
    @Operation(summary = "对象原生分页（delimiter 目录 + marker 续传）")
    public Response011<ObjectListing> selectObjectPage(@Valid @RequestBody StorageObjectPageVo011 vo, HttpServletRequest request) {
        String funcName = "select object page";
        Operator011 operator = Operator011Resolver.resolve(request);


        int limit = vo.getLimit() == null ? 100 : vo.getLimit();
        ObjectListing page = storageObjectUseCase.selectObjectPage(operator.id(), vo.getStorageCode(), vo.getBucketName(),
                vo.getPrefix(), vo.getDelimiter(), limit, vo.getMarker());

        return Response011.success(funcName, page);
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
    public Response011<String> removeObject(@Valid @RequestBody StorageObjectRefVo011 vo, HttpServletRequest request) {
        String funcName = "remove object";
        Operator011 operator = Operator011Resolver.resolve(request);


        storageObjectUseCase.remove(operator.id(), vo.getStorageCode(), vo.getBucketName(), vo.getObjectName());

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
    public Response011<String> batchRemoveObject(@Valid @RequestBody StorageObjectBatchRemoveVo011 vo, HttpServletRequest request) {
        String funcName = "batch remove object";
        Operator011 operator = Operator011Resolver.resolve(request);


        storageObjectUseCase.batchRemove(operator.id(), vo.getStorageCode(), vo.getBucketName(), vo.getObjectNames());

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
            @RequestParam("objectName") String objectName, HttpServletRequest request) {
        String funcName = "read object text";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.success(funcName, storageObjectUseCase.readText(operator.id(), storageCode, bucketName, objectName));
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
    public Response011<String> saveObjectText(@Valid @RequestBody StorageObjectSaveTextVo011 vo, HttpServletRequest request) {
        String funcName = "save object text";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.success(funcName, storageObjectUseCase.saveText(operator.id(), vo.getStorageCode(), vo.getBucketName(),
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
            @RequestParam("objectName") String objectName, HttpServletRequest request) {
        String funcName = "presign object url";
        Operator011 operator = Operator011Resolver.resolve(request);


        return Response011.success(funcName, storageObjectUseCase.presignedUrl(operator.id(), storageCode, bucketName,
                objectName));
    }
}
