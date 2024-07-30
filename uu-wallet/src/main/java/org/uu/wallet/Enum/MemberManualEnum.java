package org.uu.wallet.Enum;

/**
 * @author Admin
 */

/*
 * 会员操作枚举
 * */
public enum MemberManualEnum {
    UPPER_DIVISION("1", "人工上分", "UD"),
    LOWER_DIVISION("2", "人工下分", "LD"),
    FREEZE("3", "冻结", "DJ"),
    UNFREEZE("4", "解冻", "JD");


    private final String code;

    private final String name;

    private final String prefix;


    MemberManualEnum(String code, String name, String prefix) {
        this.code = code;
        this.name = name;
        this.prefix = prefix;
    }

    public static String getNameByCode(String code) {
        for (MemberManualEnum c : MemberManualEnum.values()) {
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

    public String getPrefix() {
        return prefix;
    }

}
