package org.uu.wallet.Enum;

public enum MemberAuthenticationStatusEnum {
    AUTHENTICATED("1", "已认证"),
    UNAUTHENTICATED("2", "未认证"),
    MANUAL_AUTHENTICATION("3", "手动认证");

    private final String code;
    private final String description;

    MemberAuthenticationStatusEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}