package org.uu.wallet.Enum;

/*
 * 风控订单类型枚举
 * */
public enum RiskOrderTypeEnum {

    /**
     * 正常
     */
    NORMAL("0", "正常"),
    /**
     * 买入订单异常
     */
    COLLECTION("1", "买入订单异常"),
    /**
     * 卖出订单异常
     */
    PAYMENT("2", "卖出订单异常"),
    /**
     * 都异常
     */
    ALL("3", "都异常");



    private final String code;

    private final String name;


    RiskOrderTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (RiskOrderTypeEnum c : RiskOrderTypeEnum.values()) {
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
