package org.uu.wallet.Enum;

/**
 * 日志模块 枚举
 *
 * @author Simon
 * @date 2024/01/15
 */
public enum LogModuleEnum {
    ADMIN_BACKEND("1", "总后台"),
    MERCHANT_BACKEND("2", "商户后台"),
    FRONTEND("3", "前台");

    private final String code;
    private final String description;

    LogModuleEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "Code: " + code + ", Description: " + description;
    }
}
