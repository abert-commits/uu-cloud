package org.uu.wallet.Enum;

/*
 * 委托状态枚举类
 * */
public enum DelegationStatusEnum {
    DELEGATIONSUCCESS("1", "委托成功"),
    DELEGATIONFAIL("0", "委托失败");


    private final String code;

    private final String name;


    DelegationStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (DelegationStatusEnum c : DelegationStatusEnum.values()) {
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
