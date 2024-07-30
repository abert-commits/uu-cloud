package org.uu.common.pay.dto;

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
public class MatchingOrderEnPageListDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    private Long id;


    /**
     * 充值平台订单号
     */
    @ApiModelProperty("Buy order number")
    private String collectionPlatformOrder;


    /**
     * 提现平台订单号
     */
    @ApiModelProperty("Sell order number")
    private String paymentPlatformOrder;

    /**
     * 订单提交金额
     */
    @ApiModelProperty("Order Amount")
    private BigDecimal orderSubmitAmount;

    /**
     * 订单实际金额
     */
    @ApiModelProperty("Actual Amount")
    private BigDecimal orderActualAmount;


    /**
     * 支付时间
     */
    @ApiModelProperty("Payment Time")
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
    @ApiModelProperty("State")
    private String status;

    /**
     *
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("Matching Time")
    private LocalDateTime createTime;

    /**
     * 修改人
     */
    @ApiModelProperty("Update By")
    private String updateBy;

    @ApiModelProperty("Completion Time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completionTime;

    /**
     * 提现会员ID
     */
    @ApiModelProperty("Seller Member ID")
    private String paymentMemberId;

    /**
     * 充值会员ID
     */
    @ApiModelProperty("Buyer Member ID")
    private String collectionMemberId;

    /**
     * 完成时长
     */
    @ApiModelProperty("Complete Duration")
    private String completeDuration;

    /**
     * 收款类型 1-UPI 2-银行卡
     */
    @ApiModelProperty(value = "collection type")
    private String payType;


}