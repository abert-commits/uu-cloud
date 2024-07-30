package org.uu.common.pay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author
 */
@Data
@ApiModel(description = "卖出订单列表返回")
public class PaymentOrderExportEnDTO implements Serializable {

    @ApiModelProperty("memberId")
    private String memberId;

    @ApiModelProperty("mobileNumber")
    private String mobileNumber;


    /**
     * 商户名称
     */
    @ApiModelProperty("merchantName")
    private String merchantName;

    /**
     * 平台订单号
     */
    @ApiModelProperty("seller order")
    private String platformOrder;

    /**
     * 商户订单号
     */
    @ApiModelProperty("recharge order")
    private String merchantOrder;

    /**
     * UTR
     */
    @ApiModelProperty("UTR")
    private String utr;



    /**
     * upiName
     */
    @ApiModelProperty(value = "UPI NAME")
    private String upiName;

    /**
     * upiId
     */
    @ApiModelProperty(value = "UPI ID")
    private String upiId;

    /**
     * itoken数量
     */
    @ApiModelProperty(value = "iToken")
    private Integer itokenNumber;

    /**
     * 支付方式 默认值: UPI
     */
    @ApiModelProperty("payType")
    private String payType = "UPI";
    /**
     * 汇率
     */
    @ApiModelProperty(value = "exchangeRates")
    private String exchangeRates;
    /**
     * 币种
     */
    @ApiModelProperty(value = "currency")
    private String currency;

    /**
     * 订单金额
     */
    @ApiModelProperty("amount")
    private String amount;

    /**
     * 奖励
     */
    @ApiModelProperty("bonus")
    private String bonus;

    /**
     * 实际金额
     */
    @ApiModelProperty("actualAmount")
    private String actualAmount;

    @ApiModelProperty("createTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 支付时间
     */
    @ApiModelProperty("paymentTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentTime;

    /**
     * 完成时间
     */
    @ApiModelProperty("completionTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completionTime;

    /**
     * 完成时长
     */
    @ApiModelProperty("completeDuration")
    private String completeDuration;


    /**
     * 交易回调状态 默认状态: 未回调
     */
    @ApiModelProperty("tradeCallbackStatus")
    private String tradeCallbackStatus;


    /**
     * 交易回调时间
     */
    @ApiModelProperty("tradeCallbackTime")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime tradeCallbackTime;



    /**
     * 订单状态 默认状态: 待支付
     */
    @ApiModelProperty("orderStatus")
    private String orderStatus;

    /**
     * 凭证
     */
    @ApiModelProperty("voucher")
    private String voucher;





}