package org.uu.wallet.Enum;

/*
 * 买入状态枚举
 * */
public enum AppealTypeEnum {
    WITHDRAW("1", "提现申诉"),
    RECHARGE("2", "充值申诉");


    private final String code;

    private final String name;


    AppealTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (AppealTypeEnum c : AppealTypeEnum.values()) {
            if (c.getCode().equals(code)) {
                return c.getName();
            }

        }
        return null;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
