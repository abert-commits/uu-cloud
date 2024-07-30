package org.uu.wallet.strategy.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.uu.common.core.result.ApiResponse;
import org.uu.common.core.result.ApiResponseEnum;
import org.uu.common.pay.enums.OrderEventEnum;
import org.uu.common.pay.req.OrderEventReq;
import org.uu.common.redis.util.RedissonUtil;
import org.uu.wallet.Enum.CurrenceEnum;
import org.uu.wallet.Enum.OrderStatusEnum;
import org.uu.wallet.Enum.TaskTypeEnum;
import org.uu.wallet.bo.ActiveKycPartnersBO;
import org.uu.wallet.bo.DelegationOrderBO;
import org.uu.wallet.entity.*;
import org.uu.wallet.mapper.MemberInfoMapper;
import org.uu.wallet.property.ArProperty;
import org.uu.wallet.rabbitmq.RabbitMQService;
import org.uu.wallet.req.ApiRequest;
import org.uu.wallet.req.DepositApplyReq;
import org.uu.wallet.service.*;
import org.uu.wallet.strategy.CollectionStrategy;
import org.uu.wallet.util.DelegationOrderRedisUtil;
import org.uu.wallet.util.IpUtil;
import org.uu.wallet.util.OrderNumberGeneratorUtil;
import org.uu.wallet.util.RsaUtil;
import org.uu.wallet.vo.ApiResponseVo;
import org.uu.wallet.vo.DepositApplyVo;

import javax.crypto.BadPaddingException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.math.BigDecimal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class UpiCollectionStrategy implements CollectionStrategy {

    private final IMerchantInfoService merchantInfoService;
    private final IMemberInfoService memberInfoService;
    private final Validator validator;
    private final ArProperty arProperty;
    private final RedisTemplate redisTemplate;
    private final MemberInfoMapper memberInfoMapper;
    private final RabbitMQService rabbitMQService;
    private final IMerchantCollectOrdersService merchantCollectOrdersService;
    private final RedissonUtil redissonUtil;
    private final OrderNumberGeneratorUtil orderNumberGenerator;
    private final ITradeConfigService tradeConfigService;

    @Autowired
    private DelegationOrderRedisUtil delegationOrderRedisUtil;

    @Autowired
    private IKycPartnersService kycPartnersService;

    @Autowired
    private IPaymentOrderService paymentOrderService;

    @Autowired
    private IMerchantRatesConfigService merchantRatesConfigService;


    /**
     * UPI 代收处理
     *
     * @param apiRequest
     * @param request
     * @return {@link ApiResponse }
     */
    @Override
    @Transactional
    public ApiResponse processCollection(ApiRequest apiRequest, HttpServletRequest request) {

        //获取请求IP
        String requestIp = IpUtil.getRealIP(request);
        log.info("UPI-API充值接口, 商户号: {}, 请求IP: {}", apiRequest.getMerchantCode(), requestIp);

        //获取商户信息
        MerchantInfo merchantInfo = merchantInfoService.getMerchantInfoByCode(apiRequest.getMerchantCode());

        String merchantPublicKeyStr = null;
        //获取商户公钥
        if (merchantInfo != null) {
            merchantPublicKeyStr = merchantInfo.getMerchantPublicKey();
        }

        //校验请求
        ApiResponse apiResponse = validateRequest(apiRequest, requestIp, merchantInfo, merchantPublicKeyStr, "UPI-API充值接口");
        if (apiResponse != null) {
            return apiResponse;
        }

        //分布式锁key ar-wallet-delegateSell
        //目前所有跟委托订单相关的 都加这同一把锁
        String key = "uu-wallet-delegateSell";
        RLock lock = redissonUtil.getLock(key);

        boolean req = false;

        try {
            req = lock.tryLock(10, TimeUnit.SECONDS);

            if (req) {

                //商户公钥
                PublicKey merchantPublicKey = RsaUtil.getPublicKeyFromString(merchantPublicKeyStr);

                //平台私钥
                PrivateKey platformPrivateKey = RsaUtil.getPrivateKeyFromString(arProperty.getPrivateKey());

                //使用平台私钥解密数据
                DepositApplyReq depositApplyReq = RsaUtil.decryptData(apiRequest.getEncryptedKey(), apiRequest.getEncryptedData(), platformPrivateKey, DepositApplyReq.class);

                if (depositApplyReq == null) {
                    log.error("UPI-API充值接口处理失败, 参数校验失败: 请求ip: {}, req: {}, 商户信息: {}", requestIp, apiRequest, merchantInfo);
                    return ApiResponse.of(ApiResponseEnum.PARAM_VALID_FAIL, null);
                }

                //手动调用验证明文参数
                Set<ConstraintViolation<DepositApplyReq>> violations = validator.validate(depositApplyReq);
                if (!violations.isEmpty()) {
                    // 处理验证错误
                    for (ConstraintViolation<DepositApplyReq> violation : violations) {
                        log.error("UPI-API充值接口处理失败, 参数校验失败: 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}", requestIp, apiRequest, merchantInfo, depositApplyReq);
                        System.out.println(violation.getMessage());
                        return ApiResponse.ofMsg(ApiResponseEnum.PARAM_VALID_FAIL, violation.getMessage(), null);
                    }
                }

                //校验支付类型是否一致
                if (!apiRequest.getChannel().equals(depositApplyReq.getChannel())) {
                    log.error("UPI-API充值接口处理失败, 支付类型不一致: 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}", requestIp, apiRequest, merchantInfo, depositApplyReq);
                    return ApiResponse.of(ApiResponseEnum.PARAM_VALID_FAIL, null);
                }

                //使用商户公钥验证签名
                if (!RsaUtil.verifySignature(depositApplyReq, depositApplyReq.getSign(), merchantPublicKey)) {
                    log.error("UPI-API充值接口处理失败, 签名校验失败: 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}", requestIp, apiRequest, merchantInfo, depositApplyReq);
                    return ApiResponse.of(ApiResponseEnum.SIGNATURE_ERROR, null);
                }

                //订单金额
                BigDecimal amount = new BigDecimal(depositApplyReq.getAmount());

                //校验订单 金额是否小于1
                if (amount.compareTo(BigDecimal.ONE) < 0) {
                    log.error("UPI-API充值接口处理失败, 金额小于1: 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}, 订单金额: {}", requestIp, apiRequest, merchantInfo, depositApplyReq, amount);
                    return ApiResponse.of(ApiResponseEnum.AMOUNT_EXCEEDS_LIMIT, null);
                }

                //判断商户代收状态
                if (merchantInfo.getRechargeStatus().equals("0")) {
                    //当前商户代收状态未开启
                    log.error("UPI-API充值接口处理失败, 当前商户代收状态未开启: 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}", requestIp, apiRequest, merchantInfo, depositApplyReq);
                    return ApiResponse.of(ApiResponseEnum.MERCHANT_COLLECTION_STATUS_DISABLED, null);
                }

                //查询当前商户的支付类型配置
                MerchantRatesConfig merchantRatesConfig = merchantRatesConfigService.getMerchantRatesConfigByCode("1", depositApplyReq.getChannel(), merchantInfo.getCode());

                //如果不存在对应的支付类型配置 驳回
                if (merchantRatesConfig == null) {
                    log.error("UPI-API充值接口处理失败, 不存在对应的支付类型配置: 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}", requestIp, apiRequest, merchantInfo, depositApplyReq);
                    return ApiResponse.of(ApiResponseEnum.UNSUPPORTED_PAY_TYPE, null);
                }

                //是否配置最小金额
                boolean isMinCostConfigured = merchantRatesConfig.getMoneyMin() != null && merchantRatesConfig.getMoneyMin().compareTo(BigDecimal.ZERO) > 0;
                //是否配置最大金额
                boolean isMaxCostConfigured = merchantRatesConfig.getMoneyMax() != null && merchantRatesConfig.getMoneyMax().compareTo(BigDecimal.ZERO) > 0;

                boolean isAmountGreaterThanMin = isMinCostConfigured ? amount.compareTo(merchantRatesConfig.getMoneyMin()) >= 0 : true;
                boolean isAmountLessThanMax = isMaxCostConfigured ? amount.compareTo(merchantRatesConfig.getMoneyMax()) <= 0 : true;

                boolean isBetween = isAmountGreaterThanMin && isAmountLessThanMax;

                if (!isBetween) {
                    //订单金额不在最小金额和最大金额之间
                    log.error("UPI-API充值接口处理失败, 金额超过限制: 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}", requestIp, apiRequest, merchantInfo, depositApplyReq);
                    return ApiResponse.of(ApiResponseEnum.AMOUNT_EXCEEDS_LIMIT, null);
                }

                //代收订单申请
                OrderEventReq orderEventReq = new OrderEventReq();
                orderEventReq.setEventId(OrderEventEnum.MERCHANT_COLLECTION_ORDER_APPLICATION.getCode());
                orderEventReq.setParams("");
                //发送事件MQ
                rabbitMQService.sendStatisticProcess(orderEventReq);

                //发送商户日报表统计MQ
                rabbitMQService.sendMerchantDailyProcess(orderEventReq);

                //获取配置信息
                TradeConfig tradeConfig = tradeConfigService.getById(1);

                //从redis里面匹配委托订单
                DelegationOrderBO delegationOrderBO = delegationOrderRedisUtil.matchOrder(amount);

                if (delegationOrderBO == null) {
                    log.error("UPI-API充值接口订单提交失败, 订单匹配失败, 订单号: {}", depositApplyReq.getMerchantTradeNo());
                    return ApiResponse.of(ApiResponseEnum.ORDER_MATCHING_FAILED, null);
                }

                //获取会员信息 加上排他行锁
                MemberInfo memberInfo = memberInfoMapper.selectMemberInfoForUpdate(Long.valueOf(delegationOrderBO.getMemberId()));

                if (memberInfo == null) {
                    log.error("UPI-API充值接口处理失败, 获取蚂蚁用户失败: 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}", requestIp, apiRequest, merchantInfo, depositApplyReq);
                    return ApiResponse.of(ApiResponseEnum.ORDER_MATCHING_FAILED, null);
                }

                if (memberInfo.getBalance().compareTo(delegationOrderBO.getAmount()) < 0) {
                    //会员余额小于当前订单金额 那么将委托信息从redis删除 并关闭委托

                    //关闭委托
                    closeDelegation(memberInfo, delegationOrderBO);

                    log.error("UPI-API充值接口处理失败, 蚂蚁余额低于当前订单金额: 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}", requestIp, apiRequest, merchantInfo, depositApplyReq);
                    return ApiResponse.of(ApiResponseEnum.ORDER_MATCHING_FAILED, null);
                }

                //获取当前正在链接的kyc
                ActiveKycPartnersBO activeKycPartnersBO = kycPartnersService.getActiveKycPartnersByMemberId(delegationOrderBO.getMemberId());

                if (activeKycPartnersBO == null || activeKycPartnersBO.getUpiPartners() == null || activeKycPartnersBO.getUpiPartners().isEmpty()) {
                    //匹配失败当前 用户没有正在连接中的kyc

                    //关闭委托
                    closeDelegation(memberInfo, delegationOrderBO);

                    log.error("UPI-API充值接口处理失败, 蚂蚁没有正在链接中的kyc: 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}", requestIp, apiRequest, merchantInfo, depositApplyReq);

                    return ApiResponse.of(ApiResponseEnum.ORDER_MATCHING_FAILED, null);
                }

                //代收订单匹配
                JSONObject drderEventParamsJson = new JSONObject();
                drderEventParamsJson.put("amount", amount);
                OrderEventReq orderEventReq1 = new OrderEventReq();
                orderEventReq1.setEventId(OrderEventEnum.MERCHANT_COLLECTION_ORDER_MATCHING.getCode());
                orderEventReq1.setParams(JSONObject.toJSONString(drderEventParamsJson));
                //发送事件MQ
                rabbitMQService.sendStatisticProcess(orderEventReq1);

                //发送商户日报表统计MQ
                rabbitMQService.sendMerchantDailyProcess(orderEventReq1);

                //用户余额
                BigDecimal balance = memberInfo.getBalance();

                //用户当前余额 = 用户余额 - 订单金额
                BigDecimal balanceNow = balance.subtract(amount);


                //订单完成后 重新将委托信息添加到redis
                DelegationOrderBO delegationOrderBONow = new DelegationOrderBO();
                //会员id
                delegationOrderBONow.setMemberId(String.valueOf(memberInfo.getId()));
                //委托时间
                delegationOrderBONow.setDelegationTime(delegationOrderBO.getDelegationTime());
                //委托金额 当前剩余余额
                delegationOrderBONow.setAmount(balanceNow);


                boolean done = false;

                //判断如果用户余额少于最低委托金额 那么关闭委托状态
                if (balanceNow.compareTo(tradeConfig.getMinimumDelegationAmount()) < 0) {
                    //当前剩余金额小于最低委托金额 委托结束
                    done = true;
                }

                //更新会员信息
                LambdaUpdateWrapper<MemberInfo> lambdaUpdateWrapperMemberInfo = new LambdaUpdateWrapper<>();

                // 指定更新条件，会员id
                lambdaUpdateWrapperMemberInfo.eq(MemberInfo::getId, memberInfo.getId());

                //增加用户交易中金额
                lambdaUpdateWrapperMemberInfo.set(MemberInfo::getFrozenAmount, memberInfo.getFrozenAmount().add(amount));

                //减少用户余额
                lambdaUpdateWrapperMemberInfo.set(MemberInfo::getBalance, memberInfo.getBalance().subtract(amount));

                if (done) {
                    //用户余额小于最低委托金额 那么关闭委托订单状态
                    lambdaUpdateWrapperMemberInfo.set(MemberInfo::getDelegationStatus, 0);
                }

                //更新累计卖出次数
                lambdaUpdateWrapperMemberInfo.set(MemberInfo::getTotalSellCount, memberInfo.getTotalSellCount() + 1);

                //更新累计卖出金额
                lambdaUpdateWrapperMemberInfo.set(MemberInfo::getTotalSellAmount, memberInfo.getTotalSellAmount().add(amount));

                // 这里传入的 null 表示不更新实体对象的其他字段
                memberInfoService.update(null, lambdaUpdateWrapperMemberInfo);

                KycPartners kycPartners = activeKycPartnersBO.getUpiPartners().get(0);

                //代收平台订单号
                String platformOrder = orderNumberGenerator.generateOrderNo("P");

                String mcOrderNo = orderNumberGenerator.generateOrderNo("MC");

                //生成卖出订单
                createPaymentOrder(depositApplyReq, platformOrder, kycPartners, memberInfo, merchantInfo, requestIp, mcOrderNo);

                //费率
                BigDecimal rates = merchantRatesConfig.getRates();
                //固定手续费
                BigDecimal fixedFee = merchantRatesConfig.getFixedFee();

                //生成 商户代收订单
                boolean createMerchantCollectOrder = createMerchantCollectOrders(platformOrder, depositApplyReq, mcOrderNo, amount, memberInfo, merchantInfo, requestIp, rates, fixedFee, kycPartners);

                if (createMerchantCollectOrder) {
                    //提交成功

                    //订单页面信息
                    PaymentInfo paymentInfo = new PaymentInfo();

                    //商户号
                    paymentInfo.setMerchantCode(merchantInfo.getCode());

                    //商户名称
                    paymentInfo.setMerchantName(merchantInfo.getUsername());

                    //支付剩余时间 秒
                    paymentInfo.setPaymentExpireTime(-1L);

                    //订单金额
                    paymentInfo.setAmount(amount);

                    //商户订单号
                    paymentInfo.setMerchantOrder(depositApplyReq.getMerchantTradeNo());

                    //平台订单号
                    paymentInfo.setPlatformOrder(platformOrder);

                    //订单时间
                    paymentInfo.setCreateTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

                    //UPI_ID
                    paymentInfo.setUpiId(kycPartners.getUpiId());

                    //生成订单token
                    String paymentToken = createPaymentToken(paymentInfo, TimeUnit.MINUTES.toMillis(arProperty.getPaymentPageExpirationTime()));

                    //返回数据
                    DepositApplyVo depositApplyVo = new DepositApplyVo();

                    //支付地址
                    depositApplyVo.setPayUrl(arProperty.getPayUrl() + "?token=" + paymentToken);

                    //订单token
                    depositApplyVo.setToken(paymentToken);

                    //商户号
                    depositApplyVo.setMerchantCode(merchantInfo.getCode());

                    //会员id
                    depositApplyVo.setMemberId(depositApplyReq.getMemberId());

                    //平台订单号
                    depositApplyVo.setTradeNo(platformOrder);

                    //商户订单号
                    depositApplyVo.setMerchantTradeNo(depositApplyReq.getMerchantTradeNo());

                    //订单有效期
                    depositApplyVo.setOrderValidityDuration(arProperty.getPaymentPageExpirationTime() * 60);

                    //签名并加密数据
                    EncryptedData encryptedData = RsaUtil.signAndEncryptData(depositApplyVo, platformPrivateKey, merchantPublicKey);

                    ApiResponseVo apiResponseVo = new ApiResponseVo();
                    BeanUtils.copyProperties(encryptedData, apiResponseVo);
                    apiResponseVo.setMerchantCode(merchantInfo.getCode());

                    log.info("UPI-API充值接口订单提交成功, 请求ip: {}, 请求明文: {}, 返回明文: {}", requestIp, depositApplyReq, depositApplyVo);

                    //注册事务同步回调, 事务提交成功后, 发送延时MQ 改变订单为超时状态
                    boolean finalDone = done;
                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            //发送使 支付订单 超时的MQ
                            Long lastUpdateTimestamp = System.currentTimeMillis();
                            TaskInfo taskInfo = new TaskInfo(platformOrder, TaskTypeEnum.MERCHANT_COLLECT_ORDER_PAYMENT_TIMEOUT_QUEUE.getCode(), lastUpdateTimestamp);
                            //防止并发竞争关系 MQ延迟5秒后再将订单改为超时状态
                            long paymentPageExpirationTime = arProperty.getPaymentPageExpirationTime();
                            long millis = TimeUnit.MINUTES.toMillis(paymentPageExpirationTime);
                            rabbitMQService.sendTimeoutTask(taskInfo, millis + 5000);

                            if (finalDone) {
                                //余额低于最低委托金额 从redis删除委托信息
                                delegationOrderRedisUtil.removeOrder(delegationOrderBO);
                            } else {
                                //重新添加redis委托信息(覆盖)
                                delegationOrderRedisUtil.addOrder(delegationOrderBONow);
                            }
                        }
                    });

                    return ApiResponse.of(ApiResponseEnum.SUCCESS, apiResponseVo);
                } else {
                    //提交失败
                    log.error("UPI-API充值接口订单提交失败, 请求ip: {}, 请求明文: {}, 请求密文: {}", requestIp, depositApplyReq, apiRequest);
                    return ApiResponse.of(ApiResponseEnum.SYSTEM_EXECUTION_ERROR, null);
                }
            } else {
                //没获取到锁 直接返回操作频繁
                log.error("UPI-API充值接口订单提交失败, 获取分布式锁失败, 请求ip: {}, 请求密文: {}", requestIp, apiRequest);
                return ApiResponse.of(ApiResponseEnum.TOO_FREQUENT, null);
            }
        } catch (DataIntegrityViolationException e) {
            //手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("UPI-API充值接口订单提交失败, 数据重复 e: {}", e.getMessage());
            return ApiResponse.of(ApiResponseEnum.DATA_DUPLICATE_SUBMISSION, null);
        } catch (BadPaddingException e) {
            //手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("UPI-API充值接口订单提交失败, 解密失败，无效的密文或密钥错误 e: {}", e.getMessage());
            return ApiResponse.of(ApiResponseEnum.DECRYPTION_ERROR, null);
        } catch (Exception e) {
            //手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("UPI-API充值接口订单提交失败 req: {}, e: {}", apiRequest, e);
        } finally {
            //释放锁
            if (req && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        return ApiResponse.of(ApiResponseEnum.SYSTEM_EXECUTION_ERROR, null);
    }

    /**
     * 关闭用户委托状态
     */
    public void closeDelegation(MemberInfo memberInfo, DelegationOrderBO delegationOrderBO) {

        //将委托信息从redis删除
        delegationOrderRedisUtil.removeOrder(delegationOrderBO);

        //更新会员信息
        LambdaUpdateWrapper<MemberInfo> lambdaUpdateWrapperMemberInfo = new LambdaUpdateWrapper<>();
        // 指定更新条件，会员id
        lambdaUpdateWrapperMemberInfo.eq(MemberInfo::getId, memberInfo.getId());
        // 关闭委托订单状态
        lambdaUpdateWrapperMemberInfo.set(MemberInfo::getDelegationStatus, 0);
        // 这里传入的 null 表示不更新实体对象的其他字段
        memberInfoService.update(null, lambdaUpdateWrapperMemberInfo);
    }

    /**
     * 创建卖出订单
     *
     * @return boolean
     */
    private boolean createPaymentOrder(DepositApplyReq depositApplyReq, String platformOrder, KycPartners kycPartners, MemberInfo memberInfo, MerchantInfo merchantInfo, String requestIp, String mcOrderNo) {
        //创建卖出订单
        PaymentOrder paymentOrder = new PaymentOrder();

        //支付类型
        paymentOrder.setPayType(depositApplyReq.getChannel());

        //商户号
        paymentOrder.setMerchantCode(depositApplyReq.getMerchantCode());

        // 商户代收订单号
        paymentOrder.setMerchantCollectionOrder(depositApplyReq.getMerchantTradeNo());

        //代收订单号
        paymentOrder.setMerchantOrder(platformOrder);

        //平台订单号
        paymentOrder.setPlatformOrder(mcOrderNo);

        //upiID
        paymentOrder.setUpiId(kycPartners.getUpiId());

        //会员id
        paymentOrder.setMemberId(String.valueOf(memberInfo.getId()));

        //会员账号
        paymentOrder.setMemberAccount(memberInfo.getMemberAccount());

        //真实姓名
        paymentOrder.setRealName(memberInfo.getRealName());

        //手机号
        paymentOrder.setMobileNumber(memberInfo.getMobileNumber());

        //订单金额
        paymentOrder.setAmount(new BigDecimal(depositApplyReq.getAmount()));

        //真实金额
        paymentOrder.setActualAmount(new BigDecimal(depositApplyReq.getAmount()));

        //订单状态 待支付
        paymentOrder.setOrderStatus(OrderStatusEnum.BE_PAID.getCode());

        //商户名称
        paymentOrder.setMerchantName(merchantInfo.getUsername());

        //时间戳
        paymentOrder.setTimestamp(depositApplyReq.getTimestamp());

        //请求ip
        paymentOrder.setClientIp(requestIp);

        //币种 从订单传值获取
        paymentOrder.setCurrency(depositApplyReq.getCurrency());

        //汇率
        paymentOrder.setExchangeRates(new BigDecimal(1));

        //iToken 订单金额 * 费率
        paymentOrder.setItokenNumber(paymentOrder.getAmount().multiply(paymentOrder.getExchangeRates()));

        //会员类型 1: 内部会员, 2: 外部会员
        paymentOrder.setMemberType(Integer.valueOf(memberInfo.getMemberType()));

        //查看是否配置了卖出奖励
        if (memberInfo.getSellBonusProportion() != null && memberInfo.getSellBonusProportion().compareTo(new BigDecimal(0)) > 0) {
            //奖励
            paymentOrder.setBonus(paymentOrder.getAmount().multiply((new BigDecimal(memberInfo.getSellBonusProportion().toString()).divide(BigDecimal.valueOf(100)))));
        }

        //匹配时间 (当前时间)
        paymentOrder.setMatchTime(LocalDateTime.now());

        //iToken数量 (实际数量) 目前只有印度币种 所以直接 1.1赋值
        paymentOrder.setItokenNumber(new BigDecimal(depositApplyReq.getAmount()));

        paymentOrder.setCurrency(CurrenceEnum.INDIA.getCode());

        //kycID
        paymentOrder.setKycId(String.valueOf(kycPartners.getId()));

        //upiName
        paymentOrder.setUpiName(kycPartners.getBankName());

        return paymentOrderService.save(paymentOrder);
    }


    /**
     * 生成代收订单
     *
     * @return boolean
     */
    private boolean createMerchantCollectOrders(String platformOrder, DepositApplyReq depositApplyReq, String mcOrderNo, BigDecimal amount, MemberInfo memberInfo, MerchantInfo merchantInfo, String requestIp, BigDecimal rates, BigDecimal fixedFee, KycPartners kycPartners) {
        MerchantCollectOrders merchantCollectOrders = new MerchantCollectOrders();

        //生成平台订单号
        merchantCollectOrders.setPlatformOrder(platformOrder);

        merchantCollectOrders.setVersion(1);

        //商户订单号
        merchantCollectOrders.setMerchantOrder(depositApplyReq.getMerchantTradeNo());

        //卖出订单号
        merchantCollectOrders.setSellOrderNo(mcOrderNo);

        //设置支付类型
        merchantCollectOrders.setPayType(depositApplyReq.getChannel());

        //商户号
        merchantCollectOrders.setMerchantCode(depositApplyReq.getMerchantCode());

        //实际金额
        merchantCollectOrders.setAmount(amount);

        //订单金额
        merchantCollectOrders.setOrderAmount(amount);

        //设置会员ID
        merchantCollectOrders.setMemberId(String.valueOf(memberInfo.getId()));

        //设置商户会员ID 商户号+商户会员id
        merchantCollectOrders.setExternalMemberId(merchantInfo.getCode() + depositApplyReq.getMemberId());

        //交易回调地址
        merchantCollectOrders.setTradeNotifyUrl(depositApplyReq.getNotifyUrl());

        //设置时间戳
        merchantCollectOrders.setTimestamp(depositApplyReq.getTimestamp());

        //币种
        merchantCollectOrders.setCurrency(depositApplyReq.getCurrency());

        //设置代收订单费率
        merchantCollectOrders.setOrderRate(rates);

        //upiId
        merchantCollectOrders.setUpiId(kycPartners.getUpiId());

        //upiName
        merchantCollectOrders.setUpiName(kycPartners.getBankName());
        //订单费用 默认为0
        BigDecimal cost = BigDecimal.ZERO;

        //代收费率大于0才计算费用
        if (rates != null && rates.compareTo(BigDecimal.ZERO) > 0) {
            //订单费用
            cost = merchantCollectOrders.getAmount().multiply((rates.divide(BigDecimal.valueOf(100))));
        }

        //设置费用 订单金额 * 费率)
        merchantCollectOrders.setCost(cost);

        //客户端ip
        merchantCollectOrders.setClientIp(requestIp);

        //商户名称
        merchantCollectOrders.setMerchantName(merchantInfo.getUsername());

        //商户类型
        merchantCollectOrders.setMerchantType(merchantInfo.getMerchantType());

        //币种 从商户信息获取 如果是USDT订单币种要写USDT
        merchantCollectOrders.setCurrency(merchantInfo.getCurrency());

        //汇率 1
        merchantCollectOrders.setExchangeRates(new BigDecimal(1));

        //itoken数量 订单金额 * 汇率
        merchantCollectOrders.setItokenNumber(merchantCollectOrders.getAmount().multiply(merchantCollectOrders.getExchangeRates()));

        //单笔手续费
        merchantCollectOrders.setFixedFee(fixedFee);

        //kycID
        merchantCollectOrders.setKycId(String.valueOf(kycPartners.getId()));

        //同步回调地址
        merchantCollectOrders.setSyncNotifyAddress(depositApplyReq.getSyncNotifyAddress());

        return merchantCollectOrdersService.save(merchantCollectOrders);
    }


    /**
     * 生成订单token并存储支付信息到Redis
     *
     * @param paymentInfo
     * @param duration
     * @return {@link String}
     */
    public String createPaymentToken(PaymentInfo paymentInfo, long duration) {
        String token = generateTokenPayment("payment" + paymentInfo.getMerchantCode() + paymentInfo.getMemberId(), duration, arProperty.getSecretKey());
        redisTemplate.opsForValue().set(token, paymentInfo, duration, TimeUnit.MILLISECONDS);
        return token;
    }


    /**
     * 校验请求
     *
     * @param apiRequest           请求对象
     * @param requestIp            请求IP
     * @param merchantInfo         商户信息
     * @param merchantPublicKeyStr 商户公钥
     * @param apiName
     * @return ApiResponse对象，如果参数有效则为null
     */
    private ApiResponse validateRequest(ApiRequest apiRequest, String requestIp, MerchantInfo merchantInfo, String merchantPublicKeyStr, String apiName) {

        if (apiRequest == null || StringUtils.isEmpty(apiRequest.getMerchantCode()) || StringUtils.isEmpty(apiRequest.getEncryptedData()) || StringUtils.isEmpty(apiRequest.getEncryptedKey())) {
            log.error(apiName + "失败, 请求参数错误, 请求参数: {}, 请求ip: {}", apiRequest, requestIp);
            return ApiResponse.of(ApiResponseEnum.PARAM_VALID_FAIL, null);
        }

        if (merchantInfo == null || StringUtils.isEmpty(merchantInfo.getCode())) {
            log.error(apiName + "失败, 商户号不存在 请求参数: {}, 商户信息: {}", apiRequest, merchantInfo);
            return ApiResponse.of(ApiResponseEnum.INVALID_REQUEST, null);
        }

        //校验ip
        if (!IpUtil.validateClientIp(requestIp, merchantInfo.getApiAllowedIps())) {
            log.error(apiName + "失败, ip校验失败: 请求ip: {}, 商户信息: {}", requestIp, merchantInfo);
            return ApiResponse.of(ApiResponseEnum.INVALID_IP, null);
        }

        if (StringUtils.isEmpty(merchantPublicKeyStr)) {
            log.error(apiName + "失败, 获取商户公钥失败: 请求ip: {}, 商户信息: {}", requestIp, merchantInfo);
            return ApiResponse.of(ApiResponseEnum.INVALID_MERCHANT_PUBLIC_KEY, null);
        }

        return null;
    }

    public String generateTokenPayment(String subject, long ttlMillis, String secretKey) {

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        return Jwts.builder().setId(UUID.randomUUID().toString()) // 使用UUID作为JTI
                .setSubject(subject).setIssuedAt(now).setExpiration(new Date(nowMillis + ttlMillis)).signWith(SignatureAlgorithm.HS256, secretKey).compact();
    }

}
