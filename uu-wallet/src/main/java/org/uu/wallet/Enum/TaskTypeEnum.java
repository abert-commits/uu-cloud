package org.uu.wallet.Enum;

/*
 * Rabbit MQ 消息类型枚举
 * */
public enum TaskTypeEnum {

    //钱包用户确认超时
    WALLET_MEMBER_CONFIRMATION_TIMEOUT("1", "WALLET_MEMBER_CONFIRMATION_TIMEOUT"),

    //商户会员确认超时
    MERCHANT_MEMBER_CONFIRMATION_TIMEOUT("2", "MERCHANT_MEMBER_CONFIRMATION_TIMEOUT"),

    //钱包用户卖出匹配超时
    WALLET_MEMBER_SALE_MATCH_TIMEOUT("3", "WALLET_MEMBER_SALE_MATCH_TIMEOUT"),

    //商户会员卖出匹配超时
    MERCHANT_MEMBER_SALE_MATCH_TIMEOUT("4", "MERCHANT_MEMBER_SALE_MATCH_TIMEOUT"),

    //支付超时
    PAYMENT_TIMEOUT("5", "PAYMENT_TIMEOUT"),

    //USDT支付超时
    USDT_PAYMENT_TIMEOUT("6", "USDT_PAYMENT_TIMEOUT"),

    //充值回调通知
    DEPOSIT_NOTIFICATION("7", "DEPOSIT_NOTIFICATION"),

    //提现回调通知
    WITHDRAW_NOTIFICATION("8", "WITHDRAW_NOTIFICATION"),

    //语音通知卖方
    NOTIFY_SELLER_BY_VOICE("9", "NOTIFY_SELLER_BY_VOICE"),

    //代收订单支付超时
    MERCHANT_COLLECT_ORDER_PAYMENT_TIMEOUT_QUEUE("10", "MERCHANT_COLLECT_ORDER_PAYMENT_TIMEOUT_QUEUE"),

    //次日自动领取每日任务奖励
    MERCHANT_AUTO_CLAIM_REWARD_QUEUE("11", "MERCHANT_AUTO_CLAIM_REWARD_QUEUE"),

    //匹配超时自动取消订单
    AUTO_CANCEL_ORDER_ON_MATCH_TIMEOUT("12", "AUTO_CANCEL_ORDER_ON_MATCH_TIMEOUT"),

    // 卖出确认超时风控标识
    RISK_TAG_ON_MEMBER_CONFIRMATION_TIMEOUT("13", "RISK_TAG_ON_MEMBER_CONFIRMATION_TIMEOUT"),

    //提现回调延迟通知
    WITHDRAW_NOTIFICATION_TIMEOUT("14", "WITHDRAW_NOTIFICATION_TIMEOUT"),

    // 卖出确认超时自动取消订单
    AUTO_CANCEL_ORDER_ON_WALLET_MEMBER_CONFIRM_TIMEOUT("15", "AUTO_CANCEL_ORDER_ON_WALLET_MEMBER_CONFIRM_TIMEOUT"),
    // 人工审核超时自动确认完成订单
    CONFIRM_FINISH_ORDER_ON_AUDIT_TIMEOUT("16", "CONFIRM_FINISH_ORDER_ON_AUDIT_TIMEOUT"),
    // 余额退回
    CASH_BACK_ORDER_PROCESS("17", "CASH_BACK_ORDER_PROCESS"),

    //自动完成充值订单
    AUTO_RECHARGE_COMPLETE("18", "AUTO_RECHARGE_COMPLETE"),

    //同步银行卡信息
    SYNC_BANK_INFO("19", "SYNC_BANK_INFO"),

    //USDT自动上分
    USDT_AUTO_CREDIT("20", "USDT_AUTO_CREDIT"),

    //拉取区块数据
    FETCH_BLOCK_DATA("21", "FETCH_BLOCK_DATA"),

    //USDT资金归集
    USDT_LOAD_BALANCE("22", "USDT_LOAD_BALANCE"),

    //获取实时汇率
    LOAD_PRICE("23", "LOAD_PRICE"),
    //处理USDT代付订单
    USDT_PAYMENT_ORDER("24", "USDT_PAYMENT_ORDER"),

    //TRX自动上分
    TRX_AUTO_CREDIT("25", "TRX_AUTO_CREDIT"),

    //处理TRX代付订单
    TRX_PAYMENT_ORDER("26", "TRX_PAYMENT_ORDER"),


    // 余额退回
    KYC_WITHDRAW_COMPLETE_PROCESS_QUEUE("27", "KYC_WITHDRAW_COMPLETE_PROCESS_QUEUE"),
    ;


    private final String code;

    private final String name;


    TaskTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static String getNameByCode(String code) {
        for (TaskTypeEnum c : TaskTypeEnum.values()) {
            if (c.getCode().equals(code)) {
                return c.getName();
            }

        }
        return null;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
