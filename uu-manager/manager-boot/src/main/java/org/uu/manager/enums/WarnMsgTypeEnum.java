package org.uu.manager.enums;

/*
* 告警消息类型
* */
public enum WarnMsgTypeEnum {
    /**
     * 短信余额不足
     */
    SMS_BALANCE_INSUFFICIENT("1", "SMS balance insufficient")
    ;


    private final String code;

    private final String name;


    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getNameByCode(String code) {
        for (WarnMsgTypeEnum c : WarnMsgTypeEnum.values()) {
            if (c.getCode().equals(code)) {
                return c.getName();
            }

        }
        return null;
    }

    WarnMsgTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
