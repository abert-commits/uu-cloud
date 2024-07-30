package org.uu.common.core.constant;

/**
 * @author Admin
 */
public interface GlobalConstants {


    String URL_PERM_ROLES_KEY = "system:perm_roles_rule:url:";
    Integer STATUS_ON = 1;
    Integer STATUS_OFF = 0;
    String USER_DEFAULT_PASSWORD = "123456";
    Long ROOT_MENU_ID = -1L;
    String ADMIN_URL_PERM = "%s:/%s%s";
    String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    String DATE_FORMAT_DAY = "yyyy-MM-dd";
    String DATE_FORMAT_MINUTE = "yyyy-MM-dd HH:mm";
    String DATE_FORMAT_MONTH = "yyyy-MM";
    Integer DAY_SECONDS = 7 * 24 * 60 * 60;

    /**
     * 卖出配置
     */
   String SELL_CONFIG = "sellConfig";
   Long timeOut = 45L;

   String ONLINE_USER_KEY = "onlineUserKey";

    /**
     * 导出查询批次
     */
    long BATCH_SIZE = 5000L;
    /**
     * 导出最大记录条数
     */
    long EXPORT_TOTAL_SIZE = 100000L;

    /**
     * 取消订单
     */
    String CANCEL_ORDER = "CANCEL_ORDER";

    /**
     *  取消支付
     */
    String CANCEL_PAY = "CANCEL_PAY";

    /**
     * 时区请求头名称
     */
    String HEADER_TIME_ZONE = "Time-Zone";
}
