package org.uu.wallet.Enum;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
@AllArgsConstructor
public enum MemberTypeEnum {
    INTERNAL_MERCHANT_MEMBER("1", "内部会员"),

    SIX_NINE_MEMBER("2", "外部会员");

    private final String code;

    private final String name;

    public static MemberTypeEnum buildMemberTypeEnumByCode(String code) {
        if (StringUtils.isNotEmpty(code) && StringUtils.isNotEmpty(code.trim())) {
            for (MemberTypeEnum memberTypeEnum : MemberTypeEnum.values()) {
                if (memberTypeEnum.getCode().equals(code)) {
                    return memberTypeEnum;
                }
            }
        }
        return null;
    }

    public static String getNameByCode(String code) {
        for (MemberTypeEnum c : MemberTypeEnum.values()) {
            if (c.getCode().equals(code)) {
                return c.getName();
            }

        }
        return null;
    }

}
