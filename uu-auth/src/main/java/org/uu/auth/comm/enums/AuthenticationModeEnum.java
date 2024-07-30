package org.uu.auth.comm.enums;

/**
 * 前台登录方式枚举  1: 前台登录  2: 商户登录
 *
 * @author Simon
 * @date 2024/01/15
 */
public enum AuthenticationModeEnum {
    MEMBER_LOGIN("1", "前台模式登录"),
    MERCHANT_LOGIN("2", "商户模式登录");

    private final String code;
    private final String description;

    AuthenticationModeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static AuthenticationModeEnum getByCode(int code) {
        for (AuthenticationModeEnum mode : values()) {
            if (mode.getCode().equals(code)) {
                return mode;
            }
        }
        throw new IllegalArgumentException("No matching constant for [" + code + "]");
    }
}
