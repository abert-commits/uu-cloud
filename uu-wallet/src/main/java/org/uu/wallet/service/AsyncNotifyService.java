package org.uu.wallet.service;

public interface AsyncNotifyService {

    /**
     * 发送 充值成功 异步回调通知
     *
     * @param orderNo
     * @param type    1 自动回调  2 手动回调
     * @return {@link Boolean}
     */
    Boolean sendRechargeSuccessCallback(String orderNo, String type);


    /**
     * 发送 提现成功 异步回调通知
     *
     * @param orderNo
     * @param type    1 自动回调  2 手动回调
     * @return {@link Boolean}
     */
    Boolean sendWithdrawalSuccessCallback(String orderNo, String type);


    /**
     * 发送 充值成功 异步回调通知
     *
     * @param orderNo
     * @param type    1 自动回调  2 手动回调
     * @return {@link Boolean}
     */
    Boolean sendRechargeSuccessCallbackWithRecordRequest(String orderNo, String type) throws Exception;

    /**
     * 发送 提现成功 异步回调通知
     *
     * @param orderNo
     * @param type    1 自动回调  2 手动回调
     * @return {@link Boolean}
     */
    Boolean sendWithDrawSuccessCallbackWithRecordRequest(String orderNo, String type) throws Exception;
}
