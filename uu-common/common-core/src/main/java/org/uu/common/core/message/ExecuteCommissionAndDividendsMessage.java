package org.uu.common.core.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class ExecuteCommissionAndDividendsMessage implements Serializable {
    private static final long serialVersionUID = 8524337645418798641L;

    /**
     * 类型 1-买入返佣 2-卖出返佣 3-分红
     */
    private Integer type;

    /**
     * 源头用户ID
     */
    private Long fromMemberId;

    /**
     * 目标用户ID
     */
    private Long toMemberId;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 订单号
     */
    private String orderNo;
}
