package org.uu.wallet.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.tron.trident.core.key.KeyPair;
import org.uu.common.core.page.PageReturn;
import org.uu.common.core.result.KycRestResult;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.common.pay.enums.OrderEventEnum;
import org.uu.common.pay.req.OrderEventReq;
import org.uu.common.redis.constants.RedisKeys;
import org.uu.common.redis.util.RedissonUtil;
import org.uu.common.web.utils.UserContext;
import org.uu.wallet.Enum.*;
import org.uu.wallet.entity.*;
import org.uu.wallet.mapper.*;
import org.uu.wallet.property.ArProperty;
import org.uu.wallet.rabbitmq.RabbitMQService;
import org.uu.wallet.req.*;
import org.uu.wallet.service.*;
import org.uu.wallet.tron.utils.RSAUtils;
import org.uu.wallet.util.*;
import org.uu.wallet.vo.*;
import org.uu.wallet.webSocket.MemberSendAmountList;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class IBuyServiceImpl implements IBuyService {

    private final RedissonUtil redissonUtil;
    private final ICollectionOrderService collectionOrderService;
    @Autowired
    private IUsdtBuyOrderService usdtBuyOrderService;
    private final IMemberInfoService memberInfoService;

    private final TradeConfigMapper tradeConfigMapper;
    private final CollectionOrderMapper collectionOrderMapper;
    private final RedisUtil redisUtil;
    private final ITradeConfigService tradeConfigService;
    private final RedisTemplate redisTemplate;
    //从nacos获取配置
    private final ArProperty arProperty;
    private final RabbitMQService rabbitMQService;

    @Autowired
    private MemberSendAmountList memberSendAmountList;

    @Autowired
    private MemberInfoMapper memberInfoMapper;

    private final OrderNumberGeneratorUtil orderNumberGenerator;

    @Autowired
    private ITradeIpBlacklistService tradeIpBlacklistService;

    @Value("${oss.baseUrl}")
    private String baseUrl;


    @Autowired
    private TradeConfigHelperUtil tradeConfigHelperUtil;
    @Autowired
    private ImageRecognitionService imageRecognitionService;

    @Autowired
    private IControlSwitchService controlSwitchService;

    @Autowired
    private ITronAddressService tronAddressService;

    @Autowired
    private IMerchantPaymentOrdersService merchantPaymentOrdersService;

    @Autowired
    private MerchantInfoMapper merchantInfoMapper;

    @Autowired
    private IMerchantInfoService merchantInfoService;

    @Autowired
    private MerchantPaymentOrdersMapper merchantPaymentOrdersMapper;

    @Autowired
    private IKycCenterService kycCenterService;

    @Autowired
    private IKycPartnersService kycPartnersService;

    @Autowired
    private ISystemCurrencyService systemCurrencyService;

    /**
     * 获取买入金额列表
     *
     * @param buyListReq
     * @return {@link List}<{@link BuyListVo}>
     */
    @Override
    public PageReturn<BuyListVo> getBuyList(BuyListReq buyListReq) {

        if (buyListReq == null) {
            buyListReq = new BuyListReq();
        }

        //获取会员信息
        MemberInfo memberInfo = memberInfoService.getMemberInfo();
        if (memberInfo == null) {
            log.error("获取买入列表失败, 获取会员信息失败");
        }

        //会员id
        buyListReq.setMemberId(String.valueOf(memberInfo.getId()));

        //从redis里面获取买入金额列表
        PageReturn<BuyListVo> buyList = redisUtil.getBuyList(buyListReq);

        log.info("获取买入金额列表: {}, 会员id: {}", buyList, memberInfo.getId());

        return buyList;
    }

    /**
     * 买入处理
     *
     * @param buyReq
     * @return {@link Boolean}
     */
    @Override
    @Transactional
    public RestResult buyProcessor(BuyReq buyReq, HttpServletRequest request) {

        //获取当前会员id
        Long memberId = UserContext.getCurrentUserId();
        if (memberId == null) {
            return RestResult.failure(ResultCode.RELOGIN);
        }

        //分布式锁key ar-wallet-buy+买入订单号
        String key1 = "ar-wallet-buy" + buyReq.getPlatformOrder();
        RLock lock1 = redissonUtil.getLock(key1);

        //分布式锁key ar-wallet-buy+会员id
        String key = "ar-wallet-sell" + memberId;
        RLock lock = redissonUtil.getLock(key);

        boolean req1 = false;
        boolean req = false;

        try {
            req1 = lock1.tryLock(10, TimeUnit.SECONDS);

            if (req1) {

                //获取配置信息
                TradeConfig tradeConfig = tradeConfigService.getById(1);

                BuyListVo orderDetails = null;

                req = lock.tryLock(10, TimeUnit.SECONDS);

                if (req) {

                    //获取redis订单信息 以便事务执行失败了进行补偿性操作
                    orderDetails = redisUtil.getOrderDetails(buyReq.getPlatformOrder());
                    if (orderDetails == null) {
                        log.error("买入下单失败, 从缓存中查不到订单详情, 订单已被其他人买入或状态异常, 会员id: {}, buyReq: {}", memberId, buyReq);
                        return RestResult.failure(ResultCode.ORDER_ALREADY_USED_BY_OTHERS);
                    }

                    //检查当前会员是否处于买入冷却期
                    if (!redisUtil.canMemberBuy(String.valueOf(memberId))) {

                        //会员处于冷却期 不能购买
                        log.error("买入下单失败, 当前会员处于买入冷却期, 会员id: {}, buyReq: {}", memberId, buyReq);

                        DisableBuyingVo disableBuyingVo = new DisableBuyingVo();

                        //获取会员被禁用的时间
                        Integer memberBuyBlockRemainingTime = redisUtil.getMemberBuyBlockRemainingTime(String.valueOf(memberId));

                        if (memberBuyBlockRemainingTime == null) {
                            memberBuyBlockRemainingTime = tradeConfig.getDisabledTime();
                        }

                        //禁止买入小时数
                        disableBuyingVo.setBuyDisableHours(memberBuyBlockRemainingTime);
                        //剩余时间(秒)
                        disableBuyingVo.setRemainingSeconds(redisUtil.getMemberBuyBlockedExpireTime(String.valueOf(memberId)));
                        // 失败次数
                        disableBuyingVo.setNumberFailures(tradeConfig.getNumberFailures());

                        return RestResult.failure(ResultCode.BUY_FAILED_OVER_TIMES, disableBuyingVo);
                    }

                    //获取当前买入会员信息 加上排他行锁
                    MemberInfo buyMemberInfo = memberInfoMapper.selectMemberInfoForUpdate(memberId);

                    if (buyMemberInfo == null) {
                        log.error("买入处理失败: 获取当前会员信息失败");
                        return RestResult.failure(ResultCode.RELOGIN);
                    }

                    //会员不是内部会员 并且没有传kycId
                    if (!MemberTypeEnum.INTERNAL_MERCHANT_MEMBER.getCode().equals(buyMemberInfo.getMemberType())) {
                        if (buyReq.getKycId() == null || buyReq.getKycId() < 1) {
                            log.error("买入处理失败, kyc信息不存在或kyc未连接或该kyc信息不属于该会员, 会员账号: {}, buyReq: {}", buyMemberInfo.getMemberAccount(), buyReq);
                            return RestResult.failure(ResultCode.KYC_NOT_CONNECTED);
                        }
                    }

                    //获取买入 ip
                    String realIP = IpUtil.getRealIP(request);

                    //获取环境信息
                    String appEnv = arProperty.getAppEnv();
                    boolean isTestEnv = "sit".equals(appEnv) || "dev".equals(appEnv);

                    if (!isTestEnv) {
                        //线上环境 校验ip是否在交易黑名单中
                        if (tradeIpBlacklistService.isIpBlacklisted(realIP)) {
                            log.error("买入处理失败, 该交易ip处于黑名单列表中, 会员id: {}, 会员账号: {}, 会员信息: {}, 交易ip: {}", memberId, buyMemberInfo.getMemberAccount(), buyMemberInfo, realIP);
                            return RestResult.failure(ResultCode.IP_BLACKLISTED);
                        }
                    }

                    //校验会员有没有实名认证
//                    if (StringUtils.isEmpty(buyMemberInfo.getRealName()) || StringUtils.isEmpty(buyMemberInfo.getIdCardNumber())) {
//                        log.error("买入下单处理失败: 该会员没有实名认证 req: {}, 会员信息: {}", buyReq, buyMemberInfo);
//                        return RestResult.failure(ResultCode.MEMBER_NOT_VERIFIED);
//                    }

                    //校验会员是否有买入权限
//                    if (!MemberPermissionCheckerUtil.hasPermission(memberGroupService.getAuthListById(buyMemberInfo.getMemberGroup()), MemberPermissionEnum.BUY)) {
//                        log.error("买入处理失败, 当前会员所在分组没有买入权限, 会员账号: {}", buyMemberInfo.getMemberAccount());
//                        return RestResult.failure(ResultCode.NO_PERMISSION);
//                    }

                    //查看当前会员是否有未完成的买入订单
                    CollectionOrder collectionOrder = collectionOrderService.countActiveBuyOrders(String.valueOf(memberId));
                    if (collectionOrder != null) {
                        log.error("买入处理失败, 当前有未完成的订单: {}, buyReq: {}, 会员账号: {}", collectionOrder, buyReq, buyMemberInfo.getMemberAccount());
                        PendingOrderVo pendingOrderVo = new PendingOrderVo();
                        pendingOrderVo.setPlatformOrder(collectionOrder.getPlatformOrder());
                        pendingOrderVo.setOrderStatus(collectionOrder.getOrderStatus());
                        return RestResult.failure(ResultCode.UNFINISHED_ORDER_EXISTS, pendingOrderVo);
                    }

                    //订单校验
                    RestResult restResult = orderValidation(buyReq, buyMemberInfo, tradeConfig);
                    if (restResult != null) {
                        log.error("买入处理失败, 订单校验失败, 会员账号: {}, 错误信息: {}", buyMemberInfo.getMemberAccount(), restResult);
                        return restResult;
                    }

                    KycPartners kycPartners = null;

                    if (buyReq.getKycId() != null && buyReq.getKycId() > 0) {
                        kycPartners = kycPartnersService.getById(buyReq.getKycId());

                        //如果不是内部会员 那么获取kyc信息 校验kyc是否在连接中
                        if (!MemberTypeEnum.INTERNAL_MERCHANT_MEMBER.getCode().equals(buyMemberInfo.getMemberType())) {
                            if (kycPartners == null || kycPartners.getStatus() != 1 || !kycPartners.getMemberId().equals(String.valueOf(buyMemberInfo.getId()))) {
                                log.error("买入处理失败, kyc信息不存在或kyc未连接或该kyc信息不属于该会员, 会员账号: {}, kycPartners: {}", buyMemberInfo.getMemberAccount(), kycPartners);
                                return RestResult.failure(ResultCode.KYC_NOT_CONNECTED);
                            }
                        }

                        //校验kyc是否属于该会员
                        if (kycPartners != null && !kycPartners.getMemberId().equals(String.valueOf(buyMemberInfo.getId()))) {
                            log.error("买入处理失败, 该kyc信息不属于该会员, 会员账号: {}, kycPartners: {}", buyMemberInfo.getMemberAccount(), kycPartners);
                            return RestResult.failure(ResultCode.KYC_NOT_CONNECTED);
                        }
                    }

                    //将订单信息从redis里面删除
                    redisUtil.deleteOrder(buyReq.getPlatformOrder());
                    //推送最新的 金额列表给前端
                    memberSendAmountList.send();

                    //买入订单号
                    String buyplatformOrder = orderNumberGenerator.generateOrderNo("MR");

                    //获取匹配到的代付订单 加上排他行锁
                    MerchantPaymentOrders merchantPaymentOrder = merchantPaymentOrdersMapper.selectMerchantPaymentOrdersByPlatformOrderForUpdate(buyReq.getPlatformOrder());

                    //判断代付订单状态是否为 待匹配
                    if (merchantPaymentOrder == null || !PaymentOrderStatusEnum.BE_MATCHED.getCode().equals(merchantPaymentOrder.getOrderStatus())) {

                        log.warn("买入处理失败, 选中到的订单已被其他人使用, 会员账号: {}, 订单信息: {}", buyMemberInfo.getMemberAccount(), merchantPaymentOrder);
                        //订单不存在或订单不是匹配中状态
                        //订单已经不是待匹配状态了, 所以删除了Redis 也不做补偿性操作了
                        return RestResult.failure(ResultCode.ORDER_ALREADY_USED_BY_OTHERS);
                    }

                    //校验买入金额 是否和 卖出金额相等
                    if (buyReq.getAmount().compareTo(merchantPaymentOrder.getAmount()) != 0) {
                        //买入失败了 将卖出订单信息添加回redis订单列表
                        addOrderIdToList(orderDetails);
                        return RestResult.failure(ResultCode.ORDER_AMOUNT_ERROR);
                    }


                    //提现订单匹配
                    JSONObject orderEventMatchingJson = new JSONObject();
                    orderEventMatchingJson.put("amount", buyReq.getAmount());
                    OrderEventReq orderEventReq = new OrderEventReq();
                    orderEventReq.setEventId(OrderEventEnum.MERCHANT_PAYMENT_ORDER_MATCHING.getCode());
                    orderEventReq.setParams(JSONObject.toJSONString(orderEventMatchingJson));
                    //发送事件MQ
                    rabbitMQService.sendStatisticProcess(orderEventReq);

                    //更新代付订单数据----------------------------------
                    LambdaUpdateWrapper<MerchantPaymentOrders> updateWrapperMerchantPaymentOrder = new LambdaUpdateWrapper<>();
                    updateWrapperMerchantPaymentOrder.eq(MerchantPaymentOrders::getPlatformOrder, merchantPaymentOrder.getPlatformOrder())  // 指定更新条件，订单号
                            .set(MerchantPaymentOrders::getOrderStatus, PaymentOrderStatusEnum.HANDLING.getCode()) // 指定更新字段 (订单状态)
                            .set(MerchantPaymentOrders::getBuyOrderNo, buyplatformOrder) // 指定更新字段 (买入订单号)
                            .set(MerchantPaymentOrders::getMemberId, buyMemberInfo.getId()); // 指定更新字段 (会员id)
                    // 这里传入的 null 表示不更新实体对象的其他字段
                    merchantPaymentOrdersService.update(null, updateWrapperMerchantPaymentOrder);

                    //生成买入订单----------------------------------
                    createBuyOrder(buyReq, buyMemberInfo, buyplatformOrder, merchantPaymentOrder, realIP, kycPartners);

                    //更新会员买入次数
                    LambdaUpdateWrapper<MemberInfo> updateWrapperMemberInfo = new LambdaUpdateWrapper<>();
                    updateWrapperMemberInfo.eq(MemberInfo::getId, buyMemberInfo.getId())  // 指定更新条件，会员id
                            .set(MemberInfo::getTotalBuyCount, buyMemberInfo.getTotalBuyCount() + 1) // 指定更新字段 (累计买入次数)
                            .set(MemberInfo::getTotalBuyAmount, buyMemberInfo.getTotalBuyAmount().add(merchantPaymentOrder.getAmount())); // 指定更新字段 (累计买入金额)
                    // 这里传入的 null 表示不更新实体对象的其他字段
                    memberInfoService.update(null, updateWrapperMemberInfo);

                    log.info("买入处理成功, 会员账号: {}, 买入订单号: {}, 代付订单号: {},  支付过期时间(分钟): {}", buyMemberInfo.getMemberAccount(), buyplatformOrder, merchantPaymentOrder, tradeConfig.getRechargeExpirationTime());

                    KycPartners finalKycPartners = kycPartners;
                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            //从配置表获取 支付超时时间(分钟)
                            long millis = TimeUnit.MINUTES.toMillis(tradeConfig.getRechargeExpirationTime());
                            Long lastUpdateTimestamp = System.currentTimeMillis();
                            //发送支付超时的MQ
                            TaskInfo taskInfo = new TaskInfo(buyplatformOrder, TaskTypeEnum.PAYMENT_TIMEOUT.getCode(), lastUpdateTimestamp);
                            rabbitMQService.sendTimeoutTask(taskInfo, millis);

                            //将支付倒计时记录到redis 买入订单
                            redisUtil.setPaymentExpireTime(buyplatformOrder, tradeConfig.getRechargeExpirationTime());

                            //如果是没有连接kyc就不用拉取
                            if (finalKycPartners != null) {
                                log.info("调用kyc开始监听买入账户是否付款, 买入订单号: {}", buyplatformOrder);

                                //调用kyc开始监听买入账户是否付款 (提现)
                                KycAutoCompleteReq kycAutoCompleteReq = new KycAutoCompleteReq();
                                //买入订单号
                                kycAutoCompleteReq.setBuyerOrder(buyplatformOrder);
                                //买入会员id
                                kycAutoCompleteReq.setBuyerMemberId(String.valueOf(buyMemberInfo.getId()));
                                //卖出订单号
                                kycAutoCompleteReq.setSellerOrder(merchantPaymentOrder.getPlatformOrder());
                                //卖出会员id
                                kycAutoCompleteReq.setSellerMemberId(merchantPaymentOrder.getExternalMemberId());
                                //订单金额
                                kycAutoCompleteReq.setOrderAmount(merchantPaymentOrder.getAmount());
                                //充值 1 提现 2
                                kycAutoCompleteReq.setType("2");
                                //提现用户upi(如果是提现必填) (传银行卡号后四位)
                                kycAutoCompleteReq.setWithdrawUpi(getLastFourDigits(merchantPaymentOrder.getBankCardNumber()));
                                //币种
                                kycAutoCompleteReq.setCurrency(merchantPaymentOrder.getCurrency());
                                //传入kycId
                                kycAutoCompleteReq.setKycId(String.valueOf(finalKycPartners.getId()));
                                kycCenterService.startPullTransaction(kycAutoCompleteReq);
                            }
                        }
                    });
                    return RestResult.ok();
                }
                log.error("买入下单失败, 未获取到买入会员ID锁, 会员id: {}, buyReq: {}", memberId, buyReq);
                //买入失败了 将卖出订单信息添加回redis订单列表
//                addOrderIdToList(orderDetails);
//                return RestResult.failure(ResultCode.SYSTEM_EXECUTION_ERROR);
            } else {
                //没获取到锁 直接失败
                log.error("买入下单失败, 未获取到买入订单锁, 会员id: {}, buyReq: {}", memberId, buyReq);
                return RestResult.failure(ResultCode.ORDER_ALREADY_USED_BY_OTHERS);
            }
        } catch (Exception e) {
            //手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("买入处理失败, req: {} e: ", buyReq, e);
        } finally {
            //释放锁
            if (req1 && lock1.isHeldByCurrentThread()) {
                lock1.unlock();
            }
            if (req && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return RestResult.failure(ResultCode.SYSTEM_EXECUTION_ERROR);
    }


    /**
     * 截取银行卡号后四位
     *
     * @param bankCardNumber
     * @return {@link String }
     */
    public String getLastFourDigits(String bankCardNumber) {
        if (bankCardNumber != null && bankCardNumber.length() >= 4) {
            return bankCardNumber.substring(bankCardNumber.length() - 4);
        } else {
            return bankCardNumber;
        }
    }

    /**
     * 买入失败 将卖出订单信息添加回redis订单列表
     *
     * @param orderDetails
     */
    public void addOrderIdToList(BuyListVo orderDetails) {
        if (orderDetails != null) {
            log.error("买入处理失败 将卖出订单信息添加回redis订单列表, 订单信息: {}", orderDetails);
            redisUtil.addOrderIdToList(orderDetails, "2");

            //推送最新的 金额列表给前端
            memberSendAmountList.send();
        }
    }

    /**
     * 买入订单校验
     *
     * @param buyReq
     * @param buyMemberInfo
     * @param tradeConfig
     * @return {@link RestResult}
     */
    @Override
    public RestResult orderValidation(BuyReq buyReq, MemberInfo buyMemberInfo, TradeConfig tradeConfig) {

        //查看是否开启实名认证交易限制
        if (controlSwitchService.isSwitchEnabled(SwitchIdEnum.REAL_NAME_VERIFICATION.getSwitchId())) {
            if (MemberAuthenticationStatusEnum.UNAUTHENTICATED.getCode().equals(buyMemberInfo.getAuthenticationStatus())) {
                log.error("买入订单校验失败 当前会员未实名认证 会员信息: {}, req: {}", buyMemberInfo, buyReq);
                return RestResult.failure(ResultCode.MEMBER_NOT_VERIFIED);
            }
        }

        //判断当前会员状态和买入状态是否可用
        if (MemberStatusEnum.DISABLE.getCode().equals(buyMemberInfo.getStatus())) {
            log.error("买入订单校验失败 当前会员状态不可用 会员信息: {}, req: {}", buyMemberInfo, buyReq);
            return RestResult.failure(ResultCode.MEMBER_STATUS_NOT_AVAILABLE);
        }

        if (BuyStatusEnum.DISABLE.getCode().equals(buyMemberInfo.getBuyStatus())) {
            log.error("买入订单校验失败 当前会员状态和买入状态不可用 会员信息: {}, req: {}", buyMemberInfo, buyReq);
            return RestResult.failure(ResultCode.MEMBER_BUY_STATUS_NOT_AVAILABLE);
        }

        //根据会员标签获取对应配置信息
        TradeConfigScheme schemeConfigByMemberTag = tradeConfigHelperUtil.getSchemeConfigByMemberTag(buyMemberInfo);

        //判断买入金额 是否在 最小买入金额 和最大买入金额之间
        OrderAmountValidationResult orderAmountValid = TradeValidationUtil.isOrderAmountValid(buyReq.getAmount(), schemeConfigByMemberTag.getSchemeMinPurchaseAmount(), schemeConfigByMemberTag.getSchemeMaxPurchaseAmount());

        if (orderAmountValid == OrderAmountValidationResult.TOO_LOW) {
            //订单金额太低
            log.error("买入订单校验失败 买入金额低于最小买入金额 会员信息: {}, req: {}, 最小买入金额: {}", buyMemberInfo, buyReq, schemeConfigByMemberTag.getSchemeMinPurchaseAmount());
            return RestResult.failure(ResultCode.ORDER_AMOUNT_TOO_LOW);
        }

        if (orderAmountValid == OrderAmountValidationResult.TOO_HIGH) {
            //订单金额超过最大限制
            log.error("买入订单校验失败 买入金额超过最大买入金额 会员信息: {}, req: {}, 最大买入金额: {}", buyMemberInfo, buyReq, schemeConfigByMemberTag.getSchemeMaxPurchaseAmount());
            return RestResult.failure(ResultCode.ORDER_AMOUNT_EXCEEDS_LIMIT);
        }

        return null;
    }

    /**
     * 生成买入订单
     *
     * @param buyReq
     * @param buyMemberInfo
     * @param buyplatformOrder
     * @param merchantPaymentOrder
     * @param realIP
     * @param kycPartners
     * @return {@link Boolean}
     */
    @Override
    public Boolean createBuyOrder(BuyReq buyReq, MemberInfo buyMemberInfo, String buyplatformOrder, MerchantPaymentOrders merchantPaymentOrder, String realIP, KycPartners kycPartners) {

        //生成买入订单----------------------------------

        CollectionOrder collectionOrder = new CollectionOrder();

        //商户号
        collectionOrder.setMerchantCode(merchantPaymentOrder.getMerchantCode());

        //商户名称
        collectionOrder.setMerchantName(merchantPaymentOrder.getMerchantName());

        // 商户代付订单号
        collectionOrder.setMerchantPaymentOrder(merchantPaymentOrder.getMerchantOrder());

        //设置支付方式 默认银行卡
        collectionOrder.setPayType(PayTypeEnum.INDIAN_CARD.getCode());

        //设置平台订单号
        collectionOrder.setPlatformOrder(buyplatformOrder);

        //设置代付订单号
        collectionOrder.setMerchantOrder(merchantPaymentOrder.getPlatformOrder());

        //设置会员id
        collectionOrder.setMemberId(String.valueOf(buyMemberInfo.getId()));

        //设置会员账号
        collectionOrder.setMemberAccount(buyMemberInfo.getMemberAccount());

        //设置订单金额
        collectionOrder.setAmount(buyReq.getAmount());

        //设置订单实际金额 默认就是订单金额
        collectionOrder.setActualAmount(collectionOrder.getAmount());

        //银行卡号
        collectionOrder.setBankCardNumber(merchantPaymentOrder.getBankCardNumber());

        //持卡人姓名
        collectionOrder.setBankCardOwner(merchantPaymentOrder.getBankCardOwner());

        //银行名称
        collectionOrder.setBankName(merchantPaymentOrder.getBankName());

        //ifscCode
        collectionOrder.setIfscCode(merchantPaymentOrder.getIfscCode());

        //设置会员手机号
        collectionOrder.setMobileNumber(buyMemberInfo.getMobileNumber());

        //设置交易ip
        collectionOrder.setClientIp(realIP);


        if (kycPartners != null) {
            //付款人 kycBankName
            collectionOrder.setKycBankName(kycPartners.getBankName());

            //付款人 kyc accent
            collectionOrder.setKycAccount(kycPartners.getAccount());

            //kycId
            collectionOrder.setKycId(String.valueOf(buyReq.getKycId()));
        }

        //币种 从商户信息获取 如果是USDT那么要写USDT
        collectionOrder.setCurrency(merchantPaymentOrder.getCurrency());

        //汇率 TODO 从汇率接口获取
        collectionOrder.setExchangeRates(new BigDecimal(1));

        //iToken 订单金额 * 费率
        collectionOrder.setItokenNumber(collectionOrder.getAmount().multiply(collectionOrder.getExchangeRates()));

        //会员类型 1: 内部会员, 2: 外部会员
        collectionOrder.setMemberType(Integer.valueOf(buyMemberInfo.getMemberType()));
        //会员id
        collectionOrder.setMemberId(String.valueOf(buyMemberInfo.getId()));

        //查看是否配置了买入奖励
        if (buyMemberInfo.getBuyBonusProportion() != null && buyMemberInfo.getBuyBonusProportion().compareTo(new BigDecimal(0)) > 0) {
            //奖励
            collectionOrder.setBonus(collectionOrder.getAmount().multiply((new BigDecimal(buyMemberInfo.getBuyBonusProportion().toString()).divide(BigDecimal.valueOf(100)))));
        }

        boolean save = collectionOrderService.save(collectionOrder);

        log.info("买入处理: 生成买入订单, 买入会员信息: {}, req: {}, 买入订单信息: {}, sql执行结果: {}", buyMemberInfo, buyReq, collectionOrder, save);

        return save;
    }


    /**
     * USDT买入处理
     *
     * @param usdtBuyReq
     * @return {@link RestResult}
     */
    @Override
    @Transactional
    public RestResult usdtBuyProcessor(UsdtBuyReq usdtBuyReq) {

        //获取当前会员id
        Long memberId = UserContext.getCurrentUserId();

        if (memberId == null) {
            log.error("USDT买入处理失败 会员id为null");
            return RestResult.failure(ResultCode.RELOGIN);
        }

        //分布式锁key ar-wallet-buy+会员id
        String key = "ar-wallet-usdt-buy" + memberId;
        RLock lock = redissonUtil.getLock(key);

        boolean req = false;

        try {
            req = lock.tryLock(10, TimeUnit.SECONDS);

            if (req) {

                //获取当前买入会员信息 加上排他行锁
                MemberInfo usdtBuyMemberInfo = memberInfoService.getById(memberId);

                if (usdtBuyMemberInfo == null) {
                    log.error("USDT买入处理失败 会员信息为null 会员id: {}", memberId);
                    return RestResult.failure(ResultCode.RELOGIN);
                }

                //校验会员有没有实名认证
//                if (StringUtils.isEmpty(usdtBuyMemberInfo.getRealName()) || StringUtils.isEmpty(usdtBuyMemberInfo.getIdCardNumber())) {
//                    log.error("USDT买入下单处理失败: 该会员没有实名认证 req: {}, 会员信息: {}", usdtBuyReq, usdtBuyMemberInfo);
//                    return RestResult.failure(ResultCode.MEMBER_NOT_VERIFIED);
//                }

                //校验当前会员买入状态是否为开启
                if (!usdtBuyMemberInfo.getBuyStatus().equals(BuyStatusEnum.ENABLE.getCode())) {
                    log.error("USDT买入失败, 会员买入状态未开启 会员账号: {}, usdtBuyReq: {}", usdtBuyMemberInfo.getMemberAccount(), usdtBuyReq);
                    return RestResult.failure(ResultCode.MEMBER_BUY_STATUS_NOT_ENABLED);
                }

                //查看当前会员是否有未完成的USDT订单
                UsdtBuyOrder usdtBuyOrder = usdtBuyOrderService.countActiveUsdtBuyOrders(String.valueOf(usdtBuyMemberInfo.getId()));

                if (usdtBuyOrder != null) {
                    log.error("USDT买入失败 当前有未完成的订单 会员账号: {}, usdtBuyReq: {}, 当前未完成的USDT订单: {}", usdtBuyMemberInfo.getMemberAccount(), usdtBuyReq, usdtBuyOrder);

                    PendingOrderVo pendingOrderVo = new PendingOrderVo();
                    pendingOrderVo.setPlatformOrder(usdtBuyOrder.getPlatformOrder());
                    pendingOrderVo.setOrderStatus(usdtBuyOrder.getStatus());

                    return RestResult.failure(ResultCode.UNFINISHED_ORDER_EXISTS, pendingOrderVo);
                }

                //检查当前会员是否处于买入冷却期
                if (!redisUtil.canMemberBuy(String.valueOf(usdtBuyMemberInfo.getId()))) {
                    //会员处于冷却期 不能购买

                    //获取配置信息
                    TradeConfig tradeConfig = tradeConfigService.getById(1);

                    //获取会员冷却期剩余时间
                    long memberBuyBlockedExpireTime = redisUtil.getMemberBuyBlockedExpireTime(String.valueOf(usdtBuyMemberInfo.getId()));

                    log.error("USDT买入下单失败, 当前会员处于买入冷却期, 会员账号: {}, buyReq: {}, 冷却期剩余时间 (秒): {}", usdtBuyMemberInfo.getMemberAccount(), usdtBuyReq, memberBuyBlockedExpireTime);

                    DisableBuyingVo disableBuyingVo = new DisableBuyingVo();

                    //获取会员被禁用的时间
                    Integer memberBuyBlockRemainingTime = redisUtil.getMemberBuyBlockRemainingTime(String.valueOf(memberId));

                    if (memberBuyBlockRemainingTime == null) {
                        memberBuyBlockRemainingTime = tradeConfig.getDisabledTime();
                    }

                    //禁止买入小时数
                    disableBuyingVo.setBuyDisableHours(memberBuyBlockRemainingTime);
                    //剩余时间(秒)
                    disableBuyingVo.setRemainingSeconds(redisUtil.getMemberBuyBlockedExpireTime(String.valueOf(memberId)));

                    return RestResult.failure(ResultCode.BUY_COOLDOWN_PERIOD, disableBuyingVo);
                }

                //获取配置信息
                TradeConfig tradeConfig = tradeConfigMapper.selectById(1);

                //获取实时汇率
//                BigDecimal currencyExchangeRate = systemCurrencyService.getCurrencyExchangeRate(CurrenceEnum.INDIA.getCode());
                //获取手动配置的汇率
                BigDecimal currencyExchangeRate = tradeConfig.getUsdtCurrency();

                //校验
                RestResult restResult = usdtOrderValidation(usdtBuyReq, usdtBuyMemberInfo, tradeConfig);
                if (restResult != null) {
                    log.error("USDT买入处理失败 会员账号: {}, 汇率错误: {}", usdtBuyMemberInfo.getMemberAccount(), restResult);
                    return restResult;
                }

                //查询用户波场钱包信息 如果不存在波场钱包的话就创建一个(每个用户对应一个波场钱包地址)
                TronAddress tronAddress = tronAddressService.getTronAddressByMerchanIdtAndUserId("uuPay", String.valueOf(memberId));


                if (tronAddress == null) {

                    //为该用户创建一个波场钱包

                    tronAddress = new TronAddress();

                    //钱包内部会员USDT充值 商户号固定 uuPay
                    tronAddress.setMerchantId("uuPay");

                    //钱包内部会员USDT充值 商户名称固定 uuPay
                    tronAddress.setMerchantName("uuPay");

                    //会员id
                    tronAddress.setMemberId(String.valueOf(memberId));

                    KeyPair keyPair = KeyPair.generate();
                    tronAddress.setAddress(keyPair.toBase58CheckAddress());
                    tronAddress.setHexAddress(keyPair.toHexAddress());
                    try {
                        //使用公钥加密私钥后进行存储
                        tronAddress.setPrivateKey(RSAUtils.enCodeKey(keyPair.toPrivateKey(), arProperty.getTronPublicKey()));
                    } catch (Exception ex) {
                        throw new RuntimeException(ex.getMessage());
                    }
                    tronAddressService.save(tronAddress);
                }

                //用户地址表 订单总数+1
                LambdaUpdateWrapper<TronAddress> lambdaUpdateWrapperTronAddress = new LambdaUpdateWrapper<>();
                // 指定更新条件，地址
                lambdaUpdateWrapperTronAddress.eq(TronAddress::getAddress, tronAddress.getAddress());
                if (tronAddress.getOrderTotal() == null) {
                    tronAddress.setOrderTotal(0L);
                }
                // 订单总数+1
                lambdaUpdateWrapperTronAddress.set(TronAddress::getOrderTotal, tronAddress.getOrderTotal() + 1);
                // 这里传入的 null 表示不更新实体对象的其他字段
                tronAddressService.update(null, lambdaUpdateWrapperTronAddress);

                //根据前端传过来的USDT金额 计算出对应的ARB金额 USDT金额 * 实时汇率 保留两位小数 舍弃后面的小数
                BigDecimal calculatedArbAmount = usdtBuyReq.getUsdtAmount().multiply(currencyExchangeRate).setScale(2, RoundingMode.DOWN);

                //生成USDT买入订单
                String platformOrder = orderNumberGenerator.generateOrderNo("USDT");
                createUsdtOrder(usdtBuyReq, usdtBuyMemberInfo, tronAddress, platformOrder, calculatedArbAmount, tradeConfig, currencyExchangeRate);

                log.info("USDT买入 会员账号: {}, 订单号: {}, USDT买入金额: {}, 汇率: {}", usdtBuyMemberInfo.getMemberAccount(), platformOrder, usdtBuyReq.getUsdtAmount(), currencyExchangeRate);


                //更新会员买入统计信息: 累计买入次数
                memberInfoService.updateAddBuyInfo(String.valueOf(usdtBuyMemberInfo.getId()));

                //将USDT支付倒计时记录到redis USDT买入订单
                redisUtil.setUsdtPaymentExpireTime(platformOrder, tradeConfig.getRechargeExpirationTime());

                log.info("USDT买入处理 成功 会员账号: {}, req: {}, 支付超时时间(分钟): {}", usdtBuyMemberInfo.getMemberAccount(), usdtBuyReq, tradeConfig.getRechargeExpirationTime());

                //事务提交成功后 将该地址存入到redis
                TronAddress finalTronAddress = tronAddress;
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        //事务提交成功后 将USDT收款地址存入redis

                        //从配置表获取 支付超时时间(分钟)
                        long millis = TimeUnit.MINUTES.toMillis(tradeConfig.getRechargeExpirationTime());
                        //发送USDT支付超时的MQ
                        Long lastUpdateTimestamp = System.currentTimeMillis();
                        TaskInfo taskInfo = new TaskInfo(platformOrder, TaskTypeEnum.USDT_PAYMENT_TIMEOUT.getCode(), lastUpdateTimestamp);
                        rabbitMQService.sendTimeoutTask(taskInfo, millis);

                        // 将USDT收款地址存入Redis，不设置过期时间
                        redisTemplate.opsForValue().set(RedisKeys.PENDING_USDT_ADDRESS + finalTronAddress.getAddress(), finalTronAddress.getAddress());
                    }
                });

                return RestResult.ok();
            }
        } catch (Exception e) {
            log.error("USDT买入处理 失败 会员id: {}, req: {}, e: {}", memberId, usdtBuyReq, e);
            //手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        } finally {
            //释放锁
            if (req && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return RestResult.failure(ResultCode.SYSTEM_EXECUTION_ERROR);
    }

    /**
     * USDT买入订单校验
     *
     * @param usdtBuyReq
     * @param usdtBuyMemberInfo
     * @param tradeConfig
     * @return {@link RestResult}
     */
    @Override
    public RestResult usdtOrderValidation(UsdtBuyReq usdtBuyReq, MemberInfo usdtBuyMemberInfo, TradeConfig tradeConfig) {

        //校验USDT数量是否在最大和最小限制内 最低1U
        if (!(usdtBuyReq.getUsdtAmount().compareTo(new BigDecimal(1)) >= 0 && usdtBuyReq.getUsdtAmount().compareTo(tradeConfig.getMaxAntUsdtDepositAmount()) <= 0)) {
            log.error("USDT买入失败 USDT数量超过限制 req: {}, 会员信息: {}, 配置信息: {}", usdtBuyReq, usdtBuyMemberInfo, tradeConfig);
            return RestResult.failure(ResultCode.USDT_AMOUNT_TOO_LOW);
        }

        //校验USDT数量是否低于1U
//        if (usdtBuyReq.getUsdtAmount().compareTo(new BigDecimal(1)) < 0) {
//            log.error("USDT买入失败 USDT数量是否低于1U req: {}, 会员信息: {}, 配置信息: {}", usdtBuyReq, usdtBuyMemberInfo, tradeConfig);
//            return RestResult.failure(ResultCode.USDT_AMOUNT_TOO_LOW);
//        }

        //判断当前会员状态和买入状态是否可用
        if (MemberStatusEnum.DISABLE.getCode().equals(usdtBuyMemberInfo.getStatus()) || BuyStatusEnum.DISABLE.getCode().equals(usdtBuyMemberInfo.getBuyStatus())) {
            log.error("USDT买入失败 当前会员状态和买入状态不可用 req: {}, 会员信息: {}, 配置信息: {}", usdtBuyReq, usdtBuyMemberInfo, tradeConfig);
            return RestResult.failure(ResultCode.MEMBER_STATUS_NOT_AVAILABLE);
        }

        return null;
    }

    /**
     * 生成USDT买入订单
     *
     * @param usdtBuyReq
     * @param usdtBuyMemberInfo
     * @param tronAddress
     * @param platformOrder
     * @param calculatedArbAmount
     * @param tradeConfig
     * @return {@link Boolean}
     */
    @Override
    public Boolean createUsdtOrder(UsdtBuyReq usdtBuyReq, MemberInfo usdtBuyMemberInfo, TronAddress tronAddress, String platformOrder, BigDecimal calculatedArbAmount, TradeConfig tradeConfig, BigDecimal currencyExchangeRate) {

        //生成USDT买入订单
        UsdtBuyOrder usdtBuyOrder = new UsdtBuyOrder();

        //设置会员ID
        usdtBuyOrder.setMemberId(String.valueOf(usdtBuyMemberInfo.getId()));

        //设置会员账号
        usdtBuyOrder.setMemberAccount(usdtBuyMemberInfo.getMemberAccount());

        //设置订单号
        usdtBuyOrder.setPlatformOrder(platformOrder);

        //设置收款地址
        usdtBuyOrder.setUsdtAddr(tronAddress.getAddress());

        //设置USDT数量
        usdtBuyOrder.setUsdtNum(usdtBuyReq.getUsdtAmount());

        //设置USDT实际数量
        usdtBuyOrder.setUsdtActualNum(usdtBuyReq.getUsdtAmount());

        //设置ARB数量
        usdtBuyOrder.setArbNum(calculatedArbAmount);

        //设置实际ARB数量
        usdtBuyOrder.setArbActualNum(calculatedArbAmount);

        //支付方式
        usdtBuyOrder.setPayType(UsdtPayTypeEnum.TRC20.getCode());

        //汇率 实时汇率
        usdtBuyOrder.setExchangeRates(currencyExchangeRate);

        boolean save = usdtBuyOrderService.save(usdtBuyOrder);

        log.info("USDT买入处理 生成USDT买入订单 会员信息: {}, req: {}, USDT买入订单: {}, sql执行结果: {}", usdtBuyMemberInfo, usdtBuyReq, usdtBuyOrder, save);

        return save;
    }

    /**
     * 取消买入订单处理
     *
     * @param cancelOrderReq
     * @return {@link RestResult}
     */
    @Override
    @Transactional
    public RestResult cancelPurchaseOrder(CancelOrderReq cancelOrderReq) {

        //分布式锁key ar-wallet-cancelPurchaseOrder+订单号
        String key = "ar-wallet-cancelPurchaseOrder" + cancelOrderReq.getPlatformOrder();
        RLock lock = redissonUtil.getLock(key);

        boolean req = false;

        try {
            req = lock.tryLock(10, TimeUnit.SECONDS);

            if (req) {

                //获取当前会员信息
                MemberInfo memberInfo = memberInfoService.getMemberInfo();

                //查询买入订单 加上排他行锁
                CollectionOrder collectionOrder = collectionOrderMapper.selectCollectionOrderForUpdate(cancelOrderReq.getPlatformOrder());

                String memberId = String.valueOf(memberInfo.getId());

                //校验该笔订单是否属于当前会员
                if (collectionOrder == null || !collectionOrder.getMemberId().equals(memberId)) {
                    log.error("取消买入订单处理失败 该笔订单不存在或该笔订单不属于该会员, 会员账号: {}, 订单信息: {}", memberInfo.getMemberAccount(), collectionOrder);
                    return RestResult.failure(ResultCode.ORDER_VERIFICATION_FAILED);
                }

                //校验订单如果是已取消或支付超时状态 那么直接返回成功(防止重复点击)
                if (OrderStatusEnum.WAS_CANCELED.getCode().equals(collectionOrder.getOrderStatus())) {
                    log.error("取消买入订单失败, 当前订单状态为已取消或支付超时: {}, cancelOrderReq: {}, 会员账号: {}", collectionOrder.getOrderStatus(), cancelOrderReq, memberInfo.getMemberAccount());
                    return RestResult.ok();
                }

                //校验当前订单状态
                //3  支付中
                //只有支付中状态才能进行取消买入订单
                if (!OrderStatusEnum.BE_PAID.getCode().equals(collectionOrder.getOrderStatus())) {
                    log.error("取消买入订单失败, 订单状态必须为: 支付中 才能够进行取消 当前订单状态为: {}, cancelOrderReq: {}, 会员账号: {}", collectionOrder.getOrderStatus(), cancelOrderReq, memberInfo.getMemberAccount());
                    return RestResult.failure(ResultCode.ORDER_STATUS_VERIFICATION_FAILED);
                }

                //查询代付订单 加上排他行锁
                MerchantPaymentOrders merchantPaymentOrder = merchantPaymentOrdersMapper.selectMerchantPaymentOrdersByPlatformOrderForUpdate(collectionOrder.getMerchantOrder());

                if (merchantPaymentOrder == null) {
                    //获取代付订单失败
                    throw new RuntimeException("Failed to retrieve payment order");
                }

                //更新买入订单
                LambdaUpdateWrapper<CollectionOrder> lambdaUpdateWrappercollectionOrder = new LambdaUpdateWrapper<>();
                lambdaUpdateWrappercollectionOrder.eq(CollectionOrder::getPlatformOrder, collectionOrder.getPlatformOrder())  // 指定更新条件 订单号
                        .set(CollectionOrder::getOrderStatus, OrderStatusEnum.WAS_CANCELED.getCode()) // 指定更新字段 (订单状态)
                        .set(CollectionOrder::getCancelBy, collectionOrder.getMemberAccount()) // 指定更新字段 (取消人)
                        .set(CollectionOrder::getCancelTime, LocalDateTime.now()); // 指定更新字段 (取消时间)
                // 这里传入的 null 表示不更新实体对象的其他字段
                collectionOrderService.update(null, lambdaUpdateWrappercollectionOrder);

                //将代付订单改为代付失败
                LambdaUpdateWrapper<MerchantPaymentOrders> lambdaUpdateWrapperMerchantPaymentOrders = new LambdaUpdateWrapper<>();
                lambdaUpdateWrapperMerchantPaymentOrders.eq(MerchantPaymentOrders::getPlatformOrder, merchantPaymentOrder.getPlatformOrder())  // 指定更新条件 订单号
                        .set(MerchantPaymentOrders::getOrderStatus, PaymentOrderStatusEnum.FAILED.getCode()); // 指定更新字段 (订单状态)
                // 这里传入的 null 表示不更新实体对象的其他字段
                merchantPaymentOrdersService.update(null, lambdaUpdateWrapperMerchantPaymentOrders);

                //将商户交易中金额退回到商户余额
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

                        //关闭kyc监控 参数1: 卖出订单号, 参数2: 买入订单号
                        kycCenterService.stopPullTransaction(merchantPaymentOrder.getPlatformOrder(), collectionOrder.getPlatformOrder());
                    }
                });
                return RestResult.ok();
            }
        } catch (Exception e) {
            log.error("取消买入订单处理失败 :{}, e: {}", cancelOrderReq, e);
            //手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        } finally {
            //释放锁
            if (req && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return RestResult.failure(ResultCode.SYSTEM_EXECUTION_ERROR);
    }

    /**
     * 完成支付 处理
     *
     * @param platformOrder
     * @param voucherImage
     * @return {@link RestResult}
     */
    @Override
    @Transactional
    public RestResult buyCompletedProcessor(String platformOrder, String voucherImage) {

        //分布式锁key ar-wallet-buyCompleted+订单号
        String key = "ar-wallet-buyCompleted" + platformOrder;
        RLock lock = redissonUtil.getLock(key);

        boolean req = false;

        try {

            req = lock.tryLock(10, TimeUnit.SECONDS);

            if (req) {

                if (!FileUtil.isValidImageExtension(voucherImage)) {
                    // 如果有文件不符合规茨，则返回错误
                    log.error("完成支付处理失败: 会员上传图片文件不符合规范 直接驳回, 订单号: {}, 文件名: {}", platformOrder, voucherImage);
                    return RestResult.failure(ResultCode.FILE_UPLOAD_REQUIRED);
                }

                MemberInfo memberInfo = memberInfoService.getMemberInfo();

                if (memberInfo == null) {
                    log.error("完成支付处理失败: 获取会员信息失败");
                    return RestResult.failure(ResultCode.RELOGIN);
                }

                //获取买入订单 加上排他行锁
                CollectionOrder collectionOrder = collectionOrderMapper.selectCollectionOrderForUpdate(platformOrder);

                //判断当前订单如果是已取消状态 那么直接返回订单已取消
                if (OrderStatusEnum.WAS_CANCELED.getCode().equals(collectionOrder.getOrderStatus())) {
                    log.error("完成支付处理失败: 当前订单为支付超时 会员账号: {}, 订单号: {}", memberInfo.getMemberAccount(), platformOrder);
                    return RestResult.failure(ResultCode.ORDER_EXPIRED);
                }

                String memberId = String.valueOf(memberInfo.getId());

                //校验该笔订单是否属于该会员
                if (collectionOrder == null || !collectionOrder.getMemberId().equals(memberId)) {
                    log.error("完成支付处理失败: 非法操作 该笔订单不存在或该笔订单不属于该会员 会员信息: {}, 订单号: {}", memberInfo, platformOrder);
                    return RestResult.failure(ResultCode.ORDER_VERIFICATION_FAILED);
                }

                //判断当前订单如果不是待支付状态 则不做处理
//                if (!OrderStatusEnum.BE_PAID.getCode().equals(collectionOrder.getOrderStatus())) {
//                    log.error("完成支付处理失败: 当前订单不是待支付状态 会员账号: {}, 订单号: {}, 当前订单状态: {}, 订单信息: {}", memberInfo.getMemberAccount(), platformOrder, collectionOrder.getOrderStatus(), collectionOrder);
//                    return RestResult.failure(ResultCode.ORDER_EXPIRED);
//                }

                //查看是否开启 支付凭证识别
                if (controlSwitchService.isSwitchEnabled(SwitchIdEnum.PAYMENT_VOUCHER_RECOGNITION.getSwitchId())) {
                    TestImageRecognitionVo recognitionResult = imageRecognitionService.isPaymentVoucher(baseUrl + voucherImage);
                    log.info("完成支付处理, 订单号: {}, 支付凭证识别结果: {}", platformOrder, recognitionResult);
                    if (recognitionResult == null || !"PASS".equals(recognitionResult.getRiskLevel())) {
                        //支付凭证识别失败后, 将图片路径和订单号存储到redis
                        storeImagePath(platformOrder, baseUrl + voucherImage);
                        return RestResult.failure(ResultCode.NOT_VOUCHER_IMAGE);
                    }
                } else {
                    log.info("完成支付处理 支付凭证识别未开启");
                }

                voucherImage = baseUrl + voucherImage;

                //更新买入订单
                LambdaUpdateWrapper<CollectionOrder> lambdaUpdateWrappercollectionOrder = new LambdaUpdateWrapper<>();
                lambdaUpdateWrappercollectionOrder.eq(CollectionOrder::getPlatformOrder, collectionOrder.getPlatformOrder())  // 指定更新条件 订单号
                        .set(CollectionOrder::getVoucher, voucherImage) // 指定更新字段 (支付凭证)
                        .set(CollectionOrder::getPaymentTime, LocalDateTime.now()); // 指定更新字段 (支付时间)
                // 这里传入的 null 表示不更新实体对象的其他字段
                collectionOrderService.update(null, lambdaUpdateWrappercollectionOrder);

                log.info("完成支付处理成功 会员账号: {}, 买入订单号: {}", memberInfo.getMemberAccount(), platformOrder);

                return receiveKycProcessInfo(platformOrder);
            }
        } catch (Exception e) {
            //手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.info("完成支付处理失败, 买入订单号: {}, e: {}", platformOrder, e);
            return RestResult.failure(ResultCode.SYSTEM_EXECUTION_ERROR);
        } finally {
            //释放锁
            if (req && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return RestResult.failure(ResultCode.SYSTEM_EXECUTION_ERROR);
    }

    /**
     * 查询KYC处理状态
     * @param buyOrderNo
     * @return
     */
    public RestResult<Integer> receiveKycProcessInfo(String buyOrderNo) {
        // 根据买入订单号查询订单信息
        CollectionOrder collectionOrder = this.collectionOrderService.lambdaQuery()
                .eq(CollectionOrder::getPlatformOrder, buyOrderNo)
                .one();
        if (Objects.isNull(collectionOrder)) {
            log.info("订单不存在 订单号: {}", buyOrderNo);
            return RestResult.failed(ResultCode.ORDER_NOT_EXIST);
        }
        KycAutoCompleteReq kycAutoCompleteReq = new KycAutoCompleteReq();
        kycAutoCompleteReq.setBuyerMemberId(collectionOrder.getMemberId());
        kycAutoCompleteReq.setBuyerOrder(buyOrderNo);
        kycAutoCompleteReq.setOrderAmount(collectionOrder.getActualAmount());
        kycAutoCompleteReq.setType("2");
        kycAutoCompleteReq.setCurrency(collectionOrder.getCurrency());
        // 根据商户订单号查询商户订单信息
        MerchantPaymentOrders merchantPaymentOrders = this.merchantPaymentOrdersService.lambdaQuery()
                .eq(MerchantPaymentOrders::getPlatformOrder, collectionOrder.getMerchantOrder())
                .one();
        if (Objects.isNull(merchantPaymentOrders)) {
            log.info("商户订单不存在 订单号: {}", collectionOrder.getMerchantOrder());
            return RestResult.failed(ResultCode.ORDER_NOT_EXIST);
        }
        String bankCardNumber = merchantPaymentOrders.getBankCardNumber();
        if (StringUtils.isEmpty(bankCardNumber) || StringUtils.isEmpty(bankCardNumber.trim())) {
            log.info("银行卡号为空 订单号: {}", collectionOrder.getMerchantOrder());
            return RestResult.failed(ResultCode.KYC_BANK_NOT_FOUND);
        }
        kycAutoCompleteReq.setWithdrawUpi(getLastFourDigits(bankCardNumber));
        KycRestResult<?> kycRestResult = kycCenterService.completePayment(kycAutoCompleteReq);
        return RestResult.ok("1".equals(kycRestResult.getCode()) ? 1 : 2);
    }

    /**
     * 生成存储 识别失败图片的 redis Key
     *
     * @return {@link String}
     */
    public String generateKey() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "failedPaymentVoucherPaths:" + date;
    }


    public void storeImagePath(String platformOrder, String imagePath) {
        String key = generateKey();
        redisTemplate.opsForHash().put(key, imagePath, platformOrder);
    }


    /**
     * 获取支付页面数据
     *
     * @return {@link RestResult}<{@link BuyVo}>
     */
    @Override
    public RestResult<BuyVo> getPaymentPageData() {

        MemberInfo memberInfo = memberInfoService.getMemberInfo();

        if (memberInfo == null) {
            log.error("获取支付页面数据失败: 获取会员信息失败");
            return RestResult.failure(ResultCode.RELOGIN);
        }

        //查询会员待支付的买入订单
        CollectionOrder pendingBuyOrder = collectionOrderService.getPendingBuyOrder(String.valueOf(memberInfo.getId()));

        if (pendingBuyOrder == null) {
            log.error("获取支付页面数据失败 订单已超时, 会员账号: {}", memberInfo.getMemberAccount());
            return RestResult.failure(ResultCode.ORDER_EXPIRED);
        }

        //创建返回数据
        BuyVo buyVo = new BuyVo();
        BeanUtils.copyProperties(pendingBuyOrder, buyVo);

        //支付方式
        buyVo.setPayType(pendingBuyOrder.getPayType());

        //获取支付剩余时间
        buyVo.setPaymentExpireTime(redisUtil.getPaymentRemainingTime(buyVo.getPlatformOrder()));

        //是否上传过支付凭证
        buyVo.setPaymentReceiptUploaded(StringUtils.isNotBlank(pendingBuyOrder.getVoucher()) ? "1" : "0");

        log.info("获取支付页面数据成功, 会员账号: {}, 返回数据: {}", memberInfo.getMemberAccount(), buyVo);

        return RestResult.ok(buyVo);
    }

    /**
     * 获取USDT支付页面数据
     *
     * @return {@link RestResult}<{@link UsdtBuyVo}>
     */
    @Override
    public RestResult<UsdtBuyVo> getUsdtPaymentPageData() {

        //获取配置信息
        TradeConfig tradeConfig = tradeConfigMapper.selectById(1);

        MemberInfo memberInfo = memberInfoService.getMemberInfo();

        if (memberInfo == null) {
            log.error("获取USDT支付页面数据失败: 获取会员信息失败");
            return RestResult.failure(ResultCode.RELOGIN);
        }

        //查询会员待支付的USDT买入订单
        UsdtBuyOrder pendingUsdtBuyOrder = usdtBuyOrderService.getPendingUsdtBuyOrder(UserContext.getCurrentUserId());

        if (pendingUsdtBuyOrder == null) {
            log.error("获取USDT支付页面数据失败 订单已超时, 会员账号: {}", memberInfo.getMemberAccount());
            return RestResult.failure(ResultCode.ORDER_EXPIRED);
        }

        UsdtBuyVo usdtBuyVo = new UsdtBuyVo();

        BeanUtils.copyProperties(pendingUsdtBuyOrder, usdtBuyVo);

        //设置USDT主网络(目前只支持TRC)
        usdtBuyVo.setNetworkProtocol("TRC-20");

        //获取支付剩余时间
        usdtBuyVo.setUsdtPaymentExpireTime(redisUtil.getUsdtPaymentRemainingTime(pendingUsdtBuyOrder.getPlatformOrder()));

        //USDT数量
        usdtBuyVo.setUsdtNum(pendingUsdtBuyOrder.getUsdtNum());

        //iToken数量
        usdtBuyVo.setIToken(pendingUsdtBuyOrder.getArbNum());

        //蚂蚁USDT最低充值金额
        usdtBuyVo.setMinAntUsdtDepositAmount(tradeConfig.getMinAntUsdtDepositAmount());

        log.info("获取USDT支付页面数据成功, 会员账号: {}, 返回数据: {}", memberInfo.getMemberAccount(), usdtBuyVo);

        return RestResult.ok(usdtBuyVo);
    }

    /**
     * 获取支付类型
     *
     * @return {@link RestResult}<{@link List}<{@link PaymentTypeVo}>>
     */
    @Override
    public RestResult<List<PaymentTypeVo>> getPaymentType() {
        PaymentTypeVo paymentTypeVo = new PaymentTypeVo();

        List<PaymentTypeVo> res = new ArrayList<>();

        res.add(paymentTypeVo);

        return RestResult.ok(res);
    }
}
