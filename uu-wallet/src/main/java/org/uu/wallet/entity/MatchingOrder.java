package org.uu.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.wallet.Enum.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 撮合列表订单
 *
 * @author
 */
@Data
@TableName("matching_order")
public class MatchingOrder extends BaseEntityOrder {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 支付方式
     */
    private String payType;

    /**
     * 充值商户订单号
     */
    private String collectionMerchantOrder;

    /**
     * 充值平台订单号
     */
    private String collectionPlatformOrder;

    /**
     * 提现商户订单号
     */
    private String paymentMerchantOrder;

    /**
     * 提现平台订单号
     */
    private String paymentPlatformOrder;

    /**
     * 订单提交金额
     */
    private BigDecimal orderSubmitAmount;

    /**
     * 订单实际金额
     */
    private BigDecimal orderActualAmount;

    /**
     * 充值商户号
     */
    private String collectionMerchantCode;

    /**
     * 提现商户号
     */
    private String paymentMerchantCode;

    /**
     * 充值会员ID
     */
    private String collectionMemberId;

    /**
     * 充值会员账号
     */
    private String collectionMemberAccount;

    /**
     * 提现会员ID
     */
    private String paymentMemberId;

    /**
     * 提现会员账号
     */
    private String paymentMemberAccount;

    /**
     * 充值交易状态 默认值为: 待支付
     */
    private String payStatus = CollectionOrderStatusEnum.BE_PAID.getCode();

    /**
     * 提现交易状态 默认值为: 处理中
     */
    private String paymentStatus = PaymentOrderStatusEnum.HANDLING.getCode();

    /**
     * 匹配回调是否发送 默认值为: 未发送
     */
    private String matchSend = SendStatusEnum.UNSENT.getCode();

    /**
     * 充值交易回调是否发送 默认值为: 未发送
     */
    private String collectionTradeSend = SendStatusEnum.UNSENT.getCode();

    /**
     * 提现交易回调是否发送 默认值为: 未发送
     */
    private String paymentTradeSend = SendStatusEnum.UNSENT.getCode();

    /**
     * 匹配回调地址
     */
    private String matchNotifyUrl;

    /**
     * 匹配回调状态
     */
    private String matchCallbackStatus = NotifyStatusEnum.NOTCALLBACK.getCode();

    /**
     * 匹配回调时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime matchCallbackTime;

    /**
     * 充值交易回调地址
     */
    private String collectionTradeNotifyUrl;

    /**
     * 充值交易回调状态
     */
    private String collectionTradeCallbackStatus = NotifyStatusEnum.NOTCALLBACK.getCode();

    /**
     * 充值交易回调时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime collectionTradeCallbackTime;

    /**
     * 提现交易回调地址
     */
    private String paymentTradeNotifyUrl;

    /**
     * 提现交易回调状态
     */
    private String paymentTradeCallbackStatus = NotifyStatusEnum.NOTCALLBACK.getCode();

    /**
     * 提现交易回调时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentTradeCallbackTime;

    /**
     * 签名key
     */
    @TableField(exist = false)
    private String key;

    /**
     * 凭证
     */
    private String voucher;

    /**
     * 支付时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentTime;


    /**
     * 完成时长
     */
    private String completeDuration;

    /**
     * UPI_ID
     */
    private String upiId;

    /**
     * UPI_Name
     */
    private String upiName;

    /**
     * UPI_Name
     */
    private String utr;

    /**
     * 订单状态: 默认值 待支付
     */
    private String status = OrderStatusEnum.BE_PAID.getCode();

    private String remark;

    /**
     * 完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completionTime;
    /**
     * 完成人
     */
    private String completedBy;
    /**
     *
     */
    private String appealReviewBy;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime appealReviewTime;

    /**
     * 取消人
     */
    private String cancelBy;

    /**
     * 取消时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelTime;

    /**
     * 撮合列表订单号
     */
    private String platformOrder;

    /**
     * 金额错误图片
     */
    private String amountErrorImage;

    /**
     * 金额错误视频
     */
    private String amountErrorVideo;

    /**
     * 金额错误提交时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime amountErrorSubmitTime;

    /**
     * 申诉时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime appealTime;

    /**
     * 取消原因
     */
    private String cancellationReason;

    /**
     * 显示申诉类型 1: 未到账  2: 金额错误
     */
    private Integer displayAppealType;

    @ApiModelProperty(value = "风控标识-超时 0-正常 1-操作超时")
    private Integer riskTagTimeout;

    @ApiModelProperty(value = "风控标识-黑名单 0-正常 1-黑名单")
    private Integer riskTagBlack;

    @ApiModelProperty(value = "风控标识订单类型 0-正常 1-买入订单异常 2-卖出订单异常 3-都异常")
    private Integer riskOrderType;

    @TableField(exist = false)
    @ApiModelProperty(value = "订单金额总计")
    private BigDecimal orderSubmitAmountTotal;

    @TableField(exist = false)
    @ApiModelProperty(value = "实际金额总计")
    private BigDecimal orderActualAmountTotal;

    /**
     * 人工审核截至时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditDelayTime;

    /**
     * 是否通过KYC自动完成 1: 是
     */
    private Integer kycAutoCompletionStatus;

}