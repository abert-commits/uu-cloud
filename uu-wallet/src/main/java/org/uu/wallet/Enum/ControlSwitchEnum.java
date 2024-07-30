package org.uu.wallet.Enum;

/**
 * @author admin
 * @date 2024/3/21 18:16
 */
public enum ControlSwitchEnum {
    /**
     * 开关枚举
     */
    TASK_SWITCH("1", "任务开关");

    private final String code;

    private final String name;

    ControlSwitchEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }
}
