package org.uu.common.pay.bo;

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
public class MemberAccountChangeBO implements Serializable {
    private static final long serialVersionUID = 7306102346606303706L;

    /**
     * 用户ID
     */
    private Long memberId;

    /**
     * 账变类型
     */
    private String changeType;

    /**
     * 账变金额
     */
    private BigDecimal amountChange;
}
