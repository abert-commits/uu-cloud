package org.uu.job.Enum;

/*
 * 会员类型枚举
 * */
public enum MemberTypeEnum {
    INTERNAL_MERCHANT_MEMBER("1", "内部商户会员"),
    MERCHANT_MEMBER("2", "商户会员"),
    WALLET_MEMBER("3", "钱包会员");


    private final String code;

    private final String name;


    MemberTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (MemberTypeEnum c : MemberTypeEnum.values()) {
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
