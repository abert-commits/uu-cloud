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
@ApiModel(description = "代付订单对象")
public class WithdrawOrderDTO implements Serializable {


    @ApiModelProperty(value = "id")
    private Long id;

    /**
     * UPI_ID
     */
    @ApiModelProperty(value = "UPI")
    private String upiId;

    /**
     * UPI_Name
     */
    @ApiModelProperty(value = "UPI Name")
    private String upiName;


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "订单时间")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "商户订单号")
    private String merchantOrder;

    @ApiModelProperty(value = "平台订单号")
    private String platformOrder;

    @ApiModelProperty(value = "支付方式")
    private String payType;

    @ApiModelProperty(value = "订单金额")
    private BigDecimal orderAmount;

    @ApiModelProperty(value = "订单实际金额")
    private BigDecimal amount;

    @ApiModelProperty(value = "手续费")
    private BigDecimal cost;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "订单完成时间")
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
     * 商户号
     */
    @ApiModelProperty(value = "商户code")
    private String merchantCode;

    /**
     * 商户名称
     */
    @ApiModelProperty(value = "商户名称")
    private String merchantName;

    /**
     * 完成时间
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING ,pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "完成时间")
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
     * 汇率
     */
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
    @ApiModelProperty(value = "itokenNumber")
    private Integer itokenNumber;

    /**
     * 交易回调地址
     */
    @ApiModelProperty(value = "交易回调地址")
    private String tradeNotifyUrl;

    /**
     * 银行卡号
     */
    private String bankCardNumber;

    /**
     * 银行名称
     */
    private String bankName;

    /**
     * ifsc_code
     */
    private String ifscCode;

    /**
     * 持卡人姓名
     */
    private String bankCardOwner;


    @ApiModelProperty("usdt地址")
    private String usdtAddr;

    /**
     * 固定手续费
     */
    @ApiModelProperty("固定手续费")
    private BigDecimal fixedFee;

    /**
     * 交易ID
     */
    @ApiModelProperty("交易ID")
    private String txid;

}