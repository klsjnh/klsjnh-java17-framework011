package com.klsjnh.application.aicenter.prompt;

/*                PromptSaveCommand record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.21
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.21  prompt save command (child of a business domain)
 *
 */

/**
 * Save command for one prompt row — the child of a business domain. Shared by
 * the child-only save (insertDetail / updateDetail) and the whole save
 * (saveWhole), so the two paths cannot drift.
 *
 * @param promptCode  prompt code, unique and immutable (ignored on update)
 * @param promptName  prompt name
 * @param scene       scene (inference / image / tts), classification only
 * @param contentMode content mode (inline / storage)
 * @param content     inline content; in storage mode the body to persist
 * @param storageCode storage instance code, storage mode only
 * @param bucket      bucket, storage mode only
 * @param variables   variable declarations, nullable
 * @param sortOrder   sort order, null falls back to the default
 * @param remark      remark, nullable
 * @param status      row status, null falls back to enabled
 */

public record PromptSaveCommand(String promptCode, String promptName, String scene, String contentMode, String content,
        String storageCode, String bucket, String variables, Integer sortOrder, String remark, String status) {
}
