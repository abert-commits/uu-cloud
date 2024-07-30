package org.uu.wallet.strategy.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
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
import org.tron.trident.core.key.KeyPair;
import org.uu.common.core.result.ApiResponse;
import org.uu.common.core.result.ApiResponseEnum;
import org.uu.common.redis.constants.RedisKeys;
import org.uu.common.redis.util.RedissonUtil;
import org.uu.wallet.Enum.CollectionOrderStatusEnum;
import org.uu.wallet.Enum.TaskTypeEnum;
import org.uu.wallet.bo.UsdtPaymentInfoBO;
import org.uu.wallet.entity.*;
import org.uu.wallet.property.ArProperty;
import org.uu.wallet.rabbitmq.RabbitMQService;
import org.uu.wallet.req.ApiRequest;
import org.uu.wallet.req.DepositApplyReq;
import org.uu.wallet.service.*;
import org.uu.wallet.strategy.CollectionStrategy;
import org.uu.wallet.tron.utils.RSAUtils;
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
import java.math.RoundingMode;
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
public class UsdtCollectionStrategy implements CollectionStrategy {


    private final IMerchantInfoService merchantInfoService;
    private final Validator validator;
    private final ArProperty arProperty;
    private final RedisTemplate redisTemplate;
    private final RabbitMQService rabbitMQService;
    private final IMerchantCollectOrdersService merchantCollectOrdersService;
    private final RedissonUtil redissonUtil;
    private final OrderNumberGeneratorUtil orderNumberGenerator;

    @Autowired
    private IMerchantRatesConfigService merchantRatesConfigService;

    @Autowired
    private ITronAddressService tronAddressService;

    @Autowired
    private ISystemCurrencyService systemCurrencyService;

    @Autowired
    private ITradeConfigService tradeConfigService;

    /**
     * USDT代收订单处理
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
        log.info("USDT-API充值接口, 商户号: {}, 请求IP: {}", apiRequest.getMerchantCode(), requestIp);

        //获取商户信息
        MerchantInfo merchantInfo = merchantInfoService.getMerchantInfoByCode(apiRequest.getMerchantCode());

        String merchantPublicKeyStr = null;
        //获取商户公钥
        if (merchantInfo != null) {
            merchantPublicKeyStr = merchantInfo.getMerchantPublicKey();
        }

        //校验请求
        ApiResponse apiResponse = validateRequest(apiRequest, requestIp, merchantInfo, merchantPublicKeyStr, "USDT-API充值接口");
        if (apiResponse != null) {
            return apiResponse;
        }

        //分布式锁key ar-wallet-usdtProcessCollection
        String key = "uu-wallet-usdtProcessCollection";
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
                    log.error("USDT-API充值接口处理失败, 参数校验失败: 请求ip: {}, req: {}, 商户信息: {}", requestIp, apiRequest, merchantInfo);
                    return ApiResponse.of(ApiResponseEnum.PARAM_VALID_FAIL, null);
                }

                //手动调用验证明文参数
                Set<ConstraintViolation<DepositApplyReq>> violations = validator.validate(depositApplyReq);
                if (!violations.isEmpty()) {
                    // 处理验证错误
                    for (ConstraintViolation<DepositApplyReq> violation : violations) {
                        log.error("USDT-API充值接口处理失败, 参数校验失败: 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}", requestIp, apiRequest, merchantInfo, depositApplyReq);
                        System.out.println(violation.getMessage());
                        return ApiResponse.ofMsg(ApiResponseEnum.PARAM_VALID_FAIL, violation.getMessage(), null);
                    }
                }

                //校验支付类型是否一致
                if (!apiRequest.getChannel().equals(depositApplyReq.getChannel())) {
                    log.error("USDT-API充值接口处理失败, 支付类型不一致: 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}", requestIp, apiRequest, merchantInfo, depositApplyReq);
                    return ApiResponse.of(ApiResponseEnum.PARAM_VALID_FAIL, null);
                }

                //使用商户公钥验证签名
                if (!RsaUtil.verifySignature(depositApplyReq, depositApplyReq.getSign(), merchantPublicKey)) {
                    log.error("USDT-API充值接口处理失败, 签名校验失败: 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}", requestIp, apiRequest, merchantInfo, depositApplyReq);
                    return ApiResponse.of(ApiResponseEnum.SIGNATURE_ERROR, null);
                }

                //查询当前商户的支付类型配置
                MerchantRatesConfig merchantRatesConfig = merchantRatesConfigService.getMerchantRatesConfigByCode("1", depositApplyReq.getChannel(), merchantInfo.getCode());

                //如果不存在对应的支付类型配置 驳回
                if (merchantRatesConfig == null) {
                    log.error("USDT-API充值接口处理失败, 不存在对应的支付类型配置: 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}", requestIp, apiRequest, merchantInfo, depositApplyReq);
                    return ApiResponse.of(ApiResponseEnum.UNSUPPORTED_PAY_TYPE, null);
                }

                //订单金额
                BigDecimal amount = new BigDecimal(depositApplyReq.getAmount());

                //校验订单 金额是否小于1
                if (amount.compareTo(BigDecimal.ONE) < 0) {
                    log.error("USDT-API充值接口处理失败, 金额小于1: 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}, 订单金额: {}", requestIp, apiRequest, merchantInfo, depositApplyReq, amount);
                    return ApiResponse.of(ApiResponseEnum.AMOUNT_EXCEEDS_LIMIT, null);
                }

                //是否配置最小金额
                boolean isMinCostConfigured = merchantRatesConfig.getMoneyMin() != null && merchantRatesConfig.getMoneyMin().compareTo(BigDecimal.ZERO) > 0;
                //是否配置最大金额
                boolean isMaxCostConfigured = merchantRatesConfig.getMoneyMax() != null && merchantRatesConfig.getMoneyMax().compareTo(BigDecimal.ZERO) > 0;

                boolean isAmountGreaterThanMin = isMinCostConfigured ? amount.compareTo(merchantRatesConfig.getMoneyMin()) >= 0 : true;
                boolean isAmountLessThanMax = isMaxCostConfigured ? amount.compareTo(merchantRatesConfig.getMoneyMax()) <= 0 : true;

                boolean isBetween = isAmountGreaterThanMin && isAmountLessThanMax;

                //校验最小金额和最大金额
                if (!isBetween) {
                    //订单金额不在最小金额和最大金额之间
                    log.error("USDT-API充值接口处理失败, 金额超过限制: 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}", requestIp, apiRequest, merchantInfo, depositApplyReq);
                    return ApiResponse.of(ApiResponseEnum.AMOUNT_EXCEEDS_LIMIT, null);
                }

                //判断商户代收状态
                if (merchantInfo.getRechargeStatus().equals("0")) {
                    //当前商户代收状态未开启
                    log.error("USDT-API充值接口处理失败, 当前商户代收状态未开启: 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}", requestIp, apiRequest, merchantInfo, depositApplyReq);
                    return ApiResponse.of(ApiResponseEnum.MERCHANT_COLLECTION_STATUS_DISABLED, null);
                }

                //费率
                BigDecimal rates = merchantRatesConfig.getRates();
                //固定手续费
                BigDecimal fixedFee = merchantRatesConfig.getFixedFee();

                //生成USDT订单 如果用户不存在地址 那么为用户创建一个地址

                //查询用户波场钱包信息 如果不存在波场钱包的话就创建一个(每个用户对应一个波场钱包地址)
                TronAddress tronAddress = tronAddressService.getTronAddressByMerchanIdtAndUserId(merchantInfo.getCode(), depositApplyReq.getMerchantCode() + depositApplyReq.getMemberId());

                if (tronAddress == null) {

                    //为该用户创建一个波场钱包
                    tronAddress = new TronAddress();

                    //商户号
                    tronAddress.setMerchantId(merchantInfo.getCode());

                    //商户名称
                    tronAddress.setMerchantName(merchantInfo.getUsername());

                    //会员id
                    tronAddress.setMemberId(depositApplyReq.getMerchantCode() + depositApplyReq.getMemberId());

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

                //获取配置信息
                TradeConfig tradeConfig = tradeConfigService.getById(1);


                //生成代收订单
                //代收平台订单号
                String platformOrder = orderNumberGenerator.generateOrderNo("P");

                //生成 商户代收订单
                boolean createMerchantCollectOrder = createMerchantCollectOrders(platformOrder, depositApplyReq, amount, merchantInfo, requestIp, rates, fixedFee, tronAddress);

                if (createMerchantCollectOrder) {
                    //提交成功

                    //USDT订单页面信息
                    UsdtPaymentInfoBO usdtPaymentInfoBO = UsdtPaymentInfoBO.builder()
                            .usdtAddr(tronAddress.getAddress())
                            .networkProtocol("TRC-20")
                            .merchantCode(merchantInfo.getCode())
                            .merchantName(merchantInfo.getUsername())
                            .memberId(depositApplyReq.getMemberId())
                            .paymentExpireTime(-1L)
                            .amount(amount)
                            .merchantOrder(depositApplyReq.getMerchantTradeNo())
                            .platformOrder(platformOrder)
                            .createTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                            .orderStatus(CollectionOrderStatusEnum.BE_PAID.getCode())
                            .minimumAmount(merchantRatesConfig.getMoneyMin())//最低充值金额
                            .build();

                    //生成订单token
                    String usdtPaymentToken = createPaymentToken(usdtPaymentInfoBO, TimeUnit.MINUTES.toMillis(arProperty.getUsdtPaymentPageExpirationTime()));

                    //返回数据
                    DepositApplyVo depositApplyVo = new DepositApplyVo();

                    //支付地址
                    depositApplyVo.setPayUrl(arProperty.getUsdtPayUrl() + "?token=" + usdtPaymentToken);

                    //订单token
                    depositApplyVo.setToken(usdtPaymentToken);

                    //商户号
                    depositApplyVo.setMerchantCode(merchantInfo.getCode());

                    //会员id
                    depositApplyVo.setMemberId(depositApplyReq.getMemberId());

                    //平台订单号
                    depositApplyVo.setTradeNo(platformOrder);

                    //商户订单号
                    depositApplyVo.setMerchantTradeNo(depositApplyReq.getMerchantTradeNo());

                    //订单有效期
                    depositApplyVo.setOrderValidityDuration(arProperty.getUsdtPaymentPageExpirationTime() * 60);

                    //签名并加密数据
                    EncryptedData encryptedData = RsaUtil.signAndEncryptData(depositApplyVo, platformPrivateKey, merchantPublicKey);

                    ApiResponseVo apiResponseVo = new ApiResponseVo();
                    BeanUtils.copyProperties(encryptedData, apiResponseVo);
                    apiResponseVo.setMerchantCode(merchantInfo.getCode());

                    log.info("USDT-API充值接口订单提交成功, 请求ip: {}, 请求明文: {}, 返回明文: {}", requestIp, depositApplyReq, depositApplyVo);

                    //注册事务同步回调, 事务提交成功后, 发送延时MQ 改变订单为超时状态
                    TronAddress finalTronAddress = tronAddress;
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

                            // 将USDT收款地址存入Redis，设置过期时间为一个月（30天）
                            long expirationTime = 30L * 24 * 60 * 60; // 30天对应的秒数
                            redisTemplate.opsForValue().set(RedisKeys.PENDING_USDT_ADDRESS + finalTronAddress.getAddress(), finalTronAddress.getAddress(), expirationTime, TimeUnit.SECONDS);
                        }
                    });
                    return ApiResponse.of(ApiResponseEnum.SUCCESS, apiResponseVo);
                } else {
                    //提交失败
                    log.error("USDT-API充值接口订单提交失败, 请求ip: {}, 请求明文: {}, 请求密文: {}", requestIp, depositApplyReq, apiRequest);
                    return ApiResponse.of(ApiResponseEnum.SYSTEM_EXECUTION_ERROR, null);
                }
            } else {
                //没获取到锁 直接返回操作频繁
                log.error("USDT-API充值接口订单提交失败, 获取分布式锁失败, 请求ip: {}, 请求密文: {}", requestIp, apiRequest);
                return ApiResponse.of(ApiResponseEnum.TOO_FREQUENT, null);
            }
        } catch (DataIntegrityViolationException e) {
            //手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("USDT-API充值接口订单提交失败, 数据重复 e: {}", e.getMessage());
            return ApiResponse.of(ApiResponseEnum.DATA_DUPLICATE_SUBMISSION, null);
        } catch (BadPaddingException e) {
            //手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("USDT-API充值接口订单提交失败, 解密失败，无效的密文或密钥错误 e: {}", e.getMessage());
            return ApiResponse.of(ApiResponseEnum.DECRYPTION_ERROR, null);
        } catch (Exception e) {
            //手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("USDT-API充值接口订单提交失败 req: {}, e: {}", apiRequest, e);
        } finally {
            //释放锁
            if (req && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }

        return ApiResponse.of(ApiResponseEnum.SYSTEM_EXECUTION_ERROR, null);
    }

    /**
     * 生成代收订单
     *
     * @param platformOrder
     * @param depositApplyReq
     * @param amount
     * @param merchantInfo
     * @param requestIp
     * @param rates
     * @param fixedFee
     * @return boolean
     */
    private boolean createMerchantCollectOrders(String platformOrder, DepositApplyReq depositApplyReq, BigDecimal amount, MerchantInfo merchantInfo, String requestIp, BigDecimal rates, BigDecimal fixedFee, TronAddress tronAddress) {
        MerchantCollectOrders merchantCollectOrders = new MerchantCollectOrders();

        //生成平台订单号
        merchantCollectOrders.setPlatformOrder(platformOrder);

        //USDT充值地址
        merchantCollectOrders.setUsdtAddr(tronAddress.getAddress());

        //商户订单号
        merchantCollectOrders.setMerchantOrder(depositApplyReq.getMerchantTradeNo());

        //订单版本号
        merchantCollectOrders.setVersion(1);

        //设置支付类型
        merchantCollectOrders.setPayType(depositApplyReq.getChannel());

        //商户号
        merchantCollectOrders.setMerchantCode(depositApplyReq.getMerchantCode());

        //实际金额
        merchantCollectOrders.setAmount(amount);

        //订单金额
        merchantCollectOrders.setOrderAmount(amount);

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

        //汇率 从实时汇率接口获取 根据币种汇率来计算 USDT订单汇率默认为0 因为不涉及到汇率转换
        merchantCollectOrders.setExchangeRates(BigDecimal.ZERO);

        //itoken数量 订单金额 * 汇率 保留两位小数 舍弃后面的小数 USDT订单iToken默认为0 因为不涉及到汇率转换
        merchantCollectOrders.setItokenNumber(BigDecimal.ZERO);

        //单笔手续费
        merchantCollectOrders.setFixedFee(fixedFee);

        //同步回调地址
        merchantCollectOrders.setSyncNotifyAddress(depositApplyReq.getSyncNotifyAddress());

        return merchantCollectOrdersService.save(merchantCollectOrders);
    }


    /**
     * 生成订单token并存储支付信息到Redis
     *
     * @param usdtPaymentInfoBO
     * @param duration
     * @return {@link String}
     */
    public String createPaymentToken(UsdtPaymentInfoBO usdtPaymentInfoBO, long duration) {
        String token = generateTokenPayment("usdtPayment" + usdtPaymentInfoBO.getMerchantCode() + usdtPaymentInfoBO.getMemberId(), duration, arProperty.getSecretKey());
        redisTemplate.opsForValue().set(token, usdtPaymentInfoBO, duration, TimeUnit.MILLISECONDS);
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
