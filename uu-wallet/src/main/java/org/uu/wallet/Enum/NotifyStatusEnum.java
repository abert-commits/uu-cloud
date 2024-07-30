package org.uu.wallet.Enum;

import java.util.Objects;

/*
 * 回调状态枚举
 * */
public enum NotifyStatusEnum {
    NOTCALLBACK("1", "未回调", "NOT CALL BACK"),
    SUCCESS("2", "回调成功", "SUCCESS"),
    FAILED("3", "回调失败", "FAILED"),
    MANUAL_SUCCESS("4", "手动回调成功", "MANUAL SUCCESS"),
    MANUAL_FAILED("5", "手动回调失败", "MANUAL FAILED");


    private final String code;

    private final String name;

    private final String enName;


    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getEnName() {
        return enName;
    }

    public static String getNameByCode(String code) {
        for (NotifyStatusEnum c : NotifyStatusEnum.values()) {
            if (c.getCode().equals(code)) {
                return c.getName();
            }

        }
        return null;
    }

    public static String getNameByCode(String code, String lang) {
        for (NotifyStatusEnum c : NotifyStatusEnum.values()) {
            if (c.getCode().equals(code)) {
                return Objects.equals(lang, "zh") ? c.getName() : c.getEnName();
            }

        }
        return null;
    }

    NotifyStatusEnum(String code, String name, String enName) {
        this.code = code;
        this.name = name;
        this.enName = enName;
    }

    /**
     * 检查给定的代码是否表示未回调、回调失败或手动回调失败。
     *
     * @param code 要检查的代码。
     * @return 如果代码表示未回调、回调失败或手动回调失败，则为 true，否则为 false。
     */
    public static boolean isUnsuccessful(String code) {
        return NOTCALLBACK.code.equals(code) || FAILED.code.equals(code) || MANUAL_FAILED.code.equals(code);
    }
}
