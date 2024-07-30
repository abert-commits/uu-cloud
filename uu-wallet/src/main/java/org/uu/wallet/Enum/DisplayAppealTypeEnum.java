package org.uu.wallet.Enum;


/**
 * 显示申诉类型枚举
 *
 * @author Simon
 * @date 2024/02/08
 */
public enum DisplayAppealTypeEnum {
    PAYMENT_NOT_RECEIVED("1", "未到账"),
    AMOUNT_INCORRECT("2", "金额错误");


    private final String code;

    private final String name;


    DisplayAppealTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }


    public static String getNameByCode(String code) {
        for (DisplayAppealTypeEnum c : DisplayAppealTypeEnum.values()) {
            if (c.getCode().equals(code)) {
                return c.getName();
            }

        }
        return null;
    }
}
