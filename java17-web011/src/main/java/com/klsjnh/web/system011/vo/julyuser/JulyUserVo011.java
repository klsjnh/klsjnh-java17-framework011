package com.klsjnh.web.system011.vo.julyuser;

/*                JulyUserVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july user vo 011 class
 *
 */

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * User response VO (detail and page rows) — the password hash is never
 * included.
 */

@Data
public class JulyUserVo011 {

    /** Primary key. */
    @Schema(description = "主键")
    private String id;

    /** Login account, unique. */
    @Schema(description = "登录账号（唯一）")
    private String userAccount;

    /** User name. */
    @Schema(description = "用户姓名")
    private String userName;

    /** Mobile number. */
    @Schema(description = "手机号")
    private String mobile;

    /** Email. */
    @Schema(description = "邮箱")
    private String email;

    /** Last login time. */
    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;

    /** Account status: 0 disabled / 1 enabled. */
    @Schema(description = "账号状态（0 禁用 / 1 启用）")
    private String status;

    /** Creator. */
    @Schema(description = "创建人")
    private String createBy;

    /** Last modifier. */
    @Schema(description = "最后修改人")
    private String updateBy;

    /** Create time. */
    @Schema(description = "创建日期")
    private LocalDateTime createTime;

    /** Update time. */
    @Schema(description = "最后修改日期")
    private LocalDateTime updateTime;
}
