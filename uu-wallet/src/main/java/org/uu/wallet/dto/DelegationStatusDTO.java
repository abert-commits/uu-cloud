package org.uu.wallet.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class DelegationStatusDTO implements Serializable {

    /**
     * 委托状态
     */
    private String delegationStatus;

    /**
     * 用户余额
     */
    private BigDecimal balance;

}
