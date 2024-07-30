package org.uu.wallet.Enum;

/*
 * 商户状态枚举
 * */
public enum MerchantStatusEnum {


    NORMAL("1", "正常"),
    CLOSE("0", "关闭");


    private final String code;

    private final String name;


    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getNameByCode(String code) {
        for (MerchantStatusEnum c : MerchantStatusEnum.values()) {
            if (c.getCode().equals(code)) {
                return c.getName();
            }

        }
        return null;
    }

    MerchantStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
