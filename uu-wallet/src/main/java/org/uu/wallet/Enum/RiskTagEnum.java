package org.uu.wallet.Enum;

/*
 * 风控标记枚举
 * */
public enum RiskTagEnum {

    /**
     * 正常
     */
    Normal("0", "正常"),
    /**
     * 操作超时
     */
    ORDER_TIME_OUT("1", "操作超时"),
    /**
     * IP黑名单
     */
    BLACK_IP("2", "IP黑名单"),
    /**
     * 余额过低
     */
    INSUFFICIENT_BALANCE("3", "余额过低"),
;



    private final String code;

    private final String name;


    RiskTagEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (RiskTagEnum c : RiskTagEnum.values()) {
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
