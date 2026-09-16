package com.klsjnh.web.lowcode011.vo;

/*                JulyMetadataServiceVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.15
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.15  july metadata service vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Service transport VO.
 */

@Data
public class JulyMetadataServiceVo011 {

    /** Service code. */
    @Schema(description = "服务编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String serviceCode;

    /** Service name. */
    @Schema(description = "服务名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String serviceName;

    /** Service description. */
    @Schema(description = "服务描述")
    private String serviceDescription;

    /** Service object type code. */
    @Schema(description = "服务对象类型（ServiceObjectType011）")
    private String objectType;

    /** Parameter type code. */
    @Schema(description = "参数类型（ServiceParamType011）")
    private String paramType;

    /** SQL / script content. */
    @Schema(description = "SQL / 脚本内容")
    private String serviceContent;

    /** Enabled flag. */
    @Schema(description = "是否启用")
    private Boolean enabled;

    /** Sort order. */
    @Schema(description = "排序（越小越靠前）")
    private Integer sortOrder;
}
