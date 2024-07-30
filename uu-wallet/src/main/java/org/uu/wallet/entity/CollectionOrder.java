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
import org.uu.wallet.Enum.OrderStatusEnum;
import org.uu.wallet.Enum.PayTypeEnum;
import org.uu.wallet.Enum.SendStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("collection_order")
public class CollectionOrder extends BaseEntityOrder {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 支付方式
     */
    private String payType;

    /**
     * upiId
     */
    private String upiId;

    /**
     * upiName
     */
    private String upiName;

    /**
     * 代付平台订单号
     */
    private String merchantOrder;

    /**
     * 平台订单号
     */
    private String platformOrder;

    /**
     * 订单金额
     */
    private BigDecimal amount = BigDecimal.ZERO;;

    /**
     * 订单费率
     */
    private BigDecimal orderRate;

    /**
     * 订单状态 默认状态: 待支付
     */
    private String orderStatus = OrderStatusEnum.BE_PAID.getCode();

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
     * 时间戳
     */
    private String timestamp;

    /**
     * 客户端ip
     */
    private String clientIp;

    /**
     * 签名key
     */
    @TableField(exist = false)
    private String key;

    /**
     * 交易回调时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime tradeCallbackTime;

    /**
     * 支付时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentTime;

    /**
     * 订单费用
     */
    private BigDecimal cost;

    /**
     * 交易回调是否发送 默认值为: 未发送
     */
    private String tradeNotifySend = SendStatusEnum.UNSENT.getCode();

    /**
     * 会员id
     */
    private String memberId;

    /**
     * UTR
     */
    private String utr;

    /**
     * 奖励
     */
    private BigDecimal bonus = BigDecimal.ZERO;;

    /**
     * 实际金额
     */
    private BigDecimal actualAmount = BigDecimal.ZERO;;

    /**
     * 会员账号
     */
    private String memberAccount;

    /**
     * 完成时长
     */
    private String completeDuration;

    /**
     * 商户名称
     */
    private String merchantName;

    /**
     * 凭证
     */
    private String voucher;

    /**
     * 备注
     */
    private String remark;

    @ApiModelProperty("完成时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completionTime;


    @ApiModelProperty("手动完成人")
    private String completedBy;


    @ApiModelProperty("审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime appealReviewTime;


    @ApiModelProperty("审核人")
    private String appealReviewBy;


    @ApiModelProperty("取消时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelTime;


    @ApiModelProperty("取消人")
    private String cancelBy;

    /**
     * 取消原因
     */
    private String cancellationReason;

    /**
     * 撮合列表订单号
     */
    private String matchingPlatformOrder;

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
     * 是否取消支付
     */
    private String isPaymentCancelled;

    /**
     * 商户类型
     */
    private String merchantType;

    /**
     * 手机号
     */
    private String mobileNumber;

    /**
     * 取消类型
     */
    private String cancelType;

    @TableField(exist = false)
    @ApiModelProperty(value = "订单金额总计")
    private BigDecimal amountTotal;

    @TableField(exist = false)
    @ApiModelProperty(value = "实际金额总计")
    private BigDecimal actualAmountTotal;

    @TableField(exist = false)
    @ApiModelProperty(value = "奖励总计")
    private BigDecimal bonusTotal;

    @TableField(exist = false)
    @ApiModelProperty(value = "IToken数量总计")
    private BigDecimal ITokenTotal;

    @ApiModelProperty(value = "风控标识-黑名单 0-正常 1-操作超时 2-ip黑名单")
    private Integer riskTagBlack;

    /**
     * 随机码
     */
    @ApiModelProperty(value = "支付随机码")
    private String randomCode;

    /**
     * 人工审核截至时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditDelayTime;

    /**
     * 是否通过KYC自动完成 1: 是
     */
    private Integer kycAutoCompletionStatus;

    /**
     * 银行卡号
     */
    @ApiModelProperty("银行卡号")
    private String bankCardNumber;

    /**
     * 持卡人
     */
    @ApiModelProperty("持卡人姓名")
    private String bankCardOwner;

    /**
     * 银行名称
     */
    @ApiModelProperty(value = "银行名称")
    private String bankName;

    /**
     * ifsc_code
     */
    @ApiModelProperty("ifsc_code")
    private String ifscCode;

    /**
     * itoken数量
     */
    private BigDecimal itokenNumber;
    /**
     * 汇率
     */
    private BigDecimal exchangeRates;
    /**
     * 币种
     */
    private String currency;

    /**
     * 回调请求参数
     */
    private String tradeCallbackRequest;

    /**
     * 回调返回参数
     */
    private String tradeCallbackResponse;

    /**
     * 银行对应交易记录
     */
    private String kycTradeDetail;

    /**
     * 付款人 kyc bankName
     */
    private String kycBankName;

    /**
     * 付款人 kyc account
     */
    private String kycAccount;

    /**
     * kycId
     */
    private String kycId;

    /**
     * 会员类型
     */
    private Integer memberType;

    /**
     * 代付商户订单号
     */
    private String merchantPaymentOrder;


    public String getAmountStr() {
        return this.getAmount().toString();
    }
}