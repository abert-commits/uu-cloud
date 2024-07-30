package org.uu.wallet.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.tron.trident.core.ApiWrapper;
import org.uu.wallet.entity.MerchantPaymentOrders;
import org.uu.wallet.entity.WithdrawTronDetail;
import org.uu.wallet.service.IMerchantPaymentOrdersService;
import org.uu.wallet.service.IWithdrawTronDetailService;

@Service
@Slf4j
@RequiredArgsConstructor
public class UsdtPaymentOrderService1 {

    @Autowired
    private IMerchantPaymentOrdersService merchantPaymentOrdersService;

    @Autowired
    private IWithdrawTronDetailService withdrawTronDetailService;

    @Autowired
    private UsdtPaymentOrderService2 usdtPaymentOrderService2;


    /**
     * 更新订单状态1
     *
     * @param orderId
     * @param txId
     * @param merchantPaymentOrders
     * @param address
     * @param withdrawTronDetail
     * @param wrapper
     * @return {@link Boolean }
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)//开启新事务
    public Boolean updatePaymentOrder1(String orderId, String txId, MerchantPaymentOrders merchantPaymentOrders, String address, WithdrawTronDetail withdrawTronDetail, ApiWrapper wrapper) {

        try {

            log.info("USDT代付出款 updatePaymentOrder1, 订单号: {}", merchantPaymentOrders.getPlatformOrder());

            //更新代付订单
            //txId
            merchantPaymentOrders.setTxid(txId);
            //转账状态 1:已转账
            merchantPaymentOrders.setTransferStatus(1);
            //更新代付订单
            merchantPaymentOrdersService.updateById(merchantPaymentOrders);

            //保存代付钱包交易记录
            //源地址
            withdrawTronDetail.setFromAddress(address);
            //txId
            withdrawTronDetail.setTxid(txId);
            //转账状态  0:转账中
            withdrawTronDetail.setStatus(0);
            //保存代付钱包交易记录
            withdrawTronDetailService.save(withdrawTronDetail);

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    //事务提交成功
                    //事务提交成功后 执行以下方法 获取交易记录
                    log.info("USDT代付出款 获取交易记录, 订单号: {}", merchantPaymentOrders.getPlatformOrder());
                    usdtPaymentOrderService2.updatePaymentOrder2(orderId, txId, merchantPaymentOrders, address, withdrawTronDetail, wrapper);
                }
            });
        } catch (Exception e) {
            log.error("USDT代付出款失败, e: {}", e);
        }
        return true;
    }


}
