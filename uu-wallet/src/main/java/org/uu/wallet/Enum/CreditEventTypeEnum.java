package org.uu.wallet.Enum;

/**
 * @author admin
 * @date 2024/4/9 9:47
 */
public enum CreditEventTypeEnum {
    /**
     * 信用分事件枚举
     */
    PAYMENT_TIMEOUT(1,  "支付超时"),
    AUTO_DONE(2, "自动完成"),
    APPEAL_SUCCESS(3, "提交申诉成功"),
    APPEAL_FAILED(4, "提交申诉失败"),
    BE_APPEAL_SUCCESS(5, "被申诉成功"),
    BE_APPEAL_FAILED(6, "被申诉失败"),
    OVERTIME(7, "超过48小时未处理"),
    MANUAL_DONE(8, "手动确认到账"),

    ;
    private final Integer code;
    private final String description;


    public Integer getCode() {
        return code;
    }


    public String getDescription() {
        return description;
    }

    CreditEventTypeEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }
}
