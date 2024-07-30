package org.uu.job.util;

import java.math.BigDecimal;

public class AmountVerifyUtil {

    /**
     * 判断金额是否为整百
     */
    public static boolean isMultipleOfHundred(BigDecimal amount) {
        return amount.remainder(new BigDecimal(100)).equals(BigDecimal.ZERO);
    }
}
