package org.uu.wallet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.uu.wallet.Enum.OrderStatusEnum;
import org.uu.wallet.Enum.PayTypeEnum;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 匹配池
 *
 * @author
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("match_pool")
public class MatchPool extends BaseEntityOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 支付方式
     */
    private String payType;

    /**
     * 匹配订单号
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
     * 订单金额
     */
    private BigDecimal amount;

    /**
     * 最小限额
     */
    private BigDecimal minimumAmount;

    /**
     * 最大限额
     */
    private BigDecimal maximumAmount;

    /**
     * 已匹配订单数
     */
    private Integer orderMatchCount;

    /**
     * 进行中订单数
     */
    private Integer inProgressOrderCount;

    /**
     * 已完成订单数
     */
    private Integer completedOrderCount;

    /**
     * 已卖出金额
     */
    private BigDecimal soldAmount;

    /**
     * 剩余金额
     */
    private BigDecimal remainingAmount;


    /**
     * 剩余金额总和 (卖出余额)
     */
    @TableField(exist = false)
    private BigDecimal sumRemainingAmount = new BigDecimal(0);


    /**
     * 订单状态 默认值: 匹配中
     */
    private String orderStatus;

    /**
     * 匹配回调地址
     */
    private String matchNotifyUrl;

    /**
     * 匹配回调状态
     */
    private Integer matchCallbackStatus;

    /**
     * 匹配回调时间
     */
    private LocalDateTime matchCallbackTime;

    /**
     * 会员id
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
     * 匹配时长
     */
    private String matchDuration;

    /**
     * 完成时长
     */
    private String completeDuration;

    /**
     * 交易回调地址
     */
    private String tradeNotifyUrl;

    /**
     * 交易回调状态
     */
    private String tradeCallbackStatus;

    /**
     * 交易回调时间
     */
    private LocalDateTime tradeCallbackTime;

    /**
     * UTR
     */
    private String utr;

    /**
     * 凭证
     */
    private String voucher;

    /**
     * 收款信息id
     */
    private Long collectionInfoId;

    /**
     * 匹配时间戳 (每次匹配时 都要更新这个值) 避免MQ延时消息问题 还有定时任务兜底方案扫描 也是扫这个值
     */
    private Long lastUpdateTimestamp;


    /**
     * 匹配超时
     */
    private String matchTimeout;


    /**
     * 继续匹配
     */
    private String continueMatching;

    /**
     * 取消匹配
     */
    private String cancelMatching;

    /**
     * 用户头像
     */
    private Integer avatar;

    /**
     * 取消原因
     */
    private String cancellationReason;

    /**
     * 预计匹配时间
     */
    private Integer estimatedMatchTime;


    /**
     * 商户号
     */
    private String merchantCode;

    /**
     * 商户名称
     */
    private String merchantName;

    @ApiModelProperty(value = "订单金额总计")
    @TableField(exist = false)
    private BigDecimal amountTotal;

    @ApiModelProperty(value = "已卖出金额总计")
    @TableField(exist = false)
    private BigDecimal soldAmountTotal;

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
}