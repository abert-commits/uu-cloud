package org.uu.wallet.Enum;

/**
 * @author lukas
 */
public enum PayTypeV2Enum {
    BANK_CARD("1", "印度银行卡"),
    UPI("3", "印度upi"),
    USDT("2", "USDT"),
;



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

    PayTypeV2Enum(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
