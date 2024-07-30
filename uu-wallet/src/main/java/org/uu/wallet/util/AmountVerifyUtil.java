package org.uu.wallet.util;

import java.math.BigDecimal;

public class AmountVerifyUtil {

    /**
     * 判断金额是否为整百
     */
    public static boolean isMultipleOfHundred(BigDecimal amount) {
        return amount.remainder(new BigDecimal(100)).equals(BigDecimal.ZERO);
    }


    /**
     * 判断金额是否为整十
     */
    public static boolean isMultipleOfTen(BigDecimal amount) {
        return amount.remainder(new BigDecimal(10)).equals(BigDecimal.ZERO);
    }
}
