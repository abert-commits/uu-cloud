package org.uu.wallet.Enum;

/**
 * 消息状态
 */
public enum NotificationStatusEnum {

    //未读
    UNREAD("0", "unread"),

    //已读
    READ("1", "have read");

    private final String code;
    private final String name;

    NotificationStatusEnum(String code, String name) {
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
