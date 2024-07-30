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
import org.uu.wallet.Enum.*;
import org.uu.wallet.entity.MerchantPaymentOrders;
import org.uu.wallet.entity.TaskInfo;
import org.uu.wallet.entity.TronWallet;
import org.uu.wallet.mapper.MerchantPaymentOrdersMapper;
import org.uu.wallet.property.ArProperty;
import org.uu.wallet.rabbitmq.RabbitMQService;
import org.uu.wallet.service.IMerchantPaymentOrdersService;
import org.uu.wallet.service.ITronWalletService;
import org.uu.wallet.service.UsdtPaymentOrderService;
import org.uu.wallet.tron.service.TronBlockService;
import org.uu.wallet.util.AmountChangeUtil;
import org.uu.wallet.util.MD5Util;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class UsdtPaymentOrderServiceImpl implements UsdtPaymentOrderService {

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

    @Autowired
    private RabbitMQService rabbitMQService;

    private final AmountChangeUtil amountChangeUtil;

    /**
     * 处理USDT代付订单
     *
     * @param platformOrder
     * @return {@link Boolean }
     */
    @Override
    @Transactional
    public Boolean usdtPaymentOrder(String platformOrder) {

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
                    log.error("处理USDT代付订单失败 获取代付订单失败, 订单号: {}", platformOrder);
                    return false;
                }

                //判断如果是测试商户 那么直接成功
                if("uu101".equals(merchantPaymentOrders.getMerchantCode())) {
                    log.info("处理USDT代付订单成功, 该商户是测试商户, 直接返回成功, 订单信息: {}", merchantPaymentOrders);

                    //更新订单信息
                    //转账状态 2: 转账成功
                    merchantPaymentOrders.setTransferStatus(2);
                    //备注
                    merchantPaymentOrders.setRemark("测试商户出款");
                    //订单状态 成功
                    merchantPaymentOrders.setOrderStatus(PaymentOrderStatusEnum.SUCCESS.getCode());
                    //订单完成时间
                    merchantPaymentOrders.setCompletionTime(LocalDateTime.now());

                    boolean update2 = merchantPaymentOrdersService.updateById(merchantPaymentOrders);

                    if (update2) {

                        //产生商户账变
                        //更新商户余额并记录商户账变
                        //记录商户账变 (订单金额)
                        Boolean updatemerchantInfo = amountChangeUtil.insertOrUpdateAccountChange(
                                merchantPaymentOrders.getMerchantCode(),//商户号
                                merchantPaymentOrders.getAmount(),//账变金额 (订单金额)
                                ChangeModeEnum.SUB,//账变类型 支出
                                "USDT",//币种
                                merchantPaymentOrders.getPlatformOrder(),//平台订单号
                                AccountChangeEnum.PAYMENT,//账变类型 代付
                                "USDT-API代付-测试",//备注
                                merchantPaymentOrders.getMerchantOrder(),
                                ChannelEnum.USDT.getName(),//商户支付通道
                                "",
                                BalanceTypeEnum.TRC20.getName()//余额通道
                        );

                        if (!updatemerchantInfo) {
                            log.error("USDT代付出款失败: 测试出款 更新商户信息失败，触发事务回滚。 订单信息: {}", merchantPaymentOrders);
                            // 抛出运行时异常
                            throw new RuntimeException("USDT代付出款失败: 测试出款 更新商户信息失败，触发事务回滚。");
                        }

                        //订单费用 = 费用 + 单笔手续费
                        BigDecimal orderFee = merchantPaymentOrders.getCost().add(merchantPaymentOrders.getFixedFee());

                        // 订单费用大于0 才记录 订单费用的账变
                        if (orderFee.compareTo(BigDecimal.ZERO) > 0) {
                            //记录商户账变 (订单费用)
                            Boolean updatemerchantInfoFee = amountChangeUtil.insertOrUpdateAccountChange(
                                    merchantPaymentOrders.getMerchantCode(),//商户号
                                    orderFee,//账变金额 (订单总费用)
                                    ChangeModeEnum.SUB,//账变类型 支出
                                    "USDT",//币种
                                    merchantPaymentOrders.getPlatformOrder(),//平台订单号
                                    AccountChangeEnum.PAYMENT_FEE,//账变类型 代付费用
                                    "USDT-API代付费用-测试",//备注
                                    merchantPaymentOrders.getMerchantOrder(),
                                    ChannelEnum.USDT.getName(),//商户支付通道
                                    "",
                                    BalanceTypeEnum.TRC20.getName()//余额通道
                            );

                            if (!updatemerchantInfoFee) {
                                log.error("USDT代付出款失败: 测试出款 更新商户信息失败，触发事务回滚。 订单信息: {}", merchantPaymentOrders);
                                // 抛出运行时异常
                                throw new RuntimeException("USDT代付出款失败: 测试出款 更新商户信息失败，触发事务回滚。");
                            }
                        }

                        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                            @Override
                            public void afterCommit() {
                                //事务提交成功

                                //发送回调通知MQ
                                //发送提现成功异步延时回调通知
                                long millis = 3000L;
                                //发送提现延时回调的MQ消息
                                TaskInfo taskInfo = new TaskInfo(merchantPaymentOrders.getPlatformOrder(), TaskTypeEnum.WITHDRAW_NOTIFICATION_TIMEOUT.getCode(), System.currentTimeMillis());
                                rabbitMQService.sendTimeoutTask(taskInfo, millis);

                            }
                        });
                    }

                    return true;
                }

                //判断订单状态
                if (!PaymentOrderStatusEnum.HANDLING.getCode().equals(merchantPaymentOrders.getOrderStatus())) {
                    log.error("处理USDT代付订单失败 代付订单状态不是待支付, 订单号: {}, 订单信息: {}", platformOrder, merchantPaymentOrders);
                    return true;
                }

                //判断转账状态 0 未转账, 1: 已转账, 2: 转账成功, 3: 转账失败
                if (merchantPaymentOrders.getTransferStatus() != 0) {
                    log.error("处理USDT代付订单失败 转账状态不是未转账, 订单号: {}, 订单信息: {}", platformOrder, merchantPaymentOrders);
                    return true;
                }

                //判断txId
                if (StringUtils.isNotBlank(merchantPaymentOrders.getTxid())) {
                    log.error("处理USDT代付订单失败 txId不为空, 订单号: {}, 订单信息: {}", platformOrder, merchantPaymentOrders);
                    return true;
                }

                //判断是否存在redis标识 存在才进行处理 (有效期三天) 转账后会将其删除
                Object value = redisTemplate.opsForValue().get("paymentOrderSign:" + platformOrder);

                if (value == null) {
                    //不存在订单sign
                    log.error("处理USDT代付订单失败 不存在订单sign, 订单号: {}, sign: {}", platformOrder, value);
                    return true;
                }

                String paymentOrderSign = String.valueOf(value);

                String md5Sign = MD5Util.generateMD5(merchantPaymentOrders.getMerchantCode() + merchantPaymentOrders.getPlatformOrder() + merchantPaymentOrders.getUsdtAddr() + arProperty.getPaymentOrderKey());

                if (!md5Sign.equals(paymentOrderSign)) {
                    log.error("处理USDT代付订单失败 代付订单redis签名校验失败, 订单号: {}, paymentOrderSign: {}, md5Sign: {}", platformOrder, paymentOrderSign, md5Sign);
                    return true;
                }

                //获取资金充足的出款账户 加上排他行锁
                TronWallet oneWalletForPayment = tronWalletService.findOneWalletForPaymentForUpdate(merchantPaymentOrders.getOrderAmount(), WalletTypeEnum.WITHDRAW.getCode());

                if (oneWalletForPayment == null) {
                    log.error("处理USDT代付订单失败 获取资金充足的出款账户失败, 订单号: {}, 订单信息: {}", platformOrder, merchantPaymentOrders);
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
                        //执行转账
                        tronBlockService.autoTransfer(oneWalletForPayment, merchantPaymentOrders);
                    }
                });
                return true;
            } else {
                //没获取到锁 直接返回操作true
                log.error("处理USDT代付订单失败 未获取到锁");
                return false;
            }
        } catch (Exception e) {
            //手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("处理USDT代付订单失败 订单号: {}, e: {}", platformOrder, e);
        } finally {
            //释放锁
            if (req && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return true;
    }

}
