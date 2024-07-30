package org.uu.wallet.consumer;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.RequiredArgsConstructor;
import org.uu.common.core.constant.RabbitMqConstants;
import org.uu.wallet.entity.CustomCorrelationData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ConfirmCallback;
import org.springframework.stereotype.Component;

/**
 * 处理消息确认
 *
 * @author Simon
 * @date 2023/11/21
 */
@Component
@RequiredArgsConstructor
public class MsgConfirmCallback implements ConfirmCallback {

    private static final Logger logger = LoggerFactory.getLogger(MsgConfirmCallback.class);

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {

        //校验 CorrelationData 格式是否正确
        if (correlationData instanceof CustomCorrelationData) {

            CustomCorrelationData customData = (CustomCorrelationData) correlationData;

            if (customData != null) {
                if (ack) {
                    switch (customData.getQueueName()) {
                        case RabbitMqConstants.UU_WALLET_TRADE_COLLECT_QUEUE_NAME:

                            //钱包项目-充值成功交易通知
                            logger.info(customData.getOrderNo() + ": 充值成功, 异步回调商户 消息发送成功: " + JSON.toJSONString(customData, SerializerFeature.WriteMapNullValue));
                            //更新充值交易MQ发送状态
//                            matchingOrderService.updateCollectionTradeSend(customData.getOrderNo());
                            break;

                        case RabbitMqConstants.UU_WALLET_TRADE_PAYMENT_QUEUE_NAME:

                            //钱包项目-提现成功交易通知
                            logger.info(customData.getOrderNo() + ": 提现成功, 异步回调商户 消息发送成功: " + JSON.toJSONString(customData, SerializerFeature.WriteMapNullValue));
                            //更新提现交易MQ发送状态
//                            matchingOrderService.updatePaymentTradeSend(customData.getOrderNo());
                            break;

                        case RabbitMqConstants.UU_WALLET_MEMBER_MATCH_TIMEOUT_QUEUE:
                            //钱包会员匹配超时
                            logger.info(customData.getOrderNo() + " 钱包会员匹配超时 " + " 消息发送成功: " + JSON.toJSONString(customData, SerializerFeature.WriteMapNullValue));
                            break;
                        case RabbitMqConstants.UU_WALLET_MERCHANT_MEMBER_MATCH_TIMEOUT_QUEUE:
                            //商户会员匹配超时
                            logger.info(customData.getOrderNo() + " 商户会员匹配超时 " + " 消息发送成功: " + JSON.toJSONString(customData, SerializerFeature.WriteMapNullValue));
                            break;
                        case RabbitMqConstants.UU_WALLET_MEMBER_CONFIRM_TIMEOUT_QUEUE:
                            //钱包会员确认超时
                            logger.info(customData.getOrderNo() + " 钱包会员确认超时 " + " 消息发送成功: " + JSON.toJSONString(customData, SerializerFeature.WriteMapNullValue));
                            break;
                        case RabbitMqConstants.UU_WALLET_MERCHANT_MEMBER_CONFIRM_TIMEOUT_QUEUE:
                            //商户会员确认超时
                            logger.info(customData.getOrderNo() + " 商户会员确认超时 " + " 消息发送成功: " + JSON.toJSONString(customData, SerializerFeature.WriteMapNullValue));
                            break;
                        case RabbitMqConstants.UU_WALLET_MEMBER_PAYMENT_TIMEOUT_QUEUE:
                            //会员支付超时
                            logger.info(customData.getOrderNo() + " 会员支付超时 " + " 消息发送成功: " + JSON.toJSONString(customData, SerializerFeature.WriteMapNullValue));
                            break;
                        case RabbitMqConstants.WALLET_MEMBER_OPERATION_LOG_QUEUE:
                            //记录会员操作日志
                            logger.info("记录会员操作日志 消息发送成功: " + JSON.toJSONString(customData, SerializerFeature.WriteMapNullValue));
                            break;
                        case RabbitMqConstants.WALLET_MEMBER_NOTIFY_SELLER_BY_VOICE_QUEUE:
                            //语音通知会员
                            logger.info("语音通知会员 消息发送成功: " + JSON.toJSONString(customData, SerializerFeature.WriteMapNullValue));
                            break;
                        case RabbitMqConstants.WALLET_MERCHANT_COLLECT_ORDER_PAYMENT_TIMEOUT_QUEUE:
                            //代收订单支付超时
                            logger.info("代收订单支付超时 消息发送成功: " + JSON.toJSONString(customData, SerializerFeature.WriteMapNullValue));
                            break;
                        case RabbitMqConstants.WALLET_MEMBER_DISABLE_QUEUE:
                            //禁用会员
                            logger.info("禁用会员 消息发送成功: " + JSON.toJSONString(customData, SerializerFeature.WriteMapNullValue));
                            break;
                        case RabbitMqConstants.WALLET_ORDER_TAGGING_QUEUE:
                            //标记订单
                            logger.info("标记订单 消息发送成功: " + JSON.toJSONString(customData, SerializerFeature.WriteMapNullValue));
                            break;
                        case RabbitMqConstants.WALLET_TRADE_IP_BLACK_ADD_QUEUE:
                            //添加交易IP黑名单
                            logger.info("添加交易IP黑名单 消息发送成功: " + JSON.toJSONString(customData, SerializerFeature.WriteMapNullValue));
                            break;
                        case RabbitMqConstants.WALLET_MEMBER_CONFIRM_TIMEOUT_RISK_TAG_QUEUE:
                            //会员确认超时风控标记
                            logger.info("会员确认超时风控标记 消息发送成功: " + JSON.toJSONString(customData, SerializerFeature.WriteMapNullValue));
                            break;
                        case RabbitMqConstants.WITHDRAW_NOTIFY_TIMEOUT_QUEUE:
                            //提现交易延时通知
                            logger.info("提现交易延时通知 消息发送成功: " + JSON.toJSONString(customData, SerializerFeature.WriteMapNullValue));
                            break;
                        case RabbitMqConstants.WALLET_MEMBER_UPGRADE_QUEUE:
                            //禁用会员
                            logger.info("会员升级 消息发送成功: " + JSON.toJSONString(customData, SerializerFeature.WriteMapNullValue));
                            break;
                        case RabbitMqConstants.WALLET_MEMBER_KYC_TRANSACTION_QUEUE:
                            //禁用会员
                            logger.info("获取KYC银行交易记录 消息发送成功: " + JSON.toJSONString(customData, SerializerFeature.WriteMapNullValue));
                            break;
                        case RabbitMqConstants.CASH_BACK_ORDER_PROCESS_QUEUE:
                            logger.info("余额退回订单 消息发送成功" + JSON.toJSONString(customData, SerializerFeature.WriteMapNullValue));
                            break;
                        case RabbitMqConstants.UU_WALLET_SYNC_BANK_INFO_QUEUE:
                            //钱包项目-同步银行卡
                            logger.info(customData.getOrderNo() + ": 同步银行卡 消息发送成功: " + JSON.toJSONString(customData, SerializerFeature.WriteMapNullValue));
                            break;
                        case RabbitMqConstants.UU_WALLET_USDT_AUTO_CREDIT_QUEUE:
                            //uuPay-USDT自动上分
                            logger.info(customData.getOrderNo() + ": USDT自动上分 消息发送成功: " + JSON.toJSONString(customData, SerializerFeature.WriteMapNullValue));
                            break;
                        case RabbitMqConstants.UU_WALLET_FETCH_BLOCK_DATA_QUEUE:
                            //uuPay-拉取区块数据
                            logger.info(customData.getOrderNo() + ": 拉取区块数据 消息发送成功: " + JSON.toJSONString(customData, SerializerFeature.WriteMapNullValue));
                            break;
                        case RabbitMqConstants.KYC_AUTO_COMPLETE_QUEUE:
                            //自动完成kyc交易账变处理
                            logger.info(customData.getOrderNo() + ": 自动完成kyc交易账变处理 消息发送成功: " + JSON.toJSONString(customData, SerializerFeature.WriteMapNullValue));
                            break;
                        case RabbitMqConstants.UU_WALLET_USDT_COLLECTION_QUEUE:
                            //USDT资金归集
                            logger.info(customData.getOrderNo() + ": USDT资金归集 消息发送成功: " + JSON.toJSONString(customData, SerializerFeature.WriteMapNullValue));
                            break;
                        case RabbitMqConstants.UU_WALLET_REALTIME_EXCHANGE_RATE_QUEUE:
                            //获取实时汇率
                            logger.info(customData.getOrderNo() + ": 获取实时汇率 消息发送成功: " + JSON.toJSONString(customData, SerializerFeature.WriteMapNullValue));
                            break;
                        case RabbitMqConstants.UU_WALLET_USDT_PAYMENT_ORDER_QUEUE:
                            //处理USDT代付订单
                            logger.info(customData.getOrderNo() + ": 处理USDT代付订单 消息发送成功: " + JSON.toJSONString(customData, SerializerFeature.WriteMapNullValue));
                            break;
                        case RabbitMqConstants.UU_WALLET_TRX_AUTO_CREDIT_QUEUE:
                            //TRX自动上分
                            logger.info(customData.getOrderNo() + ": TRX自动上分 消息发送成功: " + JSON.toJSONString(customData, SerializerFeature.WriteMapNullValue));
                            break;
                        case RabbitMqConstants.UU_WALLET_TRX_PAYMENT_ORDER_QUEUE:
                            //处理TRX代付订单
                            logger.info(customData.getOrderNo() + ": 处理TRX代付订单 消息发送成功: " + JSON.toJSONString(customData, SerializerFeature.WriteMapNullValue));
                            break;
                        case RabbitMqConstants.KYC_WITHDRAW_COMPLETE_PROCESS_QUEUE:
                            //处理kyc提现订单
                            logger.info(customData.getOrderNo() + ": 处理kyc提现订单订单 消息发送成功: " + JSON.toJSONString(customData, SerializerFeature.WriteMapNullValue));
                            break;
                        default:
                            logger.warn("未知的队列名称: " + customData.getQueueName());
                            break;
                    }
                } else {
                    //这里做MQ重发消息机制 (nacos配置了发送端的重试机制 所以会自动触发消息重发机制)
                    logger.info(customData.getOrderNo() + ": 消息发送失败: " + JSON.toJSONString(customData, SerializerFeature.WriteMapNullValue));
                }
            } else {
                logger.error("MQ消息确认 CustomCorrelationData 为null...");
            }
        } else {
            logger.error("MQ消息确认 correlationData 格式不正确...");
        }
    }
}