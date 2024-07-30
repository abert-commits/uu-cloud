package org.uu.wallet.Enum;


/**
 * 申诉订单状态枚举
 *
 * @author Simon
 * @date 2024/02/08
 */
public enum AppealStatusEnum {
    PENDING("1", "申诉中"),
    PAID("2", "申诉成功"),
    UNPAID("3", "申诉失败"),
    WRONG_AMOUNT("4", "金额错误");


    private final String code;

    private final String name;


    AppealStatusEnum(String code, String name) {
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
        for (AppealStatusEnum c : AppealStatusEnum.values()) {
            if (c.getCode().equals(code)) {
                return c.getName();
            }

        }
        return null;
    }
}
