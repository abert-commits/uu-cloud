package org.uu.wallet.rabbitmq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import lombok.extern.slf4j.Slf4j;
import org.uu.common.core.constant.RabbitMqConstants;
import org.uu.common.core.message.CommissionAndDividendsMessage;
import org.uu.common.core.message.ExecuteCommissionAndDividendsMessage;
import org.uu.common.core.message.KycCompleteMessage;
import org.uu.common.pay.req.OrderEventReq;
import org.uu.wallet.Enum.TaskTypeEnum;
import org.uu.wallet.entity.*;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.uu.wallet.req.KycAutoCompleteReq;

import java.util.UUID;

@Service
@Slf4j
public class RabbitMQService {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 使用Rabbit MQ死信队列机制 处理延时任务
     * 1.先把消息发送到主队列 并设置消息过期时间
     * 2.消息过期后 会被路由到死信队列 处理延时任务的消费者监听死信队列 进行处理延时任务
     *
     * @param taskInfo
     * @param delayMillis
     */
    public void sendTimeoutTask(TaskInfo taskInfo, long delayMillis) {

        String type = "";
        String queueName = "";

        switch (taskInfo.getTaskType()) {
            case "1":
                type = "钱包用户确认超时";
                queueName = RabbitMqConstants.UU_WALLET_MEMBER_CONFIRM_TIMEOUT_QUEUE;
                break;
            case "2":
                type = "商户会员确认超时";
                queueName = RabbitMqConstants.UU_WALLET_MERCHANT_MEMBER_CONFIRM_TIMEOUT_QUEUE;
                break;
            case "3":
                type = "钱包用户卖出匹配超时";
                queueName = RabbitMqConstants.UU_WALLET_MEMBER_MATCH_TIMEOUT_QUEUE;
                break;
            case "4":
                type = "商户会员卖出匹配超时";
                queueName = RabbitMqConstants.UU_WALLET_MERCHANT_MEMBER_MATCH_TIMEOUT_QUEUE;
                break;
            case "5":
                type = "支付超时";
                queueName = RabbitMqConstants.UU_WALLET_MEMBER_PAYMENT_TIMEOUT_QUEUE;
                break;
            case "6":
                type = "USDT支付超时";
                queueName = RabbitMqConstants.UU_WALLET_MEMBER_PAYMENT_TIMEOUT_QUEUE;
                break;
            case "9":
                type = "语音通知卖方";
                queueName = RabbitMqConstants.WALLET_MEMBER_NOTIFY_SELLER_BY_VOICE_QUEUE;
                break;
            case "10":
                type = "代收订单支付超时";
                queueName = RabbitMqConstants.WALLET_MERCHANT_COLLECT_ORDER_PAYMENT_TIMEOUT_QUEUE;
                break;
            case "13":
                type = "会员确认超时风控标记订单";
                queueName = RabbitMqConstants.WALLET_MEMBER_CONFIRM_TIMEOUT_RISK_TAG_QUEUE;
                break;
            case "14":
                type = "提现交易延时通知";
                queueName = RabbitMqConstants.WITHDRAW_NOTIFY_TIMEOUT_QUEUE;
                break;
            case "17":
                type = "余额退回";
                queueName = RabbitMqConstants.CASH_BACK_ORDER_PROCESS_QUEUE;
                break;
            case "27":
                type = "kyc提现订单自动完成";
                queueName = RabbitMqConstants.KYC_WITHDRAW_COMPLETE_PROCESS_QUEUE;
                break;
        }

        // 创建自定义CorrelationData
        CustomCorrelationData correlationData = new CustomCorrelationData(UUID.randomUUID().toString(), taskInfo.getOrderNo(), taskInfo.getTaskType(), queueName);

        // 使用 Fastjson 将对象转换为 JSON 字符串
        String messageJson = JSON.toJSONString(taskInfo);

        // 创建消息属性
        MessageProperties messageProperties = new MessageProperties();
        //设置消息为持久化
        messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
        //设置消息的 TTL过期时间
        messageProperties.setExpiration(String.valueOf(delayMillis));

        // 创建消息
        Message message = new Message(messageJson.getBytes(), messageProperties);

        log.info("RabbitMQ发送 " + type + "消息: 延迟时间: {}, 消息内容: {}, taskInfo: {}", delayMillis / 1000 + "秒", message, taskInfo);

        // 发送消息到默认交换机，并使用队列名称作为路由键
        rabbitTemplate.convertAndSend("", queueName, message, correlationData);
    }


    /**
     * 发送 充值成功 回调的MQ
     */
    public void sendRechargeSuccessCallbackNotification(TaskInfo taskInfo) {

        String queueName = RabbitMqConstants.UU_WALLET_TRADE_COLLECT_QUEUE_NAME;

        // 创建自定义CorrelationData
        CustomCorrelationData correlationData = new CustomCorrelationData(
                UUID.randomUUID().toString(),
                taskInfo.getOrderNo(),
                taskInfo.getTaskType(),
                queueName
        );

        // 使用 Fastjson 将对象转换为 JSON 字符串
        String messageJson = JSON.toJSONString(taskInfo);

        // 创建消息属性
        MessageProperties messageProperties = new MessageProperties();
        //设置消息为持久化
        messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);

        // 创建消息
        Message message = new Message(messageJson.getBytes(), messageProperties);

        log.info("RabbitMQ发送 充值成功 回调MQ消息成功 消息内容: {}, taskInfo: {}", message, taskInfo);

        // 发送消息到默认交换机，并使用队列名称作为路由键
        rabbitTemplate.convertAndSend("", queueName, message, correlationData);
    }


    /**
     * 发送 提现成功 回调的MQ
     */
    public void sendWithdrawalSuccessCallbackNotification(TaskInfo taskInfo) {

        String queueName = RabbitMqConstants.UU_WALLET_TRADE_PAYMENT_QUEUE_NAME;

        // 创建自定义CorrelationData
        CustomCorrelationData correlationData = new CustomCorrelationData(
                UUID.randomUUID().toString(),
                taskInfo.getOrderNo(),
                taskInfo.getTaskType(),
                queueName
        );

        // 使用 Fastjson 将对象转换为 JSON 字符串
        String messageJson = JSON.toJSONString(taskInfo);

        // 创建消息属性
        MessageProperties messageProperties = new MessageProperties();
        //设置消息为持久化
        messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);

        // 创建消息
        Message message = new Message(messageJson.getBytes(), messageProperties);

        log.info("RabbitMQ发送 提现成功 回调MQ消息成功 消息内容: {}, taskInfo: {}", message, taskInfo);

        // 发送消息到默认交换机，并使用队列名称作为路由键
        rabbitTemplate.convertAndSend("", queueName, message, correlationData);
    }


    /**
     * 发送 记录操作日志的MQ
     *
     * @param memberOperationLogMessage
     */
    public void sendMemberOperationLogMessage(MemberOperationLogMessage memberOperationLogMessage) {


        String queueName = RabbitMqConstants.WALLET_MEMBER_OPERATION_LOG_QUEUE;

        // 创建自定义CorrelationData
        CustomCorrelationData correlationData = new CustomCorrelationData(
                UUID.randomUUID().toString(),
                "1",
                "1",
                queueName
        );

        // 使用 Fastjson 将对象转换为 JSON 字符串
        String messageJson = JSON.toJSONString(memberOperationLogMessage);

        // 创建消息属性
        MessageProperties messageProperties = new MessageProperties();
        //设置消息为持久化
        messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);

        // 创建消息
        Message message = new Message(messageJson.getBytes(), messageProperties);

        log.info("RabbitMQ发送 记录前台会员操作日志的MQ 消息内容: {}, loginLogMessage: {}", message, memberOperationLogMessage);

        // 发送消息到默认交换机，并使用队列名称作为路由键
        rabbitTemplate.convertAndSend("", queueName, message, correlationData);

    }

    /**
     * 发送 禁用会员的MQ
     *
     * @param memberId
     */
    public void sendMemberDisableMessage(String memberId, String remark) {

        String queueName = RabbitMqConstants.WALLET_MEMBER_DISABLE_QUEUE;

        // 创建自定义CorrelationData
        CustomCorrelationData correlationData = new CustomCorrelationData(
                UUID.randomUUID().toString(),
                "1",
                "1",
                queueName
        );

        // 使用 Fastjson 将对象转换为 JSON 字符串
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("memberId", memberId);
        jsonObject.put("remark", remark);
        String messageJson = jsonObject.toJSONString();

        // 创建消息属性
        MessageProperties messageProperties = new MessageProperties();
        //设置消息为持久化
        messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);

        // 创建消息
        Message message = new Message(messageJson.getBytes(), messageProperties);

        log.info("RabbitMQ发送 禁用会员的MQ 消息内容: {}", messageJson);

        // 发送消息到默认交换机，并使用队列名称作为路由键
        rabbitTemplate.convertAndSend("", queueName, message, correlationData);

    }

    public void sendOrderTaggingMessage(OrderTaggingMessage orderTaggingMessage) {

        if(CollectionUtils.isEmpty(orderTaggingMessage.getPlatformOrderTags())){
            log.info("RabbitMQ发送 待标记订单列表为空, 不需要发送, orderTaggingMessage: {}", orderTaggingMessage);
            return;
        }

        String queueName = RabbitMqConstants.WALLET_ORDER_TAGGING_QUEUE;

        // 创建自定义CorrelationData
        CustomCorrelationData correlationData = new CustomCorrelationData(
                UUID.randomUUID().toString(),
                "1",
                "1",
                queueName
        );

        // 使用 Fastjson 将对象转换为 JSON 字符串
        String messageJson = JSON.toJSONString(orderTaggingMessage, SerializerFeature.WriteMapNullValue);

        // 创建消息属性
        MessageProperties messageProperties = new MessageProperties();
        //设置消息为持久化
        messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);

        // 创建消息
        Message message = new Message(messageJson.getBytes(), messageProperties);

        log.info("RabbitMQ发送 订单标记的MQ 消息内容: {}", messageJson);

        // 发送消息到默认交换机，并使用队列名称作为路由键
        rabbitTemplate.convertAndSend("", queueName, message, correlationData);

    }

    public void sendTradeIpBlackAddMessage(TradIpBlackMessage tradIpBlackMessage) {

        String queueName = RabbitMqConstants.WALLET_TRADE_IP_BLACK_ADD_QUEUE;

        // 创建自定义CorrelationData
        CustomCorrelationData correlationData = new CustomCorrelationData(
                UUID.randomUUID().toString(),
                "1",
                "1",
                queueName
        );

        // 使用 Fastjson 将对象转换为 JSON 字符串
        String messageJson = JSON.toJSONString(tradIpBlackMessage);

        // 创建消息属性
        MessageProperties messageProperties = new MessageProperties();
        //设置消息为持久化
        messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);

        // 创建消息
        Message message = new Message(messageJson.getBytes(), messageProperties);

        log.info("RabbitMQ发送 添加交易IP黑名单的MQ 消息内容: {}", messageJson);

        // 发送消息到默认交换机，并使用队列名称作为路由键
        rabbitTemplate.convertAndSend("", queueName, message, correlationData);

    }



    /**
     * 发送 余额退回订单mq
     *
     * @param orderNo orderNo
     */
    public void sendCashBackOrderProcess(String orderNo, String processUserName) {
        // 使用 Fastjson 将对象转换为 JSON 字符串
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("orderNo", orderNo);
        jsonObject.put("processUserName", processUserName);
        String messageJson = jsonObject.toJSONString();
        log.info("RabbitMQ发送 会员退回订单的MQ 消息内容: {}", messageJson);
        long delayMillis = 2000;
        TaskInfo taskInfo = new TaskInfo(messageJson, TaskTypeEnum.CASH_BACK_ORDER_PROCESS.getCode(), System.currentTimeMillis());
        sendTimeoutTask(taskInfo, delayMillis);
    }

    /**
     * 发送提现order延时队列
     * @param msg
     */
    public void sendKycCompleteOrderProcess(String msg) {
        log.info("RabbitMQ发送 kyc提现自动到账 消息内容: {}", msg);
        long delayMillis = 3000;
        TaskInfo taskInfo = new TaskInfo(msg, TaskTypeEnum.KYC_WITHDRAW_COMPLETE_PROCESS_QUEUE.getCode(), System.currentTimeMillis());
        sendTimeoutTask(taskInfo, delayMillis);
    }

    /**
     * 发送 概览统计mq
     *
     * @param req {@link OrderEventReq}
     */
    public void sendStatisticProcess(OrderEventReq req) {
        String queueName = RabbitMqConstants.OVER_VIEW_STATISTICS_QUEUE;

        // 创建自定义CorrelationData
        CustomCorrelationData correlationData = new CustomCorrelationData(
                UUID.randomUUID().toString(),
                "1",
                "1",
                queueName
        );

        // 使用 Fastjson 将对象转换为 JSON 字符串
        String messageJson = JSONObject.toJSONString(req);

        // 创建消息属性
        MessageProperties messageProperties = new MessageProperties();
        //设置消息为持久化
        messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);

        // 创建消息
        Message message = new Message(messageJson.getBytes(), messageProperties);
        log.info("RabbitMQ发送 概览统计的MQ 消息内容: {}", messageJson);

        // 发送消息到默认交换机，并使用队列名称作为路由键
        rabbitTemplate.convertAndSend("", queueName, message, correlationData);
    }

    /**
     * 发送 商户日报表统计 mq
     *
     * @param req {@link OrderEventReq}
     */
    public void sendMerchantDailyProcess(OrderEventReq req) {
        String queueName = RabbitMqConstants.MERCHANT_DAILY_STATISTICS_QUEUE;

        // 创建自定义CorrelationData
        CustomCorrelationData correlationData = new CustomCorrelationData(
                UUID.randomUUID().toString(),
                "1",
                "1",
                queueName
        );

        // 使用 Fastjson 将对象转换为 JSON 字符串
        String messageJson = JSONObject.toJSONString(req);

        // 创建消息属性
        MessageProperties messageProperties = new MessageProperties();
        //设置消息为持久化
        messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);

        // 创建消息
        Message message = new Message(messageJson.getBytes(), messageProperties);
        log.info("RabbitMQ发送 发送日报表统计MQ 消息内容: {}", messageJson);

        // 发送消息到默认交换机，并使用队列名称作为路由键
        rabbitTemplate.convertAndSend("", queueName, message, correlationData);
    }

    /**
     * 发送 会员升级的MQ
     *
     * @param memberId
     */
    public void sendMemberUpgradeMessage(String memberId) {

        String queueName = RabbitMqConstants.WALLET_MEMBER_UPGRADE_QUEUE;

        // 创建自定义CorrelationData
        CustomCorrelationData correlationData = new CustomCorrelationData(
                UUID.randomUUID().toString(),
                "1",
                "1",
                queueName
        );

        // 使用 Fastjson 将对象转换为 JSON 字符串
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("memberId", memberId);
        String messageJson = jsonObject.toJSONString();

        // 创建消息属性
        MessageProperties messageProperties = new MessageProperties();
        //设置消息为持久化
        messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);

        // 创建消息
        Message message = new Message(messageJson.getBytes(), messageProperties);
        log.info("RabbitMQ发送 会员升级的MQ 消息内容: {}", messageJson);

        // 发送消息到默认交换机，并使用队列名称作为路由键
        rabbitTemplate.convertAndSend("", queueName, message, correlationData);

    }


    /**
     * 发送获取KYC银行交易记录的MQ
     *
     * @param kycTransactionMessage
     */
    public void sendKycTransactionMessage(KycTransactionMessage kycTransactionMessage) {


        String queueName = RabbitMqConstants.WALLET_MEMBER_KYC_TRANSACTION_QUEUE;

        // 创建自定义CorrelationData
        CustomCorrelationData correlationData = new CustomCorrelationData(
                UUID.randomUUID().toString(),
                "1",
                "1",
                queueName
        );

        // 使用 Fastjson 将对象转换为 JSON 字符串
        String messageJson = JSON.toJSONString(kycTransactionMessage);

        // 创建消息属性
        MessageProperties messageProperties = new MessageProperties();
        //设置消息为持久化
        messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);

        // 创建消息
        Message message = new Message(messageJson.getBytes(), messageProperties);

        log.info("RabbitMQ发送 获取KYC银行交易记录的MQ 消息内容: {}, kycTransactionMessage: {}", message, kycTransactionMessage);

        // 发送消息到默认交换机，并使用队列名称作为路由键
        rabbitTemplate.convertAndSend("", queueName, message, correlationData);

    }

    /**
     * 发送计算佣金消息
     *
     * @param commissionAndDividendsMessage msg
     */
    public void sendCommissionDividendsQueue(CommissionAndDividendsMessage commissionAndDividendsMessage) {

        String queueName = RabbitMqConstants.CALC_COMMISSION_AND_DIVIDENDS_QUEUE;

        // 创建自定义CorrelationData
        CustomCorrelationData correlationData = new CustomCorrelationData(
                UUID.randomUUID().toString(),
                "1",
                "1",
                queueName
        );

        // 使用 Fastjson 将对象转换为 JSON 字符串
        String messageJson = JSON.toJSONString(commissionAndDividendsMessage);

        // 创建消息属性
        MessageProperties messageProperties = new MessageProperties();
        //设置消息为持久化
        messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);

        // 创建消息
        Message message = new Message(messageJson.getBytes(), messageProperties);

        log.info("RabbitMQ发送 计算佣金的MQ 消息内容: {}, kycTransactionMessage: {}", message, commissionAndDividendsMessage);

        // 发送消息到默认交换机，并使用队列名称作为路由键
        rabbitTemplate.convertAndSend("", queueName, message, correlationData);

    }

    /**
     * 发送kyc自动完成消息
     *
     * @param kycCompleteMessage msg
     */
    public void sendKycAutoCompleteQueue(KycCompleteMessage kycCompleteMessage) {
        String queueName = RabbitMqConstants.KYC_AUTO_COMPLETE_QUEUE;
        // 创建自定义CorrelationData
        CustomCorrelationData correlationData = new CustomCorrelationData(
                UUID.randomUUID().toString(),
                "1",
                "1",
                queueName
        );

        // 使用 Fastjson 将对象转换为 JSON 字符串
        String messageJson = JSON.toJSONString(kycCompleteMessage);

        // 创建消息属性
        MessageProperties messageProperties = new MessageProperties();
        //设置消息为持久化
        messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);

        // 创建消息
        Message message = new Message(messageJson.getBytes(), messageProperties);

        log.info("RabbitMQ发送 自动完成kyc交易账变处理 消息内容: {}, kycTransactionMessage: {}", message, kycCompleteMessage);

        // 发送消息到默认交换机，并使用队列名称作为路由键
        rabbitTemplate.convertAndSend("", queueName, message, correlationData);


    }

//    /**
//     * 发送kyc自动完成消息
//     *
//     * @param kycCompleteMessage msg
//     */
//    public void sendCompleteQueue(KycAutoCompleteReq kycCompleteMessage) {
//        String queueName = RabbitMqConstants.KYC_WITHDRAW_COMPLETE_QUEUE;
//        // 创建自定义CorrelationData
//        CustomCorrelationData correlationData = new CustomCorrelationData(
//                UUID.randomUUID().toString(),
//                "1",
//                "1",
//                queueName
//        );
//
//        // 使用 Fastjson 将对象转换为 JSON 字符串
//        String messageJson = JSON.toJSONString(kycCompleteMessage);
//
//        // 创建消息属性
//        MessageProperties messageProperties = new MessageProperties();
//        //设置消息为持久化
//        messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
//
//        // 创建消息
//        Message message = new Message(messageJson.getBytes(), messageProperties);
//
//        log.info("RabbitMQ发送 提现kyc交易账变处理 消息内容: {}, kycTransactionMessage: {}", message, kycCompleteMessage);
//
//        // 发送消息到默认交换机，并使用队列名称作为路由键
//        rabbitTemplate.convertAndSend("", queueName, message, correlationData);
//
//
//    }

    /**
     * 发送 同步银行卡信息的MQ
     */
    public void sendSyncBankInfoMessage(TaskInfo taskInfo) {

        String queueName = RabbitMqConstants.UU_WALLET_SYNC_BANK_INFO_QUEUE;

        // 创建自定义CorrelationData
        CustomCorrelationData correlationData = new CustomCorrelationData(
                UUID.randomUUID().toString(),
                taskInfo.getOrderNo(),
                taskInfo.getTaskType(),
                queueName
        );

        // 使用 Fastjson 将对象转换为 JSON 字符串
        String messageJson = JSON.toJSONString(taskInfo);

        // 创建消息属性
        MessageProperties messageProperties = new MessageProperties();
        //设置消息为持久化
        messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);

        // 创建消息
        Message message = new Message(messageJson.getBytes(), messageProperties);

        log.info("RabbitMQ发送 同步银行卡信息的MQ消息成功 消息内容: {}, taskInfo: {}", message, taskInfo);

        // 发送消息到默认交换机，并使用队列名称作为路由键
        rabbitTemplate.convertAndSend("", queueName, message, correlationData);
    }


    /**
     * 发送 uuPay USDT自动上分MQ
     */
    public void sendUsdtAutoCreditMessage(TaskInfo taskInfo) {

        String queueName = RabbitMqConstants.UU_WALLET_USDT_AUTO_CREDIT_QUEUE;

        // 创建自定义CorrelationData
        CustomCorrelationData correlationData = new CustomCorrelationData(
                UUID.randomUUID().toString(),
                taskInfo.getOrderNo(),
                taskInfo.getTaskType(),
                queueName
        );

        // 使用 Fastjson 将对象转换为 JSON 字符串
        String messageJson = JSON.toJSONString(taskInfo);

        // 创建消息属性
        MessageProperties messageProperties = new MessageProperties();
        //设置消息为持久化
        messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);

        // 创建消息
        Message message = new Message(messageJson.getBytes(), messageProperties);

        log.info("RabbitMQ发送 USDT自动上分的MQ消息成功 消息内容: {}, taskInfo: {}", message, taskInfo);

        // 发送消息到默认交换机，并使用队列名称作为路由键
        rabbitTemplate.convertAndSend("", queueName, message, correlationData);
    }

    /**
     * 发送 处理USDT代付订单的MQ
     */
    public void sendUsdtPaymentOrderMessage(TaskInfo taskInfo) {

        String queueName = RabbitMqConstants.UU_WALLET_USDT_PAYMENT_ORDER_QUEUE;

        // 创建自定义CorrelationData
        CustomCorrelationData correlationData = new CustomCorrelationData(
                UUID.randomUUID().toString(),
                taskInfo.getOrderNo(),
                taskInfo.getTaskType(),
                queueName
        );

        // 使用 Fastjson 将对象转换为 JSON 字符串
        String messageJson = JSON.toJSONString(taskInfo);

        // 创建消息属性
        MessageProperties messageProperties = new MessageProperties();
        //设置消息为持久化
        messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);

        // 创建消息
        Message message = new Message(messageJson.getBytes(), messageProperties);

        log.info("RabbitMQ发送 处理USDT代付订单的MQ 消息内容: {}, taskInfo: {}", message, taskInfo);

        // 发送消息到默认交换机，并使用队列名称作为路由键
        rabbitTemplate.convertAndSend("", queueName, message, correlationData);
    }

    /**
     * 发送 uuPay TRX自动上分
     */
    public void sendTrxAutoCreditMessage(TaskInfo taskInfo) {

        String queueName = RabbitMqConstants.UU_WALLET_TRX_AUTO_CREDIT_QUEUE;

        // 创建自定义CorrelationData
        CustomCorrelationData correlationData = new CustomCorrelationData(
                UUID.randomUUID().toString(),
                taskInfo.getOrderNo(),
                taskInfo.getTaskType(),
                queueName
        );

        // 使用 Fastjson 将对象转换为 JSON 字符串
        String messageJson = JSON.toJSONString(taskInfo);

        // 创建消息属性
        MessageProperties messageProperties = new MessageProperties();
        //设置消息为持久化
        messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);

        // 创建消息
        Message message = new Message(messageJson.getBytes(), messageProperties);

        log.info("RabbitMQ发送 TRX自动上分的MQ消息 消息内容: {}, taskInfo: {}", message, taskInfo);

        // 发送消息到默认交换机，并使用队列名称作为路由键
        rabbitTemplate.convertAndSend("", queueName, message, correlationData);
    }


    /**
     * 发送 处理TRX代付订单的MQ
     */
    public void sendTrxPaymentOrderMessage(TaskInfo taskInfo) {

        String queueName = RabbitMqConstants.UU_WALLET_TRX_PAYMENT_ORDER_QUEUE;

        // 创建自定义CorrelationData
        CustomCorrelationData correlationData = new CustomCorrelationData(
                UUID.randomUUID().toString(),
                taskInfo.getOrderNo(),
                taskInfo.getTaskType(),
                queueName
        );

        // 使用 Fastjson 将对象转换为 JSON 字符串
        String messageJson = JSON.toJSONString(taskInfo);

        // 创建消息属性
        MessageProperties messageProperties = new MessageProperties();
        //设置消息为持久化
        messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);

        // 创建消息
        Message message = new Message(messageJson.getBytes(), messageProperties);

        log.info("RabbitMQ发送 处理TRX代付订单的MQ 消息内容: {}, taskInfo: {}", message, taskInfo);

        // 发送消息到默认交换机，并使用队列名称作为路由键
        rabbitTemplate.convertAndSend("", queueName, message, correlationData);
    }

    /**
     * 发送 处理返佣或分红的MQ
     */
    public void sendExecuteCommissionOrDividendsMessage(ExecuteCommissionAndDividendsMessage executeMessage) {

        String queueName = RabbitMqConstants.EXECUTE_COMMISSION_AND_DIVIDENDS_QUEUE;

        // 创建自定义CorrelationData
        CustomCorrelationData correlationData = new CustomCorrelationData(
                UUID.randomUUID().toString(),
                "1",
                "1",
                queueName
        );

        // 使用 Fastjson 将对象转换为 JSON 字符串
        String messageJson = JSON.toJSONString(executeMessage);

        // 创建消息属性
        MessageProperties messageProperties = new MessageProperties();
        //设置消息为持久化
        messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);

        // 创建消息
        Message message = new Message(messageJson.getBytes(), messageProperties);

        log.info("RabbitMQ发送 处理返佣或分红的MQ 消息内容: {}, executeMessage: {}", message, executeMessage);

        // 发送消息到默认交换机，并使用队列名称作为路由键
        rabbitTemplate.convertAndSend("", queueName, message, correlationData);
    }
}
