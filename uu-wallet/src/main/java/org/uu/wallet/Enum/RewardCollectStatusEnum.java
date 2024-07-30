package org.uu.wallet.Enum;

/*
 * 奖励领取状态枚举
 * */
public enum RewardCollectStatusEnum {
    NOT_COLLECT(0, "未领取"),
    COLLECTED(1, "已领取"),
    ;


    private final int code;

    private final String name;


    RewardCollectStatusEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(int code) {
        for (RewardCollectStatusEnum c : RewardCollectStatusEnum.values()) {
            if (c.getCode() == code) {
                return c.getName();
            }

        }
        return null;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
