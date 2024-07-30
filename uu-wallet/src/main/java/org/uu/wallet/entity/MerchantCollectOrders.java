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
import org.uu.wallet.Enum.CollectionOrderStatusEnum;
import org.uu.wallet.Enum.NotifyStatusEnum;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 商户代收订单表
 * </p>
 *
 * @author
 * @since 2024-01-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("merchant_collect_orders")
public class MerchantCollectOrders extends BaseEntityOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商户订单号
     */
    private String merchantOrder;

    /**
     * 平台订单号
     */
    private String platformOrder;

    /**
     * 实际金额
     */
    private BigDecimal amount = BigDecimal.ZERO;

    /**
     * 订单费率
     */
    private BigDecimal orderRate;

    /**
     * 订单状态 默认 待支付
     */
    private String orderStatus = CollectionOrderStatusEnum.BE_PAID.getCode();

    /**
     * 交易回调状态 默认状态: 未回调
     */
    private String tradeCallbackStatus = NotifyStatusEnum.NOTCALLBACK.getCode();


    /**
     * 商户号
     */
    private String merchantCode;

    /**
     * 交易回调地址
     */
    private String tradeNotifyUrl;

    /**
     * 交易回调是否发送
     */
    private String tradeNotifySend;

    /**
     * 时间戳
     */
    private String timestamp;

    /**
     * 客户端ip
     */
    private String clientIp;

    /**
     * 交易回调时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime tradeCallbackTime;

    /**
     * 费用
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
     * 奖励
     */
    private Integer bonus;

    /**
     * 支付方式
     */
    private String payType;

    /**
     * 完成时长
     */
    private String completeDuration;

    /**
     * 商户名称
     */
    private String merchantName;

    /**
     * 商户类型
     */
    private String merchantType;

    /**
     * 支付时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentTime;

    /**
     * 完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completionTime;

    /**
     * 手动完成人
     */
    private String completedBy;

    /**
     * 取消人
     */
    private String cancelBy;

    /**
     * 申诉时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime appealTime;

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
     * 买入订单号
     */
    private String buyOrderNo;

    /**
     * 是否匹配钱包订单 1: 是, 0: 否
     */
    private Integer matched;

    /**
     * 订单金额
     */
    private BigDecimal orderAmount;

    /**
     * 是否自动完成 1:是, 0:否
     */
    private Integer autoCompleted;

    /**
     * 订单版本号
     */
    private Integer version;

    /**
     * utr
     */
    private String utr;

    /**
     * 卖出订单号
     */
    private String sellOrderNo;

    /**
     * 币种
     */
    private String currency;

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
     * USDT充值地址
     */
    private String usdtAddr;

    /**
     * 交易ID
     */
    private String txid;

    /**
     * upiId
     */
    private String upiId;
    /**
     * upiName
     */
    private String upiName;

    /**
     * 同步通知地址
     */
    private String syncNotifyAddress;
}
