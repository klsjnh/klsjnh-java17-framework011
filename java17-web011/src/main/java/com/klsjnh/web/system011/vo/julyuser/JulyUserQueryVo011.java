package com.klsjnh.web.system011.vo.julyuser;

/*                JulyUserQueryVo011 class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  july user query vo 011 class
 *
 */

import lombok.Data;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Page query request VO for users.
 */

@Data
public class JulyUserQueryVo011 {

    /** Page index, starts at 1. */
    @Schema(description = "页码，从 1 开始")
    private Integer pageIndex;

    /** Page size, default 10. */
    @Schema(description = "每页条数，默认 10")
    private Integer pageSize;

    /** Login account keyword (fuzzy). */
    @Schema(description = "登录账号关键字（模糊）")
    private String userAccount;

    /** User name keyword (fuzzy). */
    @Schema(description = "用户姓名关键字（模糊）")
    private String userName;
}
