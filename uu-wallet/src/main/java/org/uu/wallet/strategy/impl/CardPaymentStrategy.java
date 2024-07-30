package org.uu.wallet.strategy.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
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
import org.uu.wallet.Enum.PaymentOrderStatusEnum;
import org.uu.wallet.entity.*;
import org.uu.wallet.mapper.MerchantInfoMapper;
import org.uu.wallet.property.ArProperty;
import org.uu.wallet.rabbitmq.RabbitMQService;
import org.uu.wallet.req.ApiRequest;
import org.uu.wallet.req.WithdrawalApplyReq;
import org.uu.wallet.service.IMerchantInfoService;
import org.uu.wallet.service.IMerchantPaymentOrdersService;
import org.uu.wallet.service.IMerchantRatesConfigService;
import org.uu.wallet.strategy.PaymentStrategy;
import org.uu.wallet.util.IpUtil;
import org.uu.wallet.util.OrderNumberGeneratorUtil;
import org.uu.wallet.util.RedisUtil;
import org.uu.wallet.util.RsaUtil;
import org.uu.wallet.vo.ApiResponseVo;
import org.uu.wallet.vo.BuyListVo;
import org.uu.wallet.vo.WithdrawalApplyVo;
import org.uu.wallet.webSocket.MemberSendAmountList;

import javax.crypto.BadPaddingException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.math.BigDecimal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class CardPaymentStrategy implements PaymentStrategy {

    @Autowired
    private IMerchantInfoService merchantInfoService;

    @Autowired
    private MerchantInfoMapper merchantInfoMapper;

    @Autowired
    private ArProperty arProperty;

    private final Validator validator;
    private final RedissonUtil redissonUtil;

    @Autowired
    private RabbitMQService rabbitMQService;

    private final OrderNumberGeneratorUtil orderNumberGenerator;

    @Autowired
    private IMerchantPaymentOrdersService merchantPaymentOrdersService;

    private final RedisUtil redisUtil;

    @Autowired
    private MemberSendAmountList memberSendAmountList;

    @Autowired
    private IMerchantRatesConfigService merchantRatesConfigService;



    @Override
    @Transactional
    public ApiResponse processPayment(ApiRequest apiRequest, HttpServletRequest request) {


        try {
            //获取请求IP
            String requestIp = IpUtil.getRealIP(request);
            log.info("银行卡-API提现接口, 商户号: {}, 请求IP: {}", apiRequest.getMerchantCode(), requestIp);

            //获取商户信息 加上排他行锁
            MerchantInfo merchantInfo = merchantInfoMapper.selectMerchantInfoForUpdate(apiRequest.getMerchantCode());

            String merchantPublicKeyStr = null;
            //获取商户公钥
            if (merchantInfo != null) {
                merchantPublicKeyStr = merchantInfo.getMerchantPublicKey();
            }

            //校验请求
            ApiResponse apiResponse = validateRequest(apiRequest, requestIp, merchantInfo, merchantPublicKeyStr, "银行卡-API提现接口");

            if (apiResponse != null) {
                return apiResponse;
            }

            try {
                //商户公钥
                PublicKey merchantPublicKey = RsaUtil.getPublicKeyFromString(merchantPublicKeyStr);

                //平台私钥
                PrivateKey platformPrivateKey = RsaUtil.getPrivateKeyFromString(arProperty.getPrivateKey());

                //使用平台私钥解密数据
                WithdrawalApplyReq withdrawalApplyReq = RsaUtil.decryptData(apiRequest.getEncryptedKey(), apiRequest.getEncryptedData(), platformPrivateKey, WithdrawalApplyReq.class);

                if (withdrawalApplyReq == null) {
                    log.error("银行卡-API提现接口处理失败, 参数校验失败: 请求ip: {}, req: {}, 商户信息: {}", requestIp, apiRequest, merchantInfo);
                    return ApiResponse.of(ApiResponseEnum.PARAM_VALID_FAIL, null);
                }

                //手动调用验证明文参数
                Set<ConstraintViolation<WithdrawalApplyReq>> violations = validator.validate(withdrawalApplyReq);
                if (!violations.isEmpty()) {
                    // 处理验证错误
                    for (ConstraintViolation<WithdrawalApplyReq> violation : violations) {
                        log.error("银行卡-API提现接口处理失败, 参数校验失败: 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}", requestIp, apiRequest, merchantInfo, withdrawalApplyReq);
                        System.out.println(violation.getMessage());
                        return ApiResponse.ofMsg(ApiResponseEnum.PARAM_VALID_FAIL, violation.getMessage(), null);
                    }
                }

                //校验支付类型是否一致
                if (!apiRequest.getChannel().equals(withdrawalApplyReq.getChannel())) {
                    log.error("银行卡-API提现接口处理失败, 支付类型不一致: 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}", requestIp, apiRequest, merchantInfo, withdrawalApplyReq);
                    return ApiResponse.of(ApiResponseEnum.PARAM_VALID_FAIL, null);
                }

                //手动校验参数 银行卡信息
                if (StringUtils.isBlank(withdrawalApplyReq.getBankCardNumber())
                        || StringUtils.isBlank(withdrawalApplyReq.getBankName())
                        || StringUtils.isBlank(withdrawalApplyReq.getIfscCode())
                        || StringUtils.isBlank(withdrawalApplyReq.getBankCardOwner())) {
                    log.error("银行卡-API提现接口处理失败, 参数校验失败: 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}", requestIp, apiRequest, merchantInfo, withdrawalApplyReq);
                    return ApiResponse.of(ApiResponseEnum.PARAM_VALID_FAIL, null);
                }

                //使用商户公钥验证签名
                if (!RsaUtil.verifySignature(withdrawalApplyReq, withdrawalApplyReq.getSign(), merchantPublicKey)) {
                    log.error("银行卡-API提现接口处理失败, 签名校验失败: 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}", requestIp, apiRequest, merchantInfo, withdrawalApplyReq);
                    return ApiResponse.of(ApiResponseEnum.SIGNATURE_ERROR, null);
                }

                //处理业务
                BigDecimal amount = new BigDecimal(withdrawalApplyReq.getAmount());

                //校验订单 金额是否小于1
                if (amount.compareTo(BigDecimal.ONE) < 0) {
                    log.error("银行卡-API提现接口处理失败, 金额小于1: 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}, 订单金额: {}", requestIp, apiRequest, merchantInfo, withdrawalApplyReq, amount);
                    return ApiResponse.of(ApiResponseEnum.AMOUNT_EXCEEDS_LIMIT, null);
                }

                //判断商户代付状态
                if (merchantInfo.getWithdrawalStatus().equals("0")) {
                    //当前商户代付状态未开启
                    log.error("银行卡-API提现接口处理失败, 当前商户代付状态未开启: 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}", requestIp, apiRequest, merchantInfo, withdrawalApplyReq);
                    return ApiResponse.of(ApiResponseEnum.MERCHANT_PAYMENT_STATUS_DISABLED, null);
                }

                //查询当前商户的支付类型配置
                MerchantRatesConfig merchantRatesConfig = merchantRatesConfigService.getMerchantRatesConfigByCode("2", withdrawalApplyReq.getChannel(), merchantInfo.getCode());

                //如果不存在对应的支付类型配置 驳回
                if (merchantRatesConfig == null) {
                    log.error("银行卡-API提现接口处理失败, 不存在对应的支付类型配置: 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}", requestIp, apiRequest, merchantInfo, withdrawalApplyReq);
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
                    log.error("银行卡-API提现接口处理失败, 金额超过限制: 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}", requestIp, apiRequest, merchantInfo, withdrawalApplyReq);
                    return ApiResponse.of(ApiResponseEnum.AMOUNT_EXCEEDS_LIMIT, null);
                }

                //分布式锁key ar-wallet-sell+商户号+商户会员id
                String key2 = "ar-wallet-sell" + withdrawalApplyReq.getMerchantCode() + withdrawalApplyReq.getMemberId();
                RLock lock2 = redissonUtil.getLock(key2);

                boolean req2 = false;

                try {
                    req2 = lock2.tryLock(10, TimeUnit.SECONDS);

                    if (req2) {

                        //订单费用 默认为0
                        BigDecimal cost = BigDecimal.ZERO;

                        //订单费率
                        BigDecimal rates = merchantRatesConfig.getRates();

                        //固定手续费
                        BigDecimal fixedFee = merchantRatesConfig.getFixedFee();


                        //判断如果代付费率 大于0才计算费率
                        if (rates != null && rates.compareTo(BigDecimal.ZERO) > 0) {
                            //订单费用
                            cost = amount.multiply((rates.divide(BigDecimal.valueOf(100))));
                        }

                        //订单金额 + 订单费用 + 固定手续费
                        BigDecimal amountCost = amount.add(cost).add(fixedFee);

                        //校验商户余额是否足够
                        if (merchantInfo.getBalance().compareTo(amountCost) < 0) {
                            log.error("银行卡-API提现接口处理失败, 商户余额不足: 订单金额(包括费率): {}, 商户余额: {}, 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}", amountCost, merchantInfo.getBalance(), requestIp, apiRequest, merchantInfo, withdrawalApplyReq);
                            return ApiResponse.of(ApiResponseEnum.INSUFFICIENT_MERCHANT_BALANCE, null);
                        }

                        //提现订单申请
                        OrderEventReq orderEventReq = new OrderEventReq();
                        orderEventReq.setEventId(OrderEventEnum.MERCHANT_PAYMENT_ORDER_APPLICATION.getCode());
                        orderEventReq.setParams("");
                        //发送事件MQ
                        rabbitMQService.sendStatisticProcess(orderEventReq);

                        //商户日报表统计MQ
                        rabbitMQService.sendMerchantDailyProcess(orderEventReq);

                        //订单金额总计 (订单金额 + 费用 + 单笔手续费)
                        BigDecimal allAmount = amount.add(cost).add(fixedFee);

                        //更新商户余额 将订单金额所需费用划转到交易中金额
                        LambdaUpdateWrapper<MerchantInfo> lambdaUpdateWrapperMerchantInfo = new LambdaUpdateWrapper<>();
                        lambdaUpdateWrapperMerchantInfo.eq(MerchantInfo::getCode, merchantInfo.getCode())  // 指定更新条件 商户号
                                .set(MerchantInfo::getBalance, merchantInfo.getBalance().subtract(allAmount)) // 指定更新字段 (减少商户余额 - 总金额)
                                .set(MerchantInfo::getPendingBalance, merchantInfo.getPendingBalance().add(allAmount)); // 指定更新字段 (增加交易中金额 + 总金额)
                        // 这里传入的 null 表示不更新实体对象的其他字段
                        merchantInfoService.update(null, lambdaUpdateWrapperMerchantInfo);

                        //生成商户代付订单
                        MerchantPaymentOrders merchantPaymentOrders = new MerchantPaymentOrders();

                        //订单状态 待匹配
                        merchantPaymentOrders.setOrderStatus(PaymentOrderStatusEnum.BE_MATCHED.getCode());

                        //如果订单金额达到阈值, 那么将订单状态改为待审核
                        if (amount.compareTo(merchantRatesConfig.getPaymentReminderAmount()) >= 0) {
                            merchantPaymentOrders.setOrderStatus(PaymentOrderStatusEnum.TO_BE_REVIEWED.getCode());
                        }

                        //设置商户号
                        merchantPaymentOrders.setMerchantCode(withdrawalApplyReq.getMerchantCode());

                        //设置商户会员id
                        merchantPaymentOrders.setExternalMemberId(withdrawalApplyReq.getMerchantCode() + withdrawalApplyReq.getMemberId());

                        //设置平台订单号
                        String platformOrder = orderNumberGenerator.generateOrderNo("W");
                        merchantPaymentOrders.setPlatformOrder(platformOrder);

                        //设置商户订单号
                        merchantPaymentOrders.setMerchantOrder(withdrawalApplyReq.getMerchantTradeNo());

                        //设置实际金额
                        merchantPaymentOrders.setAmount(amount);

                        //设置订单金额
                        merchantPaymentOrders.setOrderAmount(amount);

                        //设置渠道编码 (支付方式)
                        merchantPaymentOrders.setPayType(withdrawalApplyReq.getChannel());

                        //币种
                        merchantPaymentOrders.setCurrency(withdrawalApplyReq.getCurrency());

                        //设置时间戳
                        merchantPaymentOrders.setTimestamp(withdrawalApplyReq.getTimestamp());

                        //设置交易回调地址
                        merchantPaymentOrders.setTradeNotifyUrl(withdrawalApplyReq.getNotifyUrl());

                        //设置订单费率 (代付费率)
                        merchantPaymentOrders.setOrderRate(rates);

                        //设置费用 订单金额 * 费率)
                        merchantPaymentOrders.setCost(cost);

                        //客户端ip
                        merchantPaymentOrders.setClientIp(requestIp);

                        //商户名称
                        merchantPaymentOrders.setMerchantName(merchantInfo.getUsername());

                        //银行卡号
                        merchantPaymentOrders.setBankCardNumber(withdrawalApplyReq.getBankCardNumber());

                        //银行名称
                        merchantPaymentOrders.setBankName(withdrawalApplyReq.getBankName());

                        //ifsCode
                        merchantPaymentOrders.setIfscCode(withdrawalApplyReq.getIfscCode());

                        //持卡人姓名
                        merchantPaymentOrders.setBankCardOwner(withdrawalApplyReq.getBankCardOwner());

                        //币种 从商户信息获取 如果是USDT那么要写USDT
                        merchantPaymentOrders.setCurrency(merchantInfo.getCurrency());

                        //汇率 固定1
                        merchantPaymentOrders.setExchangeRates(new BigDecimal(1));

                        //固定手续费
                        merchantPaymentOrders.setFixedFee(fixedFee);

                        //iToken 订单金额 * 汇率
                        merchantPaymentOrders.setItokenNumber(merchantPaymentOrders.getAmount().multiply(merchantPaymentOrders.getExchangeRates()));

                        //保存代付订单
                        boolean save = merchantPaymentOrdersService.save(merchantPaymentOrders);

                        if (!save) {
                            log.error("银行卡-API提现接口处理失败: 生成商户代付订单失败，触发事务回滚。 订单信息: {}, req: {}", merchantPaymentOrders, withdrawalApplyReq);
                            // 抛出运行时异常
                            throw new RuntimeException("银行卡-API提现接口处理失败: 生成商户代付订单失败，触发事务回滚。");
                        }


                        if (save) {
                            //提交成功

                            //返回数据
                            WithdrawalApplyVo withdrawalApplyVo = new WithdrawalApplyVo();

                            //商户号
                            withdrawalApplyVo.setMerchantCode(merchantInfo.getCode());

                            //会员id
                            withdrawalApplyVo.setMemberId(withdrawalApplyReq.getMemberId());

                            //平台订单号
                            withdrawalApplyVo.setTradeNo(platformOrder);

                            //商户订单号
                            withdrawalApplyVo.setMerchantTradeNo(withdrawalApplyReq.getMerchantTradeNo());

                            //订单金额
                            withdrawalApplyVo.setAmount(amount);

                            //交易状态 (默认代付中)
                            withdrawalApplyVo.setTradeStatus(merchantPaymentOrders.getOrderStatus());

                            //时间戳
                            withdrawalApplyVo.setTimestamp(String.valueOf(System.currentTimeMillis() / 1000));

                            //签名并加密数据
                            EncryptedData encryptedData = RsaUtil.signAndEncryptData(withdrawalApplyVo, platformPrivateKey, merchantPublicKey);

                            ApiResponseVo apiResponseVo = new ApiResponseVo();
                            BeanUtils.copyProperties(encryptedData, apiResponseVo);
                            apiResponseVo.setMerchantCode(merchantInfo.getCode());

                            log.info("银行卡-API提现接口订单提交成功, 请求ip: {}, 请求明文: {}, 返回明文: {}", requestIp, withdrawalApplyReq, withdrawalApplyVo);

                            //注册事务同步回调 事务提交成功了才执行以下操作
                            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                                @Override
                                public void afterCommit() {

                                    log.info("银行卡-API提现接口订单提交成功, 执行事务同步回调 请求ip: {}, 请求明文: {}, 返回明文: {}", requestIp, withdrawalApplyReq, withdrawalApplyVo);

                                    if (PaymentOrderStatusEnum.BE_MATCHED.getCode().equals(merchantPaymentOrders.getOrderStatus())){
                                        log.info("USDT-API提现接口订单提交成功, 自动出款, 订单号: {}", merchantPaymentOrders.getPlatformOrder());

                                        //将代付订单存入到redis 供买入列表进行买入
                                        // 事务提交后执行的Redis操作
                                        BuyListVo buyListVo = new BuyListVo();
                                        //订单号
                                        buyListVo.setPlatformOrder(platformOrder);
                                        //订单金额
                                        buyListVo.setAmount(amount);
                                        //支付方式
                                        buyListVo.setPayType(merchantPaymentOrders.getPayType());
                                        //存入redis买入金额列表
                                        redisUtil.addOrderIdToList(buyListVo, "1");

                                        //推送最新的 金额列表给前端
                                        memberSendAmountList.send();

                                    }else{
                                        log.info("USDT-API提现接口订单提交成功, 手动出款, 订单号: {}", merchantPaymentOrders.getPlatformOrder());
                                    }
                                }
                            });

                            return ApiResponse.of(ApiResponseEnum.SUCCESS, apiResponseVo);
                        } else {
                            //提交失败
                            log.error("银行卡-API提现接口订单提交失败, 请求ip: {}, 请求明文: {}, 请求密文: {}", requestIp, withdrawalApplyReq, apiRequest);
                            //手动回滚
                            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                            return ApiResponse.of(ApiResponseEnum.SYSTEM_EXECUTION_ERROR, null);
                        }
                    }
                } catch (Exception e) {
                    //手动回滚
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    log.error("银行卡-API提现接口订单提交失败 req: {}, e: {}", apiRequest, e);
                } finally {
                    //释放锁
                    if (req2 && lock2.isHeldByCurrentThread()) {
                        lock2.unlock();
                    }
                }
            } catch (DuplicateKeyException e) {
                //手动回滚
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                log.error("银行卡-API提现接口订单提交失败, 数据重复 e: {}", e.getMessage());
                return ApiResponse.of(ApiResponseEnum.DATA_DUPLICATE_SUBMISSION, null);
            } catch (BadPaddingException e) {
                //手动回滚
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                log.error("银行卡-API提现接口订单提交失败, 解密失败，无效的密文或密钥错误 e: {}", e.getMessage());
                return ApiResponse.of(ApiResponseEnum.DECRYPTION_ERROR, null);
            } catch (Exception e) {
                //手动回滚
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                log.error("银行卡-API提现接口订单提交失败 req: {}, e: {}", apiRequest, e);
            }
            return ApiResponse.of(ApiResponseEnum.SYSTEM_EXECUTION_ERROR, null);

        } catch (Exception e) {
            //手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("银行卡-API提现接口订单提交失败 req: {}, e: {}", apiRequest, e);
        } finally {
            //释放锁

        }
        return ApiResponse.of(ApiResponseEnum.SYSTEM_EXECUTION_ERROR, null);
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

}