package com.klsjnh.web.system011.vo.julyuser;

/*                JulyUserResetPasswordVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july user reset password vo 011 class
 *
 */

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Admin password reset request VO for a user.
 */

@Data
public class JulyUserResetPasswordVo011 {

    /** User id. */
    @Schema(description = "用户 id", requiredMode = Schema.RequiredMode.REQUIRED)
    private String id;

    /** New raw password, stored as bcrypt on the server. */
    @Schema(description = "新密码（明文传输，服务端 bcrypt 存储）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
