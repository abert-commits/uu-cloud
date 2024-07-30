package org.uu.manager.enums;

/*
* 账变类型枚举
* */
public enum AccountChangeEnum {
    COLLECTION("1", "代收"),
    PAYMENT("2", "代付"),
    WITHDRAW("3", "下发"),
    RECHARGE("4", "上分");


    private final String code;

    private final String name;


    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getNameByCode(String code) {
        for (AccountChangeEnum c : AccountChangeEnum.values()) {
            if (c.getCode().equals(code)) {
                return c.getName();
            }

        }
        return null;
    }

    AccountChangeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
