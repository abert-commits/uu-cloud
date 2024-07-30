package org.uu.wallet.Enum;

/**
 * 付款单通知 卖出成功通知 卖出失败通知 买入成功通知 买入失败通知
 */
public enum NotificationTitleEnum {

    //付款单通知
    NOTIFY_RECEIPT("1", "NOTIFY_RECEIPT"),

    //卖出成功通知
    NOTIFY_SELL_SUCCESS("2", "NOTIFY_SELL_SUCCESS"),

    //卖出失败通知
    NOTIFY_SELL_FAIL("3", "NOTIFY_SELL_FAIL"),

    //买入成功通知
    NOTIFY_BUY_SUCCESS("2", "NOTIFY_BUY_SUCCESS"),

    //买入失败通知
    NOTIFY_BUY_FAIL("3", "NOTIFY_BUY_FAIL");


    private final String code;
    private final String name;

    NotificationTitleEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
