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
import org.uu.common.redis.util.RedissonUtil;
import org.uu.wallet.Enum.*;
import org.uu.wallet.entity.*;
import org.uu.wallet.mapper.TronAddressMapper;
import org.uu.wallet.rabbitmq.RabbitMQService;
import org.uu.wallet.service.*;
import org.uu.wallet.util.AmountChangeUtil;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Service
@Slf4j
@RequiredArgsConstructor
public class TrxAutoCreditServiceImpl implements TrxAutoCreditService {

    @Autowired
    private RedissonUtil redissonUtil;
    @Autowired
    private IRechargeTronDetailService rechargeTronDetailService;

    @Autowired
    private TronAddressMapper tronAddressMapper;

    @Autowired
    private ITronAddressService tronAddressService;

    @Autowired
    private IMerchantCollectOrdersService merchantCollectOrdersService;

    @Autowired
    private IMerchantRatesConfigService merchantRatesConfigService;

    private final AmountChangeUtil amountChangeUtil;

    @Resource
    RabbitMQService rabbitMQService;


    /**
     * 处理TRX自动上分
     *
     * @param usdtAddress
     * @return {@link Boolean }
     */
    @Override
    @Transactional
    public Boolean trxAutoCredit(String usdtAddress) {

        try {
            //为了避免充值记录表还未写进去 暂停3秒再进行操作
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        //分布式锁key uu-uuPay-usdtAutoCredit+U地址
        String key = "uu-uuPay-usdtAutoCredit" + usdtAddress;
        RLock lock = redissonUtil.getLock(key);

        boolean req = false;

        try {
            req = lock.tryLock(10, TimeUnit.SECONDS);

            if (req) {

                log.info("消费TRX自动上分, u地址: {}", usdtAddress);

                //查询用户地址信息 加上排他上锁
                TronAddress tronAddress = tronAddressMapper.selectTronAddressByAddress(usdtAddress);
                if (tronAddress == null) {
                    log.error("消费TRX自动上分失败, 获取用户地址信息失败, u地址: {}", usdtAddress);
                    return false;
                }

                if ("uuPay".equals(tronAddress.getMerchantId())) {
                    //蚂蚁用户 不进行处理 直接消费成功
                    log.info("消费TRX自动上分成功, 该笔交易是蚂蚁用户, 不进行处理, u地址: {}", usdtAddress);
                    return true;
                }

                return autoCreditMerchantUsdtPurchaseOrder(tronAddress);
            }
        } catch (Exception e) {
            //手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("消费 TRX自动上分 失败: , U地址: {}, e: {}", usdtAddress, e);
            return Boolean.FALSE;
        } finally {
            //释放锁
            if (req && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        log.error("消费 TRX自动上分 失败: , U地址: {}", usdtAddress);
        return Boolean.FALSE;
    }


    /**
     * 商户TRX买入订单自动上分
     *
     * @return boolean
     */
    private boolean autoCreditMerchantUsdtPurchaseOrder(TronAddress tronAddress) {
        log.info("消费TRX自动上分 商户, u地址: {}", tronAddress.getAddress());

        //查询当前商户的支付类型配置
        MerchantRatesConfig merchantRatesConfig = merchantRatesConfigService.getMerchantRatesConfigByCode("1", PayTypeEnum.INDIAN_TRX.getCode(), tronAddress.getMerchantId());

        //如果不存在对应的支付类型配置 驳回
        if (merchantRatesConfig == null) {
            log.error("消费 TRX自动上分 失败, 不存在对应的支付类型配置: u地址: {}", tronAddress.getAddress());
            return false;
        }

        //**一次只处理一笔
        //将两天内 所有金额大于最低充值金额并且未上过分的交易记录 最新的一笔交易记录 加上排他行锁 TRX
        RechargeTronDetail rechargeTronDetail = rechargeTronDetailService.getLatestPendingOrderWithLockTRX(tronAddress.getAddress(), merchantRatesConfig.getMoneyMin());

        if (rechargeTronDetail == null) {
            log.error("消费TRX自动上分失败, 未查询到钱包交易记录, u地址: {}", tronAddress.getAddress());
            return true;
        }

        if (!"0".equals(rechargeTronDetail.getOrderId())) {
            log.error("消费TRX自动上分失败, 钱包交易记录已被处理过, u地址: {}, rechargeTronDetail: {}", tronAddress.getAddress(), rechargeTronDetail);
            return true;
        }

        //代收订单
        //查询该会员所有在待支付中的TRX代收订单
        List<MerchantCollectOrders> pendingOrdersByUAddress = merchantCollectOrdersService.getPendingOrdersByUAddressTRX(tronAddress.getAddress());

        if (pendingOrdersByUAddress == null || pendingOrdersByUAddress.size() == 0) {
            //当前地址没有在支付中的订单 直接消费成功不做处理
            log.error("消费TRX自动上分失败, 当前没有在待支付的代收订单, u地址: {}, rechargeTronDetail: {}", tronAddress.getAddress(), rechargeTronDetail);
            return true;
        }

        //默认取第一条
        MerchantCollectOrders MerchantCollectOrder = pendingOrdersByUAddress.get(0);

        for (MerchantCollectOrders ordersByUAddress : pendingOrdersByUAddress) {
            //金额相等 取第一条
            if (ordersByUAddress.getAmount().compareTo(rechargeTronDetail.getAmount()) == 0) {
                MerchantCollectOrder = ordersByUAddress;
                break;
            }
        }

        //**使用实际收到的钱计算订单费用
        //订单费用 默认为0
        BigDecimal cost = BigDecimal.ZERO;
        //代收费率大于0才计算费用
        if (merchantRatesConfig.getRates() != null && merchantRatesConfig.getRates().compareTo(BigDecimal.ZERO) > 0) {
            //使用实际收到的钱计算订单费用
            cost = rechargeTronDetail.getAmount().multiply((merchantRatesConfig.getRates().divide(BigDecimal.valueOf(100))));
        }

        //更新代收订单状态 实际金额 订单费用 txID
        LambdaUpdateWrapper<MerchantCollectOrders> wrapperMerchantCollectOrder = new LambdaUpdateWrapper<>();
        wrapperMerchantCollectOrder
                .eq(MerchantCollectOrders::getPlatformOrder, MerchantCollectOrder.getPlatformOrder())  // 指定更新条件，订单号
                .set(MerchantCollectOrders::getOrderStatus, CollectionOrderStatusEnum.PAID.getCode())  // 指定更新条件，订单状态 成功
                .set(MerchantCollectOrders::getAmount, rechargeTronDetail.getAmount()) // 指定更新字段 订单实际金额
                .set(MerchantCollectOrders::getCost, cost) // 指定更新字段 订单费用
                .set(MerchantCollectOrders::getTxid, rechargeTronDetail.getTxid()); // 指定更新字段 txId
        // 这里传入的 null 表示不更新实体对象的其他字段
        merchantCollectOrdersService.update(null, wrapperMerchantCollectOrder);

        //**累加商户余额
        //更新商户信息 并记录商户账变
        Boolean updatemerchantInfo = amountChangeUtil.insertOrUpdateAccountChange(
                MerchantCollectOrder.getMerchantCode(),//商户号
                rechargeTronDetail.getAmount(),//账变金额 TRX金额
                ChangeModeEnum.ADD,//账变类型 收入
                "TRX",//币种
                MerchantCollectOrder.getPlatformOrder(),//平台订单号
                AccountChangeEnum.COLLECTION,//账变类型 代收
                "API-TRX代收",//备注
                MerchantCollectOrder.getMerchantOrder(),//商户订单号
                ChannelEnum.INDIAN_TRX.getName(),//商户支付通道
                "",
                BalanceTypeEnum.TRX.getName()
        );

        if (!updatemerchantInfo) {
            log.error("消费 TRX自动上分 失败: 记录商户账变失败: 订单信息: {}", MerchantCollectOrder);
            // 抛出运行时异常
            throw new RuntimeException("消费 TRX自动上分 失败: 更新商户信息失败，触发事务回滚。");
        }


        //订单总费用 = 订单费用 + 固定手续费
        BigDecimal FeeTRX = cost.add(merchantRatesConfig.getFixedFee());

        if (FeeTRX != null && FeeTRX.compareTo(BigDecimal.ZERO) > 0) {
            //订单费用大于0 才进行操作

            //从商户余额减去该笔订单总费用
            //记录商户账变 (订单费用)
            Boolean updatemerchantInfoFee = amountChangeUtil.insertOrUpdateAccountChange(
                    MerchantCollectOrder.getMerchantCode(),//商户号
                    FeeTRX,//账变金额 (订单费用 TRX费用)
                    ChangeModeEnum.SUB,//账变类型 支出
                    "TRX",//币种
                    MerchantCollectOrder.getPlatformOrder(),//平台订单号
                    AccountChangeEnum.COLLECTION_FEE,//账变类型 代收费用
                    "TRX-API代收费用",//备注
                    MerchantCollectOrder.getMerchantOrder(),//商户订单号
                    ChannelEnum.INDIAN_TRX.getName(),//商户支付通道类型 手续费账变 传空值
                    "",
                    BalanceTypeEnum.TRX.getName()
            );

            if (!updatemerchantInfoFee) {
                log.error("消费 TRX自动上分 失败: 记录商户账变失败: 订单信息: {}", MerchantCollectOrder);
                // 抛出运行时异常
                throw new RuntimeException("消费 TRX自动上分 失败: 更新商户信息失败，触发事务回滚。");
            }
        }

        //为钱包交易记录绑定USDT订单关系
        updateRechargeTronDetail(rechargeTronDetail.getTxid(), MerchantCollectOrder.getPlatformOrder());

        //成功 需要增加成功笔数
        //用户地址表 订单成功总数+1
        LambdaUpdateWrapper<TronAddress> lambdaUpdateWrapperTronAddress = new LambdaUpdateWrapper<>();
        // 指定更新条件，地址
        lambdaUpdateWrapperTronAddress.eq(TronAddress::getAddress, tronAddress.getAddress());
        // 订单成功数+1
        lambdaUpdateWrapperTronAddress.set(TronAddress::getOrderSuccessNum, tronAddress.getOrderSuccessNum() + 1);
        // 这里传入的 null 表示不更新实体对象的其他字段
        tronAddressService.update(null, lambdaUpdateWrapperTronAddress);

        //注册事务同步回调, 事务提交成功后, 发送延时MQ 改变订单为超时状态
        MerchantCollectOrders finalMerchantCollectOrder = MerchantCollectOrder;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                //事务提交成功

                //发送支付超时的MQ
                //订单支付成功 异步回调通知商户
                TaskInfo taskInfo = new TaskInfo(finalMerchantCollectOrder.getPlatformOrder(), TaskTypeEnum.DEPOSIT_NOTIFICATION.getCode(), System.currentTimeMillis());
                rabbitMQService.sendRechargeSuccessCallbackNotification(taskInfo);
            }
        });

        return true;
    }


    /**
     * 为钱包交易记录绑定USDT订单关系
     */
    private boolean updateRechargeTronDetail(String txId, String platformOrder) {
        //将钱包交易记录订单号赋值
        // 创建一个 UpdateWrapper 对象，用于构建更新条件和指定更新字段
        LambdaUpdateWrapper<RechargeTronDetail> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
        lambdaUpdateWrapper.eq(RechargeTronDetail::getTxid, txId)  // 指定更新条件，txId
                .set(RechargeTronDetail::getOrderId, platformOrder); // 指定更新字段 (订单号)
        // 这里传入的 null 表示不更新实体对象的其他字段
        return rechargeTronDetailService.update(null, lambdaUpdateWrapper);
    }
}
