package org.uu.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.uu.wallet.Enum.NotifyStatusEnum;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 商户代付订单表
 * </p>
 *
 * @author
 * @since 2024-01-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("merchant_payment_orders")
public class MerchantPaymentOrders extends BaseEntityOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 支付方式
     */
    private String payType;

    /**
     * 商户订单号
     */
    private String merchantOrder;

    /**
     * 平台订单号
     */
    private String platformOrder;

    /**
     * 订单费率
     */
    private BigDecimal orderRate;

    /**
     * 实际金额
     */
    private BigDecimal amount;

    /**
     * 订单金额
     */
    private BigDecimal orderAmount;

    /**
     * 订单状态
     */
    private String orderStatus;


    /**
     * 商户号
     */
    private String merchantCode;

    /**
     * 商户名称
     */
    private String merchantName;

    /**
     * 订单费用
     */
    private BigDecimal cost = BigDecimal.ZERO;;

    /**
     * 会员id
     */
    private String memberId;

    /**
     * 商户会员id
     */
    private String externalMemberId;

    /**
     * 手机号
     */
    private String mobileNumber;

    /**
     * 时间戳
     */
    private String timestamp;

    /**
     * 请求IP
     */
    private String clientIp;

    /**
     * 奖励
     */
    private BigDecimal bonus;

    /**
     * 完成时长
     */
    private String completeDuration;

    /**
     * 交易回调地址
     */
    private String tradeNotifyUrl;

    /**
     * 交易回调状态 默认 未回调
     */
    private String tradeCallbackStatus = NotifyStatusEnum.NOTCALLBACK.getCode();

    /**
     * 交易回调时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime tradeCallbackTime;

    /**
     * 商户类型
     */
    private String merchantType;

    /**
     * 备注
     */
    private String remark;

    /**
     * 完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completionTime;

    /**
     * 支付时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentTime;

    /**
     * 取消人
     */
    private String cancelBy;

    /**
     * 取消时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelTime;

    @TableField(exist = false)
    @ApiModelProperty(value = "实际金额总计")
    private BigDecimal amountTotal;

    @TableField(exist = false)
    @ApiModelProperty(value = "奖励金额总计")
    private BigDecimal costTotal;

    @TableField(exist = false)
    @ApiModelProperty(value = "订单金额总计")
    private BigDecimal orderAmountTotal;


    /**
     * 币种
     */
    private String currency;


    /**
     * 买入订单号
     */
    private String buyOrderNo;

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

    /**
     * kycId
     */
    private String kycId;

    /**
     * 汇率
     */
    private BigDecimal exchangeRates;

    /**
     * 回调请求参数
     */
    private String tradeCallbackRequest;

    /**
     * 回调返回参数
     */
    private String tradeCallbackResponse;

    /**
     * itoken数量
     */
    private BigDecimal itokenNumber;

    /**
     * 固定手续费
     */
    private BigDecimal fixedFee;

    /**
     * 交易ID
     */
    private String txid;

    /**
     * USDT充值地址
     */
    private String usdtAddr;

    /**
     * 转账状态 0 未转账, 1: 已转账, 2: 转账成功, 3: 转账失败
     */
    private Integer transferStatus;
}
