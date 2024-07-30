package org.uu.wallet.Enum;

/*
 * 买入状态枚举
 * */
public enum UsdtBuyStatusEnum {
    ENABLE("1", "启用"),
    DISABLE("0", "禁用");


    private final String code;

    private final String name;


    UsdtBuyStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (UsdtBuyStatusEnum c : UsdtBuyStatusEnum.values()) {
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
