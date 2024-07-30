package org.uu.wallet.service;

import org.uu.wallet.dto.AsyncNotifyDTO;

/**
 * @author lukas
 */
public interface AsyncNotifyWithAopService {
    /**
     * 充值回调
     * @param orderNo orderNo
     * @param type type
     * @return {@link AsyncNotifyDTO}
     */
    AsyncNotifyDTO sendRechargeCallbackWithRecordRequest(String orderNo, String type);

    /**
     * 提现回调
     * @param orderNo orderNo
     * @param type type
     * @return {@link AsyncNotifyDTO}
     */
    AsyncNotifyDTO sendWithdrawCallbackWithRecordRequest(String orderNo, String type);
}
