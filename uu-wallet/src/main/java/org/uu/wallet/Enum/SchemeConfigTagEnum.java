package org.uu.wallet.Enum;

public enum SchemeConfigTagEnum {
    WALLET_ACTIVATION("1", "激活钱包"),
    REAL_NAME_VERIFICATION("2", "实名认证"),
    // 根据需要继续添加其他标签
    ;

    private final String code;
    private final String label;

    SchemeConfigTagEnum(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }
}
