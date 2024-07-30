package org.uu.common.pay.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 退回订单表
 * </p>
 *
 * @author 
 * @since 2024-05-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(description = "退回订单列表信息")
public class CashBackOrderListPageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    /**
     * 商户订单号
     */
    @ApiModelProperty("商户订单号")
    private String merchantOrder;

    /**
     * 平台订单号
     */
    @ApiModelProperty("平台订单号")
    private String platformOrder;

    /**
     * 订单金额
     */
    @ApiModelProperty("订单金额")
    private BigDecimal amount;

    /**
     * 订单状态 1-退回中 2-退回成功 3-退回失败
     */
    @ApiModelProperty("订单状态 1-退回中 2-退回成功 3-退回失败")
    private String orderStatus;

    /**
     * 商户号
     */
    @ApiModelProperty("商户号")
    private String merchantCode;

    /**
     * 商户名称
     */
    @ApiModelProperty("商户名称")
    private String merchantName;

    /**
     * 商户会员id
     */
    @ApiModelProperty("商户会员id")
    private String merchantMemberId;

    /**
     * 钱包会员id
     */
    @ApiModelProperty("钱包会员id")
    private String memberId;

    /**
     * 请求时间戳
     */
    @ApiModelProperty("请求时间戳")
    private String requestTimestamp;

    /**
     * 完成时间戳
     */
    @ApiModelProperty("完成时间戳")
    private String responseTimestamp;

    /**
     * 完成时长
     */
    @ApiModelProperty("完成时长")
    private String completeDuration;


    /**
     * 备注
     */
    @ApiModelProperty("备注")
    private String remark;

    /**
     * 完成时间
     */
    @ApiModelProperty("完成时间")
    private LocalDateTime completionTime;

    /**
     * 失败时间
     */
    @ApiModelProperty("失败时间")
    private LocalDateTime failedTime;

    /**
     * 失败原因
     */
    @ApiModelProperty("失败原因")
    private String failedReason;

    @ApiModelProperty("创建时间")
    private LocalDateTime createTime;

    @ApiModelProperty("更新时间")
    private LocalDateTime updateTime;

    @ApiModelProperty("创建人")
    private String createBy;

    @ApiModelProperty("更新人")
    private String updateBy;

}
