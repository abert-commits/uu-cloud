package org.uu.wallet.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.uu.common.core.websocket.send.member.MemberWebSocketSendMessage;
import org.uu.common.core.enums.MemberWebSocketMessageTypeEnum;
import org.uu.common.redis.util.RedissonUtil;
import org.uu.wallet.Enum.OrderStatusEnum;
import org.uu.wallet.Enum.PaymentOrderStatusEnum;
import org.uu.wallet.Enum.TaskTypeEnum;
import org.uu.wallet.entity.*;
import org.uu.wallet.mapper.CollectionOrderMapper;
import org.uu.wallet.mapper.MerchantInfoMapper;
import org.uu.wallet.mapper.MerchantPaymentOrdersMapper;
import org.uu.wallet.mapper.UsdtBuyOrderMapper;
import org.uu.wallet.rabbitmq.RabbitMQService;
import org.uu.wallet.service.HandleOrderTimeoutService;
import org.uu.wallet.service.IKycCenterService;
import org.uu.wallet.service.IMerchantInfoService;
import org.uu.wallet.service.IMerchantPaymentOrdersService;
import org.uu.wallet.util.RedisUtil;
import org.uu.wallet.webSocket.MemberMessageSender;
import org.uu.wallet.webSocket.massage.OrderStatusChangeMessage;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class HandleOrderTimeoutServiceImpl implements HandleOrderTimeoutService {

    private final CollectionOrderMapper collectionOrderMapper;

    private final UsdtBuyOrderMapper usdtBuyOrderMapper;

    private final RedisUtil redisUtil;

    private final RedissonUtil redissonUtil;

    private final RabbitMQService rabbitMQService;

    @Autowired
    private MemberMessageSender memberMessageSender;

    @Autowired
    private IMerchantPaymentOrdersService merchantPaymentOrdersService;

    @Autowired
    private MerchantPaymentOrdersMapper merchantPaymentOrdersMapper;

    @Autowired
    private IKycCenterService kycCenterService;

    @Autowired
    private MerchantInfoMapper merchantInfoMapper;

    @Autowired
    private IMerchantInfoService merchantInfoService;

    /**
     * 支付超时处理
     *
     * @param platformOrder
     * @return {@link Boolean}
     */
    @Override
    @Transactional
    public Boolean handlePaymentTimeout(String platformOrder) {

        log.info("处理买入订单支付超时处理: 订单号: {}", platformOrder);

        //分布式锁key ar-wallet-buyCompleted+订单号
        String key = "ar-wallet-buyCompleted" + platformOrder;
        RLock lock = redissonUtil.getLock(key);

        boolean req = false;

        try {
            req = lock.tryLock(10, TimeUnit.SECONDS);

            if (req) {

                //获取买入订单 加排他行锁
                CollectionOrder collectionOrder = collectionOrderMapper.selectCollectionOrderForUpdate(platformOrder);

                //判断买入订单是否是待支付状态
                if (collectionOrder != null && OrderStatusEnum.BE_PAID.getCode().equals(collectionOrder.getOrderStatus())) {

                    //查询代付订单 加上排他行锁
                    MerchantPaymentOrders merchantPaymentOrder = merchantPaymentOrdersMapper.selectMerchantPaymentOrdersByPlatformOrderForUpdate(collectionOrder.getMerchantOrder());

                    if (merchantPaymentOrder == null) {
                        //获取代付订单失败
                        throw new RuntimeException("Failed to retrieve payment order");
                    }

                    //将代付订单改为代付失败
                    LambdaUpdateWrapper<MerchantPaymentOrders> lambdaUpdateWrapperMerchantPaymentOrders = new LambdaUpdateWrapper<>();
                    lambdaUpdateWrapperMerchantPaymentOrders.eq(MerchantPaymentOrders::getPlatformOrder, merchantPaymentOrder.getPlatformOrder())  // 指定更新条件 订单号
                            .set(MerchantPaymentOrders::getOrderStatus, PaymentOrderStatusEnum.FAILED.getCode()); // 指定更新字段 (订单状态)
                    // 这里传入的 null 表示不更新实体对象的其他字段
                    merchantPaymentOrdersService.update(null, lambdaUpdateWrapperMerchantPaymentOrders);

                    //将支付订单状态改为已取消
                    collectionOrderMapper.updateStatus(collectionOrder.getId(), OrderStatusEnum.WAS_CANCELED.getCode());

                    //将商户交易中金额退回给商户余额
                    //获取商户信息 加上排他行锁
                    MerchantInfo merchantInfo = merchantInfoMapper.selectMerchantInfoForUpdate(merchantPaymentOrder.getMerchantCode());

                    //订单金额总计 (订单金额 + 费用 + 单笔手续费)
                    BigDecimal allAmount = merchantPaymentOrder.getAmount().add(merchantPaymentOrder.getCost()).add(merchantPaymentOrder.getFixedFee());

                    //更新商户余额 将订单金额所需费用划转到交易中金额
                    LambdaUpdateWrapper<MerchantInfo> lambdaUpdateWrapperMerchantInfo = new LambdaUpdateWrapper<>();
                    lambdaUpdateWrapperMerchantInfo.eq(MerchantInfo::getCode, merchantInfo.getCode())  // 指定更新条件 商户号
                            .set(MerchantInfo::getBalance, merchantInfo.getBalance().add(allAmount)) // 指定更新字段 (增加商户余额 + 总金额)
                            .set(MerchantInfo::getPendingBalance, merchantInfo.getPendingBalance().subtract(allAmount)); // 指定更新字段 (减少交易中金额 - 总金额)
                    // 这里传入的 null 表示不更新实体对象的其他字段
                    merchantInfoService.update(null, lambdaUpdateWrapperMerchantInfo);


                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {

                            //发送代付回调通知商户
                            //发送提现成功 异步延时回调通知
                            long millis = 3000L;
                            //发送提现延时回调的MQ消息
                            TaskInfo taskInfo = new TaskInfo(merchantPaymentOrder.getPlatformOrder(), TaskTypeEnum.WITHDRAW_NOTIFICATION_TIMEOUT.getCode(), System.currentTimeMillis());
                            rabbitMQService.sendTimeoutTask(taskInfo, millis);

                            //记录失败次数
                            redisUtil.recordMemberBuyFailure(collectionOrder.getMemberId());

                            //通知买方
                            memberMessageSender.send(
                                    // 构建用户WebSocket消息体
                                    MemberWebSocketSendMessage.buildMemberWebSocketMessage(
                                            MemberWebSocketMessageTypeEnum.BUY_INR.getMessageType(),
                                            collectionOrder.getMemberId(),
                                            OrderStatusChangeMessage
                                                    .builder()
//                                                    .orderType(MemberAccountChangeEnum.RECHARGE.getCode())
                                                    .orderNo(collectionOrder.getPlatformOrder())
                                                    .orderStatus(OrderStatusEnum.WAS_CANCELED.getCode())
                                                    .build()
                                    )
                            );

                            //关闭kyc拉取 参数1: 卖出订单号 参数2: 买入订单号
                            kycCenterService.stopPullTransaction(merchantPaymentOrder.getPlatformOrder(), collectionOrder.getPlatformOrder());

                        }
                    });
                    return Boolean.TRUE;
                } else {
                    //订单不是待支付状态 直接消费成功
                    return Boolean.TRUE;
                }
            }
        } catch (Exception e) {
            //手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("买入订单支付超时处理失败: {}", e);
            return Boolean.FALSE;
        } finally {
            //释放锁
            if (req && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return Boolean.FALSE;
    }

    /**
     * USDT支付超时处理
     *
     * @param platformOrder
     * @return {@link Boolean}
     */
    @Override
    @Transactional
    public Boolean handleUsdtPaymentTimeout(String platformOrder) {

        log.info("处理USDT支付超时处理: 订单号: {}", platformOrder);

        //分布式锁key ar-wallet-handleUsdtPaymentTimeout+订单号
        String key = "ar-wallet-handleUsdtPaymentTimeout" + platformOrder;
        RLock lock = redissonUtil.getLock(key);

        boolean req = false;

        try {
            req = lock.tryLock(10, TimeUnit.SECONDS);

            if (req) {

                //获取USDT买入订单 加上排他行锁
                UsdtBuyOrder usdtBuyOrder = usdtBuyOrderMapper.selectUsdtBuyOrderForUpdate(platformOrder);

                //判断买入订单是否是待支付状态
                if (usdtBuyOrder != null && OrderStatusEnum.BE_PAID.getCode().equals(usdtBuyOrder.getStatus())) {

                    //将USDT订单状态改为支付超时
                    usdtBuyOrderMapper.updateStatus(usdtBuyOrder.getId(), OrderStatusEnum.WAS_CANCELED.getCode());

                    //记录失败次数
                    redisUtil.recordMemberBuyFailure(usdtBuyOrder.getMemberId());

                    return Boolean.TRUE;
                } else {
                    //订单不是待支付状态 直接消费成功
                    return Boolean.TRUE;
                }
            }
        } catch (Exception e) {
            //手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("USDT支付超时处理失败: {}", e);
            return Boolean.FALSE;
        } finally {
            //释放锁
            if (req && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return Boolean.FALSE;
    }
}
