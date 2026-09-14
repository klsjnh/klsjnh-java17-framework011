package com.klsjnh.infrastructure.persistence.entity;

/*                BasePo class
 *
 *      @author     xiangrkrs@163.com
 *      @version    ver 0.0.1
 *      @createdate 2026.09.12
 *      @modifydate
 *
 *===========================================
 *          modify history
 *
 *      2026.09.12  base po class
 *
 */

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Persistence base PO mapped to the base columns
 * (id / status / create_by / update_by / create_time / update_time / dr).
 */

@Data
public class BasePo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Primary key, 32-char UUID, auto assigned on insert when blank. */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /** Status, '1' enabled / '0' disabled. */
    private String status;

    /** Creator, filled from the request-scoped operator attribute on insert. */
    @TableField(fill = FieldFill.INSERT)
    private String createBy;

    /** Last modifier, filled from the request-scoped operator attribute on insert and update. */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    /** Create time, filled by the audit handler on insert. */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** Update time, filled by the audit handler on insert and update. */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** Logic delete flag; queries auto filter dr='0', delete sets dr='1'. */
    @TableLogic(value = "0", delval = "1")
    private String dr;
}
