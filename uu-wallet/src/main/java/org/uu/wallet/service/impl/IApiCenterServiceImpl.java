package org.uu.wallet.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.uu.common.core.result.ApiResponse;
import org.uu.common.core.result.ApiResponseEnum;
import org.uu.common.core.result.RestResult;
import org.uu.common.core.result.ResultCode;
import org.uu.common.redis.util.RedissonUtil;
import org.uu.wallet.Enum.CollectionOrderStatusEnum;
import org.uu.wallet.Enum.PaymentOrderStatusEnum;
import org.uu.wallet.bo.UsdtPaymentInfoBO;
import org.uu.wallet.entity.*;
import org.uu.wallet.mapper.MerchantCollectOrdersMapper;
import org.uu.wallet.mapper.PaymentOrderMapper;
import org.uu.wallet.property.ArProperty;
import org.uu.wallet.req.*;
import org.uu.wallet.service.*;
import org.uu.wallet.strategy.CollectionContext;
import org.uu.wallet.strategy.PaymentContext;
import org.uu.wallet.util.*;
import org.uu.wallet.vo.*;

import javax.crypto.BadPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class IApiCenterServiceImpl implements IApiCenterService {

    private final IMerchantInfoService merchantInfoService;
    private final Validator validator;
    private final ArProperty arProperty;
    private final RedisTemplate redisTemplate;
    private final IMerchantCollectOrdersService merchantCollectOrdersService;
    private final MerchantCollectOrdersMapper merchantCollectOrdersMapper;
    private final RedissonUtil redissonUtil;

    @Autowired
    private IPaymentOrderService paymentOrderService;

    @Autowired
    private IKycCenterService kycCenterService;

    @Autowired
    private PaymentOrderMapper paymentOrderMapper;

    @Autowired
    private CollectionContext collectionContext;

    @Autowired
    private PaymentContext paymentContext;

    // 正则表达式，用于校验12位纯数字格式
    private static final String UTR_REGEX = "^\\d{12}$";


    /**
     * 商户 充值接口
     *
     * @param apiRequest
     * @param request
     * @return {@link ApiResponse}
     */
    @Override
    public ApiResponse depositApply(ApiRequest apiRequest, HttpServletRequest request) {
        //根据不同的支付类型 进行处理
        return collectionContext.executeStrategy(apiRequest, request);
    }

    /**
     * 获取支付页面(收银台)信息接口
     *
     * @param token
     * @return {@link RestResult}<{@link PaymentInfo}>
     */
    @Override
    public RestResult<PaymentInfo> retrievePaymentDetails(String token) {

        if (redisTemplate.hasKey(token)) {
            //获取支付页面信息
            PaymentInfo paymentInfo = (PaymentInfo) redisTemplate.opsForValue().get(token);

            //设置支付剩余时间
            paymentInfo.setPaymentExpireTime(redisTemplate.getExpire(token, TimeUnit.SECONDS));

            if (paymentInfo != null) {
                log.info("获取支付页面(收银台)信息成功, token: {}, 返回数据: {}", token, paymentInfo);
                return RestResult.ok(paymentInfo);
            }
        }

        log.error("获取支付页面(收银台)信息失败, 该订单不存在或该订单已失效, token: {}", token);
        return RestResult.failure(ResultCode.ORDER_EXPIRED);
    }

    /**
     * 收银台 提交utr 接口
     *
     * @param confirmPaymentReq
     * @return {@link RestResult}
     */
    @Override
    @Transactional
    public RestResult confirmPayment(ConfirmPaymentReq confirmPaymentReq) {


        //分布式锁key ar-wallet-confirmPayment+订单token
        String key = "ar-wallet-confirmPayment" + confirmPaymentReq.getToken();
        RLock lock = redissonUtil.getLock(key);

        boolean req = false;

        try {
            req = lock.tryLock(10, TimeUnit.SECONDS);

            if (req) {

                if (redisTemplate.hasKey(confirmPaymentReq.getToken())) {

                    //获取支付页面信息
                    PaymentInfo paymentInfo = (PaymentInfo) redisTemplate.opsForValue().get(confirmPaymentReq.getToken());

                    //获取订单信息 加排他行锁
                    MerchantCollectOrders merchantCollectOrders = null;
                    if (paymentInfo != null) {
                        merchantCollectOrders = merchantCollectOrdersMapper.selectMerchantCollectOrdersForUpdate(paymentInfo.getPlatformOrder());
                    }

                    if (merchantCollectOrders == null) {
                        log.error("收银台 提交utr 接口处理失败: 订单不存在, 订单信息: {}", paymentInfo);
                        return RestResult.failure(ResultCode.ORDER_EXPIRED);
                    }
                    log.info("收银台 提交utr : 获取到支付订单锁, 订单状态: {}, 订单号: {}", merchantCollectOrders.getOrderStatus(), merchantCollectOrders.getPlatformOrder());

                    //校验utr
                    if (!isValidUTR(confirmPaymentReq.getUtr())) {
                        log.error("收银台 提交utr 接口处理失败: utr格式不正确: req: {}", confirmPaymentReq);
                        return RestResult.failure(ResultCode.UTR_FORMAT_INCORRECT);
                    }

                    //查询卖出订单 加上排他行锁
                    PaymentOrder paymentOrder = paymentOrderMapper.selectPaymentForUpdate(merchantCollectOrders.getSellOrderNo());

                    if (paymentOrder == null) {
                        log.error("收银台 提交utr 接口处理失败: 卖出订单不存在, 订单信息: {}", paymentInfo);
                        return RestResult.failure(ResultCode.ORDER_EXPIRED);
                    }

                    //判断订单是待支付状态才进行处理
                    if (!merchantCollectOrders.getOrderStatus().equals(CollectionOrderStatusEnum.BE_PAID.getCode())) {
                        log.error("收银台 提交utr 接口处理失败: 非法的订单状态: {}, 订单号: {}", merchantCollectOrders.getOrderStatus(), merchantCollectOrders.getPlatformOrder());
                        return RestResult.failure(ResultCode.DATA_DUPLICATE_SUBMISSION);
                    }

                    //更新代收订单utr
                    LambdaUpdateWrapper<MerchantCollectOrders> lambdaUpdateWrapperMerchantCollectOrders = new LambdaUpdateWrapper<>();
                    lambdaUpdateWrapperMerchantCollectOrders.eq(MerchantCollectOrders::getPlatformOrder, merchantCollectOrders.getPlatformOrder())  // 指定更新条件 订单号
                            .set(MerchantCollectOrders::getUtr, confirmPaymentReq.getUtr()) // 指定更新字段 (utr)
                            .set(MerchantCollectOrders::getPaymentTime, LocalDateTime.now()); // 指定更新字段 (支付时间)
                    // 这里传入的 null 表示不更新实体对象的其他字段
                    merchantCollectOrdersService.update(null, lambdaUpdateWrapperMerchantCollectOrders);

                    //更新卖出订单UTR
                    LambdaUpdateWrapper<PaymentOrder> lambdaUpdateWrapperPaymentOrder = new LambdaUpdateWrapper<>();
                    lambdaUpdateWrapperPaymentOrder.eq(PaymentOrder::getPlatformOrder, paymentOrder.getPlatformOrder())  // 指定更新条件 订单号
                            .set(PaymentOrder::getUtr, confirmPaymentReq.getUtr()) // 指定更新字段 (utr)
                            .set(PaymentOrder::getPaymentTime, LocalDateTime.now()); // 指定更新字段 (支付时间)
                    // 这里传入的 null 表示不更新实体对象的其他字段
                    paymentOrderService.update(null, lambdaUpdateWrapperPaymentOrder);


                    //注册事务同步回调
                    MerchantCollectOrders finalMerchantCollectOrders = merchantCollectOrders;
                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {

                            //事务提交成功后 开始拉取kyc

                            //充值订单 拉取kyc
                            KycAutoCompleteReq kycAutoCompleteReq = new KycAutoCompleteReq();
                            //买入订单号 (代收订单号)
                            kycAutoCompleteReq.setBuyerOrder(finalMerchantCollectOrders.getPlatformOrder());
                            //买入会员id
                            kycAutoCompleteReq.setBuyerMemberId(finalMerchantCollectOrders.getExternalMemberId());
                            //卖出订单号
                            kycAutoCompleteReq.setSellerOrder(paymentOrder.getPlatformOrder());
                            //卖出会员id
                            kycAutoCompleteReq.setSellerMemberId(paymentOrder.getMemberId());
                            //订单金额
                            kycAutoCompleteReq.setOrderAmount(finalMerchantCollectOrders.getOrderAmount());
                            //充值 1 提现 2
                            kycAutoCompleteReq.setType("1");
                            //币种
                            kycAutoCompleteReq.setCurrency(finalMerchantCollectOrders.getCurrency());
                            //utr
                            kycAutoCompleteReq.setUtr(confirmPaymentReq.getUtr());
                            kycAutoCompleteReq.setKycId(finalMerchantCollectOrders.getKycId());
                            kycCenterService.startPullTransaction(kycAutoCompleteReq);
                        }
                    });
                    return RestResult.ok();
                } else {
                    log.error("收银台 提交utr 接口处理失败: 订单已失效: req: {}", confirmPaymentReq);
                    return RestResult.failure(ResultCode.ORDER_EXPIRED);
                }
            }
        } catch (Exception e) {
            //手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("收银台 提交utr 接口处理失败: req: {}, e: {}", confirmPaymentReq, e);
        } finally {
            //释放锁
            if (req && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return RestResult.failure(ResultCode.SYSTEM_EXECUTION_ERROR);
    }

    /**
     * 校验是否是有效的UTR格式.
     *
     * @param utr 要校验的UTR字符串
     * @return 如果UTR格式有效，则返回true；否则返回false
     */
    public static boolean isValidUTR(String utr) {
        Pattern pattern = Pattern.compile(UTR_REGEX);
        Matcher matcher = pattern.matcher(utr);
        return matcher.matches();
    }


    /**
     * 提现接口 (转入)
     *
     * @param apiRequest
     * @param request
     * @return {@link ApiResponse}
     */
    @Override
    public ApiResponse withdrawalApply(ApiRequest apiRequest, HttpServletRequest request) {
        //根据不同的代付类型 进行处理
        return paymentContext.executeStrategy(apiRequest, request);
    }


    /**
     * 将字符串转为 AES密钥
     *
     * @param strKey
     * @return {@link SecretKey}
     */
    public SecretKey convertStringToAESKey(String strKey) {
        byte[] decodedKey = Base64.getDecoder().decode(strKey);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }


    /**
     * 从Redis获取 aes密钥和token
     *
     * @param username
     * @return {@link JSONObject}
     */
    public JSONObject retrieveTokenAndKey(String username) {
        // 从Redis获取数据
        String key = "GENERATETOKEN:" + username;
        return (JSONObject) redisTemplate.opsForValue().get(key);
    }


    /**
     * 查询充值订单
     *
     * @param apiRequest
     * @param request
     * @return {@link ApiResponse}
     */
    @Override
    public ApiResponse depositQuery(ApiRequestQuery apiRequest, HttpServletRequest request) {

        //获取请求IP
        String requestIp = IpUtil.getRealIP(request);
        log.info("查询充值订单: {}, 商户号: {}, 请求IP: {}", apiRequest.getMerchantCode(), requestIp);

        //获取商户信息
        MerchantInfo merchantInfo = merchantInfoService.getMerchantInfoByCode(apiRequest.getMerchantCode());

        String merchantPublicKeyStr = null;
        //获取商户公钥
        if (merchantInfo != null) {
            merchantPublicKeyStr = merchantInfo.getMerchantPublicKey();
        }

        //校验请求
        ApiResponse apiResponse = validateRequest(apiRequest, requestIp, merchantInfo, merchantPublicKeyStr, "查询充值订单");
        if (apiResponse != null) {
            return apiResponse;
        }

        try {

            //商户公钥
            PublicKey merchantPublicKey = RsaUtil.getPublicKeyFromString(merchantPublicKeyStr);

            //平台私钥
            PrivateKey platformPrivateKey = RsaUtil.getPrivateKeyFromString(arProperty.getPrivateKey());

            //使用平台私钥解密数据
            DepositQueryReq depositQueryReq = RsaUtil.decryptData(apiRequest.getEncryptedKey(), apiRequest.getEncryptedData(), platformPrivateKey, DepositQueryReq.class);

            if (depositQueryReq == null) {
                log.error("查询充值订单失败, 参数校验失败: 请求ip: {}, req: {}, 商户信息: {}", requestIp, apiRequest, merchantInfo);
                return ApiResponse.of(ApiResponseEnum.PARAM_VALID_FAIL, null);
            }
            //手动调用验证明文参数
            Set<ConstraintViolation<DepositQueryReq>> violations = validator.validate(depositQueryReq);
            if (!violations.isEmpty()) {
                // 处理验证错误
                for (ConstraintViolation<DepositQueryReq> violation : violations) {
                    log.error("查询充值订单失败, 参数校验失败: 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}", requestIp, apiRequest, merchantInfo, depositQueryReq);
                    System.out.println(violation.getMessage());
                    return ApiResponse.ofMsg(ApiResponseEnum.PARAM_VALID_FAIL, violation.getMessage(), null);
                }
            }

            //使用商户公钥验证签名
            if (!RsaUtil.verifySignature(depositQueryReq, depositQueryReq.getSign(), merchantPublicKey)) {
                log.error("查询充值订单失败, 签名校验失败: 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}", requestIp, apiRequest, merchantInfo, depositQueryReq);
                return ApiResponse.of(ApiResponseEnum.SIGNATURE_ERROR, null);
            }

            //处理业务

            //查询充值订单
            MerchantCollectOrders orderInfoByOrderNumber = merchantCollectOrdersService.getOrderInfoByOrderNumber(depositQueryReq.getMerchantTradeNo());

            if (orderInfoByOrderNumber == null) {
                log.error("查询充值订单失败, 订单不存在: 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}", requestIp, apiRequest, merchantInfo, depositQueryReq);
                return ApiResponse.of(ApiResponseEnum.ORDER_NOT_FOUND, null);
            }

            //查看该订单是否属于该商户
            if (!orderInfoByOrderNumber.getMerchantCode().equals(merchantInfo.getCode())) {
                log.error("查询充值订单失败, 该笔订单不属于该商户: 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}", requestIp, apiRequest, merchantInfo, depositQueryReq);
                return ApiResponse.of(ApiResponseEnum.ORDER_NOT_FOUND, null);
            }

            //组装返回数据
            DepositQueryVo depositQueryVo = new DepositQueryVo();

            //商户号
            depositQueryVo.setMerchantCode(orderInfoByOrderNumber.getMerchantCode());

            //商户订单号
            depositQueryVo.setMerchantTradeNo(orderInfoByOrderNumber.getMerchantOrder());

            //平台订单号
            depositQueryVo.setTradeNo(orderInfoByOrderNumber.getPlatformOrder());

            //实际金额
            depositQueryVo.setAmount(String.valueOf(orderInfoByOrderNumber.getAmount()));

            //订单金额
            depositQueryVo.setOrderAmount(String.valueOf(orderInfoByOrderNumber.getOrderAmount()));

            //订单时间
            depositQueryVo.setOrderDateTime(orderInfoByOrderNumber.getCreateTime());

            //交易状态
            depositQueryVo.setTradeStatus(orderInfoByOrderNumber.getOrderStatus());

            //渠道编码
            depositQueryVo.setChannel(orderInfoByOrderNumber.getPayType());

            //签名并加密数据
            EncryptedData encryptedData = RsaUtil.signAndEncryptData(depositQueryVo, platformPrivateKey, merchantPublicKey);
            ApiResponseVo apiResponseVo = new ApiResponseVo();
            BeanUtils.copyProperties(encryptedData, apiResponseVo);
            apiResponseVo.setMerchantCode(merchantInfo.getCode());

            log.info("查询充值订单成功, 请求ip: {}, 请求明文: {}, 返回明文: {}", requestIp, depositQueryReq, depositQueryVo);

            return ApiResponse.of(ApiResponseEnum.SUCCESS, apiResponseVo);

        } catch (BadPaddingException e) {
            log.error("查询充值订单失败, 解密失败，无效的密文或密钥错误 e: {}", e.getMessage());
            return ApiResponse.of(ApiResponseEnum.DECRYPTION_ERROR, null);
        } catch (Exception e) {
            log.error("查询充值订单失败 req: {}, e: {}", apiRequest, e);
        }

        return ApiResponse.of(ApiResponseEnum.SYSTEM_EXECUTION_ERROR, null);
    }


    /**
     * 查询提现订单
     *
     * @param apiRequest
     * @param request
     * @return {@link ApiResponse}
     */
    @Override
    public ApiResponse withdrawalQuery(ApiRequestQuery apiRequest, HttpServletRequest request) {


        //获取请求IP
        String requestIp = IpUtil.getRealIP(request);
        log.info("查询提现订单: {}, 商户号: {}, 请求IP: {}", apiRequest.getMerchantCode(), requestIp);

        //获取商户信息
        MerchantInfo merchantInfo = merchantInfoService.getMerchantInfoByCode(apiRequest.getMerchantCode());

        String merchantPublicKeyStr = null;
        //获取商户公钥
        if (merchantInfo != null) {
            merchantPublicKeyStr = merchantInfo.getMerchantPublicKey();
        }

        //校验请求
        ApiResponse apiResponse = validateRequest(apiRequest, requestIp, merchantInfo, merchantPublicKeyStr, "查询提现订单");
        if (apiResponse != null) {
            return apiResponse;
        }

        try {

            //商户公钥
            PublicKey merchantPublicKey = RsaUtil.getPublicKeyFromString(merchantPublicKeyStr);

            //平台私钥
            PrivateKey platformPrivateKey = RsaUtil.getPrivateKeyFromString(arProperty.getPrivateKey());

            //使用平台私钥解密数据
            WithdrawalQueryReq withdrawalQueryReq = RsaUtil.decryptData(apiRequest.getEncryptedKey(), apiRequest.getEncryptedData(), platformPrivateKey, WithdrawalQueryReq.class);

            if (withdrawalQueryReq == null) {
                log.error("查询提现订单失败, 参数校验失败: 请求ip: {}, req: {}, 商户信息: {}", requestIp, apiRequest, merchantInfo);
                return ApiResponse.of(ApiResponseEnum.PARAM_VALID_FAIL, null);
            }
            //手动调用验证明文参数
            Set<ConstraintViolation<WithdrawalQueryReq>> violations = validator.validate(withdrawalQueryReq);
            if (!violations.isEmpty()) {
                // 处理验证错误
                for (ConstraintViolation<WithdrawalQueryReq> violation : violations) {
                    log.error("查询提现订单失败, 参数校验失败: 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}", requestIp, apiRequest, merchantInfo, withdrawalQueryReq);
                    System.out.println(violation.getMessage());
                    return ApiResponse.ofMsg(ApiResponseEnum.PARAM_VALID_FAIL, violation.getMessage(), null);
                }
            }

            //使用商户公钥验证签名
            if (!RsaUtil.verifySignature(withdrawalQueryReq, withdrawalQueryReq.getSign(), merchantPublicKey)) {
                log.error("查询提现订单失败, 签名校验失败: 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}", requestIp, apiRequest, merchantInfo, withdrawalQueryReq);
                return ApiResponse.of(ApiResponseEnum.SIGNATURE_ERROR, null);
            }

            //处理业务

            //查询提现订单
//            MerchantPaymentOrders orderInfoByOrderNumber = merchantPaymentOrdersService.getOrderInfoByOrderNumber(withdrawalQueryReq.getMerchantTradeNo());
            MerchantPaymentOrders orderInfoByOrderNumber = null;
            if (orderInfoByOrderNumber == null) {
                log.error("查询提现订单失败, 订单不存在: 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}", requestIp, apiRequest, merchantInfo, withdrawalQueryReq);
                return ApiResponse.of(ApiResponseEnum.ORDER_NOT_FOUND, null);
            }

            //查看该订单是否属于该商户
            if (!orderInfoByOrderNumber.getMerchantCode().equals(merchantInfo.getCode())) {
                log.error("查询提现订单失败, 该笔订单不属于该商户: 请求ip: {}, req: {}, 商户信息: {}, 请求明文: {}", requestIp, apiRequest, merchantInfo, withdrawalQueryReq);
                return ApiResponse.of(ApiResponseEnum.ORDER_NOT_FOUND, null);
            }

            //组装返回数据
            WithdrawalQueryVo withdrawalQueryVo = new WithdrawalQueryVo();

            //商户号
            withdrawalQueryVo.setMerchantCode(orderInfoByOrderNumber.getMerchantCode());

            //商户订单号
            withdrawalQueryVo.setMerchantTradeNo(orderInfoByOrderNumber.getMerchantOrder());

            //平台订单号
            withdrawalQueryVo.setTradeNo(orderInfoByOrderNumber.getPlatformOrder());

            //提现金额
            withdrawalQueryVo.setAmount(String.valueOf(orderInfoByOrderNumber.getAmount()));

            //订单时间
            withdrawalQueryVo.setOrderDateTime(orderInfoByOrderNumber.getCreateTime());

            //交易状态
            withdrawalQueryVo.setTradeStatus(orderInfoByOrderNumber.getOrderStatus());

            //交易状态 如果是待审核 待匹配 都返回处理中给商户
            if (PaymentOrderStatusEnum.BE_MATCHED.getCode().equals(orderInfoByOrderNumber.getOrderStatus())
                    || PaymentOrderStatusEnum.TO_BE_REVIEWED.getCode().equals(orderInfoByOrderNumber.getOrderStatus())
            ) {
                withdrawalQueryVo.setTradeStatus(PaymentOrderStatusEnum.HANDLING.getCode());
            }

            //渠道编码
            withdrawalQueryVo.setChannel(orderInfoByOrderNumber.getPayType());

            //签名并加密数据
            EncryptedData encryptedData = RsaUtil.signAndEncryptData(withdrawalQueryVo, platformPrivateKey, merchantPublicKey);
            ApiResponseVo apiResponseVo = new ApiResponseVo();
            BeanUtils.copyProperties(encryptedData, apiResponseVo);
            apiResponseVo.setMerchantCode(merchantInfo.getCode());

            log.info("查询提现订单成功, 请求ip: {}, 请求明文: {}, 返回明文: {}", requestIp, withdrawalQueryReq, withdrawalQueryVo);

            return ApiResponse.of(ApiResponseEnum.SUCCESS, apiResponseVo);

        } catch (BadPaddingException e) {
            log.error("查询提现订单失败, 解密失败，无效的密文或密钥错误 e: {}", e.getMessage());
            return ApiResponse.of(ApiResponseEnum.DECRYPTION_ERROR, null);
        } catch (Exception e) {
            log.error("查询提现订单失败 req: {}, e: {}", apiRequest, e);
        }

        return ApiResponse.of(ApiResponseEnum.SYSTEM_EXECUTION_ERROR, null);
    }


    /**
     * @param request
     * @return {@link ApiResponse }
     */
    @Override
    public String testDepositApply(HttpServletRequest request, String channel) {

        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            String timestamp = fmt.format(LocalDateTime.now());
            String orderNo = "CZ" + timestamp + 1;

            //商户号
            String merchantCode = "test7";

            //会员id
            String memberId = "80";

            //平台公钥 (从商户后台获取)
            String publicKeyStr = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAsPZvSUrfls0MAGD8VqWnk91QF4SwFQFEuccWyhPijquYcwCozv13NNFVdesLYZz1gruMpG1mEVv7I4/NhHlcEFy3e5QQDmixVGzY3tpgkyWwoELXjSsJwHeyjztQ0pJEydrpKakvWO+4g2KxQsruNlrfdoCT8iqNgb1ChTuiARIQAmWLV+g2K4KmG5kUnBdv8jHExMB1j4ZMIuMmnTQGtNzUBDGQGCpAws/wf2xJcbAA+vQre4UI11+nJ0AQNAytBcRWMv4n5zfGzXYZZ8E9rmbQc9ktBV2lFrooPGeh9iRHw+ExJ5M9vArhEctoyOtVFgtjJZA5f0Jqxm4l/cKPewIDAQAB";

            PublicKey platformPublicKey = RsaUtil.getPublicKeyFromString(publicKeyStr);

            //商户私钥 (商户自己生成的私钥)
            String merchantPrivateKeyStr = "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQC975SCIlEIVSupTY8noHfengFRxGdnRfQhBltOqo6aowBDGN9ALo/OXib4jEnDFnJVgtM5V2fEAMgA8dmo9GJxzDZX6Fm3vBRhwUZvJ8woii6aTIhInmKpoUS/84670PPukIa93Xw5SOrX9JOUtA2Apyf+2oqQsZzb97pHo2zL2SeC12pE3uYazTjl1gDuf922kFHlBsZvpiMDg+YpWV4PKdBDbnSoD75R0qOCbRbDX2OEQ/welzsJ6cR0+trTrJFQVQP9rlZqlDxyhRFi1YGtHi7B9avKbSgLzsYJiYDAhGqMl1/qmeSivGg78jA1WHu+VsuE0JNR8kJZqK54gUznAgMBAAECggEBAJ16wUVMsqcYBwVu2xzd/cVEeI+VKq8D3zBqltYYr/gi4hq/yorqkC1o/yLhHl90gwPHXt6SS+TFSQ8Pd4qQZfc4fG5SpeCjPrr2hzGua6XJPG6OugiL2f4PSnNku5mZ3EaW6kEOHOtaO+0mSh7nUrKy5YR3KqBhw07WjUrMg25XhrHv7SJENtn8+YoDYcxhtzXBY0qRnJ2KIJnt4s01UI3f5gih9AEFuC+7L+VAYXbRlpD787AmlRlbnJj+U/YtXu131TjS9BQIJ3RIPS17EP3TAAduQ907xV0X3ez8uzuZV95tamOlrO+remJGZ5Xc4sXXiIyr+nnmrVumeV7FCpECgYEA3gQV3yBhl8EFJf6zG0rzUT7vacmwvOwTd2TC1wvuX7YxddA9ZflCCIiNhOxO9YFzDYqwuSGavK9pq382q/0Vu3rDVBXzmVHliV1EO+dPibKChniPa58jahgrY0020cVEC3BIXRbcyzCK6KJ/giF3AaS6DTOYjjKXLPXCs+twflUCgYEA2wJngac2AC5AGZQqtYwqJsNmyKLFWVrWAMCPTgYYwUslKsxA0XXRS1yTL8oxz2YI4Sbp9HhTWo7F6LbFxVdtHd2HC75FBOG/hWj7qnXvQ07CrTjaJC/GXuJN8AgO1yhOoVoTlsFiIb2vO2jv7LBeE7/Xt49ztMcBRZDh1mGxIksCgYEAjJSVrNV6NndYZTij/NI2w+lP+/JkYRwzL1S0Mysw45YgN3OGjT2J3JFq2xIu5TH0wkxhnrhynKuOA7Pn0HvO+QnBCUtlFl5PM+3EuRG8wXoxQCiy1/jKmfF398b6wVVhwsR0bc1+PYMdUjUi/Cloi7fcv2M+ZDwQkb2EbhzU/IkCgYEAn8eoRjH8trMHduHfKuZgljk20qfV/PPFb3UM1+qgwtyU+B1eKLKhCC84/sOwBVS2o7TlOMNUZJwHDVnS/b9jz0cgUFP3PLGKLcXC3cD+1wcuBnyUwZPNUMof/D+UvCoe+56g7fqWInGl110etXqSmCv9MGFLFBef+OXTrblGJvMCgYAnsd1dtgdRDA6TFM6GuKjGg5//StzmS6IaHQrGQSk5Mu7Bw1QifEurym3pFghT6/HzisGEpgc+4MWNGfdZeaeCGctOc2QaM6yxaggnckURYeEeKFh9fXZqNPcsjyv90WFf6o0NBtB6mcXR4Wr0j/94YWLbniJktAaYbXklB/lROg==";

            PrivateKey merchantPrivateKey = RsaUtil.getPrivateKeyFromString(merchantPrivateKeyStr);

            //请求参数 明文
            DepositApplyReq depositApplyReq = new DepositApplyReq();
            //商户号
            depositApplyReq.setMerchantCode(merchantCode);
            //会员id
            depositApplyReq.setMemberId(memberId);
            //订单号
            depositApplyReq.setMerchantTradeNo(orderNo);
            //同步回调地址
            depositApplyReq.setSyncNotifyAddress("https://google.com");

            if (channel.equals("3")) {
                //UPI
                //金额
                depositApplyReq.setAmount("100");
                //币种
                depositApplyReq.setCurrency("INR");
            } else if (channel.equals("2")) {
                //USDT
                //金额
                depositApplyReq.setAmount("10");
                //币种
                depositApplyReq.setCurrency("USDT");
            } else if (channel.equals("6")) {
                //TRX
                //金额
                depositApplyReq.setAmount("50");
                //币种
                depositApplyReq.setCurrency("TRX");
            }

            //渠道编码
            depositApplyReq.setChannel(channel);
            //回调地址
            depositApplyReq.setNotifyUrl("https://google.com");
            //时间戳 (10位) 字符串类型
            long l = System.currentTimeMillis() / 1000;
            depositApplyReq.setTimestamp(String.valueOf(l));

            //签名
            depositApplyReq.setSign("");

            //加密 并签名数据:
            EncryptedData encryptedData = RsaUtil.signAndEncryptData(depositApplyReq, merchantPrivateKey, platformPublicKey);
            ApiRequest apiRequest = new ApiRequest();
            BeanUtils.copyProperties(encryptedData, apiRequest);
            apiRequest.setMerchantCode(merchantCode);
            apiRequest.setChannel(channel);

            //发送请求
            String res = RequestUtil.HttpRestClientToJson("http://127.0.0.1:20000/uu-wallet/v1/apiCenter/deposit/apply", JSON.toJSONString(apiRequest));

            if (res == null) {
                return null;
            }

            System.out.println("返回数据: " + res);

            JSONObject resJson = JSONObject.parseObject(res);

            if (StringUtils.isNotBlank(resJson.getString("msg")) && !resJson.getString("msg").contains("successful")) {
                return resJson.getString("msg");
            }

            //使用商户私钥进行解密数据
            JSONObject resDecryptData = decryptData(resJson.getJSONObject("data").getString("encryptedKey"), resJson.getJSONObject("data").getString("encryptedData"), merchantPrivateKey);

            System.out.println("解密后的数据: " + resDecryptData);

            return resDecryptData.getString("payUrl");


        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取USDT支付页面
     *
     * @param token
     * @return {@link RestResult }<{@link UsdtPaymentInfoBO }>
     */
    @Override
    public RestResult<UsdtPaymentInfoBO> retrieveUsdtPaymentDetails(String token) {
        if (redisTemplate.hasKey(token)) {
            //获取支付页面信息
            UsdtPaymentInfoBO usdtPaymentInfoBO = (UsdtPaymentInfoBO) redisTemplate.opsForValue().get(token);

            //设置支付剩余时间
            usdtPaymentInfoBO.setPaymentExpireTime(redisTemplate.getExpire(token, TimeUnit.SECONDS));

            if (usdtPaymentInfoBO != null) {
                log.info("获取支付页面(收银台)信息成功, token: {}, 返回数据: {}", token, usdtPaymentInfoBO);
                return RestResult.ok(usdtPaymentInfoBO);
            }
        }

        log.error("获取支付页面(收银台)信息失败, 该订单不存在或该订单已失效, token: {}", token);
        return RestResult.failure(ResultCode.ORDER_EXPIRED);
    }

    /**
     * 解密数据
     *
     * @param encryptedKey
     * @param encryptedData
     * @param privateKey
     * @return {@link JSONObject}
     * @throws Exception
     */
    private static JSONObject decryptData(String encryptedKey, String encryptedData, PrivateKey privateKey) throws Exception {

        // 1. 使用RSA平台私钥解密AES密钥
        SecretKey secretKey = RsaUtil.decryptAESKey(encryptedKey, privateKey);

        // 2. 使用解密后的AES密钥解密数据
        String decryptedData = RsaUtil.decryptData(encryptedData, secretKey);

        return JSON.parseObject(decryptedData);
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
    private ApiResponse validateRequest(ApiRequestQuery apiRequest, String requestIp, MerchantInfo merchantInfo, String merchantPublicKeyStr, String apiName) {

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
