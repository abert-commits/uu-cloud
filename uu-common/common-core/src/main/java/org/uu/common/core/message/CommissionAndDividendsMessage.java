package org.uu.common.core.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.uu.common.core.enums.MemberAccountChangeEnum;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 返佣和分红消息实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class CommissionAndDividendsMessage implements Serializable {
    private static final long serialVersionUID = -2287509894807830107L;

    /**
     * 用户ID
     */
    private Long uid;

    /**
     * 金额(账变金额)
     */
    private BigDecimal amount;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 账变类型
     */
    private MemberAccountChangeEnum changeType;
}
