package org.uu.wallet.Enum;

/**
 * @author admin
 * @date 2024/4/9 9:47
 */
public enum CreditEventNameEnum {
    /**
     * 信用分事件枚举
     */
    BUY_PAYMENT_TIMEOUT(1, 1, "买入-支付超时", 1),
    BUY_AUTO_DONE(2, 1, "买入-自动完成", 2),
    BUY_APPEAL_SUCCESS(3, 1, "买入-申诉成功", 3),
    BUY_APPEAL_FAILED(4, 1, "买入-申诉失败", 4),
    BUY_BE_APPEAL_SUCCESS(5, 1, "买入-被申诉成功", 5),
    BUY_BE_APPEAL_FAILED(6, 1, "买入-被申诉失败", 6),
    SELL_OVERTIME(7, 2, "卖出-超过48小时未处理", 7),
    SELL_MANUAL_DONE(8, 2, "卖出-手动确认到账", 8),
    SELL_APPEAL_SUCCESS(3, 2, "卖出-申诉成功", 9),
    SELL_APPEAL_FAILED(4, 2, "卖出-申诉失败", 10),
    SELL_BE_APPEAL_SUCCESS(5, 2, "卖出-被申诉成功", 11),
    SELL_BE_APPEAL_FAILED(6, 2, "卖出-被申诉失败", 12),

    ;
    private final Integer eventType;
    private final Integer tradeType;
    private final Integer code;
    private final String description;

    public Integer getEventType() {
        return eventType;
    }

    public Integer getCode() {
        return code;
    }

    public Integer getTradeType() {
        return tradeType;
    }

    public String getDescription() {
        return description;
    }

    CreditEventNameEnum(Integer eventType, Integer tradeType, String description, Integer code) {
        this.eventType = eventType;
        this.tradeType = tradeType;
        this.code = code;
        this.description = description;
    }

    public static Integer getCodeByEventTypeAndTradeType(Integer eventType, Integer tradeType) {
        for (CreditEventNameEnum value : CreditEventNameEnum.values()) {
            if (eventType.equals(value.getEventType()) && tradeType.equals(value.getTradeType())) {
                return value.getCode();
            }
        }
        return null;
    }
}
