package org.uu.wallet.Enum;

/*
 * 支付类型枚举
 * */
public enum PayTypeEnum {

    INDIAN_CARD("1", "银行卡"),
    INDIAN_USDT("2", "USDT"),
    INDIAN_UPI("3", "upi"),
    INDIAN_TRX("6", "TRX"),
    INDIAN_CARD_UPI_FIX("5", "银行卡、UPI");



    private final String code;

    private final String name;


    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getNameByCode(String code) {
        for (PayTypeEnum c : PayTypeEnum.values()) {
            if (c.getCode().equals(code)) {
                return c.getName();
            }

        }
        return null;
    }

    PayTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
