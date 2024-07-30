package org.uu.wallet.Enum;

/**
 * @author admin
 * @date 2024/4/9 16:36
 */
public enum CreditChangeTypeEnum {
    /**
     * 信用分事件枚举
     */
    ADD(1, "增加"),
    DECREASE(2, "减少"),;

    private final Integer code;
    private final String description;

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    CreditChangeTypeEnum(Integer code, String description) {
        this.code = code;
        this.description = description;
    }
}
