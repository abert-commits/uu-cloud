package org.uu.wallet.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.uu.common.redis.util.RedissonUtil;
import org.uu.wallet.Enum.PaymentOrderStatusEnum;
import org.uu.wallet.Enum.WalletTypeEnum;
import org.uu.wallet.entity.MerchantPaymentOrders;
import org.uu.wallet.entity.TronWallet;
import org.uu.wallet.mapper.MerchantPaymentOrdersMapper;
import org.uu.wallet.property.ArProperty;
import org.uu.wallet.service.IMerchantPaymentOrdersService;
import org.uu.wallet.service.ITronWalletService;
import org.uu.wallet.service.TrxPaymentOrderService;
import org.uu.wallet.tron.service.TronBlockService;
import org.uu.wallet.util.MD5Util;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class TrxPaymentOrderServiceImpl implements TrxPaymentOrderService {

    private final RedissonUtil redissonUtil;

    @Autowired
    private MerchantPaymentOrdersMapper merchantPaymentOrdersMapper;

    @Autowired
    private ITronWalletService tronWalletService;

    @Autowired
    private IMerchantPaymentOrdersService merchantPaymentOrdersService;

    @Autowired
    private TronBlockService tronBlockService;

    private final RedisTemplate redisTemplate;

    @Autowired
    private ArProperty arProperty;

    /**
     * 处理TRX代付订单
     *
     * @param platformOrder
     * @return {@link Boolean }
     */
    @Override
    @Transactional
    public Boolean trxPaymentOrder(String platformOrder) {

        //分布式锁key ar-wallet-usdtPaymentOrder+订单号
        String key = "uu-wallet-usdtPaymentOrder" + platformOrder;
        RLock lock = redissonUtil.getLock(key);

        boolean req = false;

        try {
            req = lock.tryLock(10, TimeUnit.SECONDS);

            if (req) {

                //查询代付订单 加上排他行锁
                MerchantPaymentOrders merchantPaymentOrders = merchantPaymentOrdersMapper.selectMerchantPaymentOrdersByPlatformOrderForUpdate(platformOrder);

                if (merchantPaymentOrders == null) {
                    log.error("处理TRX代付订单失败 获取代付订单失败, 订单号: {}", platformOrder);
                    return false;
                }

                //判断订单状态
                if (!PaymentOrderStatusEnum.HANDLING.getCode().equals(merchantPaymentOrders.getOrderStatus())) {
                    log.error("处理TRX代付订单失败 代付订单状态不是待支付, 订单号: {}, 订单信息: {}", platformOrder, merchantPaymentOrders);
                    return true;
                }

                //判断转账状态 0 未转账, 1: 已转账, 2: 转账成功, 3: 转账失败
                if (merchantPaymentOrders.getTransferStatus() != 0) {
                    log.error("处理TRX代付订单失败 转账状态不是未转账, 订单号: {}, 订单信息: {}", platformOrder, merchantPaymentOrders);
                    return true;
                }

                //判断txId
                if (StringUtils.isNotBlank(merchantPaymentOrders.getTxid())) {
                    log.error("处理TRX代付订单失败 txId不为空, 订单号: {}, 订单信息: {}", platformOrder, merchantPaymentOrders);
                    return true;
                }

                //判断是否存在redis标识 存在才进行处理 (有效期三天) 转账后会将其删除
                Object value = redisTemplate.opsForValue().get("trxPaymentOrderSign:" + platformOrder);

                if (value == null) {
                    //不存在订单sign
                    log.error("处理TRX代付订单失败 不存在订单sign, 订单号: {}, sign: {}", platformOrder, value);
                    return true;
                }

                String paymentOrderSign = String.valueOf(value);

                String md5Sign = MD5Util.generateMD5(merchantPaymentOrders.getMerchantCode() + merchantPaymentOrders.getPlatformOrder() + merchantPaymentOrders.getUsdtAddr() + arProperty.getPaymentOrderKey());

                if (!md5Sign.equals(paymentOrderSign)) {
                    log.error("处理TRX代付订单失败 代付订单redis签名校验失败, 订单号: {}, paymentOrderSign: {}, md5Sign: {}", platformOrder, paymentOrderSign, md5Sign);
                    return true;
                }

                //获取资金充足的出款账户 加上排他行锁
                TronWallet oneWalletForPayment = tronWalletService.findOneWalletForPaymentForUpdateTrx(merchantPaymentOrders.getOrderAmount(), WalletTypeEnum.WITHDRAW.getCode());

                if (oneWalletForPayment == null) {
                    log.error("处理TRX代付订单失败 获取资金充足的出款账户失败, 订单号: {}, 订单信息: {}", platformOrder, merchantPaymentOrders);
                    return true;
                }

                //更新代付订单转账状态
                //备注
                merchantPaymentOrders.setRemark("出款中");
                //代付状态 1 支付中
                merchantPaymentOrders.setOrderStatus(PaymentOrderStatusEnum.HANDLING.getCode());
                //转账状态 1: 已转账
                merchantPaymentOrders.setTransferStatus(1);
                //更新代付订单
                merchantPaymentOrdersService.updateById(merchantPaymentOrders);

                //执行事务同步回调
                //订单到这里已经被锁定不会被其他线程重复转账了
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        //将代付订单转账状态改为 已转账 避免重复出款
                        //事务提交成功后 执行转账
                        //执行TRX转账
                        tronBlockService.autoTransferTRX(oneWalletForPayment, merchantPaymentOrders);
                    }
                });
                return true;
            } else {
                //没获取到锁 直接返回操作true
                log.error("处理TRX代付订单失败 未获取到锁");
                return false;
            }
        } catch (Exception e) {
            //手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("处理TRX代付订单失败 订单号: {}, e: {}", platformOrder, e);
        } finally {
            //释放锁
            if (req && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return true;
    }

}
