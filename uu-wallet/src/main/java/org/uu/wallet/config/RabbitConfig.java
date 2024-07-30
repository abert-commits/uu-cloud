package org.uu.wallet.config;

import org.uu.common.core.constant.RabbitMqConstants;
import org.uu.wallet.consumer.MsgConfirmCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



@Configuration
public class RabbitConfig {
    public final static Logger logger = LoggerFactory.getLogger(RabbitConfig.class);
    @Autowired
    private CachingConnectionFactory cachingConnectionFactory;


    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {

        //设置消息确认回调
        //每当消息被 RabbitMQ 代理确认（无论成功还是失败），都会调用 MsgConfirmCallback 的 confirm 方法
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setConfirmCallback(new MsgConfirmCallback());
        return rabbitTemplate;
    }


    //充值交易回调通知
    @Bean
    Queue collectNotifyQueue() {
        return new Queue(RabbitMqConstants.UU_WALLET_TRADE_COLLECT_QUEUE_NAME, true);
    }

    @Bean
    DirectExchange collectNotifyExchange() {
        return new DirectExchange(RabbitMqConstants.UU_WALLET_TRADE_COLLECT_EXCHANGE, true, false);
    }

    @Bean
    Binding collectMailBinding() {
        return BindingBuilder.bind(collectNotifyQueue()).to(collectNotifyExchange()).with(RabbitMqConstants.UU_WALLET_TRADE_COLLECT_ROUTING_KEY);
    }

    //提现交易回调通知
    @Bean
    Queue paymentNotifyQueue() {
        return new Queue(RabbitMqConstants.UU_WALLET_TRADE_PAYMENT_QUEUE_NAME, true);
    }

    @Bean
    DirectExchange paymentNotifyExchange() {
        return new DirectExchange(RabbitMqConstants.UU_WALLET_TRADE_PAYMENT_EXCHANGE, true, false);
    }

    @Bean
    Binding paymentMailBinding() {
        return BindingBuilder.bind(paymentNotifyQueue()).to(paymentNotifyExchange()).with(RabbitMqConstants.UU_WALLET_TRADE_PAYMENT_ROUTING_KEY);
    }


    //订单超时处理-----------------------------------

    // 死信交换机
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(RabbitMqConstants.UU_WALLET_DEAD_LETTER_EXCHANGE);
    }


    // 钱包会员支付超时死信队列
    @Bean
    public Queue walletMemberPaymentDeadLetterQueue() {
        return QueueBuilder.durable(RabbitMqConstants.UU_WALLET_MEMBER_PAYMENT_DEAD_LETTER_QUEUE).build();
    }

    @Bean
    public Binding bindingWalletMemberPaymentDeadLetterQueue() {
        return BindingBuilder.bind(walletMemberPaymentDeadLetterQueue())
                .to(deadLetterExchange())
                .with(RabbitMqConstants.WALLET_MEMBER_PAYMENT_PROCESS);
    }

    // 创建六个主队列，每个队列对应一个延时任务类型

    // 1. 钱包会员匹配超时主队列
    @Bean
    public Queue walletMemberMatchTimeoutQueue() {
        return QueueBuilder.durable(RabbitMqConstants.UU_WALLET_MEMBER_MATCH_TIMEOUT_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitMqConstants.UU_WALLET_DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RabbitMqConstants.WALLET_MEMBER_MATCH_PROCESS)
                .build();
    }

    // 2. 商户会员匹配超时主队列
    @Bean
    public Queue merchantMemberMatchTimeoutQueue() {
        return QueueBuilder.durable(RabbitMqConstants.UU_WALLET_MERCHANT_MEMBER_MATCH_TIMEOUT_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitMqConstants.UU_WALLET_DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RabbitMqConstants.MERCHANT_MEMBER_MATCH_PROCESS)
                .build();
    }

    // 3. 钱包会员确认超时主队列
    @Bean
    public Queue walletMemberConfirmTimeoutQueue() {
        return QueueBuilder.durable(RabbitMqConstants.UU_WALLET_MEMBER_CONFIRM_TIMEOUT_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitMqConstants.UU_WALLET_DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RabbitMqConstants.WALLET_MEMBER_CONFIRM_PROCESS)
                .build();
    }

    // 4. 商户会员确认超时主队列
    @Bean
    public Queue merchantMemberConfirmTimeoutQueue() {
        return QueueBuilder.durable(RabbitMqConstants.UU_WALLET_MERCHANT_MEMBER_CONFIRM_TIMEOUT_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitMqConstants.UU_WALLET_DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RabbitMqConstants.MERCHANT_MEMBER_CONFIRM_PROCESS)
                .build();
    }

    // 5. 钱包会员支付超时主队列
    @Bean
    public Queue walletMemberPaymentTimeoutQueue() {
        return QueueBuilder.durable(RabbitMqConstants.UU_WALLET_MEMBER_PAYMENT_TIMEOUT_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitMqConstants.UU_WALLET_DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RabbitMqConstants.WALLET_MEMBER_PAYMENT_PROCESS)
                .build();
    }


    // 绑定主队列到死信交换机的辅助方法
    private Binding bindingQueueToExchange(Queue queue, String routingKey) {
        return BindingBuilder.bind(queue).to(deadLetterExchange()).with(routingKey);
    }

    //将钱包会员匹配超时队列绑定到死信交换机
    @Bean
    public Binding bindingWalletMemberMatchTimeoutQueue() {
        return bindingQueueToExchange(walletMemberMatchTimeoutQueue(), RabbitMqConstants.WALLET_MEMBER_MATCH_TIMEOUT);
    }

    //将商户会员匹配超时队列绑定到死信交换机
    @Bean
    public Binding bindingMerchantMemberMatchTimeoutQueue() {
        return bindingQueueToExchange(merchantMemberMatchTimeoutQueue(), RabbitMqConstants.MERCHANT_MEMBER_MATCH_TIMEOUT);
    }

    //将钱包会员确认超时队列绑定到死信交换机
    @Bean
    public Binding bindingWalletMemberConfirmTimeoutQueue() {
        return bindingQueueToExchange(walletMemberConfirmTimeoutQueue(), RabbitMqConstants.WALLET_MEMBER_CONFIRM_TIMEOUT);
    }

    //将商户会员确认超时队列绑定到死信交换机
    @Bean
    public Binding bindingMerchantMemberConfirmTimeoutQueue() {
        return bindingQueueToExchange(merchantMemberConfirmTimeoutQueue(), RabbitMqConstants.MERCHANT_MEMBER_CONFIRM_TIMEOUT);
    }

    //将钱包会员支付超时队列绑定到死信交换机
    @Bean
    public Binding bindingWalletMemberPaymentTimeoutQueue() {
        return bindingQueueToExchange(walletMemberPaymentTimeoutQueue(), RabbitMqConstants.WALLET_MEMBER_PAYMENT_TIMEOUT);
    }


    //会员登录日志记录队列
    @Bean
    public Queue loginLogQueue() {
        return new Queue(RabbitMqConstants.WALLET_MEMBER_LOGIN_LOG_QUEUE, true);
    }

    //会员操作日志记录队列
    @Bean
    public Queue operationLogQueue() {
        return new Queue(RabbitMqConstants.WALLET_MEMBER_OPERATION_LOG_QUEUE, true);
    }

    //会员登录日志记录交换机
    @Bean
    public DirectExchange loginLogExchange() {
        return new DirectExchange(RabbitMqConstants.WALLET_MEMBER_LOGIN_LOG_EXCHANGE);
    }

    //会员操作日志记录交换机
    @Bean
    public DirectExchange operationLogExchange() {
        return new DirectExchange(RabbitMqConstants.WALLET_MEMBER_OPERATION_LOG_EXCHANGE);
    }

    //会员登录日志记录路由键
    @Bean
    public Binding bindingLoginLogQueue(Queue loginLogQueue, DirectExchange loginLogExchange) {
        return BindingBuilder.bind(loginLogQueue).to(loginLogExchange).with(RabbitMqConstants.WALLET_MEMBER_ROUTING_KEY_LOGIN_LOG);
    }

    //会员操作日志记录路由键
    @Bean
    public Binding bindingOperationLogQueue(Queue operationLogQueue, DirectExchange operationLogExchange) {
        return BindingBuilder.bind(operationLogQueue).to(operationLogExchange).with(RabbitMqConstants.WALLET_MEMBER_ROUTING_KEY_OPERATION_LOG);
    }


    @Bean
    Queue regularQueue() {
        return QueueBuilder.durable(RabbitMqConstants.WALLET_MEMBER_NOTIFY_SELLER_BY_VOICE_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitMqConstants.WALLET_MEMBER_DEAD_LETTER_NOTIFY_SELLER_BY_VOICE_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RabbitMqConstants.WALLET_MEMBER_DEAD_LETTER_NOTIFY_SELLER_BY_VOICE_ROUTING_KEY)
                .build();
    }


    //语音通知会员死信队列
    @Bean
    Queue notifySellerByVoiceDeadLetterQueue() {
        return QueueBuilder.durable(RabbitMqConstants.WALLET_MEMBER_DEAD_LETTER_NOTIFY_SELLER_BY_VOICE_QUEUE).build();
    }

    //语音通知会员死信交换机
    @Bean
    DirectExchange notifySellerByVoiceDeadLetterExchange() {
        return new DirectExchange(RabbitMqConstants.WALLET_MEMBER_DEAD_LETTER_NOTIFY_SELLER_BY_VOICE_EXCHANGE);
    }

    //语音通知会员死信路由键
    @Bean
    Binding bindingNotifySellerByVoiceQueue() {
        return BindingBuilder.bind(notifySellerByVoiceDeadLetterQueue()).to(notifySellerByVoiceDeadLetterExchange()).with(RabbitMqConstants.WALLET_MEMBER_DEAD_LETTER_NOTIFY_SELLER_BY_VOICE_ROUTING_KEY);
    }


    //----------------------------------------------------
    /**
     * 代收订单支付超时 普通队列
     * 定义一个用于接收代收订单支付超时通知的普通队列。
     * 配置该队列的死信交换机和死信路由键，意味着当消息因为某些原因（如消息被拒绝或过期）无法处理时，会被发送到指定的死信交换机。
     *
     * @return {@link Queue}
     */
    @Bean
    Queue paymentCollectionTimeoutQueue() {
        // 创建一个持久的队列，如果RabbitMQ重启，队列仍然存在
        return QueueBuilder.durable(
                RabbitMqConstants.WALLET_MERCHANT_COLLECT_ORDER_PAYMENT_TIMEOUT_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitMqConstants.WALLET_MERCHANT_COLLECT_ORDER_PAYMENT_TIMEOUT_DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RabbitMqConstants.WALLET_MERCHANT_COLLECT_ORDER_PAYMENT_TIMEOUT_DEAD_LETTER_ROUTING_KEY).build();
    }

    /**
     * 代收订单支付超时 死信队列
     * 定义一个死信队列，用于接收来自普通队列的无法处理的消息。
     *
     * @return {@link Queue}
     */
    @Bean
    Queue paymentCollectionTimeoutDeadLetterQueue() {
        // 创建一个持久的死信队列
        return QueueBuilder.durable(RabbitMqConstants.WALLET_MERCHANT_COLLECT_ORDER_PAYMENT_TIMEOUT_DEAD_LETTER_QUEUE).build();
    }

    /**
     * 代收订单支付超时 死信交换机
     * 定义一个直接类型的死信交换机，用于接收来自普通队列的死信消息并根据路由键路由到死信队列。
     * @return {@link DirectExchange}
     */
    @Bean
    DirectExchange paymentCollectionTimeoutDeadLetterExchange() {
        // 创建一个直接类型的交换机
        return new DirectExchange(RabbitMqConstants.WALLET_MERCHANT_COLLECT_ORDER_PAYMENT_TIMEOUT_DEAD_LETTER_EXCHANGE);
    }

    /**
     * 代收订单支付超时 绑定关系
     * 定义死信队列和死信交换机之间的绑定关系，使用死信路由键作为绑定键。
     * @return {@link Binding}
     */
    @Bean
    Binding bindingPaymentCollectionTimeoutQueue() {
        // 绑定死信队列到死信交换机，使用死信路由键
        return BindingBuilder.bind(paymentCollectionTimeoutDeadLetterQueue()).to(paymentCollectionTimeoutDeadLetterExchange()).with(
                RabbitMqConstants.WALLET_MERCHANT_COLLECT_ORDER_PAYMENT_TIMEOUT_DEAD_LETTER_ROUTING_KEY);
    }
    //----------------------------------------------------




    //----------------------------------------------------
    // 清空每日交易数据队列
    @Bean
    public Queue dailyTradeClearQueue() {
        return new Queue(RabbitMqConstants.WALLET_MEMBER_DAILY_TRADE_CLEUU_QUEUE, true);
    }

    // 清空每日交易数据交换机
    @Bean
    public DirectExchange dailyTradeClearExchange() {
        return new DirectExchange(RabbitMqConstants.WALLET_MEMBER_DAILY_TRADE_CLEUU_EXCHANGE);
    }

    // 绑定清空每日交易数据队列与交换机
    @Bean
    public Binding bindingDailyTradeClearQueue(Queue dailyTradeClearQueue, DirectExchange dailyTradeClearExchange) {
        return BindingBuilder.bind(dailyTradeClearQueue).to(dailyTradeClearExchange).with(RabbitMqConstants.WALLET_MEMBER_DAILY_TRADE_CLEUU_ROUTINGKEY);
    }
    //----------------------------------------------------


    //----------------------------------------------------
    // 完成实名认证任务队列
    @Bean
    public Queue realNameVerificationTaskQueue() {
        return new Queue(RabbitMqConstants.WALLET_MEMBER_REAL_NAME_VERIFICATION_TASK_QUEUE, true);
    }

    // 完成实名认证任务交换机
    @Bean
    public DirectExchange realNameVerificationTaskExchange() {
        return new DirectExchange(RabbitMqConstants.WALLET_MEMBER_REAL_NAME_VERIFICATION_TASK_EXCHANGE);
    }

    // 绑定完成实名认证任务队列与交换机
    @Bean
    public Binding bindingRealNameVerificationTaskQueue(Queue realNameVerificationTaskQueue, DirectExchange realNameVerificationTaskExchange) {
        return BindingBuilder.bind(realNameVerificationTaskQueue).to(realNameVerificationTaskExchange).with(RabbitMqConstants.WALLET_MEMBER_REAL_NAME_VERIFICATION_TASK_ROUTINGKEY);
    }
    //----------------------------------------------------


    //----------------------------------------------------
    // 会员禁用队列
    @Bean
    public Queue memberDisableQueue() {
        return new Queue(RabbitMqConstants.WALLET_MEMBER_DISABLE_QUEUE, true);
    }

    // 会员禁用交换机
    @Bean
    public DirectExchange memberDisableExchange() {
        return new DirectExchange(RabbitMqConstants.WALLET_MEMBER_DISABLE_EXCHANGE);
    }

    // 会员禁用队列与交换机
    @Bean
    public Binding bindingMemberDisableQueue(Queue memberDisableQueue, DirectExchange memberDisableExchange) {
        return BindingBuilder.bind(memberDisableQueue).to(memberDisableExchange).with(RabbitMqConstants.WALLET_MEMBER_DISABLE_ROUTINGKEY);
    }
    //----------------------------------------------------

    //----------------------------------------------------
    // 订单标记队列
    @Bean
    public Queue orderTaggingQueue() {
        return new Queue(RabbitMqConstants.WALLET_ORDER_TAGGING_QUEUE, true);
    }

    // 订单标记交换机
    @Bean
    public DirectExchange orderTaggingExchange() {
        return new DirectExchange(RabbitMqConstants.WALLET_ORDER_TAGGING_EXCHANGE);
    }

    // 订单标记队列与交换机
    @Bean
    public Binding bindingorderTaggingQueue(Queue orderTaggingQueue, DirectExchange orderTaggingExchange) {
        return BindingBuilder.bind(orderTaggingQueue).to(orderTaggingExchange).with(RabbitMqConstants.WALLET_ORDER_TAGGING_ROUTINGKEY);
    }
    //----------------------------------------------------

    //----------------------------------------------------
    // 添加交易IP黑名单队列
    @Bean
    public Queue tradeIpBlackAddQueue() {
        return new Queue(RabbitMqConstants.WALLET_TRADE_IP_BLACK_ADD_QUEUE, true);
    }

    // 添加交易IP黑名单交换机
    @Bean
    public DirectExchange tradeIpBlackAddExchange() {
        return new DirectExchange(RabbitMqConstants.WALLET_TRADE_IP_BLACK_ADD_EXCHANGE);
    }

    // 添加交易IP黑名单队列与交换机
    @Bean
    public Binding bindingtradeIpBlackAddQueue(Queue tradeIpBlackAddQueue, DirectExchange tradeIpBlackAddExchange) {
        return BindingBuilder.bind(tradeIpBlackAddQueue).to(tradeIpBlackAddExchange).with(RabbitMqConstants.WALLET_TRADE_IP_BLACK_ADD_ROUTINGKEY);
    }
    //----------------------------------------------------

    //----------------------------------------------------
    // 会员确认超时风控标记 普通队列
    @Bean
    Queue memberConfirmTimeoutRiskTagQueue() {
        return QueueBuilder.durable(RabbitMqConstants.WALLET_MEMBER_CONFIRM_TIMEOUT_RISK_TAG_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitMqConstants.WALLET_MEMBER_CONFIRM_TIMEOUT_RISK_TAG_DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RabbitMqConstants.WALLET_MEMBER_CONFIRM_TIMEOUT_RISK_TAG_DEAD_LETTER_ROUTING_KEY).build();
    }
    // 会员确认超时风控标记 死信队列
    @Bean
    Queue memberConfirmTimeoutRiskTagDeadLetterQueue() {
        return QueueBuilder.durable(RabbitMqConstants.WALLET_MEMBER_CONFIRM_TIMEOUT_RISK_TAG_DEAD_LETTER_QUEUE).build();
    }

    // 会员确认超时风控标记 死信交换机
    @Bean
    DirectExchange memberConfirmTimeoutRiskTagDeadLetterExchange() {
        return new DirectExchange(RabbitMqConstants.WALLET_MEMBER_CONFIRM_TIMEOUT_RISK_TAG_DEAD_LETTER_EXCHANGE);
    }

    // 会员确认超时风控标记 绑定关系
    @Bean
    Binding bindingmemberConfirmTimeoutRiskTagQueue() {
        return BindingBuilder.bind(memberConfirmTimeoutRiskTagDeadLetterQueue()).to(memberConfirmTimeoutRiskTagDeadLetterExchange())
                .with(RabbitMqConstants.WALLET_MEMBER_CONFIRM_TIMEOUT_RISK_TAG_DEAD_LETTER_ROUTING_KEY);
    }
    //----------------------------------------------------

    //----------------------------------------------------
    // 提现交易延时回调通知 普通队列
    @Bean
    Queue withdrawNotifyTimeoutQueue() {
        return QueueBuilder.durable(RabbitMqConstants.WITHDRAW_NOTIFY_TIMEOUT_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitMqConstants.WITHDRAW_NOTIFY_TIMEOUT_DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RabbitMqConstants.WITHDRAW_NOTIFY_TIMEOUT_DEAD_LETTER_ROUTING_KEY).build();
    }
    // 提现交易延时回调通知 死信队列
    @Bean
    Queue withdrawNotifyTimeoutDeadLetterQueue() {
        return QueueBuilder.durable(RabbitMqConstants.WITHDRAW_NOTIFY_TIMEOUT_DEAD_LETTER_QUEUE).build();
    }

    // 提现交易延时回调通知 死信交换机
    @Bean
    DirectExchange withdrawNotifyTimeoutDeadLetterExchange() {
        return new DirectExchange(RabbitMqConstants.WITHDRAW_NOTIFY_TIMEOUT_DEAD_LETTER_EXCHANGE);
    }

    // 提现交易延时回调通知 绑定关系
    @Bean
    Binding bindingWithdrawNotifyTimeoutQueue() {
        return BindingBuilder.bind(withdrawNotifyTimeoutDeadLetterQueue()).to(withdrawNotifyTimeoutDeadLetterExchange())
                .with(RabbitMqConstants.WITHDRAW_NOTIFY_TIMEOUT_DEAD_LETTER_ROUTING_KEY);
    }
    //----------------------------------------------------


    //----------------------------------------------------
    // 会员升级队列
    @Bean
    public Queue memberUpgradeQueue() {
        return new Queue(RabbitMqConstants.WALLET_MEMBER_UPGRADE_QUEUE, true);
    }

    // 会员升级交换机
    @Bean
    public DirectExchange memberUpgradeExchange() {
        return new DirectExchange(RabbitMqConstants.WALLET_MEMBER_UPGRADE_EXCHANGE);
    }

    // 会员升级队列与交换机
    @Bean
    public Binding bindingMemberUpgradeQueue(Queue memberUpgradeQueue, DirectExchange memberUpgradeExchange) {
        return BindingBuilder.bind(memberUpgradeQueue).to(memberUpgradeExchange).with(RabbitMqConstants.WALLET_MEMBER_UPGRADE_ROUTINGKEY);
    }
    //----------------------------------------------------


    //----------------------------------------------------
    //获取KYC银行交易记录 队列
    @Bean
    public Queue kycTransactionQueue() {
        return new Queue(RabbitMqConstants.WALLET_MEMBER_KYC_TRANSACTION_QUEUE, true);
    }

    //获取KYC银行交易记录 交换机
    @Bean
    public DirectExchange kycTransactionExchange() {
        return new DirectExchange(RabbitMqConstants.WALLET_MEMBER_KYC_TRANSACTION_EXCHANGE);
    }

    //绑定获取KYC银行交易记录队列与交换机
    @Bean
    public Binding bindingKycTransactionQueue(Queue kycTransactionQueue, DirectExchange kycTransactionExchange) {
        return BindingBuilder.bind(kycTransactionQueue).to(kycTransactionExchange).with(RabbitMqConstants.WALLET_MEMBER_KYC_TRANSACTION_ROUTINGKEY);
    }
    //----------------------------------------------------


    //----------------------------------------------------

    // 余额退回 普通队列
    @Bean
    public Queue cashBackProcessQueue() {
        return QueueBuilder.durable(RabbitMqConstants.CASH_BACK_ORDER_PROCESS_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitMqConstants.CASH_BACK_ORDER_PROCESS_DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RabbitMqConstants.CASH_BACK_ORDER_PROCESS_DEAD_LETTER_ROUTING_KEY).build();
    }
    // 余额退回 死信队列
    @Bean
    public Queue cashBackProcessDeadLetterQueue() {
        return QueueBuilder.durable(RabbitMqConstants.CASH_BACK_ORDER_PROCESS_DEAD_LETTER_QUEUE).build();
    }

    // 余额退回 死信交换机
    @Bean
    public DirectExchange cashBackProcessDeadLetterExchange() {
        return new DirectExchange(RabbitMqConstants.CASH_BACK_ORDER_PROCESS_DEAD_LETTER_EXCHANGE);
    }

    // 余额退回 绑定关系
    @Bean
    public Binding bindingCashBackProcessQueue() {
        return BindingBuilder.bind(cashBackProcessDeadLetterQueue()).to(cashBackProcessDeadLetterExchange())
                .with(RabbitMqConstants.CASH_BACK_ORDER_PROCESS_DEAD_LETTER_ROUTING_KEY);
    }
    //----------------------------------------------------


    // 钱包项目-同步银行卡信息-队列
    @Bean
    Queue syncBankInfoQueue() {
        return new Queue(RabbitMqConstants.UU_WALLET_SYNC_BANK_INFO_QUEUE, true);
    }

    // 同步银行卡信息交换机
    @Bean
    DirectExchange syncBankInfoExchange() {
        return new DirectExchange(RabbitMqConstants.UU_WALLET_SYNC_BANK_INFO_EXCHANGE, true, false);
    }

    // 同步银行卡信息绑定
    @Bean
    Binding syncBankInfoBinding() {
        return BindingBuilder.bind(syncBankInfoQueue()).to(syncBankInfoExchange()).with(RabbitMqConstants.UU_WALLET_SYNC_BANK_INFO_ROUTING_KEY);
    }
    //----------------------------------------------------

    // 计算返佣和分红-队列
    @Bean
    public Queue calcCommissionAndDividendsQueue() {
        return new Queue(RabbitMqConstants.CALC_COMMISSION_AND_DIVIDENDS_QUEUE, true);
    }

    // 计算返佣和分红-交换机
    @Bean
    public DirectExchange calcCommissionAndDividendsExchange() {
        return new DirectExchange(RabbitMqConstants.CALC_COMMISSION_AND_DIVIDENDS_EXCHANGE, true, false);
    }

    // 计算返佣和分红-绑定
    @Bean
    public Binding calcCommissionAndDividendsBinding() {
        return BindingBuilder.bind(calcCommissionAndDividendsQueue())
                .to(calcCommissionAndDividendsExchange())
                .with(RabbitMqConstants.CALC_COMMISSION_AND_DIVIDENDS_ROUTING_KEY);
    }
    //----------------------------------------------------

    // 执行返佣和分红-队列
    @Bean
    public Queue executeCommissionAndDividendsQueue() {
        return new Queue(RabbitMqConstants.EXECUTE_COMMISSION_AND_DIVIDENDS_QUEUE, true);
    }

    // 执行返佣和分红-交换机
    @Bean
    public DirectExchange executeCommissionAndDividendsExchange() {
        return new DirectExchange(RabbitMqConstants.EXECUTE_COMMISSION_AND_DIVIDENDS_EXCHANGE, true, false);
    }

    // 执行返佣和分红-绑定
    @Bean
    public Binding executeCommissionAndDividendsBinding() {
        return BindingBuilder.bind(executeCommissionAndDividendsQueue())
                .to(executeCommissionAndDividendsExchange())
                .with(RabbitMqConstants.EXECUTE_COMMISSION_AND_DIVIDENDS_ROUTING_KEY);
    }
    //----------------------------------------------------


    // 钱包项目-USDT自动上分-队列
    @Bean
    public Queue usdtAutoCreditQueue() {
        return new Queue(RabbitMqConstants.UU_WALLET_USDT_AUTO_CREDIT_QUEUE, true);
    }

    // USDT自动上分交换机
    @Bean
    public DirectExchange usdtAutoCreditExchange() {
        return new DirectExchange(RabbitMqConstants.UU_WALLET_USDT_AUTO_CREDIT_EXCHANGE, true, false);
    }

    // USDT自动上分绑定
    @Bean
    public Binding usdtAutoCreditBinding() {
        return BindingBuilder.bind(usdtAutoCreditQueue())
                .to(usdtAutoCreditExchange())
                .with(RabbitMqConstants.UU_WALLET_USDT_AUTO_CREDIT_ROUTING_KEY);
    }
    //----------------------------------------------------

    // 钱包项目-拉取区块数据-队列
    @Bean
    public Queue fetchBlockDataQueue() {
        return new Queue(RabbitMqConstants.UU_WALLET_FETCH_BLOCK_DATA_QUEUE, true);
    }

    // 拉取区块数据交换机
    @Bean
    public DirectExchange fetchBlockDataExchange() {
        return new DirectExchange(RabbitMqConstants.UU_WALLET_FETCH_BLOCK_DATA_EXCHANGE, true, false);
    }

    // 拉取区块数据绑定
    @Bean
    public Binding fetchBlockDataBinding() {
        return BindingBuilder.bind(fetchBlockDataQueue())
                .to(fetchBlockDataExchange())
                .with(RabbitMqConstants.UU_WALLET_FETCH_BLOCK_DATA_ROUTING_KEY);
    }

    //----------------------------------------------------

    // kyc自动完成-队列
    @Bean
    public Queue kycAutoCompleteQueue() {
        return new Queue(RabbitMqConstants.KYC_AUTO_COMPLETE_QUEUE, true);
    }

    // kyc自动完成-交换机
    @Bean
    public DirectExchange kycAutoCompleteExchange() {
        return new DirectExchange(RabbitMqConstants.KYC_AUTO_COMPLETE_EXCHANGE, true, false);
    }

    // kyc自动完成-绑定
    @Bean
    public Binding kycAutoCompleteBinding() {
        return BindingBuilder.bind(kycAutoCompleteQueue()).to(kycAutoCompleteExchange()).with(RabbitMqConstants.KYC_AUTO_COMPLETE_ROUTING_KEY);
    }
    //----------------------------------------------------

    //----------------------------------------------------

    @Bean
    public Queue kycWithdrawCompleteQueue() {
        return QueueBuilder.durable(RabbitMqConstants.KYC_WITHDRAW_COMPLETE_PROCESS_QUEUE)
                .withArgument("x-dead-letter-exchange", RabbitMqConstants.KYC_WITHDRAW_COMPLETE_PROCESS_DEAD_LETTER_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", RabbitMqConstants.KYC_WITHDRAW_COMPLETE_PROCESS_DEAD_LETTER_ROUTING_KEY).build();
    }
    // kyc提现完成-绑定
    @Bean
    public Queue kycWithdrawCompleteDeadLetterQueue() {
        return QueueBuilder.durable(RabbitMqConstants.KYC_WITHDRAW_COMPLETE_PROCESS_DEAD_LETTER_QUEUE).build();
    }

    // kyc提现完成 死信交换机
    @Bean
    public DirectExchange kycWithdrawCompleteDeadLetterExchange() {
        return new DirectExchange(RabbitMqConstants.KYC_WITHDRAW_COMPLETE_PROCESS_DEAD_LETTER_EXCHANGE);
    }

    // kyc提现完成 绑定关系
    @Bean
    public Binding bindingKycWithdrawCompleteQueue() {
        return BindingBuilder.bind(kycWithdrawCompleteDeadLetterQueue()).to(kycWithdrawCompleteDeadLetterExchange())
                .with(RabbitMqConstants.KYC_WITHDRAW_COMPLETE_PROCESS_DEAD_LETTER_ROUTING_KEY);
    }
    //----------------------------------------------------


    //----------------------------------------------------

    // 概览统计-队列
    @Bean
    public Queue overViewStatisticsQueue() {
        return new Queue(RabbitMqConstants.OVER_VIEW_STATISTICS_QUEUE, true);
    }

    // 商户日报表统计-队列
    @Bean
    public Queue merchantDailyStatisticsQueue() {
        return new Queue(RabbitMqConstants.MERCHANT_DAILY_STATISTICS_QUEUE, true);
    }

    // 概览统计-交换机
    @Bean
    public DirectExchange overViewStatisticsExchange() {
        return new DirectExchange(RabbitMqConstants.OVER_VIEW_STATISTICS_EXCHANGE, true, false);
    }

    // 概览统计-绑定
    @Bean
    public Binding overViewStatisticsBinding() {
        return BindingBuilder.bind(overViewStatisticsQueue()).to(overViewStatisticsExchange()).with(RabbitMqConstants.OVER_VIEW_STATISTICS_ROUTING_KEY);
    }
    // 钱包项目-USDT资金归集-队列
    @Bean
    public Queue usdtCollectionQueue() {
        return new Queue(RabbitMqConstants.UU_WALLET_USDT_COLLECTION_QUEUE, true);
    }

    // USDT资金归集交换机
    @Bean
    public DirectExchange usdtCollectionExchange() {
        return new DirectExchange(RabbitMqConstants.UU_WALLET_USDT_COLLECTION_EXCHANGE, true, false);
    }

    // USDT资金归集绑定
    @Bean
    public Binding usdtCollectionBinding() {
        return BindingBuilder.bind(usdtCollectionQueue())
                .to(usdtCollectionExchange())
                .with(RabbitMqConstants.UU_WALLET_USDT_COLLECTION_ROUTING_KEY);
    }
    //----------------------------------------------------

    // 钱包项目-获取实时汇率-队列
    @Bean
    public Queue realtimeExchangeRateQueue() {
        return new Queue(RabbitMqConstants.UU_WALLET_REALTIME_EXCHANGE_RATE_QUEUE, true);
    }

    // 获取实时汇率交换机
    @Bean
    public DirectExchange realtimeExchangeRateExchange() {
        return new DirectExchange(RabbitMqConstants.UU_WALLET_REALTIME_EXCHANGE_RATE_EXCHANGE, true, false);
    }

    // 获取实时汇率绑定
    @Bean
    public Binding realtimeExchangeRateBinding() {
        return BindingBuilder.bind(realtimeExchangeRateQueue())
                .to(realtimeExchangeRateExchange())
                .with(RabbitMqConstants.UU_WALLET_REALTIME_EXCHANGE_RATE_ROUTING_KEY);
    }
    //----------------------------------------------------


    // 钱包项目-处理USDT代付订单-队列
    @Bean
    public Queue usdtPaymentOrderQueue() {
        return new Queue(RabbitMqConstants.UU_WALLET_USDT_PAYMENT_ORDER_QUEUE, true);
    }

    // 钱包项目-处理USDT代付订单-交换机
    @Bean
    public DirectExchange usdtPaymentOrderExchange() {
        return new DirectExchange(RabbitMqConstants.UU_WALLET_USDT_PAYMENT_ORDER_EXCHANGE, true, false);
    }

    // 钱包项目-处理USDT代付订单-绑定
    @Bean
    public Binding usdtPaymentOrderBinding() {
        return BindingBuilder.bind(usdtPaymentOrderQueue())
                .to(usdtPaymentOrderExchange())
                .with(RabbitMqConstants.UU_WALLET_USDT_PAYMENT_ORDER_ROUTING_KEY);
    }
    //----------------------------------------------------

    // 钱包项目-TRX自动上分-队列
    @Bean
    public Queue trxAutoCreditQueue() {
        return new Queue(RabbitMqConstants.UU_WALLET_TRX_AUTO_CREDIT_QUEUE, true);
    }

    // TRX自动上分交换机
    @Bean
    public DirectExchange trxAutoCreditExchange() {
        return new DirectExchange(RabbitMqConstants.UU_WALLET_TRX_AUTO_CREDIT_EXCHANGE, true, false);
    }

    // TRX自动上分绑定
    @Bean
    public Binding trxAutoCreditBinding() {
        return BindingBuilder.bind(trxAutoCreditQueue())
                .to(trxAutoCreditExchange())
                .with(RabbitMqConstants.UU_WALLET_TRX_AUTO_CREDIT_ROUTING_KEY);
    }
    //----------------------------------------------------

    // 钱包项目-处理TRX代付订单-队列
    @Bean
    public Queue trxPaymentOrderQueue() {
        return new Queue(RabbitMqConstants.UU_WALLET_TRX_PAYMENT_ORDER_QUEUE, true);
    }

    // 钱包项目-处理TRX代付订单-交换机
    @Bean
    public DirectExchange trxPaymentOrderExchange() {
        return new DirectExchange(RabbitMqConstants.UU_WALLET_TRX_PAYMENT_ORDER_EXCHANGE, true, false);
    }

    // 钱包项目-处理TRX代付订单-绑定
    @Bean
    public Binding trxPaymentOrderBinding() {
        return BindingBuilder.bind(trxPaymentOrderQueue())
                .to(trxPaymentOrderExchange())
                .with(RabbitMqConstants.UU_WALLET_TRX_PAYMENT_ORDER_ROUTING_KEY);
    }
//----------------------------------------------------
}