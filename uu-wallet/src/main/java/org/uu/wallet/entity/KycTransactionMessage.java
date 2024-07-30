package org.uu.wallet.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;


/**
 * 获取KYC银行交易记录 消息实体
 *
 * @author Simon
 * @date 2024/04/27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KycTransactionMessage implements Serializable {

    /**
     * 买方会员ID
     */
    private Long buyerMemberId;

    /**
     * 买方会员账号
     */
    private String buyerMemberAccount;

    /**
     * 卖方会员ID
     */
    private Long sellerMemberId;

    /**
     * 卖方会员账号
     */
    private String sellerMemberAccount;

    /**
     * 付款人UPI
     */
//    private String payerUPI;

    /**
     * 收款人UPI
     */
    private String recipientUPI;

    /**
     * 订单金额
     */
    private BigDecimal amount;

    /**
     * 交易 UTR
     */
    private String transactionUTR;

    /**
     * 交易时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime transactionTime;

    /**
     * 买入订单号
     */
    private String buyerOrderId;

    /**
     * 卖出订单号
     */
    private String sellerOrderId;
}
