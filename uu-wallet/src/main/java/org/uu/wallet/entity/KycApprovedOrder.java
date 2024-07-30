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

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 通过 KYC 验证完成的订单表
 * </p>
 *
 * @author
 * @since 2024-05-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("kyc_approved_order")
public class KycApprovedOrder extends BaseEntityOrder {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 买入订单号
     */
    private String buyerOrderId;

    /**
     * 卖出订单号
     */
    private String sellerOrderId;

    /**
     * 买入会员id
     */
    private String buyerMemberId;

    /**
     * 卖出会员id
     */
    private String sellerMemberId;

    /**
     * 收款人 UPI
     */
    private String recipientUpi;

    /**
     * 付款人 UPI
     */
    private String payerUpi;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * utr
     */
    private String utr;

    /**
     * 交易状态, 1: 表示成功
     */
    private String transactionStatus;

    /**
     * 交易类型, 1: 收入, 2: 支出
     */
    private String transactionType;

    /**
     * 银行交易时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime bankTransactionTime;

    /**
     * 钱包交易时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime walletTransactionTime;

    /**
     * kycId
     */
    private Long kycId;

    /**
     * 银行编码
     */
    private String bankCode;

    /**
     * 收款人账户
     */
    private String recipientAccount;

    /**
     * 收款人姓名
     */
    private String recipientName;

    /**
     * KYC订单号
     */
    private String orderId;


    @TableField(exist = false)
    @ApiModelProperty(value = "金额统计")
    private BigDecimal amountTotal;

}
