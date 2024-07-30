package org.uu.wallet.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.RestResult;
import org.uu.common.mybatis.util.PageUtils;
import org.uu.common.pay.dto.UsdtBuySuccessOrderDTO;
import org.uu.common.pay.req.UsdtBuyOrderReq;
import org.uu.common.redis.util.RedissonUtil;
import org.uu.wallet.Enum.CollectionOrderStatusEnum;
import org.uu.wallet.Enum.OrderStatusEnum;
import org.uu.wallet.Enum.PayTypeEnum;
import org.uu.wallet.Enum.TaskTypeEnum;
import org.uu.wallet.bo.DelegationOrderBO;
import org.uu.wallet.dto.MerchantCollectionOrderStatusDTO;
import org.uu.wallet.entity.*;
import org.uu.wallet.mapper.MemberInfoMapper;
import org.uu.wallet.mapper.MerchantCollectOrdersMapper;
import org.uu.wallet.mapper.PaymentOrderMapper;
import org.uu.wallet.rabbitmq.RabbitMQService;
import org.uu.wallet.req.MerchantCollectionOrderStatusReq;
import org.uu.wallet.service.*;
import org.uu.wallet.util.DelegationOrderRedisUtil;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 商户代收订单表 服务实现类
 * </p>
 *
 * @author
 * @since 2024-01-05
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MerchantCollectOrdersServiceImpl extends ServiceImpl<MerchantCollectOrdersMapper, MerchantCollectOrders> implements IMerchantCollectOrdersService {

    private final RedissonUtil redissonUtil;
    private final MerchantCollectOrdersMapper merchantCollectOrdersMapper;
    private final RabbitMQService rabbitMQService;

    @Autowired
    private PaymentOrderMapper paymentOrderMapper;

    @Autowired
    private MemberInfoMapper memberInfoMapper;

    @Autowired
    private IMemberInfoService memberInfoService;

    @Autowired
    private IPaymentOrderService paymentOrderService;

    @Autowired
    private DelegationOrderRedisUtil delegationOrderRedisUtil;

    @Autowired
    private IKycCenterService kycCenterService;
    @Resource
    private IRechargeTronDetailService rechargeTronDetailService;

    /**
     * 根据商户订单号 获取订单信息
     *
     * @return {@link MerchantCollectOrders}
     */
    @Override
    public MerchantCollectOrders getOrderInfoByOrderNumber(String merchantOrder) {
        return lambdaQuery()
                .eq(MerchantCollectOrders::getMerchantOrder, merchantOrder)
                .or().eq(MerchantCollectOrders::getPlatformOrder, merchantOrder)
                .one();
    }


    /**
     * 取消充值订单
     *
     * @param platformOrder 平台订单号
     * @return {@link Boolean}
     */
    @Override
    public Boolean cancelPayment(String platformOrder) {
        return lambdaUpdate()
                .eq(MerchantCollectOrders::getPlatformOrder, platformOrder)
                .set(MerchantCollectOrders::getOrderStatus, CollectionOrderStatusEnum.WAS_CANCELED.getCode())
                .update();
    }


    /**
     * 支付超时处理
     *
     * @param orderNo
     * @return boolean
     */
    @Override
    @Transactional
    public boolean handlePaymentTimeout(String orderNo) {

        //加上分布式锁 锁名和确认支付的锁名一致 保证同时只有一个线程再操作支付超时或确认支付

        //分布式锁key ar-wallet-merchantCollectOrderPaymentTimeoutConsumer+订单号
//        String key = "ar-wallet-merchantCollectOrderPaymentTimeoutConsumer" + orderNo;
        String key = "uu-wallet-delegateSell";//与委托用同一把锁
        RLock lock = redissonUtil.getLock(key);

        boolean req = false;

        try {
            req = lock.tryLock(10, TimeUnit.SECONDS);

            if (req) {

                //获取代收订单信息 加上排他行锁
                MerchantCollectOrders merchantCollectOrders = merchantCollectOrdersMapper.selectMerchantCollectOrdersForUpdate(orderNo);

                if (merchantCollectOrders == null) {
                    //代收订单不存在
                    log.error("商户代收订单支付超时处理失败, 获取代收订单信息失败, 订单号: {}", orderNo);
                    return false;
                }

                //校验代收订单状态是否是 支付中 如果不是则不进行处理
                if (!CollectionOrderStatusEnum.BE_PAID.getCode().equals(merchantCollectOrders.getOrderStatus())) {
                    //该订单不是待支付状态, 直接将该消息消费成功
                    log.info("商户代收订单支付超时处理成功, 该笔代收订单状态不是待支付, 订单号: {}, 订单信息: {}", orderNo, merchantCollectOrders);
                    return true;
                }

                //判断是UPI还是USDT
                if (PayTypeEnum.INDIAN_USDT.getCode().equals(merchantCollectOrders.getPayType()) || PayTypeEnum.INDIAN_TRX.getCode().equals(merchantCollectOrders.getPayType())) {
                    //USDT或TRX
                    //将代收订单状态改为代收失败
                    boolean updateRes = lambdaUpdate()
                            .eq(MerchantCollectOrders::getPlatformOrder, merchantCollectOrders.getPlatformOrder())
                            .eq(MerchantCollectOrders::getOrderStatus, CollectionOrderStatusEnum.BE_PAID.getCode())
                            .set(MerchantCollectOrders::getOrderStatus, CollectionOrderStatusEnum.WAS_CANCELED.getCode())
                            .update();
                    if (updateRes) {
                        log.info("商户代收订单支付超时处理成功, 订单号: {}", orderNo);
                    } else {
                        log.info("商户代收订单支付超时处理失败, 订单号: {}", orderNo);
                    }

                    //注册事务同步回调, 事务提交成功后, 发送延时MQ 改变订单为超时状态
                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            //事务提交成功

                            //发送支付超时的MQ
                            //取消订单成功 异步回调通知商户
                            TaskInfo taskInfo = new TaskInfo(merchantCollectOrders.getPlatformOrder(), TaskTypeEnum.DEPOSIT_NOTIFICATION.getCode(), System.currentTimeMillis());
                            rabbitMQService.sendRechargeSuccessCallbackNotification(taskInfo);
                        }
                    });
                    return updateRes;
                }

                //查询卖出订单 加上排他行锁
                PaymentOrder paymentOrder = paymentOrderMapper.selectPaymentForUpdate(merchantCollectOrders.getSellOrderNo());

                if (paymentOrder == null) {
                    //订单不存在
                    log.error("商户代收订单支付超时处理失败, 获取卖出订单信息失败, 订单号: {}", orderNo);
                    return false;
                }

                if (!OrderStatusEnum.BE_PAID.getCode().equals(paymentOrder.getOrderStatus())) {
                    //卖出订单不是待支付状态 直接消费成功
                    log.error("商户代收订单支付超时处理失败, 卖出订单状态不是待支付, 订单号: {}", orderNo);
                    return true;
                }

                //获取会员信息 加上排他行锁
                MemberInfo memberInfo = memberInfoMapper.selectMemberInfoForUpdate(Long.valueOf(paymentOrder.getMemberId()));

                if (memberInfo == null) {
                    //获取会员信息失败 直接消费成功
                    log.error("商户代收订单支付超时处理失败, 获取卖出会员信息失败, 订单号: {}", orderNo);
                    return false;
                }

                //如果会员委托状态还在开启中 那么重新添加redis委托信息(覆盖先前的委托信息, 将最新的余额进行委托)
                DelegationOrderBO delegationOrderBO = new DelegationOrderBO();
                delegationOrderBO.setMemberId(String.valueOf(memberInfo.getId()));
                delegationOrderBO.setDelegationTime(LocalDateTime.now());
                delegationOrderBO.setAmount(memberInfo.getBalance().add(paymentOrder.getAmount()));


                //将代收订单状态改为代收失败
                lambdaUpdate()
                        .eq(MerchantCollectOrders::getPlatformOrder, merchantCollectOrders.getPlatformOrder())
                        .eq(MerchantCollectOrders::getOrderStatus, CollectionOrderStatusEnum.BE_PAID.getCode())
                        .set(MerchantCollectOrders::getOrderStatus, CollectionOrderStatusEnum.WAS_CANCELED.getCode())
                        .update();

                log.info("商户代收订单支付超时处理成功, 订单号: {}", orderNo);

                //更新会员信息
                LambdaUpdateWrapper<MemberInfo> lambdaUpdateWrapperMemberInfo = new LambdaUpdateWrapper<>();
                // 指定更新条件，会员id
                lambdaUpdateWrapperMemberInfo.eq(MemberInfo::getId, memberInfo.getId());
                //减少用户交易中金额
                lambdaUpdateWrapperMemberInfo.set(MemberInfo::getFrozenAmount, memberInfo.getFrozenAmount().subtract(paymentOrder.getAmount()));
                //增加可用余额
                lambdaUpdateWrapperMemberInfo.set(MemberInfo::getBalance, memberInfo.getBalance().add(paymentOrder.getAmount()));
                // 这里传入的 null 表示不更新实体对象的其他字段
                memberInfoService.update(null, lambdaUpdateWrapperMemberInfo);

                //更新卖出订单
                LambdaUpdateWrapper<PaymentOrder> lambdaUpdateWrapperPayment = new LambdaUpdateWrapper<>();
                // 指定更新条件，订单号
                lambdaUpdateWrapperPayment.eq(PaymentOrder::getPlatformOrder, paymentOrder.getPlatformOrder());
                //更新卖出订单状态为 已取消
                lambdaUpdateWrapperPayment.set(PaymentOrder::getOrderStatus, OrderStatusEnum.WAS_CANCELED.getCode());
                // 这里传入的 null 表示不更新实体对象的其他字段
                paymentOrderService.update(null, lambdaUpdateWrapperPayment);


                //事务提交成功后再操作redis
                //注册事务同步回调, 事务提交成功后, 发送延时MQ 改变订单为超时状态
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        //事务提交成功

                        //如果会员委托状态还在开启中 那么将最新的余额进行委托
                        if (memberInfo.getDelegationStatus() == 1) {
                            //将最新的余额进行委托
                            delegationOrderRedisUtil.addOrder(delegationOrderBO);
                        }

                        //发送支付超时的MQ
                        //取消订单成功 异步回调通知商户
                        TaskInfo taskInfo = new TaskInfo(merchantCollectOrders.getPlatformOrder(), TaskTypeEnum.DEPOSIT_NOTIFICATION.getCode(), System.currentTimeMillis());
                        rabbitMQService.sendRechargeSuccessCallbackNotification(taskInfo);

                        //调用关闭拉取kyc监控 参数1: 卖出订单号 参数2: 买入订单号
                        kycCenterService.stopPullTransaction(paymentOrder.getPlatformOrder(), merchantCollectOrders.getPlatformOrder());
                    }
                });
                return true;
            }
        } catch (Exception e) {
            //手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("商户代收订单支付超时处理失败: 订单号: {}, e: {}", orderNo, e);
        } finally {
            //释放锁
            if (req && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return false;
    }


    /**
     * 根据U地址查询待支付的订单 USDT
     *
     * @param uAddress
     * @return {@link List }<{@link MerchantCollectOrders }>
     */
    @Override
    public List<MerchantCollectOrders> getPendingOrdersByUAddress(String uAddress) {
        return lambdaQuery()
                .eq(MerchantCollectOrders::getUsdtAddr, uAddress)
                .eq(MerchantCollectOrders::getOrderStatus, CollectionOrderStatusEnum.BE_PAID.getCode())
                .eq(MerchantCollectOrders::getPayType, PayTypeEnum.INDIAN_USDT.getCode())
                .orderByDesc(MerchantCollectOrders::getCreateTime)  // 按创建时间降序排序
                .list();
    }

    /**
     * 根据U地址查询待支付的订单 TRX
     *
     * @param uAddress
     * @return {@link List }<{@link MerchantCollectOrders }>
     */
    @Override
    public List<MerchantCollectOrders> getPendingOrdersByUAddressTRX(String uAddress) {
        return lambdaQuery()
                .eq(MerchantCollectOrders::getUsdtAddr, uAddress)
                .eq(MerchantCollectOrders::getOrderStatus, CollectionOrderStatusEnum.BE_PAID.getCode())
                .eq(MerchantCollectOrders::getPayType, PayTypeEnum.INDIAN_TRX.getCode())
                .orderByDesc(MerchantCollectOrders::getCreateTime)  // 按创建时间降序排序
                .list();
    }

    @Override
    public PageReturn<UsdtBuySuccessOrderDTO> merchantSuccessOrdersPage(UsdtBuyOrderReq req) {
        Page<MerchantCollectOrders> page = new Page<>(req.getPageNo(), req.getPageSize());
        lambdaQuery()
                .orderByDesc(MerchantCollectOrders::getPaymentTime)
                .eq(MerchantCollectOrders::getOrderStatus, CollectionOrderStatusEnum.PAID.getCode())
                .eq(MerchantCollectOrders::getUsdtAddr, req.getUsdtAddr())
                .eq(StringUtils.isNotEmpty(req.getPlatformOrder()), MerchantCollectOrders::getPlatformOrder, req.getPlatformOrder())
                .page(page);

        List<String> txids = page.getRecords().stream()
                .map(MerchantCollectOrders::getTxid)
                .collect(Collectors.toList());

        final Map<String, RechargeTronDetail> tronDetailMap = new HashMap<>();
        if (!txids.isEmpty()) {
            tronDetailMap.putAll(rechargeTronDetailService.getRechargeTronDetailByTxid(txids));
        }

        List<UsdtBuySuccessOrderDTO> list = page.getRecords().stream()
                .map(entity -> convertToSuccessOrderDTO(entity, tronDetailMap))
                .collect(Collectors.toList());

        return PageUtils.flush(page, list);
    }

    @Override
    public RestResult<MerchantCollectionOrderStatusDTO> merchantCollectionOrderStatus(
            MerchantCollectionOrderStatusReq requestVO
    ) {
        MerchantCollectOrders merchantCollectOrders = this.lambdaQuery()
                .eq(MerchantCollectOrders::getPlatformOrder, requestVO.getMerchantCollectionOrderNo())
                .eq(MerchantCollectOrders::getCurrency, requestVO.getCurrency())
                .one();
        if (Objects.isNull(merchantCollectOrders)) {
            return RestResult.failed("MerchantCollectionOrder is not exist");
        }
        return RestResult.ok(
                MerchantCollectionOrderStatusDTO.builder()
                        .orderNo(merchantCollectOrders.getPlatformOrder())
                        .orderStatus(merchantCollectOrders.getOrderStatus())
                        .syncNotifyAddress(merchantCollectOrders.getSyncNotifyAddress())
                        .build()
        );
    }

    private UsdtBuySuccessOrderDTO convertToSuccessOrderDTO(MerchantCollectOrders entity, Map<String, RechargeTronDetail> tronDetailMap) {
        RechargeTronDetail tronDetail = tronDetailMap.getOrDefault(entity.getTxid(), null);
        return UsdtBuySuccessOrderDTO.builder()
                .paymentTime(Objects.nonNull(tronDetail) ? tronDetail.getCreateTime() : null)
                .id(entity.getId())
                .txid(entity.getTxid())
                .memberUsdtAddr(Objects.nonNull(tronDetail) ? tronDetail.getFromAddress() : StringUtils.EMPTY)
                .usdtAddr(Objects.nonNull(tronDetail) ? tronDetail.getToAddress() : StringUtils.EMPTY)
                .platformOrder(entity.getPlatformOrder())
                .usdtNum(Objects.nonNull(tronDetail) ? tronDetail.getAmount() : BigDecimal.ZERO)
                .merchantId(entity.getMerchantCode())
                .build();
    }


    @Override
    public Map<String, List<MerchantCollectOrders>> collectionMap(String merchantCode) {
        return lambdaQuery()
                .eq(MerchantCollectOrders::getOrderStatus, CollectionOrderStatusEnum.PAID.getCode())
                .eq(MerchantCollectOrders::getMerchantCode, merchantCode)
                .list()
                .parallelStream()
                .collect(Collectors.groupingByConcurrent(MerchantCollectOrders::getPayType));
    }

}
