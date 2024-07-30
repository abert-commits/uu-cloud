package org.uu.wallet.Enum;

import java.util.Objects;

/*
 * 商户 代收订单状态枚举
 * */
public enum CollectionOrderStatusEnum {
    BE_PAID("1", "支付中", "BE PAID"),
    PAID("2", "已完成", "PAID"),
    WAS_CANCELED("3", "代收失败", "WAS CANCELED");


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
        for (CollectionOrderStatusEnum c : CollectionOrderStatusEnum.values()) {
            if (c.getCode().equals(code)) {
                return Objects.equals(lang, "zh") ? c.getName() : c.getEnName();
            }

        }
        return null;
    }

    CollectionOrderStatusEnum(String code, String name, String enName) {
        this.code = code;
        this.name = name;
        this.enName = enName;
    }
}
