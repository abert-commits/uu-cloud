package org.uu.wallet.Enum;

/*
 * 会员在线状态枚举
 * */
public enum MemberOnlineStatusEnum {
    ON_LINE("1", "在线"),
    OFF_LINE("0", "离线");


    private final String code;

    private final String name;


    MemberOnlineStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (MemberOnlineStatusEnum c : MemberOnlineStatusEnum.values()) {
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
