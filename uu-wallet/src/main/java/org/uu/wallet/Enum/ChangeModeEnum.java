package org.uu.wallet.Enum;

/**
 * 账变类型
 */
public  enum ChangeModeEnum {

    ADD("add","收入"),
    SUB("sub","支出");

    private final String code;

    private final String name;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getNameByCode(String code) {
        for (ChangeModeEnum c : ChangeModeEnum.values()) {
            if (c.getCode().equals(code)) {
                return c.getName();
            }

        }
        return null;
    }






    ChangeModeEnum(String code, String name){
        this.code = code;
        this.name = name;
    }


}
