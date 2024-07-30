package org.uu.wallet.Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommissionFlagEnum {
    COMMISSION_NO(0, "未返佣"),

    COMMISSION_YES(1, "已返佣");

    private final Integer commissionFlag;

    private final String desc;
}
