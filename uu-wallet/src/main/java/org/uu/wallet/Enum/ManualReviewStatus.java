package org.uu.wallet.Enum;


/**
 * @author admin
 * @date 2024/4/10 11:38
 */
public enum ManualReviewStatus {
    PASS(1, "通过"),
    NOT_PASS(2, "不通过"),
    ;

    private final Integer code;
    private final String description;

    ManualReviewStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
