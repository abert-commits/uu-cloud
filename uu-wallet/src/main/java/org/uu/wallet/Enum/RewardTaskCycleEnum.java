package org.uu.wallet.Enum;

/**
 * @author admin
 * @date 2024/3/18 16:33
 */
public enum RewardTaskCycleEnum {
    ONCE("1", "一次性任务"),
    DAY_CYCLE("2", "周期性-每天"),
    ;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    private final String code;

    private final String name;

    RewardTaskCycleEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
