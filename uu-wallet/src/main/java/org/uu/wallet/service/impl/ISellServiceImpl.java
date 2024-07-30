package org.uu.wallet.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.redisson.api.RLock;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.uu.common.core.constant.GlobalConstants;
import org.uu.common.core.enums.MemberAccountChangeEnum;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.common.pay.bo.MemberAccountChangeBO;
import org.uu.common.redis.util.RedissonUtil;
import org.uu.common.web.utils.UserContext;
import org.uu.wallet.Enum.CurrenceEnum;
import org.uu.wallet.Enum.DelegationStatusEnum;
import org.uu.wallet.Enum.OrderStatusEnum;
import org.uu.wallet.Enum.PayTypeEnum;
import org.uu.wallet.bo.ActiveKycPartnersBO;
import org.uu.wallet.bo.DelegationOrderBO;
import org.uu.wallet.entity.*;
import org.uu.wallet.mapper.MemberInfoMapper;
import org.uu.wallet.req.PlatformOrderReq;
import org.uu.wallet.service.*;
import org.uu.wallet.util.DelegationOrderRedisUtil;
import org.uu.wallet.util.PayTypeUtil;
import org.uu.wallet.util.RedisUtil;
import org.uu.wallet.vo.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ISellServiceImpl implements ISellService {

    private final IPaymentOrderService paymentOrderService;
    private final IMemberInfoService memberInfoService;
    private final ICollectionInfoService collectionInfoService;
    private final RedissonUtil redissonUtil;
    private final ITradeConfigService tradeConfigService;
    private final MemberInfoMapper memberInfoMapper;
    private final IMemberAccountChangeService memberAccountChangeService;
    private final IAppealOrderService appealOrderService;
    private final RedisUtil redisUtil;
    private final IWithdrawalCancellationService withdrawalCancellationService;
    private final ISystemCurrencyService systemCurrencyService;

    @Autowired
    private IMatchPoolService matchPoolService;

    @Autowired
    private IKycPartnersService kycPartnersService;

    @Autowired
    private DelegationOrderRedisUtil delegationOrderRedisUtil;

    /**
     * 获取取消卖出页面数据
     *
     * @param platformOrderReq
     * @return {@link RestResult}<{@link CancelSellPageDataVo}>
     */
    @Override
    public RestResult<CancelSellPageDataVo> getCancelSellPageData(PlatformOrderReq platformOrderReq) {

        //获取当前会员信息
        MemberInfo memberInfo = memberInfoService.getMemberInfo();

        if (memberInfo == null) {
            log.error("获取取消卖出页面数据失败: 获取会员信息失败");
            return RestResult.failure(ResultCode.RELOGIN);
        }

        String memberId = String.valueOf(memberInfo.getId());

        //判断是否拆单
        if (platformOrderReq.getPlatformOrder().startsWith("C2C")) {
            //拆单
            //根据匹配池订单号 获取匹配池订单
            MatchPool matchPool = matchPoolService.getMatchPoolOrderByOrderNo(platformOrderReq.getPlatformOrder());


            if (matchPool == null || !matchPool.getMemberId().equals(memberId)) {
                log.error("获取取消卖出页面数据 失败: 订单不存在或订单不属于该会员");
                return RestResult.failure(ResultCode.ORDER_VERIFICATION_FAILED);
            }

            CancelSellPageDataVo cancelSellPageDataVo = new CancelSellPageDataVo();

            BeanUtils.copyProperties(matchPool, cancelSellPageDataVo);

            //设置卖出数量
            cancelSellPageDataVo.setSellQuantity(matchPool.getAmount());

            //获取充值取消原因列表
            cancelSellPageDataVo.setReason(withdrawalCancellationService.getSellCancelReasonsList());

            //订单号
            cancelSellPageDataVo.setPlatformOrder(matchPool.getMatchOrder());

            log.info("获取取消卖出页面数据 成功 会员账号: {}, req: {}, 返回数据: {}", memberInfo.getMemberAccount(), platformOrderReq, cancelSellPageDataVo);

            return RestResult.ok(cancelSellPageDataVo);

        } else {
            //非拆单

            //根据卖出订单号 查询卖出订单
            PaymentOrder paymentOrder = paymentOrderService.getPaymentOrderByOrderNo(platformOrderReq.getPlatformOrder());

            if (paymentOrder == null || !paymentOrder.getMemberId().equals(memberId)) {
                log.error("获取取消卖出页面数据 失败: 订单不存在或订单不属于该会员");
                return RestResult.failure(ResultCode.ORDER_VERIFICATION_FAILED);
            }

            CancelSellPageDataVo cancelSellPageDataVo = new CancelSellPageDataVo();

            BeanUtils.copyProperties(paymentOrder, cancelSellPageDataVo);

            //设置卖出数量
            cancelSellPageDataVo.setSellQuantity(paymentOrder.getAmount());

            //获取充值取消原因列表
            cancelSellPageDataVo.setReason(withdrawalCancellationService.getSellCancelReasonsList());

            log.info("获取取消卖出页面数据 成功 会员账号: {}, req: {}, 返回数据: {}", memberInfo.getMemberAccount(), platformOrderReq, cancelSellPageDataVo);

            return RestResult.ok(cancelSellPageDataVo);
        }
    }

    /**
     * 查看卖出订单详情
     *
     * @param platformOrderReq
     * @return {@link RestResult}<{@link MatchPoolSplittingVo}>
     */
    @Override
    public RestResult<SellOrderDetailsVo> getSellOrderDetails(PlatformOrderReq platformOrderReq) {
        //获取当前会员信息
        MemberInfo memberInfo = memberInfoService.getMemberInfo();

        if (memberInfo == null) {
            log.error("查看卖出订单详情失败: 获取会员信息失败");
            return RestResult.failure(ResultCode.RELOGIN);
        }
        //卖出订单详情vo
        SellOrderDetailsVo sellOrderDetailsVo = new SellOrderDetailsVo();

        // 查询卖出订单
        PaymentOrder paymentOrder = paymentOrderService.getPaymentOrderByOrderNo(platformOrderReq.getPlatformOrder());
        if (Objects.isNull(paymentOrder)) {
            return RestResult.failed(ResultCode.ORDER_NOT_EXIST);
        }

        BeanUtils.copyProperties(paymentOrder, sellOrderDetailsVo);

        // 查询当前订单的KYC信息
        String kycId = paymentOrder.getKycId();
        if (Objects.nonNull(kycId) && StringUtils.isNotEmpty(kycId) && StringUtils.isNotEmpty(kycId.trim())) {
            KycPartners kycPartners = this.kycPartnersService.getById(Integer.valueOf(kycId));
            if (Objects.nonNull(kycPartners)) {
                sellOrderDetailsVo.setKycAccount(kycPartners.getAccount())
                        .setKycBankName(kycPartners.getBankName());
            }
            sellOrderDetailsVo.setMemberType(paymentOrder.getMemberType());
        }
        //是否经过申诉
        if (paymentOrder.getAppealTime() != null) {
            sellOrderDetailsVo.setIsAppealed(1);
            //查询申诉订单
            AppealOrder appealOrderBySellOrderNo = appealOrderService.getAppealOrderBySellOrderNo(paymentOrder.getPlatformOrder());

            if (appealOrderBySellOrderNo != null) {
                //设置申诉类型
                sellOrderDetailsVo.setDisplayAppealType(appealOrderBySellOrderNo.getDisplayAppealType());
            }
        }

        //匹配剩余时间
        sellOrderDetailsVo.setMatchExpireTime(redisUtil.getMatchRemainingTime(platformOrderReq.getPlatformOrder()));
        //确认中剩余时间
        sellOrderDetailsVo.setConfirmExpireTime(redisUtil.getConfirmRemainingTime(platformOrderReq.getPlatformOrder()));
        //待支付剩余时间
        sellOrderDetailsVo.setPaymentExpireTime(redisUtil.getPaymentRemainingTime(platformOrderReq.getPlatformOrder()));

        //优化剩余时间为0 状态还没更新的延迟
        //判断如果订单是待支付状态, 但是支付剩余时间低于0 那么将返回前端的订单状态改为已取消
        if (sellOrderDetailsVo.getOrderStatus().equals(OrderStatusEnum.BE_PAID.getCode()) && (sellOrderDetailsVo.getPaymentExpireTime() == null || sellOrderDetailsVo.getPaymentExpireTime() < 1)) {
            sellOrderDetailsVo.setOrderStatus(OrderStatusEnum.WAS_CANCELED.getCode());
        }

        //是否是子订单
        if (StringUtils.isNotEmpty(paymentOrder.getMatchOrder())) {
            sellOrderDetailsVo.setIsSubOrder(1);
        }

        //获取配置信息
        TradeConfig tradeConfig = tradeConfigService.getById(1);

        int canCancelInt = redisUtil.canCancel(memberInfo.getId());
        //是否可以取消匹配
        sellOrderDetailsVo.setCancellable(canCancelInt < tradeConfig.getCancelMatchTimesLimit());

        return RestResult.ok(sellOrderDetailsVo);
//        //获取当前会员信息
//        MemberInfo memberInfo = memberInfoService.getMemberInfo();
//
//        if (memberInfo == null) {
//            log.error("查看卖出订单详情失败: 获取会员信息失败");
//            return RestResult.failure(ResultCode.RELOGIN);
//        }
//
//        String memberId = String.valueOf(memberInfo.getId());
//
//        //卖出订单详情vo
//        SellOrderDetailsVo sellOrderDetailsVo = new SellOrderDetailsVo();
//
//        //判断订单是匹配池订单还是卖出订单
//        if (platformOrderReq.getPlatformOrder().startsWith("C2C")) {
//            //匹配池订单
//            MatchPool matchPool = matchPoolService.getMatchPoolOrderByOrderNo(platformOrderReq.getPlatformOrder());
//
//            if (matchPool == null || !matchPool.getMemberId().equals(memberId)) {
//                log.error("查看卖出订单详情失败 订单不存在或订单不属于该会员, 会员信息: {}, 订单信息: {}", memberInfo, matchPool);
//                return RestResult.failure(ResultCode.ORDER_VERIFICATION_FAILED);
//            }
//
//            BeanUtils.copyProperties(matchPool, sellOrderDetailsVo);
//
//            //可能需要同时显示两种收款信息
//            CollectionInfo upiCollectionInfo = collectionInfoService.getById(matchPool.getUpiCollectionInfoId());
//
//            if (upiCollectionInfo != null) {
//                sellOrderDetailsVo.setUpiId(upiCollectionInfo.getUpiId());
//                sellOrderDetailsVo.setUpiName(upiCollectionInfo.getUpiName());
//
//            }
//
//            CollectionInfo bankCollectionInfo = collectionInfoService.getById(matchPool.getBankCollectionInfoId());
//            if (bankCollectionInfo != null) {
//                sellOrderDetailsVo.setBankCardNumber(bankCollectionInfo.getBankCardNumber());
//                sellOrderDetailsVo.setBankName(bankCollectionInfo.getBankName());
//                sellOrderDetailsVo.setBankCardOwner(bankCollectionInfo.getBankCardOwner());
//                sellOrderDetailsVo.setIfscCode(bankCollectionInfo.getIfscCode());
//            }
//
//            //兼容取消原因和失败原因
//            if (sellOrderDetailsVo.getRemark() == null) {
//                sellOrderDetailsVo.setRemark(sellOrderDetailsVo.getCancellationReason());
//            }
//
//            //匹配池订单号
//            sellOrderDetailsVo.setPlatformOrder(matchPool.getMatchOrder());
//
//            //设置匹配剩余时间
//            sellOrderDetailsVo.setMatchExpireTime(redisUtil.getMatchRemainingTime(matchPool.getMatchOrder()));
//
//            //判断如果订单是待支付状态, 但是支付剩余时间低于0 那么将返回前端的订单状态改为已取消
//            if (sellOrderDetailsVo.getOrderStatus().equals(OrderStatusEnum.BE_PAID.getCode()) && (sellOrderDetailsVo.getPaymentExpireTime() == null || sellOrderDetailsVo.getPaymentExpireTime() < 1)) {
//                sellOrderDetailsVo.setOrderStatus(OrderStatusEnum.WAS_CANCELED.getCode());
//            }
//
//
//            //查询该匹配池订单的子订单
//            List<SellOrderListVo> paymentOrderListByMatchOrder = paymentOrderService.getPaymentOrderListByMatchOrder(matchPool.getMatchOrder());
//
//            sellOrderDetailsVo.setSellOrderList(paymentOrderListByMatchOrder);
//
//            return RestResult.ok(sellOrderDetailsVo);
//
//        } else if (platformOrderReq.getPlatformOrder().startsWith("MC")) {
//            //卖出订单
//            PaymentOrder paymentOrder = paymentOrderService.getPaymentOrderByOrderNo(platformOrderReq.getPlatformOrder());
//
//            BeanUtils.copyProperties(paymentOrder, sellOrderDetailsVo);
//
//            CollectionInfo collectionInfo;
//
//            //根据卖出订单当前支付方式 查询收款信息
//            if (PayTypeEnum.INDIAN_UPI.getCode().equals(paymentOrder.getPayType())) {
//                //UPI
//                collectionInfo = collectionInfoService.getById(paymentOrder.getUpiCollectionInfoId());
//                if (collectionInfo != null) {
//                    sellOrderDetailsVo.setUpiId(collectionInfo.getUpiId());
//                }
//            } else if (PayTypeEnum.INDIAN_CARD.getCode().equals(paymentOrder.getPayType())) {
//                //银行卡
//                collectionInfo = collectionInfoService.getById(paymentOrder.getBankCollectionInfoId());
//                if (collectionInfo != null) {
//                    sellOrderDetailsVo.setBankCardNumber(collectionInfo.getBankCardNumber());
//                    sellOrderDetailsVo.setBankName(collectionInfo.getBankName());
//                    sellOrderDetailsVo.setBankCardOwner(collectionInfo.getBankCardOwner());
//                    sellOrderDetailsVo.setIfscCode(collectionInfo.getIfscCode());
//                }
//            }
//
//            //是否经过申诉
//            if (paymentOrder.getAppealTime() != null) {
//                sellOrderDetailsVo.setIsAppealed(1);
//
//                //查询申诉订单
//                AppealOrder appealOrderBySellOrderNo = appealOrderService.getAppealOrderBySellOrderNo(paymentOrder.getPlatformOrder());
//
//                if (appealOrderBySellOrderNo != null) {
//                    //设置申诉类型
//                    sellOrderDetailsVo.setDisplayAppealType(appealOrderBySellOrderNo.getDisplayAppealType());
//                }
//            }
//
//            //匹配剩余时间
//            sellOrderDetailsVo.setMatchExpireTime(redisUtil.getMatchRemainingTime(platformOrderReq.getPlatformOrder()));
//            //确认中剩余时间
//            sellOrderDetailsVo.setConfirmExpireTime(redisUtil.getConfirmRemainingTime(platformOrderReq.getPlatformOrder()));
//            //待支付剩余时间
//            sellOrderDetailsVo.setPaymentExpireTime(redisUtil.getPaymentRemainingTime(platformOrderReq.getPlatformOrder()));
//
//
//            //优化剩余时间为0 状态还没更新的延迟
//            //判断如果订单是待支付状态, 但是支付剩余时间低于0 那么将返回前端的订单状态改为已取消
//            if (sellOrderDetailsVo.getOrderStatus().equals(OrderStatusEnum.BE_PAID.getCode()) && (sellOrderDetailsVo.getPaymentExpireTime() == null || sellOrderDetailsVo.getPaymentExpireTime() < 1)) {
//                sellOrderDetailsVo.setOrderStatus(OrderStatusEnum.WAS_CANCELED.getCode());
//            }
//
//            //是否是子订单
//            if (StringUtils.isNotEmpty(paymentOrder.getMatchOrder())) {
//                sellOrderDetailsVo.setIsSubOrder(1);
//            }
//
//            //获取配置信息
//            TradeConfig tradeConfig = tradeConfigService.getById(1);
//
//            int canCancelInt = redisUtil.canCancel(memberInfo.getId());
//            //是否可以取消匹配
//            sellOrderDetailsVo.setCancellable(canCancelInt < tradeConfig.getCancelMatchTimesLimit());
//
//            return RestResult.ok(sellOrderDetailsVo);
//        } else {
//            //订单号错误
//            log.error("查看卖出订单详情失败, 订单号错误, 会员账号: {}, 订单号: {}", memberInfo.getMemberAccount(), platformOrderReq.getPlatformOrder());
//            return RestResult.failure(ResultCode.ORDER_NUMBER_ERROR);
//        }
    }

    /**
     * 委托卖出
     *
     * @param request
     * @return {@link RestResult }
     */
    @Override
    @Transactional
    public RestResult delegateSell(HttpServletRequest request) {

        //获取当前会员id
        Long memberId = UserContext.getCurrentUserId();

        if (memberId == null) {
            log.error("委托卖出处理失败: 获取会员id失败");
            return RestResult.failure(ResultCode.RELOGIN);
        }

        //分布式锁key ar-wallet-delegateSell
        String key = "uu-wallet-delegateSell";
        RLock lock = redissonUtil.getLock(key);

        boolean req = false;

        try {
            req = lock.tryLock(10, TimeUnit.SECONDS);

            if (req) {

                //获取当前会员信息 加上排他行锁
                MemberInfo memberInfo = memberInfoMapper.selectMemberInfoForUpdate(memberId);

                if (memberInfo == null) {
                    log.error("委托卖出处理失败: 获取会员信息失败");
                    return RestResult.failure(ResultCode.RELOGIN);
                }

                //判断会员状态 会员卖出状态
               if ("0".equals(memberInfo.getStatus()) || "0".equals(memberInfo.getSellStatus())){
                   log.error("委托卖出处理失败: 会员状态或卖出状态未开启");
                   return RestResult.failure(ResultCode.MEMBER_SELL_STATUS_NOT_AVAILABLE);
               }

                //判断如果会员已经开启委托了, 那么直接返回当前已在委托中
                if (DelegationStatusEnum.DELEGATIONSUCCESS.getCode().equals(String.valueOf(memberInfo.getDelegationStatus()))) {
                    log.warn("委托卖出处理失败: 会员处于委托中状态");
                    return RestResult.failure(ResultCode.CURRENTLY_IN_DELEGATION);
                }

                //获取配置信息
                TradeConfig tradeConfig = tradeConfigService.getById(1);

                //判断余额是否充足, 是否低于最低充值金额
                if (memberInfo.getBalance().compareTo(tradeConfig.getMinimumDelegationAmount()) < 0) {
                    log.error("委托卖出处理失败: 会员余额不足, 会员id: {}, 会员余额: {}", memberInfo.getId(), memberInfo.getBalance());
                    return RestResult.failure(ResultCode.INSUFFICIENT_BALANCE);
                }


                //校验会员是否有链接kyc 自己尝试连接 如果能连接成功就视为有正在连接的kyc
                KycPartners kycPartners = kycPartnersService.hasActiveKycForCurrentMember(memberId);

                if (kycPartners == null) {
                    log.error("委托卖出处理失败: 会员没有在连接中的kyc, 会员id: {}, 会员信息: {}", memberInfo.getId(), memberInfo);
                    return RestResult.failure(ResultCode.KYC_NOT_CONNECTED);
                }


                //判断是否有链接upiKyc
//                ActiveKycPartnersBO activeKycPartnersByMemberId = kycPartnersService.getActiveKycPartnersByMemberId(String.valueOf(memberInfo.getId()));

//                if (activeKycPartnersByMemberId == null || activeKycPartnersByMemberId.getUpiPartners() == null || activeKycPartnersByMemberId.getUpiPartners().isEmpty()) {
//                    log.error("委托卖出处理失败: 会员没有在连接中的kyc, 会员id: {}, 会员信息: {}", memberInfo.getId(), memberInfo);
//                    return RestResult.failure(ResultCode.KYC_NOT_CONNECTED);
//                }

//                String payType = PayTypeUtil.getPayType(activeKycPartnersByMemberId);

//                if (payType == null) {
//                    log.error("委托卖出处理失败: 会员没有在连接中的kyc, 会员id: {}, 会员信息: {}", memberInfo.getId(), memberInfo);
//                    return RestResult.failure(ResultCode.KYC_NOT_CONNECTED);
//                }

                //业务处理

                DelegationOrderBO delegationOrderBO = new DelegationOrderBO();
                //会员ID
                delegationOrderBO.setMemberId(String.valueOf(memberId));
                //委托时间
                delegationOrderBO.setDelegationTime(LocalDateTime.now());
                //委托金额
                delegationOrderBO.setAmount(memberInfo.getBalance());

                //将会员委托状态开启
                //将可用余额 转到委托金额
                // 创建一个 UpdateWrapper 对象，用于构建更新条件和指定更新字段
                LambdaUpdateWrapper<MemberInfo> lambdaUpdateWrapperMemberInfo = new LambdaUpdateWrapper<>();
                lambdaUpdateWrapperMemberInfo
                        .eq(MemberInfo::getId, memberInfo.getId())  // 指定更新条件，会员id
                        .set(MemberInfo::getDelegationStatus, DelegationStatusEnum.DELEGATIONSUCCESS.getCode()); // 指定更新字段 (委托状态)
                // 这里传入的 null 表示不更新实体对象的其他字段
                memberInfoService.update(null, lambdaUpdateWrapperMemberInfo);

                //执行事务同步回调
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        //事务提交后 将委托订单(订单金额 支付类型 会员iD 委托时间 支付方式) 添加到redis供匹配
                        delegationOrderRedisUtil.addOrder(delegationOrderBO);
                    }
                });

                log.info("委托卖出处理成功, 会员id: {}, 会员信息: {}, 委托信息: {}}", memberInfo.getId(), memberInfo, delegationOrderBO);

                return RestResult.ok();
            } else {
                //没获取到锁 直接返回操作频繁
                return RestResult.failure(ResultCode.TOO_FREQUENT);
            }
        } catch (Exception e) {
            //手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("委托卖出处理失败 会员id: {}, e: {}", memberId, e);
        } finally {
            //释放锁
            if (req && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return RestResult.failure(ResultCode.SYSTEM_EXECUTION_ERROR);
    }

    /**
     * 获取交易页面数据
     *
     * @return {@link RestResult }<{@link FetchTransactionPageDataVo }>
     */
    @Override
    public RestResult<FetchTransactionPageDataVo> fetchTransactionPageData() {

        //获取当前会员信息
        MemberInfo memberInfo = memberInfoService.getMemberInfo();

        if (memberInfo == null) {
            log.error("获取交易页面数据失败: 获取会员信息失败");
            return RestResult.failure(ResultCode.RELOGIN);
        }

        //返回vo
        FetchTransactionPageDataVo fetchTransactionPageDataVo = new FetchTransactionPageDataVo();


        ActiveKycPartnersBO activeKycPartnersBO = kycPartnersService.getActiveKycPartnersByMemberId(memberInfo.getId().toString());

        //买入状态
        fetchTransactionPageDataVo.setBuyStatus(Integer.valueOf(memberInfo.getBuyStatus()));

        //卖出状态
        fetchTransactionPageDataVo.setSellStatus(Integer.valueOf(memberInfo.getSellStatus()));

        //已连接的upi kyc
        fetchTransactionPageDataVo.setActiveUpiKycCount(activeKycPartnersBO.getUpiPartners().size());

        //已连接的bank KYC
        fetchTransactionPageDataVo.setActiveBankKycCount(activeKycPartnersBO.getBankPartners().size());

        //用户kyc总数量
        fetchTransactionPageDataVo.setCurrentUserKycCount(kycPartnersService.getKycPartnersCount(memberInfo.getId()));

        //用户委托状态 1: 关闭  1: 开启
        fetchTransactionPageDataVo.setDelegationStatus(memberInfo.getDelegationStatus());

        //用户余额
        fetchTransactionPageDataVo.setIToken(memberInfo.getBalance());

        //总奖励=买入总奖励+卖出总奖励+买入返佣+卖出返佣+平台分红
        fetchTransactionPageDataVo.setTotalBonus(
                memberInfo.getTotalBuyBonus()
                        .add(memberInfo.getTotalSellBonus())
                        .add(memberInfo.getPlatformDividends())
                        .add(memberInfo.getTotalBuyCommissionAmount())
                        .add(memberInfo.getTotalSellCommissionAmount())
        );

        //交易中金额
        fetchTransactionPageDataVo.setTransactionAmount(memberInfo.getFrozenAmount());

        //当前用户的买入奖励比例
        fetchTransactionPageDataVo.setBuyBonusProportion(memberInfo.getBuyBonusProportion() == null ? new BigDecimal("0") : memberInfo.getBuyBonusProportion());

        //当前USDT汇率
        TradeConfig tradeConfig = tradeConfigService.getById(1);
        fetchTransactionPageDataVo.setUsdtBuyItoken(systemCurrencyService.getCurrencyExchangeRate(CurrenceEnum.INDIA.getCode()));
        fetchTransactionPageDataVo.setMinAntUsdtDepositAmount(tradeConfig.getMinAntUsdtDepositAmount());
        fetchTransactionPageDataVo.setMaxAntUsdtDepositAmount(tradeConfig.getMaxAntUsdtDepositAmount());

        //  获取当前用户今日的账变记录 包含奖励和返佣和USDT买入
        LocalDate localDate = LocalDate.now();
        String timeZone = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest()
                .getHeader(GlobalConstants.HEADER_TIME_ZONE);
        if (StringUtils.isNotEmpty(timeZone)) {
            localDate = LocalDateTime.now(ZoneId.of(timeZone)).toLocalDate();
        }
        List<MemberAccountChangeBO> accountChangeBOS = this.memberAccountChangeService.queryAccountChangeListByIds(
                Collections.singletonList(memberInfo.getId()),
                localDate.atStartOfDay(),
                localDate.atStartOfDay().plusDays(1).plusSeconds(-1),
                true,
                true,
                true
        );

        BigDecimal sellAndBuyAndUsdtRechanrgeAmount = BigDecimal.ZERO;
        BigDecimal rewardAmount = BigDecimal.ZERO;

        if (!CollectionUtils.isEmpty(accountChangeBOS)) {
            // 计算买入和卖出金额和USDT充值的总和
            sellAndBuyAndUsdtRechanrgeAmount = accountChangeBOS.stream()
                    .filter(item -> Objects.nonNull(item)
                            &&
                            Stream.of(
                                    MemberAccountChangeEnum.RECHARGE.getCode(),
                                    MemberAccountChangeEnum.WITHDRAW.getCode(),
                                    MemberAccountChangeEnum.USDT_RECHARGE.getCode()
                            ).collect(Collectors.toList()).contains(item.getChangeType()))
                    .map(MemberAccountChangeBO::getAmountChange)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            // 计算买入奖励、卖出奖励、买入返佣奖励、卖出返佣奖励、平台分红金额总和
            rewardAmount = accountChangeBOS.stream()
                    .filter(item -> Objects.nonNull(item)
                            &&
                            Stream.of(
                                    MemberAccountChangeEnum.BUY_BONUS.getCode(),
                                    MemberAccountChangeEnum.SELL_BONUS.getCode(),
                                    MemberAccountChangeEnum.BUY_COMMISSION.getCode(),
                                    MemberAccountChangeEnum.SELL_COMMISSION.getCode(),
                                    MemberAccountChangeEnum.PLATFORM_DIVIDENDS.getCode()
                            ).collect(Collectors.toList()).contains(item.getChangeType()))
                    .map(MemberAccountChangeBO::getAmountChange)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

        }
        //今日交易成功金额
        fetchTransactionPageDataVo.setTodayTransactionSuccessAmount(sellAndBuyAndUsdtRechanrgeAmount);

        //今日交易奖励
        fetchTransactionPageDataVo.setTodayTransactionReward(rewardAmount);

        //INR买入iToken汇率
        fetchTransactionPageDataVo.setBuyInrRatio(tradeConfig.getBuyInrRatio());


        return RestResult.ok(fetchTransactionPageDataVo);
    }

    @Override
    public RestResult<SellListVo> fetchPageData() {
        Long currentUserId = UserContext.getCurrentUserId();
        if (Objects.isNull(currentUserId)) {
            return RestResult.failed(ResultCode.RELOGIN);
        }
        MemberInfo memberInfo = this.memberInfoMapper.selectById(currentUserId);
        if (Objects.isNull(memberInfo)) {
            return RestResult.failed(ResultCode.USER_NOT_EXIST);
        }

        // 查询当前用户进行中、已完成的卖出订单
        List<PaymentOrder> successAndSellingOrderList = this.paymentOrderService.lambdaQuery()
                .eq(PaymentOrder::getMemberId, currentUserId)
                .in(PaymentOrder::getOrderStatus, Arrays.asList(OrderStatusEnum.BE_PAID.getCode(), OrderStatusEnum.SUCCESS.getCode()))
                .list();

        // 计算当前已经完成订单的金额总和
        BigDecimal successAmount = CollectionUtils.isEmpty(successAndSellingOrderList) ? BigDecimal.ZERO : successAndSellingOrderList.stream()
                .filter(item -> Objects.nonNull(item) && OrderStatusEnum.SUCCESS.getCode().equals(item.getOrderStatus()))
                .map(PaymentOrder::getActualAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 获取当前交易配置信息
        TradeConfig tradeConfig = this.tradeConfigService.getById(1);
        if (Objects.isNull(tradeConfig)) {
            return RestResult.failed("Trade config error");
        }

        List<OnSellingOrderVO> onSellingOrderVOList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(successAndSellingOrderList)) {
            // 收集进行中的订单
            onSellingOrderVOList = successAndSellingOrderList.stream()
                    .filter(item -> Objects.nonNull(item) && OrderStatusEnum.BE_PAID.getCode().equals(item.getOrderStatus()))
                    .map(item -> OnSellingOrderVO.builder()
                            .id(item.getId())
                            .orderNo(item.getPlatformOrder())
                            .orderStatus(item.getOrderStatus())
                            .actualAmount(item.getActualAmount())
                            .arbAmount(item.getActualAmount().multiply(tradeConfig.getBuyInrRatio()).setScale(2, RoundingMode.DOWN))
                            .createTime(item.getCreateTime())
                            .build())
                    .sorted(Comparator.comparing(OnSellingOrderVO::getCreateTime).reversed())
                    .collect(Collectors.toList());
        }
        return RestResult.ok(
                SellListVo.builder()
                        .balance(memberInfo.getBalance())
                        .sellBalance(successAmount)
                        .inTransaction(memberInfo.getFrozenAmount())
                        .onSellingOrderVOList(onSellingOrderVOList)
                        .build()
        );
    }
}
