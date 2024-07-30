package org.uu.common.pay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author
 */
@Data
@ApiModel(description = "代收订单对象")
public class RechargeOrderDTO implements Serializable {


    @ApiModelProperty(value = "id")
    private Long id;


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "订单时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "商户订单号")
    private String merchantOrder;

    @ApiModelProperty(value = "平台订单号")
    private String platformOrder;

    @ApiModelProperty(value = "支付方式")
    private String payType;

    @ApiModelProperty(value = "实际金额")
    private BigDecimal amount;

    /**
     * 订单金额
     */
    @ApiModelProperty(value = "订单金额")
    private BigDecimal orderAmount;


    @ApiModelProperty(value = "手续费")
    private BigDecimal cost;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    /**
     * 交易回调时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "订单回调时间")
    private LocalDateTime tradeCallbackTime;

    @ApiModelProperty(value = "订单状态")
    private String orderStatus;

    /**
     * 交易回调状态 默认状态: 未回调
     */
    @ApiModelProperty(value = "订单回调状态")
    private String tradeCallbackStatus;

    /**
     * 交易回调状态 默认状态: 未回调
     */
    @ApiModelProperty(value = "备注")
    private String remark;

    /**
     * 商户名称
     */
    @ApiModelProperty(value = "商户名称")
    private String merchantName;

    /**
     * 商户号
     */
    @ApiModelProperty(value = "商户号")
    private String merchantCode;

    /**
     * 完成时间
     */
    @ApiModelProperty(value = "完成时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING ,pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completionTime;


    /**
     * 钱包会员ID
     */
    @ApiModelProperty(value = "钱包会员ID")
    private String memberId;

    /**
     * 商户会员ID
     */
    @ApiModelProperty(value = "商户会员ID")
    private String externalMemberId;

    /**
     * 买入订单号
     */
    @ApiModelProperty(value = "买入订单号")
    private String buyOrderNo;

    @ApiModelProperty(value = "汇率")
    private BigDecimal exchangeRates;

    /**
     * 回调请求参数
     */
    @ApiModelProperty(value = "回调请求参数")
    private String tradeCallbackRequest;

    /**
     * 回调返回参数
     */
    @ApiModelProperty(value = "回调返回参数")
    private String tradeCallbackResponse;

    /**
     * itoken数量
     */
    @ApiModelProperty(value = "支付金额")
    private Integer itokenNumber;

    /**
     * 交易回调地址
     */
    @ApiModelProperty(value = "交易回调地址")
    private String tradeNotifyUrl;

    /**
     * USDT充值地址
     */
    @ApiModelProperty(value = "USDT充值地址")
    private String usdtAddr;

    /**
     * upiId
     */
    @ApiModelProperty(value = "upiId")
    private String upiId;
    /**
     * upiName
     */
    @ApiModelProperty(value = "upiName")
    private String upiName;

    /**
     * utr
     */
    @ApiModelProperty(value = "utr")
    private String utr;

    /**
     * 固定手续费
     */
    @ApiModelProperty(value = "固定手续费")
    private BigDecimal fixedFee;

    /**
     * 交易ID
     */
    @ApiModelProperty(value = "交易ID")
    private String txid;
}