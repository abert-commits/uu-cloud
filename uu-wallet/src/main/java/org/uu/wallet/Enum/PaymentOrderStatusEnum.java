package org.uu.wallet.Enum;

import java.util.Objects;

/*
 * 代付订单状态枚举
 * */
public enum PaymentOrderStatusEnum {
    HANDLING("1", "支付中", "HANDLING"),
    SUCCESS("2", "已完成", "SUCCESS"),
    FAILED("3", "代付失败", "FAILED"),
    BE_MATCHED("5", "待匹配", "BE_MATCHED"),
    TO_BE_REVIEWED("6", "待审核", "TO_BE_REVIEWED");



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

    public static String getNameByCode(String code, String lang) {
        for (PaymentOrderStatusEnum c : PaymentOrderStatusEnum.values()) {
            if (c.getCode().equals(code)) {
                return Objects.equals(lang, "zh") ? c.getName() : c.getEnName();
            }

        }
        return null;
    }

    PaymentOrderStatusEnum(String code, String name, String enName) {
        this.code = code;
        this.name = name;
        this.enName = enName;
    }
}
