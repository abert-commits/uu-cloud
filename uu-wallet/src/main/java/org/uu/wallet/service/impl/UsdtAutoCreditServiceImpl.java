package org.uu.wallet.service.impl;

import cn.hutool.core.date.DateUtil;
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
import org.uu.common.core.enums.MemberAccountChangeEnum;
import org.uu.common.core.enums.MemberWebSocketMessageTypeEnum;
import org.uu.common.core.websocket.send.member.MemberWebSocketSendMessage;
import org.uu.common.redis.util.RedissonUtil;
import org.uu.wallet.Enum.*;
import org.uu.wallet.entity.*;
import org.uu.wallet.mapper.MemberInfoMapper;
import org.uu.wallet.mapper.TronAddressMapper;
import org.uu.wallet.mapper.UsdtBuyOrderMapper;
import org.uu.wallet.rabbitmq.RabbitMQService;
import org.uu.wallet.service.*;
import org.uu.wallet.util.AmountChangeUtil;
import org.uu.wallet.util.DelegationOrderUtil;
import org.uu.wallet.util.OrderNumberGeneratorUtil;
import org.uu.wallet.webSocket.MemberMessageSender;
import org.uu.wallet.webSocket.massage.OrderStatusChangeMessage;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class UsdtAutoCreditServiceImpl implements UsdtAutoCreditService {

    @Autowired
    private RedissonUtil redissonUtil;
    @Autowired
    private IRechargeTronDetailService rechargeTronDetailService;
    @Autowired
    private UsdtBuyOrderMapper usdtBuyOrderMapper;
    @Autowired
    private MemberInfoMapper memberInfoMapper;

    @Autowired
    private IUsdtBuyOrderService usdtBuyOrderService;

    @Autowired
    private IMemberAccountChangeService memberAccountChangeService;

    @Autowired
    private IMemberInfoService memberInfoService;

    @Autowired
    private TronAddressMapper tronAddressMapper;

    @Autowired
    private ITronAddressService tronAddressService;

    private final OrderNumberGeneratorUtil orderNumberGenerator;

    @Autowired
    private ISystemCurrencyService systemCurrencyService;

    @Autowired
    private IMerchantCollectOrdersService merchantCollectOrdersService;

    @Autowired
    private IMerchantRatesConfigService merchantRatesConfigService;

    @Autowired
    private MemberMessageSender memberMessageSender;

    private final AmountChangeUtil amountChangeUtil;

    @Resource
    RabbitMQService rabbitMQService;

    private final TradeConfigServiceImpl tradeConfigService;

    @Autowired
    private DelegationOrderUtil delegationOrderUtil;

    /**
     * 处理USDT自动上分
     *
     * @return {@link Boolean }
     */
    @Override
    @Transactional
    public Boolean usdtAutoCredit(String usdtAddress) {

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

                log.info("消费USDT自动上分, u地址: {}", usdtAddress);

                //查询用户地址信息 加上排他上锁
                TronAddress tronAddress = tronAddressMapper.selectTronAddressByAddress(usdtAddress);
                if (tronAddress == null) {
                    log.error("消费USDT自动上分失败, 获取用户地址信息失败, u地址: {}", usdtAddress);
                    return false;
                }

                if ("uuPay".equals(tronAddress.getMerchantId())) {
                    //蚂蚁USDT买入
                    return autoCreditAntUsdtPurchaseOrder(tronAddress);
                } else {
                    //商户USDT代收订单
                    return autoCreditMerchantUsdtPurchaseOrder(tronAddress);
                }
            }
        } catch (Exception e) {
            //手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("消费 USDT自动上分 失败: , U地址: {}, e: {}", usdtAddress, e);
            return Boolean.FALSE;
        } finally {
            //释放锁
            if (req && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        log.error("消费 USDT自动上分 失败: , U地址: {}", usdtAddress);
        return Boolean.FALSE;
    }

    /**
     * 蚂蚁USDT买入订单自动上分
     *
     * @return boolean
     */
    private boolean autoCreditAntUsdtPurchaseOrder(TronAddress tronAddress) {

        log.info("消费USDT自动上分 蚂蚁, u地址: {}", tronAddress.getAddress());

        //获取配置信息
        TradeConfig tradeConfig = tradeConfigService.getById(1);

        //**一次只处理一笔
        //将两天内 所有金额大于最低充值金额并且未上过分的交易记录 最新的一笔交易记录 加上排他行锁
        RechargeTronDetail rechargeTronDetail = rechargeTronDetailService.getLatestPendingOrderWithLock(tronAddress.getAddress(), tradeConfig.getMinAntUsdtDepositAmount());

        if (rechargeTronDetail == null){
            log.error("消费USDT自动上分失败, 未查询到钱包交易记录, u地址: {}", tronAddress.getAddress());
            return true;
        }

        if (!"0".equals(rechargeTronDetail.getOrderId())) {
            log.error("消费USDT自动上分失败, 钱包交易记录已被处理过, u地址: {}, rechargeTronDetail: {}", tronAddress.getAddress(), rechargeTronDetail);
            return true;
        }

        //蚂蚁
        //查询会员信息 加上排他行锁
        MemberInfo buyMemberInfo = memberInfoMapper.selectMemberInfoForUpdate(Long.valueOf(tronAddress.getMemberId()));
        if (buyMemberInfo == null) {
            log.error("消费USDT自动上分失败, 获取会员信息失败, u地址: {}, rechargeTronDetail: {}", tronAddress.getAddress(), rechargeTronDetail);
            return false;
        }

        String platformOrder = orderNumberGenerator.generateOrderNo("USDT");

        //获取实时汇率
        BigDecimal currencyExchangeRate = tradeConfig.getUsdtCurrency();

        //将实际USDT金额 计算出对应的ARB金额 USDT金额 * 汇率 保留两位小数 舍弃后面的小数
        BigDecimal calculatedArbAmount = rechargeTronDetail.getAmount().multiply(currencyExchangeRate).setScale(2, RoundingMode.DOWN);

        //查询该地址对应的USDT买入订单 支付中
        UsdtBuyOrder usdtBuyOrder = usdtBuyOrderMapper.selectPendingUsdtBuyOrderForAddressForUpdate(tronAddress.getAddress());
        if (usdtBuyOrder == null) {
            //为钱包交易记录绑定USDT订单关系
            updateRechargeTronDetail(rechargeTronDetail.getTxid(), platformOrder);
            //为用户生成一笔USDT支付订单
            createUsdtBuyOrder(buyMemberInfo, tronAddress.getAddress(), rechargeTronDetail, currencyExchangeRate, calculatedArbAmount, platformOrder);
            //更新会员账变
            updateMemberInfo(buyMemberInfo, calculatedArbAmount, platformOrder, tronAddress);

            log.info("消费USDT自动上分成功, 未匹配到合适订单, 系统自动为用户生成USDT订单, 订单号: {} u地址: {}, rechargeTronDetail: {}", platformOrder, tronAddress.getAddress(), rechargeTronDetail);

            return true;
        }

        //如果查询到的USDT订单是已完成的 那么不做处理
        if (OrderStatusEnum.SUCCESS.getCode().equals(usdtBuyOrder.getStatus())) {
            log.error("消费USDT自动上分失败, 该笔订单状态是已完成, u地址: {}, rechargeTronDetail: {}, usdtBuyOrder: {}",  tronAddress.getAddress(), rechargeTronDetail, usdtBuyOrder);
            return true;
        }

        if (usdtBuyOrder.getUsdtNum().compareTo(rechargeTronDetail.getAmount()) != 0) {
            //订单金额不符合
            //为钱包交易记录绑定USDT订单关系
            updateRechargeTronDetail(rechargeTronDetail.getTxid(), platformOrder);
            //为用户生成一笔USDT支付订单
            createUsdtBuyOrder(buyMemberInfo, tronAddress.getAddress(), rechargeTronDetail, currencyExchangeRate, calculatedArbAmount, platformOrder);
            //更新会员账变
            updateMemberInfo(buyMemberInfo, calculatedArbAmount, platformOrder, tronAddress);

            //将之前的订单关闭
            LambdaUpdateWrapper<UsdtBuyOrder> lambdaUpdateWrapperUsdtBuyOrder = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapperUsdtBuyOrder.eq(UsdtBuyOrder::getPlatformOrder, usdtBuyOrder.getPlatformOrder())  // 指定更新条件，订单号
                    .set(UsdtBuyOrder::getStatus, OrderStatusEnum.WAS_CANCELED.getCode()); // 指定更新字段 (订单状态)
            // 这里传入的 null 表示不更新实体对象的其他字段
            usdtBuyOrderService.update(null, lambdaUpdateWrapperUsdtBuyOrder);

            log.info("消费USDT自动上分成功, 订单金额不符, 系统自动为用户生成USDT订单, 订单号: {} u地址: {}, rechargeTronDetail: {}", platformOrder, tronAddress.getAddress(), rechargeTronDetail);

            return true;
        }

        //订单状态支付中 并且金额相等 那么更新USDT订单状态
        if (usdtBuyOrder.getStatus().equals(OrderStatusEnum.BE_PAID.getCode()) && usdtBuyOrder.getUsdtNum().compareTo(rechargeTronDetail.getAmount()) == 0) {
            LambdaUpdateWrapper<UsdtBuyOrder> lambdaUpdateWrapperUsdtBuyOrder2 = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapperUsdtBuyOrder2.eq(UsdtBuyOrder::getPlatformOrder, usdtBuyOrder.getPlatformOrder())  // 指定更新条件，订单号
                    .set(UsdtBuyOrder::getStatus, OrderStatusEnum.SUCCESS.getCode()) // 指定更新字段 (订单状态)
                    .set(UsdtBuyOrder::getTxid, rechargeTronDetail.getTxid()) // 指定更新字段 (txId)
                    .set(UsdtBuyOrder::getFromAddress, rechargeTronDetail.getFromAddress()) // 指定更新字段 (付款人地址)
                    .set(UsdtBuyOrder::getPaymentTime, LocalDateTime.now()) // 指定更新字段 (付款时间)
                    .set(UsdtBuyOrder::getCompletionTime, LocalDateTime.now()); // 指定更新字段 (订单完成时间)
            // 这里传入的 null 表示不更新实体对象的其他字段
            usdtBuyOrderService.update(null, lambdaUpdateWrapperUsdtBuyOrder2);

            //**这里避免汇率浮动 所以采用生成订单时的汇率
            //将实际USDT金额 计算出对应的ARB金额 USDT金额 * 生成订单时的汇率 保留两位小数 舍弃后面的小数
            BigDecimal calculatedArbAmountNow = rechargeTronDetail.getAmount().multiply(usdtBuyOrder.getExchangeRates()).setScale(2, RoundingMode.DOWN);

            //为钱包交易记录绑定USDT订单关系
            updateRechargeTronDetail(rechargeTronDetail.getTxid(), usdtBuyOrder.getPlatformOrder());
            //更新会员账变
            updateMemberInfo(buyMemberInfo, calculatedArbAmountNow, usdtBuyOrder.getPlatformOrder(), tronAddress);

            log.info("消费USDT自动上分成功, 订单状态与金额都相符, 更新之前订单状态为已完成, 订单号: {} u地址: {}, rechargeTronDetail: {}", usdtBuyOrder.getPlatformOrder(), tronAddress.getAddress(), rechargeTronDetail);

            memberMessageSender.send(
                    // 构建用户WebSocket消息体
                    MemberWebSocketSendMessage.buildMemberWebSocketMessage(
                            MemberWebSocketMessageTypeEnum.BUY_USDT.getMessageType(),
                            String.valueOf(buyMemberInfo.getId()),
                            OrderStatusChangeMessage
                                    .builder()
                                    .orderNo(usdtBuyOrder.getPlatformOrder())
                                    .orderStatus(OrderStatusEnum.SUCCESS.getCode())
                                    .build()
                    )
            );

            return true;
        }

        log.error("消费USDT自动上分失败, 未知状态, u地址: {}, rechargeTronDetail: {}", tronAddress.getAddress(), rechargeTronDetail);

        return false;
    }

    /**
     * 商户USDT买入订单自动上分
     *
     * @return boolean
     */
    private boolean autoCreditMerchantUsdtPurchaseOrder(TronAddress tronAddress) {

        log.info("消费USDT自动上分 商户, u地址: {}", tronAddress.getAddress());

        //查询当前商户的支付类型配置
        MerchantRatesConfig merchantRatesConfig = merchantRatesConfigService.getMerchantRatesConfigByCode("1", PayTypeEnum.INDIAN_USDT.getCode(), tronAddress.getMerchantId());

        //如果不存在对应的支付类型配置 驳回
        if (merchantRatesConfig == null) {
            log.error("消费 USDT自动上分 失败, 不存在对应的支付类型配置: u地址: {}", tronAddress.getAddress());
            return false;
        }

        //**一次只处理一笔
        //将两天内 所有金额大于最低充值金额并且未上过分的交易记录 最新的一笔交易记录 加上排他行锁
        RechargeTronDetail rechargeTronDetail = rechargeTronDetailService.getLatestPendingOrderWithLock(tronAddress.getAddress(), merchantRatesConfig.getMoneyMin());

        if (rechargeTronDetail == null){
            log.error("消费USDT自动上分失败, 未查询到钱包交易记录, u地址: {}", tronAddress.getAddress());
            return true;
        }

        if (!"0".equals(rechargeTronDetail.getOrderId())) {
            log.error("消费USDT自动上分失败, 钱包交易记录已被处理过, u地址: {}, rechargeTronDetail: {}", tronAddress.getAddress(), rechargeTronDetail);
            return true;
        }

        //代收订单
        //查询该会员所有在待支付中的USDT代收订单
        List<MerchantCollectOrders> pendingOrdersByUAddress = merchantCollectOrdersService.getPendingOrdersByUAddress(tronAddress.getAddress());

        if (pendingOrdersByUAddress == null || pendingOrdersByUAddress.size() == 0) {
            //当前地址没有在支付中的订单 直接消费成功不做处理
            log.error("消费USDT自动上分失败, 当前没有在待支付的代收订单, u地址: {}, rechargeTronDetail: {}", tronAddress.getAddress(), rechargeTronDetail);
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
                .set(MerchantCollectOrders::getTxid, rechargeTronDetail.getTxid()) // 指定更新字段 txId
                .set(MerchantCollectOrders::getPaymentTime, LocalDateTime.now()) // 指定更新字段 付款时间
                .set(MerchantCollectOrders::getCompletionTime, LocalDateTime.now()); // 指定更新字段 完成时间
        // 这里传入的 null 表示不更新实体对象的其他字段
        merchantCollectOrdersService.update(null, wrapperMerchantCollectOrder);

        //**累加商户余额
        //更新商户信息 并记录商户账变
        Boolean updatemerchantInfo = amountChangeUtil.insertOrUpdateAccountChange(
                MerchantCollectOrder.getMerchantCode(),//商户号
                rechargeTronDetail.getAmount(),//账变金额 USDT金额
                ChangeModeEnum.ADD,//账变类型 收入
                "USDT",//币种
                MerchantCollectOrder.getPlatformOrder(),//平台订单号
                AccountChangeEnum.COLLECTION,//账变类型 代收
                "API-USDT代收",//备注
                MerchantCollectOrder.getMerchantOrder(),//商户订单号
                ChannelEnum.USDT.getName(),//商户支付通道
                "",
                BalanceTypeEnum.TRC20.getName()
        );

        if (!updatemerchantInfo) {
            log.error("消费 USDT自动上分 失败: 记录商户账变失败: 订单信息: {}", MerchantCollectOrder);
            // 抛出运行时异常
            throw new RuntimeException("消费 USDT自动上分 失败: 更新商户信息失败，触发事务回滚。");
        }


        //订单总费用 = 订单费用 + 固定手续费
        BigDecimal feeUSdt = cost.add(merchantRatesConfig.getFixedFee());

        if (feeUSdt != null && feeUSdt.compareTo(BigDecimal.ZERO) > 0) {
            //订单费用大于0 才进行操作

            //从商户余额减去该笔订单总费用
            //记录商户账变 (订单费用)
            Boolean updatemerchantInfoFee = amountChangeUtil.insertOrUpdateAccountChange(
                    MerchantCollectOrder.getMerchantCode(),//商户号
                    feeUSdt,//账变金额 (订单费用 USDT费用)
                    ChangeModeEnum.SUB,//账变类型 支出
                    "USDT",//币种
                    MerchantCollectOrder.getPlatformOrder(),//平台订单号
                    AccountChangeEnum.COLLECTION_FEE,//账变类型 代收费用
                    "USDT-API代收费用",//备注
                    MerchantCollectOrder.getMerchantOrder(),//商户订单号
                    ChannelEnum.USDT.getName(),//商户支付通道类型 手续费账变 传空值
                    "",
                    BalanceTypeEnum.TRC20.getName()
            );

            if (!updatemerchantInfoFee) {
                log.error("消费 USDT自动上分 失败: 记录商户账变失败: 订单信息: {}", MerchantCollectOrder);
                // 抛出运行时异常
                throw new RuntimeException("消费 USDT自动上分 失败: 更新商户信息失败，触发事务回滚。");
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
     * 创建USDT订单
     *
     * @return boolean
     */
    private boolean createUsdtBuyOrder(MemberInfo buyMemberInfo, String usdtAddress, RechargeTronDetail rechargeTronDetail, BigDecimal currencyExchangeRate, BigDecimal calculatedArbAmount, String platformOrder) {
        UsdtBuyOrder usdtBuyOrderNew = new UsdtBuyOrder();
        usdtBuyOrderNew.setMemberId(String.valueOf(buyMemberInfo.getId()));
        usdtBuyOrderNew.setMemberAccount(buyMemberInfo.getMemberAccount());
        usdtBuyOrderNew.setPlatformOrder(platformOrder);
        usdtBuyOrderNew.setUsdtAddr(usdtAddress);
        usdtBuyOrderNew.setUsdtNum(rechargeTronDetail.getAmount());
        usdtBuyOrderNew.setUsdtActualNum(rechargeTronDetail.getAmount());
        usdtBuyOrderNew.setArbNum(calculatedArbAmount);
        usdtBuyOrderNew.setArbActualNum(calculatedArbAmount);
        usdtBuyOrderNew.setStatus(OrderStatusEnum.SUCCESS.getCode());
        usdtBuyOrderNew.setPaymentTime(LocalDateTime.now());
        usdtBuyOrderNew.setPayType(UsdtPayTypeEnum.TRC20.getCode());
        usdtBuyOrderNew.setExchangeRates(currencyExchangeRate);
        usdtBuyOrderNew.setTxid(rechargeTronDetail.getTxid());
        usdtBuyOrderNew.setFromAddress(rechargeTronDetail.getFromAddress());
        usdtBuyOrderNew.setCompletionTime(LocalDateTime.now());
        return usdtBuyOrderService.save(usdtBuyOrderNew);
    }

    /**
     * 更新会员信息
     *
     * @return boolean
     */
    private boolean updateMemberInfo(MemberInfo buyMemberInfo, BigDecimal orderAmount, String platformOrder, TronAddress tronAddress) {

        //成功 需要增加成功笔数
        //用户地址表 订单成功总数+1
        LambdaUpdateWrapper<TronAddress> lambdaUpdateWrapperTronAddress = new LambdaUpdateWrapper<>();
        // 指定更新条件，地址
        lambdaUpdateWrapperTronAddress.eq(TronAddress::getAddress, tronAddress.getAddress());
        // 订单成功数+1
        lambdaUpdateWrapperTronAddress.set(TronAddress::getOrderSuccessNum, tronAddress.getOrderSuccessNum() + 1);
        // 这里传入的 null 表示不更新实体对象的其他字段
        tronAddressService.update(null, lambdaUpdateWrapperTronAddress);

        //修改会员余额
        //账变前余额
        BigDecimal previousBalance = buyMemberInfo.getBalance();

        //增加买入会员余额
        buyMemberInfo.setBalance(buyMemberInfo.getBalance().add(orderAmount));

        //账变后余额
        BigDecimal newBalance = buyMemberInfo.getBalance();

        //累计买入成功次数
        buyMemberInfo.setTotalBuySuccessCount(buyMemberInfo.getTotalBuySuccessCount() + 1);

        //累计买入成功金额
        buyMemberInfo.setTotalBuySuccessAmount(buyMemberInfo.getTotalBuySuccessAmount().add(orderAmount));

        //更新今日买入成功金额
        buyMemberInfo.setTodayBuySuccessAmount(buyMemberInfo.getTodayBuySuccessAmount().add(orderAmount));

        //更新今日买入成功次数
        buyMemberInfo.setTodayBuySuccessCount(buyMemberInfo.getTodayBuySuccessCount() + 1);

        //添加会员余额账变
        memberAccountChangeService.recordMemberTransaction(String.valueOf(buyMemberInfo.getId()), orderAmount, MemberAccountChangeEnum.USDT_RECHARGE.getCode(), platformOrder, previousBalance, newBalance, "", "", "USDT自动完成订单");

        //判断如果会员在委托中 那么重新进行委托
        if (buyMemberInfo.getDelegationStatus() == 1) {
            //会员买入成功后 重新进行委托
            delegationOrderUtil.redelegate(buyMemberInfo.getId(), newBalance);
        }

        //更新买入会员信息
        return memberInfoService.updateById(buyMemberInfo);
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
