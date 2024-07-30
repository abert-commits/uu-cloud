package org.uu.wallet.Enum;

public enum NotificationTypeEnum {

    //通知卖方
    NOTIFY_SELLER("1", "NOTIFY_SELLER"),

    //通知买方
    NOTIFY_BUYER("2", "NOTIFY_BUYER"),
    // 可以添加更多的通知类型

    //通知所有
    NOTIFY_ALL("3", "NOTIFY_ALL");

    private final String code;
    private final String name;

    NotificationTypeEnum(String code, String name) {
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
