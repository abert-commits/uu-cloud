package org.uu.wallet.Enum;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;

/*
 * 订单状态枚举
 * */
public enum OrderStatusEnum {


    BE_PAID("3", "待支付", "进行中"),
    SUCCESS("7", "已完成", "已结束"),
    WAS_CANCELED("8", "已取消", "已结束"),
   ;

    private final String code;

    private final String name;

    private final String remark;

    OrderStatusEnum(String code, String name, String remark) {
        this.code = code;
        this.name = name;
        this.remark = remark;
    }

    public static String getNameByCode(String code) {
        for (OrderStatusEnum c : OrderStatusEnum.values()) {
            if (c.getCode().equals(code)) {
                return c.getName();
            }

        }
        return null;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getRemark() {
        return remark;
    }

    public static OrderStatusEnum buildOrderStatusEnumByCode(String code) {
        if (StringUtils.isNotEmpty(code) && StringUtils.isNotEmpty(code.trim())) {
            for (OrderStatusEnum orderStatusEnum : OrderStatusEnum.values()) {
                if (orderStatusEnum.code.equals(code)) {
                    return orderStatusEnum;
                }
            }
        }
        return null;
    }

}
