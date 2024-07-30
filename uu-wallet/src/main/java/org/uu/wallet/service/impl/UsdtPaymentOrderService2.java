package org.uu.wallet.service.impl;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.tron.trident.core.ApiWrapper;
import org.tron.trident.proto.Response;
import org.uu.wallet.Enum.*;
import org.uu.wallet.entity.MerchantInfo;
import org.uu.wallet.entity.MerchantPaymentOrders;
import org.uu.wallet.entity.TaskInfo;
import org.uu.wallet.entity.WithdrawTronDetail;
import org.uu.wallet.mapper.MerchantInfoMapper;
import org.uu.wallet.rabbitmq.RabbitMQService;
import org.uu.wallet.service.IMerchantInfoService;
import org.uu.wallet.service.IMerchantPaymentOrdersService;
import org.uu.wallet.service.IWithdrawTronDetailService;
import org.uu.wallet.util.AmountChangeUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class UsdtPaymentOrderService2 {

    @Autowired
    private IMerchantPaymentOrdersService merchantPaymentOrdersService;

    @Autowired
    private MerchantInfoMapper merchantInfoMapper;

    @Autowired
    private IMerchantInfoService merchantInfoService;

    @Autowired
    private IWithdrawTronDetailService withdrawTronDetailService;

    @Autowired
    private RabbitMQService rabbitMQService;

    private final AmountChangeUtil amountChangeUtil;


    /**
     * 更新订单状态2
     *
     * @param orderId
     * @param txId
     * @param merchantPaymentOrders
     * @param address               源地址
     * @param withdrawTronDetail
     * @param wrapper
     * @return {@link Boolean }
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)//开启新的事务
    public Boolean updatePaymentOrder2(String orderId, String txId, MerchantPaymentOrders merchantPaymentOrders, String address, WithdrawTronDetail withdrawTronDetail, ApiWrapper wrapper) {

        int flag = 0;

        if (StringUtils.isNotBlank(txId)) {
            //获取交易结果
            while (true) {
                try {

                    log.info("USDT代付出款 updatePaymentOrder2, 订单号: {}, txId", merchantPaymentOrders.getPlatformOrder(), txId);

                    Response.TransactionInfo transactionInfo = wrapper.getTransactionInfoById(txId);
                    if (transactionInfo != null) {

                        if (transactionInfo.getResult() == Response.TransactionInfo.code.SUCESS) {

                            flag = 1;

                            log.info("USDT代付出款 循环获取交易记录成功 订单已成功, 订单号: {}", merchantPaymentOrders.getPlatformOrder());
                        } else {
                            flag = 2;

                            log.info("USDT代付出款 循环获取交易记录成功 订单已失败, 订单号: {}", merchantPaymentOrders.getPlatformOrder());
                        }

                        log.info("USDT代付出款 退出循环获取交易记录");
                        break;
                    } else {
                        log.error("USDT代付出款 循环获取交易记录失败 txId: {}", txId);
                    }
                } catch (Exception ex) {
                    log.error("USDT代付出款失败, 循环获取 交易结果 失败, 订单号: {}, ex: {}", merchantPaymentOrders.getPlatformOrder(), ex);
                    try {
                        Thread.sleep(2000);
                    } catch (Exception e) {
                    }
                }
            }
        } else {
            log.info("USDT代付出款失败 txId为null, 请人工进行确认交易状态 updatePaymentOrder2, 订单号: {}, txId", merchantPaymentOrders.getPlatformOrder(), txId);
        }

        try {
            if (flag == 1) {
                //交易成功
                //交易状态 1: 成功
                withdrawTronDetail.setStatus(1);
                //更新代付钱包交易记录
                boolean update1 = withdrawTronDetailService.updateById(withdrawTronDetail);

                //更新订单信息
                //转账状态 2: 转账成功
                merchantPaymentOrders.setTransferStatus(2);
                //交易id
                merchantPaymentOrders.setTxid(txId);
                //备注
                merchantPaymentOrders.setRemark("出款成功");
                //订单状态 成功
                merchantPaymentOrders.setOrderStatus(PaymentOrderStatusEnum.SUCCESS.getCode());
                //订单完成时间
                merchantPaymentOrders.setCompletionTime(LocalDateTime.now());

                boolean update2 = merchantPaymentOrdersService.updateById(merchantPaymentOrders);

                if (update1 && update2) {

                    //产生商户账变
                    //更新商户余额并记录商户账变
                    //记录商户账变 (订单金额)
                    Boolean updatemerchantInfo = amountChangeUtil.insertOrUpdateAccountChange(
                            merchantPaymentOrders.getMerchantCode(),//商户号
                            merchantPaymentOrders.getAmount(),//账变金额 (订单金额)
                            ChangeModeEnum.SUB,//账变类型 支出
                            "USDT",//币种
                            merchantPaymentOrders.getPlatformOrder(),//平台订单号
                            AccountChangeEnum.PAYMENT,//账变类型 代付
                            "USDT-API代付",//备注
                            merchantPaymentOrders.getMerchantOrder(),
                            ChannelEnum.USDT.getName(),//商户支付通道
                            "",
                            BalanceTypeEnum.TRC20.getName()//余额通道
                    );

                    if (!updatemerchantInfo) {
                        log.error("USDT代付出款失败: 更新商户信息失败，触发事务回滚。 订单信息: {}", merchantPaymentOrders);
                        // 抛出运行时异常
                        throw new RuntimeException("USDT代付出款失败: 更新商户信息失败，触发事务回滚。");
                    }


                    //订单费用 = 费用 + 单笔手续费
                    BigDecimal orderFee = merchantPaymentOrders.getCost().add(merchantPaymentOrders.getFixedFee());

                    // 订单费用大于0 才记录 订单费用的账变
                    if (orderFee.compareTo(BigDecimal.ZERO) > 0) {
                        //记录商户账变 (订单费用)
                        Boolean updatemerchantInfoFee = amountChangeUtil.insertOrUpdateAccountChange(
                                merchantPaymentOrders.getMerchantCode(),//商户号
                                orderFee,//账变金额 (订单总费用)
                                ChangeModeEnum.SUB,//账变类型 支出
                                "USDT",//币种
                                merchantPaymentOrders.getPlatformOrder(),//平台订单号
                                AccountChangeEnum.PAYMENT_FEE,//账变类型 代付费用
                                "USDT-API代付费用",//备注
                                merchantPaymentOrders.getMerchantOrder(),
                                ChannelEnum.USDT.getName(),//商户支付通道
                                "",
                                BalanceTypeEnum.TRC20.getName()//余额通道
                        );

                        if (!updatemerchantInfoFee) {
                            log.error("USDT代付出款失败: 更新商户信息失败，触发事务回滚。 订单信息: {}", merchantPaymentOrders);
                            // 抛出运行时异常
                            throw new RuntimeException("USDT代付出款失败: 更新商户信息失败，触发事务回滚。");
                        }
                    }

                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            //事务提交成功

                            log.info("USDT代付出款, 事务提交成功, 订单号: {}", merchantPaymentOrders.getPlatformOrder());

                            try {
                                //发送回调通知MQ
                                //发送提现成功异步延时回调通知
                                long millis = 3000L;
                                //发送提现延时回调的MQ消息
                                TaskInfo taskInfo = new TaskInfo(merchantPaymentOrders.getPlatformOrder(), TaskTypeEnum.WITHDRAW_NOTIFICATION_TIMEOUT.getCode(), System.currentTimeMillis());
                                rabbitMQService.sendTimeoutTask(taskInfo, millis);
                            } catch (Exception e) {
                                log.error("USDT代付出款, 事务提交成功, 发送MQ失败 订单号: {}", merchantPaymentOrders.getPlatformOrder());
                            }
                        }
                    });
                }
            } else if (flag == 2) {
                //更新代付钱包交易状态 失败
                withdrawTronDetail.setStatus(2);
                //更新代付钱包交易记录
                boolean update1 = withdrawTronDetailService.updateById(withdrawTronDetail);

                //更新代付订单信息
                merchantPaymentOrders.setTxid("");
                //备注
                merchantPaymentOrders.setRemark("出款失败");
                //订单状态 失败
                merchantPaymentOrders.setOrderStatus(PaymentOrderStatusEnum.FAILED.getCode());
                boolean update2 = merchantPaymentOrdersService.updateById(merchantPaymentOrders);

                if (update1 && update2) {

                    //将交易中金额退回到商户余额
                    //获取商户信息 加上排他行锁
                    MerchantInfo merchantInfo = merchantInfoMapper.selectMerchantInfoForUpdate(merchantPaymentOrders.getMerchantCode());

                    //订单金额总计 (订单金额 + 费用 + 单笔手续费)
                    BigDecimal allAmount = merchantPaymentOrders.getAmount().add(merchantPaymentOrders.getCost()).add(merchantPaymentOrders.getFixedFee());

                    //更新商户余额 将订单金额所需费用划转到交易中金额
                    LambdaUpdateWrapper<MerchantInfo> lambdaUpdateWrapperMerchantInfo = new LambdaUpdateWrapper<>();
                    lambdaUpdateWrapperMerchantInfo.eq(MerchantInfo::getCode, merchantInfo.getCode())  // 指定更新条件 商户号
                            .set(MerchantInfo::getUsdtBalance, merchantInfo.getUsdtBalance().add(allAmount)) // 指定更新字段 (增加商户余额 + 总金额)
                            .set(MerchantInfo::getPendingUsdtBalance, merchantInfo.getPendingUsdtBalance().subtract(allAmount)); // 指定更新字段 (减少交易中金额 - 总金额)
                    // 这里传入的 null 表示不更新实体对象的其他字段
                    merchantInfoService.update(null, lambdaUpdateWrapperMerchantInfo);

                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            //事务提交成功

                            try {
                                log.info("USDT代付出款, 事务提交成功, 订单号: {}", merchantPaymentOrders.getPlatformOrder());

                                //发送回调通知MQ
                                //发送提现失败异步延时回调通知
                                long millis = 3000L;
                                //发送提现延时回调的MQ消息
                                TaskInfo taskInfo = new TaskInfo(merchantPaymentOrders.getPlatformOrder(), TaskTypeEnum.WITHDRAW_NOTIFICATION_TIMEOUT.getCode(), System.currentTimeMillis());
                                rabbitMQService.sendTimeoutTask(taskInfo, millis);
                            } catch (Exception e) {
                                log.error("USDT代付出款, 事务提交成功, 发送MQ失败 订单号: {}", merchantPaymentOrders.getPlatformOrder());
                            }
                        }
                    });
                }
            }
        } catch (Exception e) {
            log.error("USDT代付出款失败, 更新订单信息或商户信息失败 订单号: {}. e: {}", merchantPaymentOrders.getPlatformOrder(), e.getMessage());
            //手动回滚
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
        return true;
    }
}
