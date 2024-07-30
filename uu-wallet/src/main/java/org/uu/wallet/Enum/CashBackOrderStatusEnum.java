package org.uu.wallet.Enum;

/**
 * @author admin
 * @date 2024/5/10 10:08
 */
public enum CashBackOrderStatusEnum {
    /**
     * 余额退回状态枚举
     */
    CASH_BACK_PROCESSING("1", "退回中"),
    CASH_BACK_SUCCESS("2", "退回成功"),
    CASH_BACK_FAILED("3", "退回失败"),
    ;

    private final String code;

    public String getDes() {
        return des;
    }

    public String getCode() {
        return code;
    }

    private final String des;
    CashBackOrderStatusEnum(String code, String des) {
        this.code = code;
        this.des = des;
    }
}
