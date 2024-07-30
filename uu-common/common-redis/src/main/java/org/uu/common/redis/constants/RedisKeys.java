package org.uu.common.redis.constants;

/**
 * Redis键名常量类
 *
 * @author Simon
 * @date 2023/12/06
 */
public class RedisKeys {

    //会员30分钟内连续买入失败次数
    public static final String MEMBER_BUY_FAILURE = "member:buy:failure:";

    //禁用会员买入锁
    public static final String MEMBER_BUY_BLOCKED = "member:buy:blocked:";

    //订单匹配剩余时间
    public static final String ORDER_MATCH_EXPIRE = "order:match:expire:";

    //订单确认剩余时间
    public static final String ORDER_CONFIRM_EXPIRE = "order:confirm:expire:";

    //支付剩余时间
    public static final String ORDER_PAYMENT_EXPIRE = "order:payment:expire:";

    //USDT支付剩余时间
    public static final String ORDER_USDT_PAYMENT_EXPIRE = "order:usdt:payment:expire:";

    //最后匹配时间
    public static final String ORDER_LASTMATCHTIME = "order:lastMatchTime:";

    //webSocket推送推荐金额列表给用户 查询条件: 用户id, 最小金额, 最大金额, 当前页码, 支付类型
    public static final String AMOUNT_LIST_REQ = "amount:list:req";

    //买入金额列表
    public static final String ORDERS_LIST = "ordersList";

    // 匹配中的卖出订单ID列表,按金额排序
    public static final String MATCH_SELL_ORDERS = "match:sell:orders";

    //买入金额列表订单详情
    public static final String ORDER_DETAILS = "orderDetails:";

    //ip交易次数
    public static final String TRADE = "trade:";

    //任务奖励领取记录
    public static final String TASK_COMPLETED = "taskCompleted:";

    public static final String MEMBER_BALCK_LIST = "memberBlackList";

    // 会员最后一次操作IP
    public static final String MEMBER_LAST_IP = "member:last:ip:";

    /**
     * 订单支付图片识别失败次数
     */
    public static final String ORDER_PAY_OSR_FAIL = "pay:osr:fail:cnt";

    /**
     * 会员进行中的订单
     */
    public static final String MEMBER_PROCESSING_ORDER = "mmb:prcsng:order:%s";

    public static final String CREDIT_CONFIG = "credit:config";

    public static final String PROCESSING_ORDER_SYNC_SWITCH = "processing:order:sync:switch";

    /**
     * 站内信推送
     */
    public static final String SYS_MESSAGE = "sys:message";


    /**
     * 订单匹配页面信息
     */
    public static final String ORDER_MATCH_PAGE_INFO = "orderMatchPageInfo:";

    /**
     * 同步银行卡信息
     */
    public static final String SYNC_BANK_INFO = "syncBankInfo:";

    /**
     * 会员当日取消匹配次数
     */
    public static final String CANCEL_MATCHING_COUNT_TODAY = "cancel:count:today:";
    /**
     * kyc待自动完成列表
     */
    public static final String KYC_AUTO_COMPLETE_HASH = "kyc:auto:complete:hash";


    /**
     * 系统区块高度
     */
    public static final String TRON_BLOCK_NUM = "tron:block_num";


    /**
     * tx_id (交易id)
     */
    public static final String TRON_TX_ID = "tron:tx_id:";


    /**
     * 充值地址: 充值信息 hash类型存储redis
     */
    public static final String TRON_RECHARGED_ADDRESS = "tron:recharged_address:";


    /**
     * 待支付的USDT收款地址
     */
    public static final String PENDING_USDT_ADDRESS = "pending:usdt:address:";


    /**
     * 波场钱包地址
     */
    public static final String TRON_WALLET = "TRON_WALLET:";

    /**
     * 能量租赁
     */
    public static final String TRON_RENT_ENERGY = "TRON_RENT_ENERGY:";
}
