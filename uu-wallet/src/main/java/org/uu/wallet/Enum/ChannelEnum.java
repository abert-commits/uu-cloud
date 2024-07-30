package org.uu.wallet.Enum;

public enum ChannelEnum {

    BANK("1", "银行卡"),
    USDT("2", "USDT"),
    UPI("3", "UPI"),
    INDIAN_TRX("6", "TRX");
    private final String code;

    private final String name;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getNameByCode(String code) {
        for (ChannelEnum c : ChannelEnum.values()) {
            if (c.getCode().equals(code)) {
                return c.getName();
            }

        }
        return null;
    }

    ChannelEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }


}
