package org.uu.wallet.Enum;

/**
 * 会员操作模块枚举
 *
 * @author Simon
 * @date 2024/02/13
 */
public enum MemberOperationModuleEnum {
    BUY_ORDER("1", "买入下单接口"),
    USDT_BUY_ORDER("2", "USDT买入下单接口"),
    USDT_COMPLETE_TRANSFER("3", "USDT完成转账"),
    COMPLETE_PAYMENT("4", "完成支付"),
    CANCEL_BUY_ORDER("5", "取消买入订单"),
    CANCEL_PAYMENT("6", "取消支付"),
    SUBMIT_BUY_APPEAL("7", "提交买入订单申诉"),
    REAL_NAME_AUTHENTICATION("8", "实名认证"),
    VERIFY_SMS_CODE("9", "校验短信验证码"),
    CHANGE_PHONE_NUMBER("10", "更换手机号码"),
    CHANGE_EMAIL("11", "更换邮箱号"),
    ADD_PAYMENT_INFO("12", "添加收款信息"),
    START_RECEIVING("13", "开启收款"),
    STOP_RECEIVING("14", "停止收款"),
    DELETE_PAYMENT_INFO("15", "删除收款信息"),
    SET_AVATAR("16", "设置头像"),
    SET_NICKNAME("17", "设置昵称"),
    SET_NEW_PAYMENT_PASSWORD("18", "设置新支付密码"),
    CHANGE_PAYMENT_PASSWORD("19", "修改支付密码"),
    FORGET_PAYMENT_PASSWORD("20", "忘记支付密码"),
    SELL_ORDER("21", "卖出下单接口"),
    CONFIRM_ARRIVAL("22", "确认到账"),
    CANCEL_SELL_ORDER("23", "取消卖出订单"),
    CONTINUE_MATCHING("24", "继续匹配"),
    SUBMIT_AMOUNT_ERROR("25", "提交金额错误"),
    CANCEL_AMOUNT_ERROR_REQUEST("26", "取消申请金额错误"),
    SUBMIT_SELL_APPEAL("27", "提交卖出申诉"),
    PHONE_NUMBER_REGISTRATION("28", "手机号码注册"),
    EMAIL_ACCOUNT_REGISTRATION("29", "邮箱账号注册"),
    FORGET_PASSWORD("30", "忘记密码"),
    SEND_SMS_CODE("31", "发送短信验证码"),
    SEND_EMAIL_CODE("32", "发送邮箱验证码"),
    SET_DEFAULT_COLLECTION_INFO("33", "设置默认收款信息"),
    CLAIM_ACTIVITY_TASK_REWARD("34", "领取活动任务奖励"),
    FINISH_NEW_USER_GUIDE("35", "完成新手引导"),
    QUICK_BUY("36", "快捷买入"),
    CANCEL_MATCHING("37", "取消匹配"),
    DELEGATION_STATUS("38", "委托状态"),
    DELEGATE_SELL("39", "委托卖出"),
    ;

    private final String code;
    private final String description;

    MemberOperationModuleEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
