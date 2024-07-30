package org.uu.wallet.Enum;

/*
 * 会员任务状态枚举
 * */
public enum MemberTaskStatusEnum {
    NOT_FINISH(0, "未完成"),
    TO_COLLECT(1, "待领取"),
    FINISHED(2, "已完成"),
    ;


    private final int code;

    private final String name;


    MemberTaskStatusEnum(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(int code) {
        for (MemberTaskStatusEnum c : MemberTaskStatusEnum.values()) {
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
