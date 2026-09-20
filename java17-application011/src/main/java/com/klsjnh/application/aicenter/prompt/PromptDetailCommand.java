package com.klsjnh.application.aicenter.prompt;

/*                PromptDetailCommand record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.20
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.20  prompt detail command
 *
 */

/**
 * One prompt detail command (business domain + content).
 *
 * @param domainCode  business domain
 * @param contentMode inline / storage
 * @param content     inline content
 * @param storageCode storage instance code (storage mode)
 * @param bucket      bucket (storage mode)
 * @param variables   variable declarations
 * @param sortOrder   sort order
 */

public record PromptDetailCommand(String domainCode, String contentMode, String content, String storageCode,
        String bucket, String variables, Integer sortOrder) {
}
