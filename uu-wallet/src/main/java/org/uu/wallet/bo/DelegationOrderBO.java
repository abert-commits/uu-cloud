package org.uu.wallet.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DelegationOrderBO implements Serializable {

    /**
     * 订单ID
     */
//    private String orderId;

    /**
     * 会员ID
     */
    private String memberId;

    /**
     * 支付类型, 1:银行卡, 3:UPI, 5:银行卡和UPI
     */
//    private String paymentType;

    /**
     * 委托时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime delegationTime;

    /**
     * 订单状态: 1-匹配中, 2-已完成, 3-已取消
     */
//    private Integer status;

    /**
     * 委托金额
     */
    private BigDecimal amount;
}
