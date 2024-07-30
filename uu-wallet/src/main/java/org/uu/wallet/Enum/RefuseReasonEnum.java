package org.uu.wallet.Enum;

/**
 * @author admin
 * @date 2024/4/12 15:02
 */
public enum RefuseReasonEnum {

    PAID_PIC_FAKE(1, "The picture of the buyer’s payment voucher is fake!"),
    OTHER(2, "other"),
    ;



    private final Integer code;

    private final String name;


    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static String getNameByCode(Integer code) {
        for (RefuseReasonEnum c : RefuseReasonEnum.values()) {
            if (c.getCode().equals(code) && !code.equals(OTHER.code)) {
                return c.getName();
            }
        }
        return null;
    }

    RefuseReasonEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }
}
