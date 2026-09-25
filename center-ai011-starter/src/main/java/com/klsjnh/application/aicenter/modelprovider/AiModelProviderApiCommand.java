package com.klsjnh.application.aicenter.modelprovider;

/*                AiModelProviderApiCommand record
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.22
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.22  provider api key save command (whole save child row)
 *
 */

/**
 * Save command for one provider api key row (child), used by the whole save.
 *
 * @param apiCode   api key code, unique within the provider
 * @param apiName   api key name
 * @param apiKey    api key value
 * @param sortOrder sort order, null falls back to the list position
 * @param remark    remark, nullable
 */

public record AiModelProviderApiCommand(String apiCode, String apiName, String apiKey, Integer sortOrder,
        String remark) {
}
