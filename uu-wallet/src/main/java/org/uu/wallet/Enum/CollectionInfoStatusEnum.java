package org.uu.wallet.Enum;

/*
 * 收款信息状态枚举
 * */
public enum CollectionInfoStatusEnum {
    NORMAL("1", "正常"),
    CLOSE("0", "关闭");


    private final String code;

    private final String name;


    CollectionInfoStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (CollectionInfoStatusEnum c : CollectionInfoStatusEnum.values()) {
            if (c.getCode().equals(code)) {
                return c.getName();
            }

        }
        return null;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
