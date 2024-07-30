package org.uu.wallet.Enum;

/**
 * 委托订单状态枚举
 *
 * @author simon
 * @date 2024/07/07
 */
public enum DelegationOrderEnum {

    BE_MATCHED("1", "匹配中", "进行中"),

    SUCCESS("2", "已完成", "已结束"),

    WAS_CANCELED("3", "已取消", "已结束");

    private final String code;

    private final String name;

    private final String remark;


    DelegationOrderEnum(String code, String name, String remark) {
        this.code = code;
        this.name = name;
        this.remark = remark;
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
}
