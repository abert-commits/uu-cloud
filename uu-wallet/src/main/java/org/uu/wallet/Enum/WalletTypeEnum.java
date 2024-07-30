package org.uu.wallet.Enum;

/*
 * 波场钱包账户枚举
 * */
public enum WalletTypeEnum {


    TRANSFER(1, "中转账户", "enum.walletType.transfer"),
    FUND(2, "资金账户", "enum.walletType.fund"),
    WITHDRAW(3, "出款账户", "enum.walletType.withdraw"),
    DEPOSIT(4, "储蓄账户", "enum.walletType.deposit"),
    C2C(9, "C2C账户", "enum.walletType.c2c");

    private final Integer code;

    private final String name;

    private final String remark;

    WalletTypeEnum(Integer code, String name, String remark) {
        this.code = code;
        this.name = name;
        this.remark = remark;
    }


    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getRemark() {
        return remark;
    }

}
