package org.uu.common.pay.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 匹配订单记录表
 *
 * @author
 */
@Data
@ApiModel(description = "匹配订单返回")
public class MatchingOrderPageListDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    private Long id;


    /**
     * 充值平台订单号
     */
    @ApiModelProperty("买入订单号")
    private String collectionPlatformOrder;


    /**
     * 提现平台订单号
     */
    @ApiModelProperty("卖出订单号")
    private String paymentPlatformOrder;

    /**
     * 订单提交金额
     */
    @ApiModelProperty("订单金额")
    private BigDecimal orderSubmitAmount;

    /**
     * 订单实际金额
     */
    @ApiModelProperty("订单实际金额")
    private BigDecimal orderActualAmount;


    /**
     * 支付时间
     */
    @ApiModelProperty("支付时间")
    private LocalDateTime paymentTime;



    /**
     * UPI_ID
     */
    @ApiModelProperty("UPI ID")
    private String upiId;

    /**
     * UPI_Name
     */
    @ApiModelProperty("UPI NAME")
    private String upiName;

    @ApiModelProperty("UTR")
    private String utr;


    /**
     * 状态
     */
    @ApiModelProperty("撮合订单状态")
    private String status;

    /**
     *
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("撮合时间")
    private LocalDateTime createTime;

    /**
     * 修改人
     */
    @ApiModelProperty("最后更新人")
    private String updateBy;

    @ApiModelProperty("完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completionTime;

    /**
     * 提现会员ID
     */
    @ApiModelProperty("卖出会员ID")
    private String paymentMemberId;

    /**
     * 充值会员ID
     */
    @ApiModelProperty("买入会员ID")
    private String collectionMemberId;

    /**
     * 完成时长
     */
    @ApiModelProperty("完成时长")
    private String completeDuration;

    /**
     * 显示申诉类型 1: 未到账  2: 金额错误
     */
    @ApiModelProperty("显示申诉类型 1: 未到账  2: 金额错误")
    private Integer displayAppealType;


    @ApiModelProperty(value = "风控标识-超时 0-正常 1-操作超时")
    private Integer riskTagTimeout;

    @ApiModelProperty(value = "风控标识-黑名单 0-正常 1-ip黑名单")
    private Integer riskTagBlack;

    @ApiModelProperty(value = "风控标识订单类型 0-正常 1-买入订单异常 2-卖出订单异常 3-都异常")
    private Integer riskOrderType;

    @ApiModelProperty(value = "风控标识 0-正常 1-操作超时 2-ip黑名单 3-余额过低")
    private String riskTag;

    @ApiModelProperty(value = "审核秒数")
    private String auditSeconds;

    /**
     * 是否通过KYC自动完成 1: 是
     */
    @ApiModelProperty(value = "是否通过KYC自动完成 0：否 1: 是")
    private Integer kycAutoCompletionStatus;

    /**
     * 支付方式 默认值: UPI
     */
    @ApiModelProperty(value = "支付方式")
    private String payType;

}