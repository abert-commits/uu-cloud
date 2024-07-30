package org.uu.wallet.Enum;

public enum SendStatusEnum {

    UNSENT("0", "未发送"),
    HAS_BEEN_SENT("1", "已发送");


    private final String code;

    private final String name;


    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getNameByCode(String code) {
        for (SendStatusEnum c : SendStatusEnum.values()) {
            if (c.getCode().equals(code)) {
                return c.getName();
            }

        }
        return null;
    }

    SendStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

}
