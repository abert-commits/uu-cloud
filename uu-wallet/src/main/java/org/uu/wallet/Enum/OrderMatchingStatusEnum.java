package org.uu.wallet.Enum;

/*
 * 订单匹配状态枚举
 * */
public enum OrderMatchingStatusEnum {
    HANDLING("1", "匹配中"),
    SUCCESS("2", "匹配成功"),
    FAILED("3", "匹配失败");


    private final String code;

    private final String name;


    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getNameByCode(String code) {
        for (OrderMatchingStatusEnum c : OrderMatchingStatusEnum.values()) {
            if (c.getCode().equals(code)) {
                return c.getName();
            }

        }
        return null;
    }

    OrderMatchingStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
