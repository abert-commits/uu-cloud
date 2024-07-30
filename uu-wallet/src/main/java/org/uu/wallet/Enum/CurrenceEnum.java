package org.uu.wallet.Enum;

import java.util.ArrayList;
import java.util.List;

public  enum CurrenceEnum {


    CHINA("CNY","人民币","中国"),

    USDT("USDT","泰达币","美国"),

    ARB("ITOKEN","虚拟币","印度"),

    INDIA("INR","印度卢比","印度"),

    VIETNAM("VND","越南盾","越南"),

    INDONESIA("IDR","印尼盾","印度尼西亚");

    private final String code;

    private final String name;


    public String country;

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public static String getNameByCode(String code) {
        for (CurrenceEnum c : CurrenceEnum.values()) {
            if (c.getCode().equals(code)) {
                return c.getName();
            }
        }
        return null;
    }


    public static List<String> getCurrenceEnumList(){
        List<String> list = new ArrayList<String>();
        for (CurrenceEnum c : CurrenceEnum.values()) {
             list.add(c.getCode());
        }
        return list;
    }




    CurrenceEnum(String code, String name,String country){
        this.code = code;
        this.name = name;
        this.country = country;
    }


}
