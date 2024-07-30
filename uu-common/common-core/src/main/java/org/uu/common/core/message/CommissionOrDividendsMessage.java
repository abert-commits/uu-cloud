package org.uu.common.core.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 返佣/分红消息实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class CommissionOrDividendsMessage implements Serializable {
    private static final long serialVersionUID = -2287509894807830107L;

    /**
     * 用户ID
     */
    private Long uid;

    /**
     * 返佣/分红金额
     */
    private BigDecimal amount;

    /**
     * 是否返佣 TRUE-是 FALSE-否
     */
    private Boolean isCommission = Boolean.TRUE;
}
