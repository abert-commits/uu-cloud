package org.uu.wallet.util;

import org.uu.wallet.Enum.PayTypeEnum;
import org.uu.wallet.bo.ActiveKycPartnersBO;

/**
 * 通过在连接中的kyc来自动选择支付方式
 *
 * @author simon
 * @date 2024/07/07
 */
public class PayTypeUtil {

    public static String getPayType(ActiveKycPartnersBO activeKycPartners) {
        if (hasUpiPartners(activeKycPartners) && hasBankPartners(activeKycPartners)) {
            return PayTypeEnum.INDIAN_CARD_UPI_FIX.getCode();
        } else if (hasUpiPartners(activeKycPartners)) {
            return PayTypeEnum.INDIAN_UPI.getCode();
        } else if (hasBankPartners(activeKycPartners)) {
            return PayTypeEnum.INDIAN_CARD.getCode();
        }
        return null; // 或者其他默认值
    }

    private static boolean hasUpiPartners(ActiveKycPartnersBO activeKycPartners) {
        return activeKycPartners != null && !activeKycPartners.getUpiPartners().isEmpty();
    }

    private static boolean hasBankPartners(ActiveKycPartnersBO activeKycPartners) {
        return activeKycPartners != null && !activeKycPartners.getBankPartners().isEmpty();
    }
}
