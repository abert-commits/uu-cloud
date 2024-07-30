package org.uu.common.pay.dto;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 会员手动操作记录
 * </p>
 *
 * @author 
 * @since 2024-02-29
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("member_manual_log")
public class MemberManualLogDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 操作类型: 1-上分,2-下分,3-冻结,4-解冻
     */
    @ApiModelProperty("操作类型: 1-上分,2-下分,3-冻结,4-解冻")
    private Integer opType;

    /**
     * 操作金额
     */
    @ApiModelProperty("操作金额")
    private BigDecimal amount;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    @ApiModelProperty("创建人")
    private String createBy;

    /**
     * 修改人
     */
    private String updateBy;

    /**
     * 备注
     */
    @ApiModelProperty("备注")
    private String remark;

    /**
     * 会员id
     */
    @ApiModelProperty("会员id")
    private String memberId;


}
