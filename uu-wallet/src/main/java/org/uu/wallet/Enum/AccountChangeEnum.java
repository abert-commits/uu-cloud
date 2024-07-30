package org.uu.wallet.Enum;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Admin
 */

/*
* 账变类型枚举
* */
public enum AccountChangeEnum {
    COLLECTION("1", "代收", "MC"),
    PAYMENT("2", "代付", "MP"),
    WITHDRAW("3", "下发", "MW"),
    RECHARGE("4", "上分", "MR"),
    COLLECTION_FEE("5", "代收费用", "DSFY"),
    PAYMENT_FEE("6", "代付费用", "DFFY"),
    WITHDRAW_BACK("7", "下发回退", "WB"),
    ;


    private final String code;

    private final String name;

    private final String prefix;


    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public static String getNameByCode(String code) {
        for (AccountChangeEnum c : AccountChangeEnum.values()) {
            if (c.getCode().equals(code)) {
                return c.getName();
            }

        }
        return null;
    }

    AccountChangeEnum(String code, String name, String prefix) {
        this.code = code;
        this.name = name;
        this.prefix = prefix;
    }

    public static Map<Integer, String> getNameCodeKeyValue(){
        Map<Integer, String> map = new HashMap<>();
        for (AccountChangeEnum c : AccountChangeEnum.values()) {
            map.put(Integer.valueOf(c.getCode()), c.getName());
        }
        return map;
    }


}
