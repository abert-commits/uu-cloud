package org.uu.wallet.Enum;

/**
 * usdt买入类型枚举
 * @author lukas
 */
public enum UsdtPayTypeEnum {
    TRC20("1", "TRC20"),;

    private final String code;

    private final String name;


    UsdtPayTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (OrderStatusEnum c : OrderStatusEnum.values()) {
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
