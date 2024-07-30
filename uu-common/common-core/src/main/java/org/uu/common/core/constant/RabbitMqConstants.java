package org.uu.common.core.constant;

public class RabbitMqConstants {

    public static final Integer DELIVERING = 0;//消息投递中
    public static final Integer SUCCESS = 1;//消息投递成功
    public static final Integer FAILURE = 2;//消息投递失败
    public static final Integer MAX_TRY_COUNT = 3;//最大重试次数
    public static final Integer MSG_TIMEOUT = 1;//消息超时时间


    //钱包项目-充值交易通知-交换机
    public static final String UU_WALLET_TRADE_COLLECT_EXCHANGE = "uu.wallet.trade.collect.exchange";

    //钱包项目-充值交易通知-路由key
    public static final String UU_WALLET_TRADE_COLLECT_ROUTING_KEY = "uu.wallet.trade.collect.routing.key";

    //钱包项目-充值交易通知-队列
    public static final String UU_WALLET_TRADE_COLLECT_QUEUE_NAME = "uu.wallet.trade.collect.queue";


    //钱包项目-提现交易通知-交换机
    public static final String UU_WALLET_TRADE_PAYMENT_EXCHANGE = "uu.wallet.trade.payment.exchange";

    //钱包项目-提现交易通知-路由key
    public static final String UU_WALLET_TRADE_PAYMENT_ROUTING_KEY = "uu.wallet.trade.payment.routing.key";

    //钱包项目-提现交易通知-队列
    public static final String UU_WALLET_TRADE_PAYMENT_QUEUE_NAME = "uu.wallet.trade.payment.queue";


    //订单超时处理 start-------------------------------------------------
    //流程: 先将消息发送到主队列(无人监听)并设置消息过期时间 消息过期后被路由到死信队列 消费者监听死信队列进行处理延时任务


    // 钱包项目-延迟任务-订单超时-死信交换机
    public static final String UU_WALLET_DEAD_LETTER_EXCHANGE = "uu.wallet.dead_letter.exchange";



    // 5.钱包会员支付超时死信队列
    public static final String UU_WALLET_MEMBER_PAYMENT_DEAD_LETTER_QUEUE = "uu.wallet.member.payment.dead_letter.queue";


    // 1. 钱包会员匹配超时主队列
    public static final String UU_WALLET_MEMBER_MATCH_TIMEOUT_QUEUE = "uu.wallet.member.matchtimeout.queue";
    // 2. 商户会员匹配超时主队列
    public static final String UU_WALLET_MERCHANT_MEMBER_MATCH_TIMEOUT_QUEUE = "uu.wallet.merchant.member.match.timeout.queue";
    // 3. 钱包会员确认超时主队列
    public static final String UU_WALLET_MEMBER_CONFIRM_TIMEOUT_QUEUE = "uu.wallet.member.confirm.timeout.queue";
    // 4. 商户会员确认超时主队列
    public static final String UU_WALLET_MERCHANT_MEMBER_CONFIRM_TIMEOUT_QUEUE = "uu.wallet.merchant.member.confirm.timeout.queue";
    // 5. 钱包会员支付超时主队列
    public static final String UU_WALLET_MEMBER_PAYMENT_TIMEOUT_QUEUE = "uu.wallet.member.payment.timeout.queue";


    //1.将钱包会员匹配超时队列绑定到死信交换机路由key
    public static final String WALLET_MEMBER_MATCH_TIMEOUT = "wallet.member.match.timeout";
    //2.将商户会员匹配超时队列绑定到死信交换机路由key
    public static final String MERCHANT_MEMBER_MATCH_TIMEOUT = "merchant.member.match.timeout";
    //3.将钱包会员确认超时队列绑定到死信交换机路由key
    public static final String WALLET_MEMBER_CONFIRM_TIMEOUT = "wallet.member.confirm.timeout";
    //4.将商户会员确认超时队列绑定到死信交换机路由key
    public static final String MERCHANT_MEMBER_CONFIRM_TIMEOUT = "merchant.member.confirm.timeout";
    //5.将钱包会员支付超时队列绑定到死信交换机路由key
    public static final String WALLET_MEMBER_PAYMENT_TIMEOUT = "wallet.member.payment.timeout";


    //1.钱包会员匹配超时队列 超时后 路由到死信交换机的key
    public static final String WALLET_MEMBER_MATCH_PROCESS = "wallet.member.match.process";
    //2.将商户会员匹配超时队 超时后 路由到死信交换机的key
    public static final String MERCHANT_MEMBER_MATCH_PROCESS = "merchant.member.match.process";
    //3.将钱包会员确认超时队 超时后 路由到死信交换机的key
    public static final String WALLET_MEMBER_CONFIRM_PROCESS = "wallet.member.confirm.process";
    //4.将商户会员确认超时队 超时后 路由到死信交换机的key
    public static final String MERCHANT_MEMBER_CONFIRM_PROCESS = "merchant.member.confirm.process";
    //5.将钱包会员支付超时队 超时后 路由到死信交换机的key
    public static final String WALLET_MEMBER_PAYMENT_PROCESS = "wallet.member.payment.process";


    //订单超时处理 end-------------------------------------------------

    // 会员登录日志 和 操作日志记录 MQ

    // 队列名称
    public static final String WALLET_MEMBER_LOGIN_LOG_QUEUE = "wallet.member.login.log.queue";
    public static final String WALLET_MEMBER_OPERATION_LOG_QUEUE = "wallet.member.operation.log.queue";

    // 交换机名称
    public static final String WALLET_MEMBER_LOGIN_LOG_EXCHANGE = "wallet.member.login.log.exchange";
    public static final String WALLET_MEMBER_OPERATION_LOG_EXCHANGE = "wallet.member.operation.log.exchange";

    // 路由键
    public static final String WALLET_MEMBER_ROUTING_KEY_LOGIN_LOG = "wallet.member.routing.key.login.log";
    public static final String WALLET_MEMBER_ROUTING_KEY_OPERATION_LOG = "wallet.member.routing.key.operation.log";


    // 语音通知卖方 普通队列名称
    public static final String WALLET_MEMBER_NOTIFY_SELLER_BY_VOICE_QUEUE = "wallet.member.notify.seller.by.voice.queue";

    //语音通知卖方 死信队列名称
    public static final String WALLET_MEMBER_DEAD_LETTER_NOTIFY_SELLER_BY_VOICE_QUEUE = "wallet.member.dead.letter.notify.seller.by.voice.queue";

    //语音通知卖方 死信交换机名称
    public static final String WALLET_MEMBER_DEAD_LETTER_NOTIFY_SELLER_BY_VOICE_EXCHANGE = "wallet.member.dead.letter.notify.seller.by.voice.exchange";

    //语音通知卖方 死信路由键
    public static final String WALLET_MEMBER_DEAD_LETTER_NOTIFY_SELLER_BY_VOICE_ROUTING_KEY = "wallet.member.dead.letter.notify.seller.by.voice.routing.key";


    // 代收订单支付超时 普通队列名称
    public static final String WALLET_MERCHANT_COLLECT_ORDER_PAYMENT_TIMEOUT_QUEUE = "wallet.merchant.collect.order.payment.timeout.queue";

    //代收订单支付超时 死信队列名称
    public static final String WALLET_MERCHANT_COLLECT_ORDER_PAYMENT_TIMEOUT_DEAD_LETTER_QUEUE = "wallet.merchant.collect.order.payment.timeout.dead.letter.queue";

    //代收订单支付超时 死信交换机名称
    public static final String WALLET_MERCHANT_COLLECT_ORDER_PAYMENT_TIMEOUT_DEAD_LETTER_EXCHANGE = "wallet.merchant.collect.order.payment.timeout.dead.letter.exchange";

    //代收订单支付超时 死信路由键
    public static final String WALLET_MERCHANT_COLLECT_ORDER_PAYMENT_TIMEOUT_DEAD_LETTER_ROUTING_KEY = "wallet.merchant.collect.order.payment.timeout.dead.letter.routing.key";


    // 统计在线人数 MQ-----------------------------
    // 队列名称
    public static final String WALLET_MEMBER_ONLINE_COUNT_QUEUE = "wallet.member.online.count.queue";

    // 交换机名称
    public static final String WALLET_MEMBER_ONLINE_COUNT_EXCHANGE = "wallet.member.online.count.exchange";

    // 路由键
    public static final String WALLET_MEMBER_ONLINE_COUNT_ROUTINGKEY = "wallet.member.online.count.routingkey";
    //------------------------------------------


    // 清空每日交易数据 MQ-----------------------------
    // 清空每日交易数据队列
    public static final String WALLET_MEMBER_DAILY_TRADE_CLEUU_QUEUE = "wallet.member.daily.trade.clear.queue";

    // 清空每日交易数据交换机
    public static final String WALLET_MEMBER_DAILY_TRADE_CLEUU_EXCHANGE = "wallet.member.daily.trade.clear.exchange";

    // 清空每日交易数据路由键
    public static final String WALLET_MEMBER_DAILY_TRADE_CLEUU_ROUTINGKEY = "wallet.member.daily.trade.clear.routingkey";
    //------------------------------------------


    // 完成实名认证任务 MQ-----------------------------
    // 完成实名认证任务队列
    public static final String WALLET_MEMBER_REAL_NAME_VERIFICATION_TASK_QUEUE = "wallet.member.real.name.verification.task.queue";

    // 完成实名认证任务交换机
    public static final String WALLET_MEMBER_REAL_NAME_VERIFICATION_TASK_EXCHANGE = "wallet.member.real.name.verification.task.exchange";

    // 完成实名认证任务路由键
    public static final String WALLET_MEMBER_REAL_NAME_VERIFICATION_TASK_ROUTINGKEY = "wallet.member.real.name.verification.task.routingkey";
    //------------------------------------------


    // 禁用会员 MQ-----------------------------
    // 禁用会员队列
    public static final String WALLET_MEMBER_DISABLE_QUEUE = "wallet.member.disable.queue";

    // 禁用会员交换机
    public static final String WALLET_MEMBER_DISABLE_EXCHANGE = "wallet.member.disable.exchange";

    // 禁用会员路由键
    public static final String WALLET_MEMBER_DISABLE_ROUTINGKEY = "wallet.member.disable.routingkey";
    //------------------------------------------


    // 订单标记 MQ-----------------------------
    // 订单标记队列
    public static final String WALLET_ORDER_TAGGING_QUEUE = "wallet.order.tagging.queue";

    // 订单标记交换机
    public static final String WALLET_ORDER_TAGGING_EXCHANGE = "wallet.order.tagging.exchange";

    // 订单标记路由键
    public static final String WALLET_ORDER_TAGGING_ROUTINGKEY = "wallet.order.tagging.routingkey";
    //------------------------------------------


    // 添加交易IP黑名单 MQ-----------------------------
    // 添加交易IP黑名单队列
    public static final String WALLET_TRADE_IP_BLACK_ADD_QUEUE = "wallet.trade.ip.black.add.queue";

    // 添加交易IP黑名单交换机
    public static final String WALLET_TRADE_IP_BLACK_ADD_EXCHANGE = "wallet.trade.ip.black.add.exchange";

    // 添加交易IP黑名单路由键
    public static final String WALLET_TRADE_IP_BLACK_ADD_ROUTINGKEY = "wallet.trade.ip.black.add.routingkey";
    //------------------------------------------

    //------------------------------------------
    // 会员确认超时风控标记 普通队列名称
    public static final String WALLET_MEMBER_CONFIRM_TIMEOUT_RISK_TAG_QUEUE = "wallet.member.confirm.timeout.risk.tag.queue";

    // 会员确认超时风控标记 死信队列名称
    public static final String WALLET_MEMBER_CONFIRM_TIMEOUT_RISK_TAG_DEAD_LETTER_QUEUE = "wallet.member.confirm.timeout.risk.tag.dead.letter.queue";

    // 会员确认超时风控标记 死信交换机名称
    public static final String WALLET_MEMBER_CONFIRM_TIMEOUT_RISK_TAG_DEAD_LETTER_EXCHANGE = "wallet.member.confirm.timeout.risk.tag.dead.letter.exchange";

    // 会员确认超时风控标记 死信路由键
    public static final String WALLET_MEMBER_CONFIRM_TIMEOUT_RISK_TAG_DEAD_LETTER_ROUTING_KEY = "wallet.member.confirm.timeout.risk.tag.dead.letter.routing.key";
//------------------------------------------

    //------------------------------------------
    // 提现交易延时回调通知 普通队列名称
    public static final String WITHDRAW_NOTIFY_TIMEOUT_QUEUE = "withdraw.notify.timeout.queue";

    // 提现交易延时回调通知 死信队列名称
    public static final String WITHDRAW_NOTIFY_TIMEOUT_DEAD_LETTER_QUEUE = "withdraw.notify.timeout.dead.letter.queue";

    // 提现交易延时回调通知 死信交换机名称
    public static final String WITHDRAW_NOTIFY_TIMEOUT_DEAD_LETTER_EXCHANGE = "withdraw.notify.timeout.dead.letter.exchange";

    // 提现交易延时回调通知 死信路由键
    public static final String WITHDRAW_NOTIFY_TIMEOUT_DEAD_LETTER_ROUTING_KEY = "withdraw.notify.timeout.dead.letter.routing.key";
//------------------------------------------

    // 会员升级 MQ-----------------------------
    // 会员升级队列
    public static final String WALLET_MEMBER_UPGRADE_QUEUE = "wallet.member.upgrade.queue";

    // 会员升级交换机
    public static final String WALLET_MEMBER_UPGRADE_EXCHANGE = "wallet.member.upgrade.exchange";

    // 会员升级路由键
    public static final String WALLET_MEMBER_UPGRADE_ROUTINGKEY = "wallet.member.upgrade.routingkey";
    //------------------------------------------



    // 统计KYC交易记录 MQ-----------------------------
    // 队列名称
    public static final String WALLET_MEMBER_KYC_TRANSACTION_QUEUE = "wallet.member.kyc.transaction.queue";

    // 交换机名称
    public static final String WALLET_MEMBER_KYC_TRANSACTION_EXCHANGE = "wallet.member.kyc.transaction.exchange";

    // 路由键
    public static final String WALLET_MEMBER_KYC_TRANSACTION_ROUTINGKEY = "wallet.member.kyc.transaction.routingkey";
    //------------------------------------------



    // 余额退回 普通队列名称
    public static final String CASH_BACK_ORDER_PROCESS_QUEUE = "cash.back.order.process.queue";

    // 余额退回 死信队列名称
    public static final String CASH_BACK_ORDER_PROCESS_DEAD_LETTER_QUEUE = "cash.back.order.process.dead.letter.queue";

    // 余额退回 死信交换机名称
    public static final String CASH_BACK_ORDER_PROCESS_DEAD_LETTER_EXCHANGE = "cash.back.order.process.dead.letter.exchange";

    // 余额退回 死信路由键
    public static final String CASH_BACK_ORDER_PROCESS_DEAD_LETTER_ROUTING_KEY = "cash.back.order.process.dead.letter.routing.key";
    //------------------------------------------

    // 钱包项目-同步银行卡信息-交换机
    public static final String UU_WALLET_SYNC_BANK_INFO_EXCHANGE = "uu.wallet.sync.bank.info.exchange";

    // 钱包项目-同步银行卡信息-路由key
    public static final String UU_WALLET_SYNC_BANK_INFO_ROUTING_KEY = "uu.wallet.sync.bank.info.routing.key";

    // 钱包项目-同步银行卡信息-队列
    public static final String UU_WALLET_SYNC_BANK_INFO_QUEUE = "uu.wallet.sync.bank.info.queue";
    //------------------------------------------

    // 计算返佣和分红-交换机
    public static final String CALC_COMMISSION_AND_DIVIDENDS_EXCHANGE = "calc.commission.and.dividends.exchange";

    // 计算返佣和分红-路由key
    public static final String CALC_COMMISSION_AND_DIVIDENDS_ROUTING_KEY = "calc.commission.and.dividends.routing.key";

    // 计算返佣和分红-队列
    public static final String CALC_COMMISSION_AND_DIVIDENDS_QUEUE = "calc.commission.and.dividends.queue";
    //------------------------------------------

    // 执行返佣和分红-交换机
    public static final String EXECUTE_COMMISSION_AND_DIVIDENDS_EXCHANGE = "execute.commission.and.dividends.exchange";

    // 执行返佣和分红-路由key
    public static final String EXECUTE_COMMISSION_AND_DIVIDENDS_ROUTING_KEY = "execute.commission.and.dividends.routing.key";

    // 执行返佣和分红-队列
    public static final String EXECUTE_COMMISSION_AND_DIVIDENDS_QUEUE = "execute.commission.and.dividends.queue";
    //------------------------------------------

    // 钱包项目-USDT自动上分-交换机
    public static final String UU_WALLET_USDT_AUTO_CREDIT_EXCHANGE = "uu.wallet.usdt.auto.credit.exchange";

    // 钱包项目-USDT自动上分-路由key
    public static final String UU_WALLET_USDT_AUTO_CREDIT_ROUTING_KEY = "uu.wallet.usdt.auto.credit.routing.key";

    // 钱包项目-USDT自动上分-队列
    public static final String UU_WALLET_USDT_AUTO_CREDIT_QUEUE = "uu.wallet.usdt.auto.credit.queue";
    //------------------------------------------

    // 钱包项目-拉取区块数据-交换机
    public static final String UU_WALLET_FETCH_BLOCK_DATA_EXCHANGE = "uu.wallet.fetch.block.data.exchange";

    // 钱包项目-拉取区块数据-路由key
    public static final String UU_WALLET_FETCH_BLOCK_DATA_ROUTING_KEY = "uu.wallet.fetch.block.data.routing.key";

    // 钱包项目-拉取区块数据-队列
    public static final String UU_WALLET_FETCH_BLOCK_DATA_QUEUE = "uu.wallet.fetch.block.data.queue";
    //------------------------------------------

    // 自动完成kyc交易账变处理-交换机
    public static final String KYC_AUTO_COMPLETE_EXCHANGE = "kyc.auto.complete.exchange";

    // 自动完成kyc交易账变处理-路由key
    public static final String KYC_AUTO_COMPLETE_ROUTING_KEY = "kyc.auto.complete.routing.key";

    // 自动完成kyc交易账变处理-队列
    public static final String KYC_AUTO_COMPLETE_QUEUE = "kyc.auto.complete.queue";

    // 余额退回 普通队列名称KYC_WITHDRAW_COMPLETE_EXCHANGE
    public static final String KYC_WITHDRAW_COMPLETE_PROCESS_QUEUE = "kyc.withdraw.complete.process.queue";

    // 提现完成kyc交易账变处理 死信队列名称
    public static final String KYC_WITHDRAW_COMPLETE_PROCESS_DEAD_LETTER_QUEUE = "kyc.withdraw.complete.process.dead.letter.queue";

    // 提现完成kyc交易账变处理 死信交换机名称
    public static final String KYC_WITHDRAW_COMPLETE_PROCESS_DEAD_LETTER_EXCHANGE = "kyc.withdraw.complete.process.dead.letter.exchange";

    // 提现完成kyc交易账变处理 死信路由键
    public static final String KYC_WITHDRAW_COMPLETE_PROCESS_DEAD_LETTER_ROUTING_KEY = "kyc.withdraw.complete.process.dead.letter.routing.key";
    //------------------------------------------


    // 统计首页信息 MQ-----------------------------
    // 队列名称
    public static final String OVER_VIEW_STATISTICS_QUEUE = "over.view.statistics.queue";

    // 商户日报表统计Mq
    // 队列名称
    public static final String MERCHANT_DAILY_STATISTICS_QUEUE = "merchant.daily.statistics.queue";

    // 交换机名称
    public static final String OVER_VIEW_STATISTICS_EXCHANGE = "over.view.statistics.exchange";

    // 路由键
    public static final String OVER_VIEW_STATISTICS_ROUTING_KEY = "over.view.statistics.routing.key";
    //------------------------------------------

    // 钱包项目-USDT资金归集-交换机
    public static final String UU_WALLET_USDT_COLLECTION_EXCHANGE = "uu.wallet.usdt.collection.exchange";

    // 钱包项目-USDT资金归集-路由key
    public static final String UU_WALLET_USDT_COLLECTION_ROUTING_KEY = "uu.wallet.usdt.collection.routing.key";

    // 钱包项目-USDT资金归集-队列
    public static final String UU_WALLET_USDT_COLLECTION_QUEUE = "uu.wallet.usdt.collection.queue";
    //------------------------------------------


    // 钱包项目-获取实时汇率-交换机
    public static final String UU_WALLET_REALTIME_EXCHANGE_RATE_EXCHANGE = "uu.wallet.realtime.exchange.rate.exchange";

    // 钱包项目-获取实时汇率-路由key
    public static final String UU_WALLET_REALTIME_EXCHANGE_RATE_ROUTING_KEY = "uu.wallet.realtime.exchange.rate.routing.key";

    // 钱包项目-获取实时汇率-队列
    public static final String UU_WALLET_REALTIME_EXCHANGE_RATE_QUEUE = "uu.wallet.realtime.exchange.rate.queue";
    //------------------------------------------

    // 钱包项目-处理USDT代付订单-交换机
    public static final String UU_WALLET_USDT_PAYMENT_ORDER_EXCHANGE = "uu.wallet.usdt.payment.order.exchange";

    // 钱包项目-处理USDT代付订单-路由key
    public static final String UU_WALLET_USDT_PAYMENT_ORDER_ROUTING_KEY = "uu.wallet.usdt.payment.order.routing.key";

    // 钱包项目-处理USDT代付订单-队列
    public static final String UU_WALLET_USDT_PAYMENT_ORDER_QUEUE = "uu.wallet.usdt.payment.order.queue";
    //------------------------------------------

    // 钱包项目-TRX自动上分-交换机
    public static final String UU_WALLET_TRX_AUTO_CREDIT_EXCHANGE = "uu.wallet.trx.auto.credit.exchange";

    // 钱包项目-TRX自动上分-路由key
    public static final String UU_WALLET_TRX_AUTO_CREDIT_ROUTING_KEY = "uu.wallet.trx.auto.credit.routing.key";

    // 钱包项目-TRX自动上分-队列
    public static final String UU_WALLET_TRX_AUTO_CREDIT_QUEUE = "uu.wallet.trx.auto.credit.queue";
    //------------------------------------------

    // 钱包项目-处理TRX代付订单-交换机
    public static final String UU_WALLET_TRX_PAYMENT_ORDER_EXCHANGE = "uu.wallet.trx.payment.order.exchange";

    // 钱包项目-处理TRX代付订单-路由key
    public static final String UU_WALLET_TRX_PAYMENT_ORDER_ROUTING_KEY = "uu.wallet.trx.payment.order.routing.key";

    // 钱包项目-处理TRX代付订单-队列
    public static final String UU_WALLET_TRX_PAYMENT_ORDER_QUEUE = "uu.wallet.trx.payment.order.queue";
//------------------------------------------
}
