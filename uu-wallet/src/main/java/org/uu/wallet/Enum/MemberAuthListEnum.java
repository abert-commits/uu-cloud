package org.uu.wallet.Enum;

import org.uu.common.pay.dto.MemberAuthListDTO;

import java.util.ArrayList;
import java.util.List;

/*
 * 商户类型枚举
 * */
public enum MemberAuthListEnum {


    ARBBUY("1", "ARB买入"),
    ARBSELL("2", "ARB卖出"),
    ARBTRANSFER("3", "ARB转账");


    private final String code;

    private final String name;


    MemberAuthListEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (MemberAuthListEnum c : MemberAuthListEnum.values()) {
            if (c.getCode().equals(code)) {
                return c.getName();
            }

        }
        return null;
    }

    public static List<MemberAuthListDTO> getList() {
        List<MemberAuthListDTO> list = new ArrayList<MemberAuthListDTO>();
        for (MemberAuthListEnum c : MemberAuthListEnum.values()) {
            MemberAuthListDTO memberAuthListDTO = new MemberAuthListDTO();
            memberAuthListDTO.setName(c.name);
            memberAuthListDTO.setCode(c.code);
            list.add(memberAuthListDTO);
        }
        return list;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
