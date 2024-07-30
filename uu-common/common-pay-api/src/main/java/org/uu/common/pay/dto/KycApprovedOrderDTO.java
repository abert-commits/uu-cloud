package org.uu.common.pay.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
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
@ApiModel(description = "验证完成的订单")
public class KycApprovedOrderDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("id")
    private Long id;

    /**
     * 买入订单号
     */
    @ApiModelProperty("买入订单号")
    private String buyerOrderId;

    /**
     * 卖出订单号
     */
    @ApiModelProperty("卖出订单号")
    private String sellerOrderId;

    /**
     * 买入会员id
     */
    @ApiModelProperty("买入会员id")
    private String buyerMemberId;

    /**
     * 卖出会员id
     */
    @ApiModelProperty("卖出会员id")
    private String sellerMemberId;

    /**
     * 收款人 UPI
     */
    @ApiModelProperty("收款人UPI")
    private String recipientUpi;

    /**
     * 付款人 UPI
     */
    @ApiModelProperty("付款人UPI")
    private String payerUpi;

    /**
     * 金额
     */
    @ApiModelProperty("金额")
    private BigDecimal amount;

    /**
     * utr
     */
    @ApiModelProperty("utr")
    private String utr;

    /**
     * 交易状态, 1: 表示成功
     */
    @ApiModelProperty("交易状态, 1: 表示成功")
    private String transactionStatus;

    /**
     * 交易类型, 1: 收入, 2: 支出
     */
    @ApiModelProperty("交易类型, 1: 收入, 2: 支出")
    private String transactionType;

    /**
     * 银行交易时间
     */
    @ApiModelProperty("银行交易时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime bankTransactionTime;

    /**
     * 钱包交易时间
     */
    @ApiModelProperty("钱包交易时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime walletTransactionTime;

    /**
     * kycId
     */
    @ApiModelProperty("kycId")
    private Long kycId;

    /**
     * 银行编码
     */
    @ApiModelProperty("银行编码")
    private String bankCode;

    /**
     * 收款人账户
     */
    @ApiModelProperty("收款人账户")
    private String recipientAccount;

    /**
     * 收款人姓名
     */
    @ApiModelProperty("收款人姓名")
    private String recipientName;

    /**
     * KYC订单号
     */
    @ApiModelProperty("KYC订单号")
    private String orderId;

}
