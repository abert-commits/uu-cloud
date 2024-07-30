package org.uu.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.uu.wallet.Enum.NotifyStatusEnum;
import org.uu.wallet.Enum.OrderStatusEnum;
import org.uu.wallet.Enum.PayTypeEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author
 */
@Data
@TableName("payment_order")
public class PaymentOrder extends BaseEntityOrder {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;


    /**
     * 支付方式
     */
    private String payType;

    /**
     * 商户号
     */
    private String merchantCode;

    /**
     * 代收订单号
     */
    private String merchantOrder;

    /**
     * 平台订单号
     */
    private String platformOrder;

    /**
     * 委托订单号
     */
    private String matchOrder;

    /**
     * UPI_ID
     */
    private String upiId;

    /**
     * UPI_Name
     */
    private String upiName;

    /**
     * 会员ID
     */
    private String memberId;

    /**
     * 会员账号
     */
    private String memberAccount;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 手机号
     */
    private String mobileNumber;

    /**
     * 订单金额
     */
    private BigDecimal amount = BigDecimal.ZERO;

    /**
     * 实际金额
     */
    private BigDecimal actualAmount = BigDecimal.ZERO;

    /**
     * 订单状态 默认状态: 待支付
     */
    private String orderStatus;

    /**
     * 商户名称
     */
    private String merchantName;

    /**
     * 时间戳
     */
    private String timestamp;

    /**
     * 客户端ip
     */
    private String clientIp;

    /**
     * 奖励
     */
    private BigDecimal bonus = BigDecimal.ZERO;;

    /**
     * 完成时长
     */
    private String completeDuration;

    /**
     * UTR
     */
    private String utr;


    /**
     * 备注
     */
    private String remark;

    /**
     * 收款信息id
     */
    private Long collectionInfoId;

    /**
     * 完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completionTime;

    /**
     * 匹配时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime matchTime;

    /**
     * 支付时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentTime;

    /**
     * 申诉审核人
     */
    private String appealReviewBy;

    /**
     * 申诉审核时间
     */
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
    private String matchingPlatformOrder;

    /**
     * 申诉时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime appealTime;


    /**
     * 是否通过KYC自动完成 1: 是
     */
    private Integer kycAutoCompletionStatus;

    /**
     * 商户类型
     */
    private String merchantType;


    @ApiModelProperty(value = "风控标识-超时 0-正常 1-操作超时")
    private Integer riskTagTimeout;

    @ApiModelProperty(value = "风控标识-黑名单 0-正常 1-黑名单")
    private Integer riskTagBlack;

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

    /**
     * 人工审核截至时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditDelayTime;

    /**
     * 银行卡收款信息ID
     */
    private Long bankCollectionInfoId;

    /**
     * UPI收款信息ID
     */
    private Long upiCollectionInfoId;

    /**
     * 银行卡号
     */
    private String bankCardNumber;

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
     * 回调状态
     */
    private String tradeCallbackStatus;

    /**
     * 回调地址
     */
    private String tradeNotifyUrl;

    /**
     * 回调是否发送
     */
    private String tradeNotifySend;

    /**
     * 回调时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime tradeCallbackTime;

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
     * kycId
     */
    private String kycId;

    /**
     * 会员类型
     */
    private Integer memberType;

    /**
     * 代收商户订单号
     */
    private String merchantCollectionOrder;


    public String getAmountStr() {
        return this.getAmount().toString();
    }

}