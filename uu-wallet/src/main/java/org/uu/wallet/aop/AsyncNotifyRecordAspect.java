package org.uu.wallet.aop;


import cn.hutool.core.bean.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.uu.wallet.Enum.NotifyStatusEnum;
import org.uu.wallet.dto.AsyncNotifyDTO;
import org.uu.wallet.entity.CollectionOrder;
import org.uu.wallet.entity.MerchantCollectOrders;
import org.uu.wallet.entity.MerchantPaymentOrders;
import org.uu.wallet.entity.PaymentOrder;
import org.uu.wallet.mapper.CollectionOrderMapper;
import org.uu.wallet.mapper.MerchantCollectOrdersMapper;
import org.uu.wallet.mapper.MerchantPaymentOrdersMapper;
import org.uu.wallet.mapper.PaymentOrderMapper;
import org.uu.wallet.util.DurationCalculatorUtil;

import javax.annotation.Resource;
import java.time.LocalDateTime;


/**
 * @author lukas
 */
@Aspect
@Component
@Slf4j
public class AsyncNotifyRecordAspect {
    @Resource
    PaymentOrderMapper paymentOrderMapper;
    @Resource
    MerchantCollectOrdersMapper merchantCollectOrdersMapper;
    @Resource
    MerchantPaymentOrdersMapper merchantPaymentOrdersMapper;
    @Resource
    CollectionOrderMapper collectionOrderMapper;

    @Pointcut("execution(* org.uu.wallet.service.impl.AsyncNotifyWithAopServiceImpl.*CallbackWithRecordRequest*(..))")
    public void recordLog() {
    }

    @Around("recordLog()")
    public AsyncNotifyDTO httpsCheck(ProceedingJoinPoint joinPoint) throws Throwable {
        AsyncNotifyDTO dto = new AsyncNotifyDTO();
        String processTypeInfo = "";
        String changeType = "";
        try {
            dto = (AsyncNotifyDTO) joinPoint.proceed();
            processTypeInfo = "1".equals(dto.getType()) ? "MQ异步回调通知" : "手动回调通知";
            changeType = dto.getChangType() == 1 ? "充值" : "提现";
            if(dto.isSuccess()){
                successProcess(dto);
                return dto;
            }
            // 打印报错信息
            if(ObjectUtils.isNotEmpty(dto.getErrorInfo())){
                log.error(dto.getErrorInfo());
            }
            // 失败并且订单信息存在
            failedProcess(dto);
            return dto;
        }catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            dto.setErrorInfo(e.getMessage());
            log.error("{} 发送  {} 订单回调 失败: 更新订单信息失败{}， info:{}", processTypeInfo, changeType , e.getMessage(), dto);
            return dto;
        }
    }

    /**
     * 回调成功处理
     * @param dto dto
     */
    private void successProcess(AsyncNotifyDTO dto) {
        setSuccessInfo(dto);
        if (dto.getChangType() == 1) {
            // 调用接口成功 需要更新订单信息
            if (ObjectUtils.isNotEmpty(dto.getPaymentOrder())) {
                paymentOrderMapper.updateById(dto.getPaymentOrder());
            }
            if (ObjectUtils.isNotEmpty(dto.getMerchantCollectOrders())) {
                merchantCollectOrdersMapper.updateById(dto.getMerchantCollectOrders());
            }
        } else {
            // 调用接口成功 需要更新订单信息
            if (ObjectUtils.isNotEmpty(dto.getCollectionOrder())) {
                collectionOrderMapper.updateById(dto.getCollectionOrder());
            }
            if (ObjectUtils.isNotEmpty(dto.getMerchantPaymentOrders())) {
                merchantPaymentOrdersMapper.updateById(dto.getMerchantPaymentOrders());
            }
        }
    }

    /**
     * 回调失败处理
     * @param dto dto
     */
    private void failedProcess(AsyncNotifyDTO dto) {
        setFailedInfo(dto);
        if(dto.getChangType() == 1){
            if(ObjectUtils.isNotEmpty(dto.getPaymentOrder()) && ObjectUtils.isNotEmpty(dto.getPaymentOrder().getId())){
                PaymentOrder paymentOrder = dto.getPaymentOrder();
                paymentOrderMapper.updateById(paymentOrder);
            }
            if(ObjectUtils.isNotEmpty(dto.getMerchantCollectOrders()) && ObjectUtils.isNotEmpty(dto.getMerchantCollectOrders().getId())){
                MerchantCollectOrders merchantCollectOrders = dto.getMerchantCollectOrders();
                merchantCollectOrdersMapper.updateById(merchantCollectOrders);
            }
        }else{
            if(ObjectUtils.isNotEmpty(dto.getCollectionOrder()) && ObjectUtils.isNotEmpty(dto.getCollectionOrder().getId())){
                CollectionOrder collectionOrder = dto.getCollectionOrder();
                collectionOrderMapper.updateById(collectionOrder);
            }
            if(ObjectUtils.isNotEmpty(dto.getMerchantPaymentOrders()) && ObjectUtils.isNotEmpty(dto.getMerchantPaymentOrders().getId())){
                MerchantPaymentOrders merchantPaymentOrders = dto.getMerchantPaymentOrders();
                merchantPaymentOrdersMapper.updateById(merchantPaymentOrders);
            }
        }
    }

    /**
     * 设置回调成功参数
     * @param dto dto
     */
    private void setSuccessInfo(AsyncNotifyDTO dto) {
        String successCode = "1".equals(dto.getType()) ? NotifyStatusEnum.SUCCESS.getCode() : NotifyStatusEnum.MANUAL_SUCCESS.getCode();
        LocalDateTime now = LocalDateTime.now();
        if(dto.getChangType() == 1){
            MerchantCollectOrders merchantCollectOrders = dto.getMerchantCollectOrders();

            //更新订单完成时间
            merchantCollectOrders.setCompletionTime(now);

            //交易回调状态
            //判断是自动回调还是手动回调
            merchantCollectOrders.setTradeCallbackStatus(
                    successCode
            );
            //交易回调时间
            merchantCollectOrders.setTradeCallbackTime(now);

            //计算完成时长
            merchantCollectOrders.setCompleteDuration(DurationCalculatorUtil.orderCompleteDuration(merchantCollectOrders.getCreateTime(), now));

            //更新订单信息
            merchantCollectOrders.setTradeCallbackRequest(dto.getRequest());
            merchantCollectOrders.setTradeCallbackResponse(dto.getResponse());
            dto.setMerchantCollectOrders(merchantCollectOrders);

            PaymentOrder paymentOrder = dto.getPaymentOrder();
            if(paymentOrder != null){
                paymentOrder.setTradeNotifyUrl(dto.getTradeNotifyUrl());
                paymentOrder.setTradeCallbackRequest(dto.getRequest());
                paymentOrder.setTradeCallbackResponse(dto.getResponse());
                paymentOrder.setTradeNotifySend("1");
                paymentOrder.setCompletionTime(now);
                paymentOrder.setTradeCallbackStatus(
                        successCode
                );
                paymentOrder.setTradeCallbackTime(now);
                paymentOrder.setCompleteDuration(DurationCalculatorUtil.orderCompleteDuration(merchantCollectOrders.getCreateTime(), now));
                dto.setPaymentOrder(paymentOrder);
            }
        }else{
            MerchantPaymentOrders merchantPaymentOrders = dto.getMerchantPaymentOrders();

            //更新订单完成时间
            merchantPaymentOrders.setCompletionTime(now);

            //交易回调状态
            //判断是自动回调还是手动回调
            merchantPaymentOrders.setTradeCallbackStatus(
                    successCode
            );
            //交易回调时间
            merchantPaymentOrders.setTradeCallbackTime(now);

            //计算完成时长
            merchantPaymentOrders.setCompleteDuration(DurationCalculatorUtil.orderCompleteDuration(merchantPaymentOrders.getCreateTime(), now));

            //更新订单信息
            merchantPaymentOrders.setTradeCallbackRequest(dto.getRequest());
            merchantPaymentOrders.setTradeCallbackResponse(dto.getResponse());
            dto.setMerchantPaymentOrders(merchantPaymentOrders);

            CollectionOrder collectionOrder = dto.getCollectionOrder();
            if(collectionOrder != null){
                collectionOrder.setTradeNotifyUrl(dto.getTradeNotifyUrl());
                collectionOrder.setTradeCallbackRequest(dto.getRequest());
                collectionOrder.setTradeCallbackResponse(dto.getResponse());
                collectionOrder.setTradeNotifySend("1");
                collectionOrder.setCompletionTime(now);
                collectionOrder.setTradeCallbackStatus(
                        successCode
                );
                collectionOrder.setTradeCallbackTime(now);
                collectionOrder.setCompleteDuration(DurationCalculatorUtil.orderCompleteDuration(collectionOrder.getCreateTime(), now));
                dto.setCollectionOrder(collectionOrder);
            }
        }
    }

    /**
     * 设置失败参数
     * @param dto dto
     */
    private void setFailedInfo(AsyncNotifyDTO dto) {
        String failedCode = "1".equals(dto.getType()) ? NotifyStatusEnum.FAILED.getCode() : NotifyStatusEnum.MANUAL_FAILED.getCode();
        if(dto.getChangType() == 1){
            if(ObjectUtils.isNotEmpty(dto.getPaymentOrder())){
                String response = ObjectUtils.isEmpty(dto.getResponse()) ? dto.getErrorInfo() : dto.getResponse();
                dto.getPaymentOrder().setTradeNotifyUrl(dto.getTradeNotifyUrl());
                dto.getPaymentOrder().setTradeCallbackRequest(dto.getRequest());
                dto.getPaymentOrder().setTradeCallbackResponse(response);
                dto.getPaymentOrder().setTradeNotifyUrl(dto.getTradeNotifyUrl());
                dto.getPaymentOrder().setTradeCallbackStatus(failedCode);
                dto.getPaymentOrder().setTradeCallbackTime(LocalDateTime.now());
            }
            if(ObjectUtils.isNotEmpty(dto.getMerchantCollectOrders())){
                String response = ObjectUtils.isEmpty(dto.getResponse()) ? dto.getErrorInfo() : dto.getResponse();
                dto.getMerchantCollectOrders().setTradeNotifyUrl(dto.getTradeNotifyUrl());
                dto.getMerchantCollectOrders().setTradeCallbackRequest(dto.getRequest());
                dto.getMerchantCollectOrders().setTradeCallbackResponse(response);
                dto.getMerchantCollectOrders().setTradeCallbackStatus(failedCode);
                dto.getMerchantCollectOrders().setTradeCallbackTime(LocalDateTime.now());
            }
        }else {
            if(ObjectUtils.isNotEmpty(dto.getCollectionOrder())){
                String response = ObjectUtils.isEmpty(dto.getResponse()) ? dto.getErrorInfo() : dto.getResponse();
                dto.getCollectionOrder().setTradeNotifyUrl(dto.getTradeNotifyUrl());
                dto.getCollectionOrder().setTradeCallbackRequest(dto.getRequest());
                dto.getCollectionOrder().setTradeCallbackResponse(response);
                dto.getCollectionOrder().setTradeCallbackStatus(failedCode);
                dto.getCollectionOrder().setTradeCallbackTime(LocalDateTime.now());

            }
            if(ObjectUtils.isNotEmpty(dto.getMerchantPaymentOrders())){
                String response = ObjectUtils.isEmpty(dto.getResponse()) ? dto.getErrorInfo() : dto.getResponse();
                dto.getMerchantPaymentOrders().setTradeNotifyUrl(dto.getTradeNotifyUrl());
                dto.getMerchantPaymentOrders().setTradeCallbackRequest(dto.getRequest());
                dto.getMerchantPaymentOrders().setTradeCallbackResponse(response);
                dto.getMerchantPaymentOrders().setTradeCallbackStatus(failedCode);
                dto.getMerchantPaymentOrders().setTradeCallbackTime(LocalDateTime.now());

            }
        }

    }
}
