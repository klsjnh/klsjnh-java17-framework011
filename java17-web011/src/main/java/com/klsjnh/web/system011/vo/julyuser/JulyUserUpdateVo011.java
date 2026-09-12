package com.klsjnh.web.system011.vo.julyuser;

/*                JulyUserUpdateVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july user update vo 011 class
 *
 */

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Profile update request VO for a user (account and password excluded).
 */

@Data
public class JulyUserUpdateVo011 {

    /** Primary key. */
    @Schema(description = "主键", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    /** User name, max 60. */
    @Schema(description = "用户姓名（最长 60）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String userName;

    /** Mobile number. */
    @Schema(description = "手机号")
    private String mobile;

    /** Email. */
    @Schema(description = "邮箱")
    private String email;
}
