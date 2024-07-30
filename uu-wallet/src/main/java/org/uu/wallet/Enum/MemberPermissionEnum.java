package org.uu.wallet.Enum;

public enum MemberPermissionEnum {
    BUY("1", "买入权限"),
    SELL("2", "卖出权限"),
    TRANSFER("3", "转账权限"),
    DELEGATE("4", "委托权限");

    private final String code;
    private final String description;

    MemberPermissionEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public static MemberPermissionEnum fromCode(String code) {
        for (MemberPermissionEnum permission : values()) {
            if (permission.getCode().equals(code)) {
                return permission;
            }
        }
        throw new IllegalArgumentException("未知的权限代码: " + code);
    }
}