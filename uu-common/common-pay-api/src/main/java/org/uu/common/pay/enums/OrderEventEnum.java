package org.uu.common.pay.enums;

/**
 * @author lukas
 */

public enum OrderEventEnum {
    /**
     * 商户充值订单申请
     */
    MERCHANT_COLLECTION_ORDER_APPLICATION("1", "商户充值订单申请"),
    /**
     * 商户充值订单匹配
     */
    MERCHANT_COLLECTION_ORDER_MATCHING("2", "商户充值订单匹配"),
    /**
     * 商户充值订单完成
     */
    MERCHANT_COLLECTION_ORDER_SUCCESS("3", "商户充值订单完成"),

    /**
     * 商户提现订单申请
     */
    MERCHANT_PAYMENT_ORDER_APPLICATION("4", "商户提现订单申请"),
    /**
     * 商户提现订单匹配
     */
    MERCHANT_PAYMENT_ORDER_MATCHING("5", "商户提现订单匹配"),
    /**
     * 商户提现订单完成
     */
    MERCHANT_PAYMENT_ORDER_SUCCESS("6", "商户提现订单完成"),

    ;

    private final String code;

    public String getEventName() {
        return eventName;
    }

    public String getCode() {
        return code;
    }

    private final String eventName;

    OrderEventEnum(String code, String eventName) {
        this.code = code;
        this.eventName = eventName;
    }
}
