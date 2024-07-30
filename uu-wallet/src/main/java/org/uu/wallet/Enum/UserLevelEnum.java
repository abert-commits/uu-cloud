package org.uu.wallet.Enum;


/**
 * @author Admin
 */

/*
* 用户级别枚举
* */
public enum UserLevelEnum {
    NORMAL(0, "低信用", "PT"),
    QING_TONG(1, "青铜", "QT"),
    BAI_YIN(2, "白银", "BY"),
    HUANG_JIN(3, "黄金", "HJ"),
    BO_JIN(4, "铂金", "BJ"),
    ZUAN_SHI(5, "钻石", "ZS");


    private final Integer code;

    private final String name;

    private final String prefix;


    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public static String getNameByCode(String code) {
        for (UserLevelEnum c : UserLevelEnum.values()) {
            if (c.getCode().equals(code)) {
                return c.getName();
            }
        }
        return null;
    }

    UserLevelEnum(Integer code, String name, String prefix) {
        this.code = code;
        this.name = name;
        this.prefix = prefix;
    }


}
