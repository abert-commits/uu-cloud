package org.uu.common.pay.dto;


import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(description = "申诉返回")
public class ApplyDistributedDTO implements Serializable {
    @ApiModelProperty("主键")
    private Long id;


    /**
     * 订单号
     */
    @ApiModelProperty("订单号")
    private String orderNo;

    /**
     * 商户
     */
    @ApiModelProperty("商户号")
    private String merchantCode;

    /**
     * 商户名称
     */
    @ApiModelProperty("商户名称")
    private String username;

    /**
     * 下发usdt地址
     */
    @ApiModelProperty("下发usdt地址")
    private String usdtAddr;

    /**
     * 币种
     */
    @ApiModelProperty("币种")
    private String currence;

    /**
     * 总额度
     */
    @ApiModelProperty("总额度")
    private BigDecimal balance;
    @ApiModelProperty("上分或者下发金额")
    private BigDecimal amount;


    /**
     * 创建人
     */
    @ApiModelProperty("创建人")
    private String createBy;
    @ApiModelProperty("备注")
    private String remark;

    /**
     * 状态
     */
    @ApiModelProperty("状态 0未下发  1已下发 2 不同意")
    private String status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    /**
     * 修改人
     */
    @ApiModelProperty("修改人")
    private String updateBy;


}