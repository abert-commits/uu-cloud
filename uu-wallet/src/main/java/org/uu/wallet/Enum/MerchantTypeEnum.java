package org.uu.wallet.Enum;

/*
 * 商户类型枚举
 * */
public enum MerchantTypeEnum {


    INTERNAL_MERCHANT("1", "内部商户"),
    EXTERNAL_MERCHANT("2", "外部商户");


    private final String code;

    private final String name;


    MerchantTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (MerchantTypeEnum c : MerchantTypeEnum.values()) {
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
