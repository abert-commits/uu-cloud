package org.uu.common.pay.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@ApiModel(description = "卖出订单列表返回")
public class PaymentOrderListPageDTO implements Serializable {


    @ApiModelProperty("id")
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
     * 匹配订单号
     */
    @ApiModelProperty("匹配订单号")
    private String matchOrder;


    /**
     * 会员ID
     */
    @ApiModelProperty("会员ID")
    private String memberId;

    /**
     * 会员账号
     */
    @ApiModelProperty("会员账号")
    private String memberAccount;


    /**
     * 订单金额
     */
    @ApiModelProperty("订单金额")
    private BigDecimal amount;

    /**
     * 实际金额
     */
    @ApiModelProperty("实际金额")
    private BigDecimal actualAmount;

    /**
     * 订单状态 默认状态: 待匹配
     */
    @ApiModelProperty("订单状态")
    private String orderStatus;


    /**
     * 交易回调状态 默认状态: 未回调
     */
    @ApiModelProperty("交易回调状态")
    private String tradeCallbackStatus;


    /**
     * 商户名称
     */
    @ApiModelProperty("商户名称")
    private String merchantName;


    /**
     * 奖励
     */
    @ApiModelProperty("奖励")
    private BigDecimal bonus;

    /**
     * 匹配时长
     */
    @ApiModelProperty("匹配时长")
    private String matchDuration;

    /**
     * 完成时长
     */
    @ApiModelProperty("完成时长")
    private String completeDuration;


    /**
     * 凭证
     */
    @ApiModelProperty("凭证")
    private String voucher;

    /**
     * 备注
     */
    @ApiModelProperty("备注")
    private String remark;

    @ApiModelProperty("创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;


    @ApiModelProperty("完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completionTime;

    /**
     * UTR
     */
    @ApiModelProperty("UTR")
    private String utr;


    @ApiModelProperty(value = "风控标识-超时 0-正常 1-操作超时")
    private Integer riskTagTimeout;

    @ApiModelProperty(value = "风控标识-黑名单 0-正常 1-ip黑名单")
    private Integer riskTagBlack;

    @ApiModelProperty(value = "风控标识 0-正常 1-操作超时 2-ip黑名单 3-余额过低")
    private String riskTag;
    /**
     * 是否通过KYC自动完成 1: 是
     */
    @ApiModelProperty(value = "是否通过KYC自动完成 0：否 1: 是")
    private Integer kycAutoCompletionStatus;

    @ApiModelProperty(value = "支付方式 1-印度银行卡 2-印度USDT 3-印度upi 4-印度pix")
    private String payType;


    /**
     * itoken数量
     */
    @ApiModelProperty(value = "itoken数量")
    private Integer itokenNumber;
    /**
     * 汇率
     */
    @ApiModelProperty(value = "汇率")
    private BigDecimal exchangeRates;
    /**
     * 币种
     */
    @ApiModelProperty(value = "币种")
    private String currency;


    /**
     * UPI_ID
     */
    @ApiModelProperty(value = "upiId")
    private String upiId;

    /**
     * UPI_Name
     */
    @ApiModelProperty(value = "upiName")
    private String upiName;

    @ApiModelProperty(value = "手机号")
    private String mobileNumber;

    /**
     * 支付时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentTime;

    /**
     * 会员类型
     */
    @ApiModelProperty(value = "会员类型")
    private Integer memberType;

    /**
     * 代收商户订单号
     */
    @ApiModelProperty(value = "代收商户订单号")
    private String merchantCollectionOrder;

    /**
     * 回调时间
     */
    @ApiModelProperty(value = "回调时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime tradeCallbackTime;

    /**
     * 商户号
     */
    @ApiModelProperty(value = "商户号")
    private String merchantCode;

}