package org.uu.wallet.Enum;

import java.util.ArrayList;
import java.util.List;

/**
 * @author admin
 * @date 2024/3/18 16:16
 */
public enum RewardTaskTypeEnum {
    // 任务类型
    BUY("1", "买入"),
    SELL("2", "卖出"),
    SIGN("3", "签到"),
    REAL_AUTH("4", "实名认证"),
    STARTER_QUESTS_BUY("5", "新手任务-买入引导"),
    STARTER_QUESTS_SELL("6", "新手任务-卖出引导"),

    ;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    private final String code;

    private final String name;

    RewardTaskTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * 周期任务
     *
     * @return list
     */
    public static List<String> getCycleTaskCode() {
        List<String> list = new ArrayList<>();
        list.add(BUY.code);
        list.add(SELL.code);
        list.add(SIGN.code);
        return list;
    }

    /**
     * 一次性任务
     *
     * @return list
     */
    public static List<String> getOnceTaskCode() {
        List<String> list = new ArrayList<>();
        list.add(REAL_AUTH.code);
        list.add(STARTER_QUESTS_BUY.code);
        list.add(STARTER_QUESTS_SELL.code);
        return list;
    }
}
