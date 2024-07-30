package org.uu.wallet.strategy.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.uu.common.core.result.ApiResponse;
import org.uu.common.core.result.ApiResponseEnum;
import org.uu.common.redis.util.RedissonUtil;
import org.uu.wallet.Enum.PaymentOrderStatusEnum;
import org.uu.wallet.Enum.TaskTypeEnum;
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
import org.uu.wallet.tron.service.TronService;
import org.uu.wallet.util.IpUtil;
import org.uu.wallet.util.MD5Util;
import org.uu.wallet.util.OrderNumberGeneratorUtil;
import org.uu.wallet.util.RsaUtil;
import org.uu.wallet.vo.ApiResponseVo;
import org.uu.wallet.vo.WithdrawalApplyVo;

import javax.crypto.BadPaddingException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.math.BigDecimal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * TRX代付处理
 *
 * @author simon
 * @date 2024/07/18
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TrxPaymentStrategy implements PaymentStrategy {

    @Autowired
    private MerchantInfoMapper merchantInfoMapper;

    private final IMerchantInfoService merchantInfoService;
    private final Validator validator;
    private final ArProperty arProperty;

    private final OrderNumberGeneratorUtil orderNumberGenerator;

    @Autowired
    private IMerchantPaymentOrdersService merchantPaymentOrdersService;

    @Autowired
    private IMerchantRatesConfigService merchantRatesConfigService;

    @Autowired
    private RedissonUtil redissonUtil;

    @Autowired
    private RabbitMQService rabbitMQService;

    private final RedisTemplate redisTemplate;

    @Autowired
    private TronService tronService;


    /**
     * TRX代付 提交
     *
     * @param apiRequest
     * @param request
     * @return {@link ApiResponse }
     */
    @Override
    @Transactional
    public ApiResponse processPayment(ApiRequest apiRequest, HttpServletRequest request) {

        try {
            //获取请求IP
            String requestIp = IpUtil.getRealIP(request);
            log.info("TRX-API提现接口, 商户号: {}, 请求IP: {}", apiRequest.getMerchantCode(), requestIp);

            //获取商户信息 加上排他行锁
            MerchantInfo merchantInfo = merchantInfoMapper.selectMerchantInfoForUpdate(apiRequest.getMerchantCode());

            String merchantPublicKeyStr = null;
            //获取商户公钥
            if (merchantInfo != null) {
                merchantPublicKeyStr = merchantInfo.getMerchantPublicKey();
            }

            //校验请求
            ApiResponse apiResponse = validateRequest(apiRequest, requestIp, merchantInfo, merchantPublicKeyStr, "TRX-API提现接口");

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
                    log.error("TRX-API提现接口处理失败, 参数校验失败: 请求ip: {}, req: {}, 商户信息: {}", requestIp, apiRequest, merchantInfo);
                    return ApiResponse.of(ApiResponseEnum.PARAM_VALID_FAIL, null);
                }

                //手动调用验证明文参数
                Set<ConstraintViolation<WithdrawalApplyReq>> violations = validator.validate(withdrawalApplyReq);
                if (!violations.isEmpty()) {
                    // 处理验证错误
                    for (ConstraintViolation<WithdrawalApplyReq> violation : violations) {
                        log.error("TRX-API提现接口处理失败, 参数校验失败: 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}", requestIp, apiRequest, merchantInfo, withdrawalApplyReq);
                        System.out.println(violation.getMessage());
                        return ApiResponse.ofMsg(ApiResponseEnum.PARAM_VALID_FAIL, violation.getMessage(), null);
                    }
                }

                //手动校验参数 USDT信息
                if (StringUtils.isBlank(withdrawalApplyReq.getUsdtAddr())) {
                    log.error("TRX-API提现接口处理失败, 参数校验失败: 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}", requestIp, apiRequest, merchantInfo, withdrawalApplyReq);
                    return ApiResponse.of(ApiResponseEnum.PARAM_VALID_FAIL, null);
                }

                //校验支付类型是否一致
                if (!apiRequest.getChannel().equals(withdrawalApplyReq.getChannel())) {
                    log.error("TRX-API提现接口处理失败, 支付类型不一致: 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}", requestIp, apiRequest, merchantInfo, withdrawalApplyReq);
                    return ApiResponse.of(ApiResponseEnum.PARAM_VALID_FAIL, null);
                }

                //使用商户公钥验证签名
                if (!RsaUtil.verifySignature(withdrawalApplyReq, withdrawalApplyReq.getSign(), merchantPublicKey)) {
                    log.error("TRX-API提现接口处理失败, 签名校验失败: 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}", requestIp, apiRequest, merchantInfo, withdrawalApplyReq);
                    return ApiResponse.of(ApiResponseEnum.SIGNATURE_ERROR, null);
                }

                //处理业务
                BigDecimal amount = new BigDecimal(withdrawalApplyReq.getAmount());

                //校验订单 金额是否小于1
                if (amount.compareTo(BigDecimal.ONE) < 0) {
                    log.error("TRX-API提现接口处理失败, 金额小于1: 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}, 订单金额: {}", requestIp, apiRequest, merchantInfo, withdrawalApplyReq, amount);
                    return ApiResponse.of(ApiResponseEnum.AMOUNT_EXCEEDS_LIMIT, null);
                }

                //判断商户代付状态
                if (merchantInfo.getWithdrawalStatus().equals("0")) {
                    //当前商户代付状态未开启
                    log.error("TRX-API提现接口处理失败, 当前商户代付状态未开启: 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}", requestIp, apiRequest, merchantInfo, withdrawalApplyReq);
                    return ApiResponse.of(ApiResponseEnum.MERCHANT_PAYMENT_STATUS_DISABLED, null);
                }

                //查询当前商户的支付类型配置
                MerchantRatesConfig merchantRatesConfig = merchantRatesConfigService.getMerchantRatesConfigByCode("2", withdrawalApplyReq.getChannel(), merchantInfo.getCode());

                //如果不存在对应的支付类型配置 驳回
                if (merchantRatesConfig == null) {
                    log.error("TRX-API提现接口处理失败, 不存在对应的支付类型配置: 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}", requestIp, apiRequest, merchantInfo, withdrawalApplyReq);
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
                    log.error("TRX-API提现接口处理失败, 金额超过限制: 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}", requestIp, apiRequest, merchantInfo, withdrawalApplyReq);
                    return ApiResponse.of(ApiResponseEnum.AMOUNT_EXCEEDS_LIMIT, null);
                }

                //校验USDT地址是否正确
                String toAddress = withdrawalApplyReq.getUsdtAddr().trim();

                // 检查钱包地址是否有效
                if (toAddress.length() != 34 || !tronService.checkAddress(toAddress)) {
                    log.error("TRX-API提现接口处理失败, USDT地址无效: 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}", requestIp, apiRequest, merchantInfo, withdrawalApplyReq);
                    return ApiResponse.of(ApiResponseEnum.INVALID_USDT_ADDRESS, null);
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

                        //卖出订单费率
                        BigDecimal rates = merchantRatesConfig.getRates();

                        //单笔手续费
                        BigDecimal fixedFee = merchantRatesConfig.getFixedFee();

                        //判断如果代付费率 大于0才计算费率
                        if (rates != null && rates.compareTo(BigDecimal.ZERO) > 0) {
                            //订单费用
                            cost = amount.multiply((rates.divide(BigDecimal.valueOf(100))));
                        }

                        //订单金额 + 订单费用 + 手续费
                        BigDecimal amountCost = amount.add(cost).add(fixedFee);

                        //校验商户TRX余额是否足够
                        if (merchantInfo.getTrxBalance().compareTo(amountCost) < 0) {
                            log.error("TRX-API提现接口处理失败, 商户余额不足: 订单金额(包括费率+单笔手续费): {}, 商户余额: {}, 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}", amountCost, merchantInfo.getBalance(), requestIp, apiRequest, merchantInfo, withdrawalApplyReq);
                            return ApiResponse.of(ApiResponseEnum.INSUFFICIENT_MERCHANT_BALANCE, null);
                        }


                        //订单金额总计 (订单金额 + 费用 + 单笔手续费)
                        BigDecimal allAmount = amount.add(cost).add(fixedFee);

                        //更新商户余额 将订单金额所需费用划转到交易中金额
                        LambdaUpdateWrapper<MerchantInfo> lambdaUpdateWrapperMerchantInfo = new LambdaUpdateWrapper<>();
                        lambdaUpdateWrapperMerchantInfo.eq(MerchantInfo::getCode, merchantInfo.getCode())  // 指定更新条件 商户号
                                .set(MerchantInfo::getTrxBalance, merchantInfo.getTrxBalance().subtract(allAmount)) // 指定更新字段 (减少商户余额 - 总金额)
                                .set(MerchantInfo::getPendingTrxBalance, merchantInfo.getPendingTrxBalance().add(allAmount)); // 指定更新字段 (增加交易中金额 + 总金额)
                        // 这里传入的 null 表示不更新实体对象的其他字段
                        merchantInfoService.update(null, lambdaUpdateWrapperMerchantInfo);

                        //生成商户代付订单
                        MerchantPaymentOrders merchantPaymentOrders = new MerchantPaymentOrders();

                        //订单状态 支付中
                        merchantPaymentOrders.setOrderStatus(PaymentOrderStatusEnum.HANDLING.getCode());

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

                        //设置订单金额
                        merchantPaymentOrders.setOrderAmount(amount);

                        //设置实际金额
                        merchantPaymentOrders.setAmount(amount);

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

                        //USDT地址
                        merchantPaymentOrders.setUsdtAddr(toAddress);

                        //币种 从请求参数获取
                        merchantPaymentOrders.setCurrency(withdrawalApplyReq.getCurrency());

                        //固定手续费
                        merchantPaymentOrders.setFixedFee(fixedFee);

                        //汇率 获取实时汇率 固定为0
                        merchantPaymentOrders.setExchangeRates(BigDecimal.ZERO);

                        //iToken 固定为0
                        merchantPaymentOrders.setItokenNumber(BigDecimal.ZERO);

                        //保存代付订单
                        boolean save = merchantPaymentOrdersService.save(merchantPaymentOrders);

                        if (!save) {
                            log.error("TRX-API提现接口处理失败: 生成商户代付订单失败，触发事务回滚。 订单信息: {}, req: {}", merchantPaymentOrders, withdrawalApplyReq);
                            // 抛出运行时异常
                            throw new RuntimeException("TRX-API提现接口处理失败: 生成商户代付订单失败，触发事务回滚。");
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

                            //交易状态 (支付中)
                            withdrawalApplyVo.setTradeStatus(merchantPaymentOrders.getOrderStatus());

                            //时间戳
                            withdrawalApplyVo.setTimestamp(String.valueOf(System.currentTimeMillis() / 1000));

                            //签名并加密数据
                            EncryptedData encryptedData = RsaUtil.signAndEncryptData(withdrawalApplyVo, platformPrivateKey, merchantPublicKey);

                            ApiResponseVo apiResponseVo = new ApiResponseVo();
                            BeanUtils.copyProperties(encryptedData, apiResponseVo);
                            apiResponseVo.setMerchantCode(merchantInfo.getCode());

                            log.info("TRX-API提现接口订单提交成功, 请求ip: {}, 请求明文: {}, 返回明文: {}", requestIp, withdrawalApplyReq, withdrawalApplyVo);

                            //注册事务同步回调 事务提交成功了才执行以下操作
                            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                                @Override
                                public void afterCommit() {

                                    log.info("TRX-API提现接口订单提交成功, 订单号: {}, 订单状态: {} 执行事务同步回调 请求ip: {}, 请求明文: {}, 返回明文: {}", merchantPaymentOrders.getPlatformOrder(), merchantPaymentOrders.getOrderStatus(), requestIp, withdrawalApplyReq, withdrawalApplyVo);

                                    if (PaymentOrderStatusEnum.HANDLING.getCode().equals(merchantPaymentOrders.getOrderStatus())) {
                                        log.info("TRX-API提现接口订单提交成功, 自动出款, 订单号: {}", merchantPaymentOrders.getPlatformOrder());
                                        TaskInfo taskInfo = new TaskInfo(platformOrder, TaskTypeEnum.TRX_PAYMENT_ORDER.getCode(), System.currentTimeMillis());
                                        //发送处理USDT代付订单的MQ
                                        rabbitMQService.sendTrxPaymentOrderMessage(taskInfo);
                                    } else {
                                        log.info("TRX-API提现接口订单提交成功, 手动出款, 订单号: {}", merchantPaymentOrders.getPlatformOrder());
                                    }

                                    //生成USDT代付订单标识 存储到redis 过期时间 3天
                                    // md5(商户号 + 订单号 + USDT地址)
                                    String paymentOrderSign = MD5Util.generateMD5(merchantPaymentOrders.getMerchantCode() + merchantPaymentOrders.getPlatformOrder() + merchantPaymentOrders.getUsdtAddr() + arProperty.getPaymentOrderKey());
                                    redisTemplate.opsForValue().set("trxPaymentOrderSign:" + merchantPaymentOrders.getPlatformOrder(), paymentOrderSign, 3, TimeUnit.DAYS);
                                }
                            });

                            return ApiResponse.of(ApiResponseEnum.SUCCESS, apiResponseVo);
                        } else {
                            //提交失败
                            log.error("TRX-API提现接口订单提交失败, 请求ip: {}, 请求明文: {}, 请求密文: {}", requestIp, withdrawalApplyReq, apiRequest);
                            //手动回滚
                            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                            return ApiResponse.of(ApiResponseEnum.SYSTEM_EXECUTION_ERROR, null);
                        }
                    }
                } catch (Exception e) {
                    //手动回滚
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    log.error("TRX-API提现接口订单提交失败 req: {}, e: {}", apiRequest, e);
                } finally {
                    //释放锁
                    if (req2 && lock2.isHeldByCurrentThread()) {
                        lock2.unlock();
                    }
                }
            } catch (DuplicateKeyException e) {
                //手动回滚
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                log.error("TRX-API提现接口订单提交失败, 数据重复 e: {}", e.getMessage());
                return ApiResponse.of(ApiResponseEnum.DATA_DUPLICATE_SUBMISSION, null);
            } catch (BadPaddingException e) {
                //手动回滚
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                log.error("TRX-API提现接口订单提交失败, 解密失败，无效的密文或密钥错误 e: {}", e.getMessage());
                return ApiResponse.of(ApiResponseEnum.DECRYPTION_ERROR, null);
            } catch (Exception e) {
                //手动回滚
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                log.error("TRX-API提现接口订单提交失败 req: {}, e: {}", apiRequest, e);
            }
            return ApiResponse.of(ApiResponseEnum.SYSTEM_EXECUTION_ERROR, null);

        } catch (Exception e) {
            //手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("TRX-API提现接口订单提交失败 req: {}, e: {}", apiRequest, e);
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