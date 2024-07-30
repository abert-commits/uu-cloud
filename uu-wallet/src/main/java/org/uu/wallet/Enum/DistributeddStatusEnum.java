package org.uu.wallet.Enum;

/*
 * 下发
 * */
public enum DistributeddStatusEnum {
    NODISTRIBUTED("2", "不同意"),
    FINISHED("1", "已下发"),
    NOFISHED("0", "未下发");


    private final String code;

    private final String name;


    DistributeddStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (DistributeddStatusEnum c : DistributeddStatusEnum.values()) {
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
