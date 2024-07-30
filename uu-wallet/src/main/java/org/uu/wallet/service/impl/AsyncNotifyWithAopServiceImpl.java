package org.uu.wallet.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.redisson.api.RLock;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.uu.common.redis.util.RedissonUtil;
import org.uu.wallet.Enum.NotifyStatusEnum;
import org.uu.wallet.dto.AsyncNotifyDTO;
import org.uu.wallet.entity.*;
import org.uu.wallet.mapper.CollectionOrderMapper;
import org.uu.wallet.mapper.MerchantCollectOrdersMapper;
import org.uu.wallet.mapper.PaymentOrderMapper;
import org.uu.wallet.property.ArProperty;
import org.uu.wallet.req.SendRechargeSuccessCallbackReq;
import org.uu.wallet.service.AsyncNotifyWithAopService;
import org.uu.wallet.service.IMerchantInfoService;
import org.uu.wallet.service.IMerchantPaymentOrdersService;
import org.uu.wallet.util.RequestUtil;
import org.uu.wallet.util.RsaUtil;
import org.uu.wallet.vo.ApiResponseVo;

import javax.annotation.Resource;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.concurrent.TimeUnit;

/**
 * @author lukas
 */
@Service
@Slf4j
public class AsyncNotifyWithAopServiceImpl implements AsyncNotifyWithAopService {
    @Resource
    MerchantCollectOrdersMapper merchantCollectOrdersMapper;
    @Resource
    PaymentOrderMapper paymentOrderMapper;
    @Resource
    RedissonUtil redissonUtil;
    @Resource
    IMerchantInfoService merchantInfoService;
    @Resource
    ArProperty arProperty;
    @Resource
    @Lazy
    IMerchantPaymentOrdersService merchantPaymentOrdersService;
    @Resource
    CollectionOrderMapper collectionOrderMapper;

    /**
     * 调用充值回调接口 并且整理需要记录的数据返回
     * @param orderNo orderNo
     * @param type type
     * @return {@link AsyncNotifyDTO}
     */
    @Override
    @Transactional
    public AsyncNotifyDTO sendRechargeCallbackWithRecordRequest(String orderNo, String type) {
        String key = "ar-wallet-sendRechargeSuccessCallback" + orderNo;
        RLock lock = redissonUtil.getLock(key);
        AsyncNotifyDTO dto = new AsyncNotifyDTO();
        dto.setType(type);
        boolean req = false;
        try {
            req = lock.tryLock(10, TimeUnit.SECONDS);
            if (req) {
                // 判断是否存在该订单
                MerchantCollectOrders merchantCollectOrders = merchantCollectOrdersMapper.selectMerchantCollectOrdersForUpdate(orderNo);
                if(ObjectUtils.isEmpty(merchantCollectOrders)){
                    dto.setErrorInfo("发送 充值订单回调 失败: 商户订单不存在：" + orderNo);
                    return dto;
                }
                dto.setTradeNotifyUrl(merchantCollectOrders.getTradeNotifyUrl());
                dto.setMerchantCollectOrders(merchantCollectOrders);
                //查询对应卖出订单
                PaymentOrder paymentOrder = paymentOrderMapper.selectPaymentByMerchantOrderForUpdate(merchantCollectOrders.getPlatformOrder());
                dto.setPaymentOrder(paymentOrder);
                //校验订单状态是否属于 未回调  回调失败  手动回调失败
                if (!NotifyStatusEnum.isUnsuccessful(merchantCollectOrders.getTradeCallbackStatus())) {
                    // 表示需要确认消费消息
                    dto.setNeedMqAck(true);
                    dto.setErrorInfo("发送 充值订单回调 失败: 订单已经是回调成功状态, 订单号: " + orderNo);
                    return dto;
                }
                //获取商户信息
                MerchantInfo merchantInfo = merchantInfoService.getMerchantInfoByCode(merchantCollectOrders.getMerchantCode());
                if (merchantInfo == null) {
                    dto.setErrorInfo("发送 充值订单回调 失败: 获取商户信息失败, 订单号: "+orderNo+", 商户号: " + merchantCollectOrders.getMerchantCode());
                    return dto;
                }

                String merchantPublicKeyStr = merchantInfo.getMerchantPublicKey();

                if (StringUtils.isEmpty(merchantPublicKeyStr)) {
                    dto.setErrorInfo("发送 充值订单回调 失败: 获取商户公钥失败, 订单号: " + orderNo + ", 商户号: " +  merchantCollectOrders.getMerchantCode());
                    return dto;
                }

                //商户公钥
                PublicKey merchantPublicKey = RsaUtil.getPublicKeyFromString(merchantPublicKeyStr);

                //平台私钥
                PrivateKey platformPrivateKey = RsaUtil.getPrivateKeyFromString(arProperty.getPrivateKey());

                //通知数据
                Object data = getRechargeCallBackParams(merchantCollectOrders);
                dto.setRequest(JSONObject.toJSONString(data));
                //签名并加密数据
                String res = send(merchantCollectOrders.getTradeNotifyUrl(), data, platformPrivateKey, merchantPublicKey, merchantInfo.getCode(), 1);
                dto.setResponse(res);
                if (StringUtils.isNotEmpty(res) && "SUCCESS".equals(res)) {
                    dto.setSuccess(true);
                    dto.setNeedMqAck(true);
                    return dto;
                }
                String errorInfo = "发送 充值订单回调 失败, 订单号: " + merchantCollectOrders.getPlatformOrder() + ", 返回数据: " + res;
                dto.setErrorInfo(errorInfo);
                //发送回调失败
                return dto;
            }
        }catch (Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            log.error("发送 充值订单回调 失败: 订单号: {}, e: {}", orderNo, e.getMessage());
            return dto;
        }finally {
            //释放锁
            if (req && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return dto;
    }


    /**
     * 获取充值回调接口参数
     * @param merchantCollectOrders merchantCollectOrders
     * @return {@link SendRechargeSuccessCallbackReq}
     */
    private SendRechargeSuccessCallbackReq getRechargeCallBackParams(MerchantCollectOrders merchantCollectOrders){
        SendRechargeSuccessCallbackReq sendRechargeSuccessCallbackReq = new SendRechargeSuccessCallbackReq();

        //商户号
        sendRechargeSuccessCallbackReq.setMerchantCode(merchantCollectOrders.getMerchantCode());

        //商户订单号
        sendRechargeSuccessCallbackReq.setMerchantTradeNo(merchantCollectOrders.getMerchantOrder());

        //平台订单号
        sendRechargeSuccessCallbackReq.setTradeNo(merchantCollectOrders.getPlatformOrder());

        //实际金额
        sendRechargeSuccessCallbackReq.setAmount(String.valueOf(merchantCollectOrders.getAmount()));

        //订单金额
        sendRechargeSuccessCallbackReq.setOrderAmount(String.valueOf(merchantCollectOrders.getOrderAmount()));

        //交易状态
        sendRechargeSuccessCallbackReq.setTradeStatus(merchantCollectOrders.getOrderStatus());

        //时间戳
        sendRechargeSuccessCallbackReq.setTimestamp(String.valueOf(System.currentTimeMillis() / 1000));

        //设置币种
        sendRechargeSuccessCallbackReq.setCurrency(merchantCollectOrders.getCurrency());
        return sendRechargeSuccessCallbackReq;
    }


    /**
     * 发送 提现订单 异步回调通知
     *
     * @param orderNo
     * @param type    1 自动回调  2 手动回调
     * @return {@link Boolean}
     */
    @Override
    public AsyncNotifyDTO sendWithdrawCallbackWithRecordRequest(String orderNo, String type) {
        AsyncNotifyDTO dto = new AsyncNotifyDTO();

        dto.setType(type);
        dto.setChangType(2);
        //查询代付订单
        MerchantPaymentOrders merchantPaymentOrders = merchantPaymentOrdersService.getOrderInfoByOrderNumber(orderNo);
        if (merchantPaymentOrders == null) {
            dto.setErrorInfo("发送 提现订单通知失败：订单不存在， 订单号：" + orderNo);
            return dto;
        }
        dto.setTradeNotifyUrl(merchantPaymentOrders.getTradeNotifyUrl());
        dto.setMerchantPaymentOrders(merchantPaymentOrders);
        CollectionOrder collectionOrder = collectionOrderMapper.selectCollectionOrderByMerchantOrderForUpdate(merchantPaymentOrders.getPlatformOrder());
        dto.setCollectionOrder(collectionOrder);
        //校验订单状态是否属于 未回调  回调失败  手动回调失败
        if (!NotifyStatusEnum.isUnsuccessful(merchantPaymentOrders.getTradeCallbackStatus())) {
            dto.setErrorInfo("发送 提现订单通知失败：订单已经是回调成功状态 , 订单号：" + orderNo);
            dto.setNeedMqAck(true);
            return dto;
        }
        //获取商户信息
        MerchantInfo merchantInfo = merchantInfoService.getMerchantInfoByCode(merchantPaymentOrders.getMerchantCode());
        if (merchantInfo == null) {
            dto.setErrorInfo("发送 提现订单通知失败: 获取商户信息失败 订单号：" + orderNo + ", 商户号:" + merchantPaymentOrders.getMerchantCode());
            return dto;
        }

        //获取商户公钥
        String merchantPublicKeyStr = merchantInfo.getMerchantPublicKey();
        if (StringUtils.isEmpty(merchantPublicKeyStr)) {
            dto.setErrorInfo("发送 提现订单通知失败: 获取商户公钥失败, 订单号：" + orderNo + ", 商户号:" + merchantPaymentOrders.getMerchantCode());
            return dto;
        }
        try {
            //商户公钥
            PublicKey merchantPublicKey = RsaUtil.getPublicKeyFromString(merchantPublicKeyStr);

            //平台私钥
            PrivateKey platformPrivateKey = RsaUtil.getPrivateKeyFromString(arProperty.getPrivateKey());

            SendRechargeSuccessCallbackReq sendRechargeSuccessCallbackReq = getRechargeCallBackParamsV1(merchantPaymentOrders);
            dto.setRequest(JSONObject.toJSONString(sendRechargeSuccessCallbackReq));
            String res = send(merchantPaymentOrders.getTradeNotifyUrl(), sendRechargeSuccessCallbackReq, platformPrivateKey, merchantPublicKey, merchantInfo.getCode(), 2);
            dto.setResponse(res);
            if (StringUtils.isNotEmpty(res) && "SUCCESS".equals(res)) {
                dto.setSuccess(true);
                dto.setNeedMqAck(true);
                return dto;
            }
            String errorInfo = "发送 提现订单通知失败:  订单号: " + merchantPaymentOrders.getPlatformOrder() + ", 返回数据: " + res;
            dto.setErrorInfo(errorInfo);
            //发送回调失败
            return dto;
        } catch (Exception e) {
            String error = "发送 提现订单通知失败: 订单号: "+orderNo+", 商户号: "+merchantPaymentOrders.getMerchantCode()+", e: "+ e.getMessage();
            log.error(error);
            dto.setErrorInfo(error);
            return dto;
        }
    }


    /**
     * 获取提现回调接口参数
     * @param merchantPaymentOrders merchantCollectOrders
     * @return {@link SendRechargeSuccessCallbackReq}
     */
    private SendRechargeSuccessCallbackReq getRechargeCallBackParamsV1(MerchantPaymentOrders merchantPaymentOrders){
        //通知数据
        SendRechargeSuccessCallbackReq sendRechargeSuccessCallbackReq = new SendRechargeSuccessCallbackReq();

        //商户号
        sendRechargeSuccessCallbackReq.setMerchantCode(merchantPaymentOrders.getMerchantCode());

        //商户订单号
        sendRechargeSuccessCallbackReq.setMerchantTradeNo(merchantPaymentOrders.getMerchantOrder());

        //平台订单号
        sendRechargeSuccessCallbackReq.setTradeNo(merchantPaymentOrders.getPlatformOrder());

        //实际金额
        sendRechargeSuccessCallbackReq.setAmount(merchantPaymentOrders.getAmount().toString());

        //交易状态
        sendRechargeSuccessCallbackReq.setTradeStatus(merchantPaymentOrders.getOrderStatus());

        //时间戳
        sendRechargeSuccessCallbackReq.setTimestamp(String.valueOf(System.currentTimeMillis() / 1000));

        //订单金额
        sendRechargeSuccessCallbackReq.setOrderAmount(String.valueOf(merchantPaymentOrders.getOrderAmount()));

        //币种
        sendRechargeSuccessCallbackReq.setCurrency(merchantPaymentOrders.getCurrency());
        return sendRechargeSuccessCallbackReq;
    }

    /**
     * 实际发送回调过程
     * @param url url
     * @param data data
     * @param platformPrivateKey platformPrivateKey
     * @param merchantPublicKey merchantPublicKey
     * @param merchantCode merchantCode
     * @return String
     * @throws Exception ex
     */
    private String send(String url, Object data, PrivateKey platformPrivateKey, PublicKey merchantPublicKey, String merchantCode, Integer type) throws Exception {
        String typeStr = (type == 1) ? "充值" : "提现";
        EncryptedData encryptedData = RsaUtil.signAndEncryptData(data, platformPrivateKey, merchantPublicKey);
        ApiResponseVo apiResponseVo = new ApiResponseVo();
        BeanUtils.copyProperties(encryptedData, apiResponseVo);
        apiResponseVo.setMerchantCode(merchantCode);
        //发送请求
        String res = RequestUtil.HttpRestClientToJson(url, JSON.toJSONString(apiResponseVo));
        log.info("发送 发送{}订单回调 请求地址: {}, 请求明文: {}, 请求密文: {}, 返回数据: {}", typeStr, url, data, apiResponseVo, res);
        return res;
    }
}
