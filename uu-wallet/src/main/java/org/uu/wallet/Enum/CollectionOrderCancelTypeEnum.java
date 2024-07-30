package org.uu.wallet.Enum;

/**
 * 取消订单类型枚举
 *
 * @author Administrator
 */
public enum CollectionOrderCancelTypeEnum {
    /**
     * 取消订单
     */
    CancelOrder("1", "取消订单"),
    /**
     * 取消支付
     */
    CancelPay("2", "取消支付");

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    private final String code;

    private final String name;


    public static String getNameByCode(String code) {
        for (CollectionOrderCancelTypeEnum c : CollectionOrderCancelTypeEnum.values()) {
            if (c.getCode().equals(code)) {
                return c.getName();
            }

        }
        return null;
    }

    CollectionOrderCancelTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
