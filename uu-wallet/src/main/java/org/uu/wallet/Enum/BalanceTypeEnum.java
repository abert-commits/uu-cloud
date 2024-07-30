package org.uu.wallet.Enum;

public enum BalanceTypeEnum {

    TRC20("1", "TRC20"),
    //法币余额
    FCB("3", "FCB"),

    TRX("2", "TRX");

    private final String code;

    private final String name;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getNameByCode(String code) {
        for (BalanceTypeEnum c : BalanceTypeEnum.values()) {
            if (c.getCode().equals(code)) {
                return c.getName();
            }

        }
        return null;
    }

    BalanceTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }


}
