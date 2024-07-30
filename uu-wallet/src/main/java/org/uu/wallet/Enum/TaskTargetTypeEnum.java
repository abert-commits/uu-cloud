package org.uu.wallet.Enum;

/**
 * 任务目标类型枚举类
 *
 * @author admin
 * @date 2024/3/18 16:16
 */
public enum TaskTargetTypeEnum {
    TIMES("1", "次数"),
    MONEY("2", "金额");

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    private final String code;

    private final String name;

    TaskTargetTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

}
