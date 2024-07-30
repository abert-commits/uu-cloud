package org.uu.common.pay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author admin
 * @date 2024/3/13 9:37
 */
@Data
@ApiModel(description = "充值列表返回")
public class CollectionOrderExportDTO  implements Serializable {
    @ApiModelProperty("会员Id")
    private String memberId;

    @ApiModelProperty("手机号")
    private String mobileNumber;


    /**
     * 商户名称
     */
    @ApiModelProperty("商户名称")
    private String merchantName;

    /**
     * 平台订单号
     */
    @ApiModelProperty("买入订单号")
    private String platformOrder;

    /**
     * 商户订单号
     */
    @ApiModelProperty("提现订单号")
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
    @ApiModelProperty(value = "iToken数量")
    private Integer itokenNumber;

    /**
     * 支付方式 默认值: UPI
     */
    @ApiModelProperty("支付方式")
    private String payType = "UPI";
    /**
     * 汇率
     */
    @ApiModelProperty(value = "汇率")
    private String exchangeRates;
    /**
     * 币种
     */
    @ApiModelProperty(value = "支付币种")
    private String currency;

    /**
     * 订单金额
     */
    @ApiModelProperty("转汇金额")
    private String amount;

    /**
     * 奖励
     */
    @ApiModelProperty("买入奖励")
    private String bonus;

    /**
     * 实际金额
     */
    @ApiModelProperty("实际金额")
    private String actualAmount;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 支付时间
     */
    @ApiModelProperty("支付时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentTime;

    /**
     * 完成时间
     */
    @ApiModelProperty("完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completionTime;

    /**
     * 完成时长
     */
    @ApiModelProperty("完成时长")
    private String completeDuration;


    /**
     * 交易回调状态 默认状态: 未回调
     */
    @ApiModelProperty("回调状态")
    private String tradeCallbackStatus;


    /**
     * 交易回调时间
     */
    @ApiModelProperty("回调时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime tradeCallbackTime;



    /**
     * 订单状态 默认状态: 待支付
     */
    @ApiModelProperty("订单状态")
    private String orderStatus;

    /**
     * 凭证
     */
    @ApiModelProperty("支付凭证")
    private String voucher;
}
